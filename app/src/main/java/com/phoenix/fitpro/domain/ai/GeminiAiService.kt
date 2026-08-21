package com.phoenix.fitpro.domain.ai

import com.google.gson.Gson
import com.phoenix.fitpro.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Service centralise pour les reponses du Coach Ashes via l'API Gemini. */
object GeminiAiService {

    private const val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun ask(userMessage: String, systemContext: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            sendGenerateContent(buildSystemPrompt(systemContext), userMessage)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    suspend fun generate(prompt: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            sendGenerateContent(baseCoachInstructions(), prompt)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun sendGenerateContent(systemPrompt: String, userMessage: String): String {
        val key = BuildConfig.GEMINI_API_KEY.trim()
        if (key.isBlank()) throw IllegalStateException("Cle API Gemini manquante")

        val payload = GeminiRequest(
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(systemPrompt))),
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(userMessage)))),
            generationConfig = GeminiGenerationConfig(maxOutputTokens = 1024)
        )
        val request = Request.Builder()
            .url("$GEMINI_URL${BuildConfig.GEMINI_MODEL}:generateContent")
            .addHeader("x-goog-api-key", key)
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(payload).toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("Gemini ${response.code}: ${parseError(body)}")
            }

            val completion = gson.fromJson(body, GeminiResponse::class.java)
            return completion.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.joinToString("") { it.text.orEmpty() }
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: "Je n'ai pas pu generer une reponse. Reessaie !"
        }
    }

    private fun parseError(body: String): String {
        if (body.isBlank()) return "Reponse vide"
        return runCatching {
            gson.fromJson(body, GeminiErrorResponse::class.java).error?.message
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: body.take(300)
    }

    private fun buildSystemPrompt(systemContext: String): String {
        return """
${baseCoachInstructions()}

--- CONTEXTE UTILISATEUR ---
$systemContext
--- FIN DU CONTEXTE ---
        """.trimIndent()
    }

    private fun baseCoachInstructions(): String {
        return """
Tu es le Coach Ashes, une IA de coaching sportif et nutritionnel ultra-precise, bienveillante et motivante.
Tu reponds toujours en FRANCAIS sauf si l'utilisateur ecrit en anglais.
Tu es concis, precis et tu adaptes tes conseils aux donnees de l'utilisateur.
Ne donne pas de diagnostic medical et recommande un professionnel de sante en cas de douleur, blessure ou symptome serieux.
Reponds de maniere personnalisee, precise et actionnable.
        """.trimIndent()
    }

    private fun handleError(e: Exception): String {
        val errorMsg = e.message ?: e.localizedMessage ?: "Erreur inconnue"
        val lower = errorMsg.lowercase()
        return when {
            BuildConfig.GEMINI_API_KEY.isBlank() ->
                "Cle API Gemini manquante dans la configuration de l'application."
            lower.contains("401") || lower.contains("403") || lower.contains("unauthorized") ->
                "Cle API Gemini invalide ou non autorisee pour ce projet."
            lower.contains("quota") || lower.contains("rate limit") || lower.contains("429") ->
                "Limite Gemini atteinte. Reessaie dans quelques instants."
            lower.contains("network") || lower.contains("connect") || lower.contains("timeout") ->
                "Erreur de connexion. Verifie ta connexion internet."
            else -> "Erreur IA Gemini : $errorMsg"
        }
    }

    private data class GeminiRequest(
        val systemInstruction: GeminiContent,
        val contents: List<GeminiContent>,
        val generationConfig: GeminiGenerationConfig
    )

    private data class GeminiContent(val parts: List<GeminiPart>)
    private data class GeminiPart(val text: String)
    private data class GeminiGenerationConfig(val maxOutputTokens: Int)
    private data class GeminiResponse(val candidates: List<GeminiCandidate>?)
    private data class GeminiCandidate(val content: GeminiContent?)
    private data class GeminiErrorResponse(val error: GeminiError?)
    private data class GeminiError(val message: String?)
}
