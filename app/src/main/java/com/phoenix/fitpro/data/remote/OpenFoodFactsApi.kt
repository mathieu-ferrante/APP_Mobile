package com.phoenix.fitpro.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open Food Facts, API v2.
 *
 * L'ancien point d'entree `cgi/search.pl` renvoie desormais une page HTML 503
 * aux clients anonymes des les premieres requetes ("not available to anonymous
 * users"). Gson echouait alors sur du HTML et l'erreur etait avalee : la
 * recherche paraissait ne rien trouver. L'API v2 repond correctement, a
 * condition d'envoyer un User-Agent identifiant (voir NetworkModule) et de ne
 * pas marteler le service.
 */
interface OpenFoodFactsApi {

    @GET("api/v2/search")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("lc") lang: String = "fr",
        @Query("page_size") pageSize: Int = 20,
        @Query("fields") fields: String =
            "code,product_name,product_name_fr,generic_name,generic_name_fr,nutriments,quantity"
    ): OpenFoodSearchResponse

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"

        /** Exige par la politique d'usage d'Open Food Facts. */
        const val USER_AGENT = "PhoenixFit/1.0 (Android; https://github.com/mathieu-ferrante/APP_Mobile)"
    }
}

// ── Response DTOs ─────────────────────────────────────────────────────────────

data class OpenFoodSearchResponse(
    @SerializedName("products") val products: List<OpenFoodProduct> = emptyList(),
    @SerializedName("count") val count: Int = 0
)

data class OpenFoodProduct(
    @SerializedName("code") val code: String? = null,
    @SerializedName("product_name_fr") val nameFr: String? = null,
    @SerializedName("product_name") val nameEn: String? = null,
    @SerializedName("generic_name_fr") val genericNameFr: String? = null,
    @SerializedName("generic_name") val genericName: String? = null,
    @SerializedName("quantity") val quantity: String? = null,
    @SerializedName("nutriments") val nutriments: OpenFoodNutriments? = null
) {
    /** Meilleur nom disponible, dans la langue active. */
    val displayName: String
        get() {
            val isEn = java.util.Locale.getDefault().language.equals("en", ignoreCase = true)
            val ordered = if (isEn) listOf(nameEn, genericName, nameFr, genericNameFr)
            else listOf(nameFr, genericNameFr, nameEn, genericName)
            return ordered.firstOrNull { !it.isNullOrBlank() }?.take(80).orEmpty()
        }

    /** Un produit sans nom ni valeur energetique n'a aucun interet a l'affichage. */
    val isUsable: Boolean get() = displayName.isNotBlank() && caloriesPer100g != null

    val caloriesPer100g: Int? get() = nutriments?.energyKcal100g?.toInt()
    val proteinPer100g: Float? get() = nutriments?.proteins100g
    val carbsPer100g: Float? get() = nutriments?.carbohydrates100g
    val fatPer100g: Float? get() = nutriments?.fat100g
}

data class OpenFoodNutriments(
    @SerializedName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerializedName("proteins_100g") val proteins100g: Float? = null,
    @SerializedName("carbohydrates_100g") val carbohydrates100g: Float? = null,
    @SerializedName("fat_100g") val fat100g: Float? = null
)
