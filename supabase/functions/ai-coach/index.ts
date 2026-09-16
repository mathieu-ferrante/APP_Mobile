// Proxy IA pour Phoenix Fit.
//
// Objectif : la cle du fournisseur d'IA ne quitte jamais le serveur.
// L'application mobile n'envoie que le prompt, accompagne du jeton de session
// Supabase de l'utilisateur. La fonction verifie ce jeton, decompte un quota
// journalier, puis relaie la requete au fournisseur configure.
//
// Variables d'environnement (secrets de la fonction) :
//   AI_PROVIDER   "gemini" (defaut) | "openai"
//   AI_MODEL      modele par defaut (chat, conseils) - le moins cher suffit
//   AI_MODEL_PRECISE  modele des taches courtes ou la justesse compte
//                     (estimation nutritionnelle). Defaut : AI_MODEL.
//   AI_API_KEY    cle du fournisseur
//   AI_DAILY_LIMIT nombre maximal d'appels par utilisateur et par jour
//                  (defaut 250). Seuls les appels reellement servis comptent :
//                  une requete rejetee ou un echec du fournisseur est rembourse.
// Fournies automatiquement par Supabase :
//   SUPABASE_URL, SUPABASE_ANON_KEY, SUPABASE_SERVICE_ROLE_KEY

import { createClient } from "jsr:@supabase/supabase-js@2";

const CORS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const MAX_PROMPT_CHARS = 12_000;
const DEFAULT_DAILY_LIMIT = 250;

// Sur les modeles "flash", un token de sortie coute environ 5x un token d'entree.
// Chaque tache recoit donc le plafond de sortie strictement necessaire, et le
// modele adapte : une estimation nutritionnelle tient en quelques dizaines de
// tokens, on peut donc y mettre le meilleur modele pour presque rien.
const TASKS: Record<string, { precise: boolean; maxOut: number }> = {
  nutrition: { precise: true, maxOut: 160 },   // 5 nombres + une portion
  generate: { precise: false, maxOut: 320 },   // listes d'exercices, conseils
  chat: { precise: false, maxOut: 500 },       // reponses conversationnelles
};
const DEFAULT_TASK = "chat";

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...CORS, "Content-Type": "application/json" },
  });
}

interface AskBody {
  system?: string;
  user?: string;
  /** "nutrition" | "generate" | "chat" (defaut). Pilote modele et plafond. */
  task?: string;
  maxOutputTokens?: number;
}

/** Appelle Google Gemini. */
async function callGemini(
  model: string,
  apiKey: string,
  system: string,
  user: string,
  maxOutputTokens: number,
): Promise<string> {
  const url =
    `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`;
  const res = await fetch(url, {
    method: "POST",
    headers: { "x-goog-api-key": apiKey, "Content-Type": "application/json" },
    body: JSON.stringify({
      systemInstruction: { parts: [{ text: system }] },
      contents: [{ parts: [{ text: user }] }],
      generationConfig: { maxOutputTokens },
    }),
  });
  const payload = await res.json().catch(() => null);
  if (!res.ok) {
    const message = payload?.error?.message ?? `HTTP ${res.status}`;
    throw new Error(`gemini: ${message}`);
  }
  const parts = payload?.candidates?.[0]?.content?.parts ?? [];
  return parts.map((p: { text?: string }) => p.text ?? "").join("").trim();
}

/** Appelle une API compatible OpenAI (OpenAI, DeepSeek, Mistral, Groq, ...). */
async function callOpenAiCompatible(
  model: string,
  apiKey: string,
  system: string,
  user: string,
  maxOutputTokens: number,
): Promise<string> {
  const base = Deno.env.get("AI_BASE_URL") ?? "https://api.openai.com/v1";
  const res = await fetch(`${base}/chat/completions`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${apiKey}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      model,
      max_completion_tokens: maxOutputTokens,
      messages: [
        { role: "system", content: system },
        { role: "user", content: user },
      ],
    }),
  });
  const payload = await res.json().catch(() => null);
  if (!res.ok) {
    const message = payload?.error?.message ?? `HTTP ${res.status}`;
    throw new Error(`openai: ${message}`);
  }
  return (payload?.choices?.[0]?.message?.content ?? "").trim();
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: CORS });
  if (req.method !== "POST") return json({ error: "Methode non autorisee" }, 405);

  // ── 1. Authentification : la session Supabase de l'appelant ────────────────
  const authHeader = req.headers.get("Authorization") ?? "";
  if (!authHeader.startsWith("Bearer ")) {
    return json({ error: "Authentification requise" }, 401);
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
  const userClient = createClient(supabaseUrl, anonKey, {
    global: { headers: { Authorization: authHeader } },
    auth: { persistSession: false },
  });

  const { data: userData, error: userError } = await userClient.auth.getUser();
  const user = userData?.user;
  if (userError || !user) return json({ error: "Session invalide" }, 401);

  // ── 2. Requete : tout ce qui peut etre refuse l'est avant le decompte ─────
  // Une requete que l'on rejette ne doit rien couter, sinon une session de
  // tests epuise le quota du jour sans qu'un seul appel ait atteint l'IA.
  let body: AskBody;
  try {
    body = await req.json();
  } catch {
    return json({ error: "Corps de requete invalide" }, 400);
  }

  const userPrompt = (body.user ?? "").slice(0, MAX_PROMPT_CHARS);
  const systemPrompt = (body.system ?? "").slice(0, MAX_PROMPT_CHARS);
  if (!userPrompt.trim()) return json({ error: "Prompt vide" }, 400);

  const task = TASKS[body.task ?? DEFAULT_TASK] ?? TASKS[DEFAULT_TASK];
  // Le client peut demander moins, jamais plus que le plafond de la tache.
  const maxOutputTokens = Math.max(
    Math.min(body.maxOutputTokens ?? task.maxOut, task.maxOut),
    64,
  );

  const provider = (Deno.env.get("AI_PROVIDER") ?? "gemini").toLowerCase();
  const baseModel = Deno.env.get("AI_MODEL") ?? "gemini-3.1-flash-lite";
  const model = task.precise
    ? (Deno.env.get("AI_MODEL_PRECISE") ?? baseModel)
    : baseModel;
  const apiKey = Deno.env.get("AI_API_KEY");
  if (!apiKey) return json({ error: "Fournisseur IA non configure" }, 500);

  // ── 3. Quota journalier par utilisateur ───────────────────────────────────
  const configuredLimit = Number(Deno.env.get("AI_DAILY_LIMIT"));
  const dailyLimit = Number.isFinite(configuredLimit) && configuredLimit > 0
    ? Math.floor(configuredLimit)
    : DEFAULT_DAILY_LIMIT;
  const adminClient = createClient(
    supabaseUrl,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
    { auth: { persistSession: false } },
  );

  const { data: allowed, error: quotaError } = await adminClient.rpc(
    "ai_consume_quota",
    { p_user: user.id, p_limit: dailyLimit },
  );
  // Un compteur en panne ne doit pas rendre le coach indisponible : il protege
  // le budget, il n'est pas une brique du service.
  let consumed = !quotaError;
  if (quotaError) {
    console.error("quota", quotaError);
  } else if (allowed === false) {
    return json(
      { error: `Quota IA atteint (${dailyLimit} requetes par jour). Reessaie demain.` },
      429,
    );
  }

  /** Un echec cote serveur ne doit pas etre facture a l'utilisateur. */
  async function refund(): Promise<void> {
    if (!consumed) return;
    consumed = false;
    const { error } = await adminClient.rpc("ai_refund_quota", { p_user: user!.id });
    if (error) console.error("refund", error);
  }

  // ── 4. Relai vers le fournisseur ──────────────────────────────────────────
  try {
    const text = provider === "gemini"
      ? await callGemini(model, apiKey, systemPrompt, userPrompt, maxOutputTokens)
      : await callOpenAiCompatible(model, apiKey, systemPrompt, userPrompt, maxOutputTokens);

    if (!text) {
      await refund();
      return json({ error: "Reponse vide du fournisseur" }, 502);
    }
    return json({ text });
  } catch (e) {
    console.error("provider", e);
    await refund();
    return json({ error: (e as Error).message }, 502);
  }
});
