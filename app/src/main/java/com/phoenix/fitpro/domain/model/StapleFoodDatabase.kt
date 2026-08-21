package com.phoenix.fitpro.domain.model

/**
 * Base de données locale de référence (inspirée table CIQUAL)
 * Contient les aliments de base courants avec valeurs pour 100g.
 * Permet une recherche instantanée (0ms), 100% hors-ligne et ultra-précise.
 */
object StapleFoodDatabase {

    val items: List<FoodItem> = listOf(
        // ── Viandes & Volailles ───────────────────────────────────────────────
        FoodItem(name = "Blanc de poulet cuit", quantity = "100g", calories = 165, proteinG = 31f, carbsG = 0f, fatG = 3.6f),
        FoodItem(name = "Filet de dinde", quantity = "100g", calories = 135, proteinG = 30f, carbsG = 0f, fatG = 1.5f),
        FoodItem(name = "Steak haché 5% MG", quantity = "100g", calories = 125, proteinG = 21f, carbsG = 0f, fatG = 5f),
        FoodItem(name = "Steak haché 15% MG", quantity = "100g", calories = 215, proteinG = 19f, carbsG = 0f, fatG = 15f),
        FoodItem(name = "Bœuf (bavette / faux-filet)", quantity = "100g", calories = 150, proteinG = 26f, carbsG = 0f, fatG = 5f),
        FoodItem(name = "Jambon blanc découenné", quantity = "100g (2 tranches)", calories = 110, proteinG = 21f, carbsG = 1f, fatG = 2.5f),
        FoodItem(name = "Filet mignon de porc", quantity = "100g", calories = 143, proteinG = 26f, carbsG = 0f, fatG = 4f),
        FoodItem(name = "Gigot d'agneau dégraissé", quantity = "100g", calories = 180, proteinG = 25f, carbsG = 0f, fatG = 9f),

        // ── Poissons & Fruits de mer ──────────────────────────────────────────
        FoodItem(name = "Pavé de saumon", quantity = "100g", calories = 208, proteinG = 20f, carbsG = 0f, fatG = 13f),
        FoodItem(name = "Thon au naturel (boîte)", quantity = "100g", calories = 116, proteinG = 26f, carbsG = 0f, fatG = 1f),
        FoodItem(name = "Cabillaud / Colin", quantity = "100g", calories = 82, proteinG = 18f, carbsG = 0f, fatG = 0.7f),
        FoodItem(name = "Crevettes cuites", quantity = "100g", calories = 99, proteinG = 24f, carbsG = 0.2f, fatG = 0.3f),
        FoodItem(name = "Sardines à l'huile d'olive", quantity = "100g", calories = 208, proteinG = 24f, carbsG = 0f, fatG = 12f),
        FoodItem(name = "Maquereau", quantity = "100g", calories = 205, proteinG = 19f, carbsG = 0f, fatG = 14f),

        // ── Œufs & Protéines végétales ────────────────────────────────────────
        FoodItem(name = "Œuf entier (moyen)", quantity = "1 pièce (~55g)", calories = 75, proteinG = 6.5f, carbsG = 0.4f, fatG = 5f),
        FoodItem(name = "Blanc d'œuf", quantity = "100g", calories = 52, proteinG = 11f, carbsG = 0.7f, fatG = 0.2f),
        FoodItem(name = "Tofu nature", quantity = "100g", calories = 76, proteinG = 8f, carbsG = 1.9f, fatG = 4.8f),
        FoodItem(name = "Tempeh", quantity = "100g", calories = 192, proteinG = 20f, carbsG = 7.6f, fatG = 10.8f),
        FoodItem(name = "Whey protéine isolate", quantity = "1 dose (30g)", calories = 110, proteinG = 26f, carbsG = 1f, fatG = 0.5f),

        // ── Féculents, Céréales & Légumineuses ────────────────────────────────
        FoodItem(name = "Riz basmati (cru)", quantity = "100g", calories = 350, proteinG = 8.5f, carbsG = 77f, fatG = 0.6f),
        FoodItem(name = "Riz basmati (cuit)", quantity = "100g", calories = 130, proteinG = 2.7f, carbsG = 28f, fatG = 0.3f),
        FoodItem(name = "Pâtes complètes (crues)", quantity = "100g", calories = 348, proteinG = 13f, carbsG = 65f, fatG = 2.5f),
        FoodItem(name = "Pâtes blanches (cuites)", quantity = "100g", calories = 140, proteinG = 5f, carbsG = 28f, fatG = 0.8f),
        FoodItem(name = "Flocons d'avoine", quantity = "100g", calories = 370, proteinG = 13.5f, carbsG = 60f, fatG = 7f),
        FoodItem(name = "Patate douce", quantity = "100g", calories = 86, proteinG = 1.6f, carbsG = 20f, fatG = 0.1f),
        FoodItem(name = "Pomme de terre vapeur", quantity = "100g", calories = 77, proteinG = 2f, carbsG = 17f, fatG = 0.1f),
        FoodItem(name = "Quinoa (cru)", quantity = "100g", calories = 368, proteinG = 14f, carbsG = 64f, fatG = 6f),
        FoodItem(name = "Lentilles vertes / corail (cuites)", quantity = "100g", calories = 116, proteinG = 9f, carbsG = 20f, fatG = 0.4f),
        FoodItem(name = "Pois chiches (cuits)", quantity = "100g", calories = 164, proteinG = 8.9f, carbsG = 27f, fatG = 2.6f),
        FoodItem(name = "Haricots rouges (cuits)", quantity = "100g", calories = 127, proteinG = 8.7f, carbsG = 22.8f, fatG = 0.5f),
        FoodItem(name = "Pain complet", quantity = "1 tranche (35g)", calories = 85, proteinG = 3.5f, carbsG = 15f, fatG = 1f),
        FoodItem(name = "Pain de seigle", quantity = "100g", calories = 240, proteinG = 8.5f, carbsG = 48f, fatG = 1.5f),

        // ── Légumes ───────────────────────────────────────────────────────────
        FoodItem(name = "Brocoli vapeur", quantity = "100g", calories = 35, proteinG = 2.8f, carbsG = 4.4f, fatG = 0.4f),
        FoodItem(name = "Haricots verts", quantity = "100g", calories = 31, proteinG = 1.8f, carbsG = 7f, fatG = 0.2f),
        FoodItem(name = "Épinards frais", quantity = "100g", calories = 23, proteinG = 2.9f, carbsG = 3.6f, fatG = 0.4f),
        FoodItem(name = "Courgette", quantity = "100g", calories = 17, proteinG = 1.2f, carbsG = 3.1f, fatG = 0.3f),
        FoodItem(name = "Tomate", quantity = "100g", calories = 18, proteinG = 0.9f, carbsG = 3.9f, fatG = 0.2f),
        FoodItem(name = "Concombre", quantity = "100g", calories = 15, proteinG = 0.7f, carbsG = 3.6f, fatG = 0.1f),
        FoodItem(name = "Carotte", quantity = "100g", calories = 41, proteinG = 0.9f, carbsG = 9.6f, fatG = 0.2f),
        FoodItem(name = "Poivron (rouge / vert)", quantity = "100g", calories = 26, proteinG = 1f, carbsG = 4.6f, fatG = 0.3f),
        FoodItem(name = "Champignons de Paris", quantity = "100g", calories = 22, proteinG = 3.1f, carbsG = 3.3f, fatG = 0.3f),
        FoodItem(name = "Asperges", quantity = "100g", calories = 20, proteinG = 2.2f, carbsG = 3.9f, fatG = 0.1f),
        FoodItem(name = "Salade verte / Laitue", quantity = "100g", calories = 15, proteinG = 1.4f, carbsG = 2.9f, fatG = 0.2f),

        // ── Fruits ────────────────────────────────────────────────────────────
        FoodItem(name = "Banane", quantity = "1 pièce moyenne (~120g)", calories = 105, proteinG = 1.3f, carbsG = 27f, fatG = 0.3f),
        FoodItem(name = "Pomme", quantity = "1 pièce (~150g)", calories = 78, proteinG = 0.4f, carbsG = 20f, fatG = 0.3f),
        FoodItem(name = "Myrtilles / Fruits rouges", quantity = "100g", calories = 57, proteinG = 0.7f, carbsG = 14f, fatG = 0.3f),
        FoodItem(name = "Fraise", quantity = "100g", calories = 32, proteinG = 0.7f, carbsG = 7.7f, fatG = 0.3f),
        FoodItem(name = "Orange", quantity = "1 pièce (~130g)", calories = 62, proteinG = 1.2f, carbsG = 15f, fatG = 0.2f),
        FoodItem(name = "Kiwi", quantity = "1 pièce (~75g)", calories = 42, proteinG = 0.8f, carbsG = 10f, fatG = 0.4f),
        FoodItem(name = "Ananas", quantity = "100g", calories = 50, proteinG = 0.5f, carbsG = 13f, fatG = 0.1f),
        FoodItem(name = "Mangue", quantity = "100g", calories = 60, proteinG = 0.8f, carbsG = 15f, fatG = 0.4f),

        // ── Produits Laitiers & Alternatives ───────────────────────────────────
        FoodItem(name = "Fromage blanc 0%", quantity = "100g", calories = 48, proteinG = 8f, carbsG = 4f, fatG = 0.1f),
        FoodItem(name = "Fromage blanc 3% / Skyr", quantity = "100g", calories = 65, proteinG = 10f, carbsG = 4f, fatG = 0.2f),
        FoodItem(name = "Yaourt grec authentique", quantity = "100g", calories = 115, proteinG = 9f, carbsG = 4f, fatG = 7f),
        FoodItem(name = "Lait demi-écrémé", quantity = "100ml", calories = 46, proteinG = 3.3f, carbsG = 4.8f, fatG = 1.5f),
        FoodItem(name = "Lait d'amande sans sucre", quantity = "100ml", calories = 13, proteinG = 0.4f, carbsG = 0.2f, fatG = 1.1f),
        FoodItem(name = "Mozzarella", quantity = "100g", calories = 280, proteinG = 22f, carbsG = 2.2f, fatG = 21f),
        FoodItem(name = "Parmesan", quantity = "100g", calories = 431, proteinG = 38f, carbsG = 4.1f, fatG = 29f),
        FoodItem(name = "Emmental", quantity = "100g", calories = 380, proteinG = 28f, carbsG = 0.5f, fatG = 30f),

        // ── Oléagineux, Graines & Huiles ───────────────────────────────────────
        FoodItem(name = "Avocat", quantity = "1/2 pièce (~100g)", calories = 160, proteinG = 2f, carbsG = 8.5f, fatG = 14.7f),
        FoodItem(name = "Amandes", quantity = "30g (une poignée)", calories = 175, proteinG = 6.3f, carbsG = 6.5f, fatG = 15f),
        FoodItem(name = "Noix de Grenoble", quantity = "30g", calories = 196, proteinG = 4.5f, carbsG = 4f, fatG = 19.5f),
        FoodItem(name = "Beurre de cacahuète 100%", quantity = "1 c. à soupe (20g)", calories = 120, proteinG = 5f, carbsG = 3f, fatG = 10f),
        FoodItem(name = "Huile d'olive vierge extra", quantity = "1 c. à soupe (10ml)", calories = 90, proteinG = 0f, carbsG = 0f, fatG = 10f),
        FoodItem(name = "Graines de chia", quantity = "15g", calories = 73, proteinG = 2.5f, carbsG = 6.3f, fatG = 4.6f),
        FoodItem(name = "Chocolat noir 85%", quantity = "20g (2 carrés)", calories = 120, proteinG = 2f, carbsG = 4f, fatG = 10.5f)
    )

    fun search(query: String): List<FoodItem> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()
        val terms = q.split(" ").filter { it.isNotBlank() }

        return items.filter { item ->
            val name = item.name.lowercase()
            terms.all { term -> name.contains(term) }
        }
    }
}
