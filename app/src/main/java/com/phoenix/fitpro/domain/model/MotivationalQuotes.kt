package com.phoenix.fitpro.domain.model

/** Bilingual motivational quotes displayed on the dashboard */
object MotivationalQuotes {
    val french = listOf(
        "La douleur d'aujourd'hui est la force de demain.",
        "Chaque répétition te rapproche de ta meilleure version.",
        "Le corps accomplit ce que l'esprit croit possible.",
        "N'attends pas demain pour faire ce que tu peux faire aujourd'hui.",
        "La régularité bat l'intensité sur le long terme.",
        "Tu es plus fort que tu ne le crois.",
        "L'effort d'aujourd'hui est le résultat de demain.",
        "Chaque séance compte, même les petites.",
        "Le succès, c'est la somme de petits efforts répétés chaque jour.",
        "Ton seul compétiteur, c'est qui tu étais hier.",
        "Arrête de rêver, commence à transpirer.",
        "Les champions ne naissent pas, ils se forgent.",
        "Fais-le, même si tu n'en as pas envie.",
        "Ton corps peut tout faire. C'est ton esprit qu'il faut convaincre.",
        "Le confort est l'ennemi de la progression.",
        "Un pas à la fois, une répétition à la fois.",
        "La discipline est le pont entre les objectifs et les accomplissements.",
        "Ne compte pas les jours, rends chaque jour comptable.",
        "Quand tu veux abandonner, rappelle-toi pourquoi tu as commencé.",
        "Sois la meilleure version de toi-même, pas une copie des autres.",
        "Les excuses ne brûlent pas de calories.",
        "Ton futur toi te remerciera.",
        "Il n'y a pas de raccourci vers un endroit qui vaut le voyage.",
        "La motivation te démarre, l'habitude te maintient en route.",
        "Chaque goutte de sueur est une victoire."
    )

    val english = listOf(
        "Today's pain is tomorrow's strength.",
        "Every rep brings you closer to your best self.",
        "The body achieves what the mind believes.",
        "Don't wait for tomorrow to do what you can do today.",
        "Consistency beats intensity in the long run.",
        "You are stronger than you think.",
        "Today's effort is tomorrow's result.",
        "Every workout counts, even the small ones.",
        "Success is the sum of small efforts repeated every day.",
        "Your only competitor is who you were yesterday.",
        "Stop dreaming, start sweating.",
        "Champions are not born, they are forged.",
        "Do it, even when you don't feel like it.",
        "Your body can do anything. It's your mind you need to convince.",
        "Comfort is the enemy of progress.",
        "One step at a time, one rep at a time.",
        "Discipline is the bridge between goals and accomplishment.",
        "Don't count the days, make every day count.",
        "When you want to quit, remember why you started.",
        "Be the best version of yourself, not a copy of others.",
        "Excuses don't burn calories.",
        "Your future self will thank you.",
        "There are no shortcuts to any place worth going.",
        "Motivation starts you, habit keeps you going.",
        "Every drop of sweat is a victory."
    )

    fun getRandom(lang: String = "fr"): String {
        return if (lang == "fr") french.random() else english.random()
    }
}
