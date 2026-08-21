package com.phoenix.fitpro.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/** Open Food Facts REST API — free, no API key required */
interface OpenFoodFactsApi {

    /**
     * Search endpoint — uses the French OFF server for better French product coverage.
     * Includes country/language filters so that typing "tomate" or "poulet" yields
     * relevant results with French names and nutritional data.
     */
    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms")  query: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action")        action: String  = "process",
        @Query("json")          json: Int       = 1,
        @Query("page_size")     pageSize: Int   = 20,
        @Query("lc")            lang: String    = "fr",
        @Query("cc")            country: String = "fr",
        @Query("fields")        fields: String  =
            "id,product_name,product_name_fr,generic_name_fr,generic_name,nutriments,quantity"
    ): OpenFoodSearchResponse

    companion object {
        // Use the French server — much better coverage for French food names
        const val BASE_URL = "https://fr.openfoodfacts.org/"
    }
}

// ── Response DTOs ─────────────────────────────────────────────────────────────

data class OpenFoodSearchResponse(
    @SerializedName("products") val products: List<OpenFoodProduct> = emptyList(),
    @SerializedName("count")    val count: Int = 0
)

data class OpenFoodProduct(
    @SerializedName("id")               val id: String? = null,
    @SerializedName("product_name_fr")  val nameFr: String? = null,
    @SerializedName("product_name")     val nameEn: String? = null,
    @SerializedName("generic_name_fr")  val genericNameFr: String? = null,
    @SerializedName("generic_name")     val genericName: String? = null,
    @SerializedName("quantity")         val quantity: String? = null,
    @SerializedName("nutriments")       val nutriments: OpenFoodNutriments? = null
) {
    /** Best available name localized to active language (FR or EN) */
    val displayName: String
        get() {
            val isEn = java.util.Locale.getDefault().language.equals("en", ignoreCase = true)
            return if (isEn) {
                listOf(nameEn, genericName, nameFr, genericNameFr)
                    .firstOrNull { !it.isNullOrBlank() }
                    ?.take(80)
                    ?: "Unknown Food"
            } else {
                listOf(nameFr, genericNameFr, nameEn, genericName)
                    .firstOrNull { !it.isNullOrBlank() }
                    ?.take(80)
                    ?: "Aliment inconnu"
            }
        }

    val caloriesPer100g: Int?   get() = nutriments?.energyKcal100g?.toInt()
    val proteinPer100g: Float?  get() = nutriments?.proteins100g
    val carbsPer100g: Float?    get() = nutriments?.carbohydrates100g
    val fatPer100g: Float?      get() = nutriments?.fat100g
}

data class OpenFoodNutriments(
    @SerializedName("energy-kcal_100g")   val energyKcal100g: Double? = null,
    @SerializedName("proteins_100g")       val proteins100g: Float? = null,
    @SerializedName("carbohydrates_100g") val carbohydrates100g: Float? = null,
    @SerializedName("fat_100g")            val fat100g: Float? = null
)
