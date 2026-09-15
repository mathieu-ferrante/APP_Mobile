package com.phoenix.fitpro.domain.ai

import com.google.gson.Gson
import com.phoenix.fitpro.BuildConfig
import com.phoenix.fitpro.data.remote.sync.SupabaseBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Acces au Coach Ashes via l'Edge Function Supabase "ai-coach".
 *
 * L'application n'embarque aucune cle de fournisseur d'IA : elle envoie le
 * prompt accompagne du jeton de session Supabase de l'utilisateur, et c'est le
 * serveur qui detient la cle, applique un quota et choisit le modele. Changer
 * de fournisseur se fait cote serveur, sans recompiler l'application.
 */
object AiService {

    private const val FUNCTION_PATH = "/functions/v1/ai-coach"
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun ask(userMessage: String, systemContext: String): String =
        withContext(Dispatchers.IO) {
            try {
                send(buildSystemPrompt(systemContext), userMessage, TASK_CHAT)
            } catch (e: Exception) {
                handleError(e)
            }
        }

    /**
     * @param task "nutrition" (modele precis, sortie courte), "generate" ou
     *   "chat". Le serveur choisit le modele et plafonne la sortie en fonction.
     */
    suspend fun generate(prompt: String, task: String = TASK_GENERATE): String =
        withContext(Dispatchers.IO) {
            try {
                send(baseCoachInstructions(), prompt, task)
            } catch (e: Exception) {
                handleError(e)
            }
        }

    /**
     * Comme [generate], mais laisse remonter l'echec au lieu de renvoyer le
     * message d'erreur sous forme de texte. Indispensable partout ou la reponse
     * est analysee : une erreur rendue comme une reponse normale traversait le
     * parseur sans declencher aucun signal.
     */
    suspend fun generateOrThrow(prompt: String, task: String = TASK_GENERATE): String =
        withContext(Dispatchers.IO) { send(baseCoachInstructions(), prompt, task) }

    private fun send(systemPrompt: String, userMessage: String, task: String): String {
        val baseUrl = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
        if (baseUrl.isBlank()) throw IllegalStateException(NOT_CONFIGURED)

        val token = SupabaseBackend.current?.accessToken()
            ?: throw IllegalStateException(NO_SESSION)

        val payload = ProxyRequest(system = systemPrompt, user = userMessage, task = task)
        val request = Request.Builder()
            .url(baseUrl + FUNCTION_PATH)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(payload).toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            val parsed = runCatching { gson.fromJson(body, ProxyResponse::class.java) }.getOrNull()

            if (!response.isSuccessful) {
                val detail = parsed?.error?.takeIf { it.isNotBlank() } ?: body.take(300)
                throw IOException("${response.code}: $detail")
            }

            return parsed?.text?.trim()?.takeIf { it.isNotBlank() }
                ?: "Je n'ai pas pu generer une reponse. Reessaie !"
        }
    }

    private fun buildSystemPrompt(systemContext: String): String = """
${baseCoachInstructions()}

--- CONTEXTE UTILISATEUR ---
$systemContext
--- FIN DU CONTEXTE ---
    """.trimIndent()

    private fun baseCoachInstructions(): String = """
Tu es le Coach Ashes, une IA de coaching sportif et nutritionnel ultra-precise, bienveillante et motivante.
Tu reponds toujours en FRANCAIS sauf si l'utilisateur ecrit en anglais.
Tu es concis, precis et tu adaptes tes conseils aux donnees de l'utilisateur.
Ne donne pas de diagnostic medical et recommande un professionnel de sante en cas de douleur, blessure ou symptome serieux.
Reponds de maniere personnalisee, precise et actionnable.
    """.trimIndent()

    private fun handleError(e: Exception): String {
        val message = e.message ?: e.localizedMessage ?: "Erreur inconnue"
        val lower = message.lowercase()
        return when {
            lower.startsWith("401") || lower.contains("session") ->
                "Session expiree. Reconnecte-toi pour utiliser le coach IA."
            lower.startsWith("429") || lower.contains("quota") ->
                message.substringAfter(": ", message)
            lower.startsWith("500") ->
                "Le service IA n'est pas encore configure sur le serveur."
            lower.startsWith("502") ->
                "Le fournisseur d'IA est indisponible : ${message.substringAfter(": ", "")}"
            lower.contains("network") || lower.contains("connect") || lower.contains("timeout") ->
                "Erreur de connexion. Verifie ta connexion internet."
            else -> "Erreur IA : $message"
        }
    }

    private const val NOT_CONFIGURED =
        "500: Synchronisation Supabase non configuree, le coach IA est indisponible."
    private const val NO_SESSION =
        "401: Aucune session active."

    const val TASK_CHAT = "chat"
    const val TASK_GENERATE = "generate"
    const val TASK_NUTRITION = "nutrition"

    private data class ProxyRequest(
        val system: String,
        val user: String,
        val task: String
    )

    private data class ProxyResponse(val text: String?, val error: String?)
}
