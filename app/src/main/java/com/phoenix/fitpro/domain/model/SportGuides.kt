package com.phoenix.fitpro.domain.model

import java.util.Locale

/** Niveau de pratique, utilise pour graduer les programmes. */
enum class SportLevel(val labelFr: String, val labelEn: String, val emoji: String) {
    BEGINNER("Débutant", "Beginner", "🌱"),
    INTERMEDIATE("Intermédiaire", "Intermediate", "⚡"),
    ADVANCED("Confirmé", "Advanced", "🔥");

    val label: String get() = if (isEnglish()) labelEn else labelFr
}

internal fun isEnglish(): Boolean =
    Locale.getDefault().language.equals("en", ignoreCase = true)

/** Texte disponible dans les deux langues reellement traduites de l'application. */
data class Localized(val fr: String, val en: String) {
    val value: String get() = if (isEnglish()) en else fr
    override fun toString(): String = value
}

private fun t(fr: String, en: String) = Localized(fr, en)

/**
 * Fiche d'un exercice : consigne chiffree, objectif, technique detaillee et
 * tutoriel video.
 *
 * [videoId] est un identifiant YouTube verifie : le bouton ouvre alors la video
 * precise. Quand il vaut null, on retombe sur une recherche ciblee construite a
 * partir de [searchKeyword], ce qui evite un lien mort si une video disparait.
 */
data class ExerciseGuide(
    val name: Localized,
    val setsReps: Localized,
    val description: Localized,
    val writtenTutorial: Localized,
    val searchKeyword: String,
    val videoId: String? = null
)

/**
 * Programmes par sport et par niveau.
 *
 * Auparavant seuls la musculation, le tir a l'arc et la course etaient rediges ;
 * les onze autres sports retombaient sur un texte generique ("Initiation aux
 * fondamentaux") qui n'apprenait rien a personne.
 */
object SportGuides {

    fun forSport(sportName: String, level: SportLevel): List<ExerciseGuide> {
        val s = sportName.lowercase()
        val table = when {
            s.contains("muscu") || s.contains("force") || s.contains("strength") ||
                s.contains("crossfit") -> strength
            s.contains("arc") || s.contains("archery") -> archery
            s.contains("course") || s.contains("run") || s.contains("marche") ||
                s.contains("walk") -> running
            s.contains("cycl") || s.contains("vélo") || s.contains("velo") ||
                s.contains("bike") -> cycling
            s.contains("natation") || s.contains("nage") || s.contains("swim") -> swimming
            s.contains("yoga") -> yoga
            s.contains("stretch") || s.contains("souplesse") -> stretching
            s.contains("football") || s.contains("foot") || s.contains("soccer") -> football
            s.contains("basket") -> basketball
            s.contains("tennis") || s.contains("padel") -> tennis
            s.contains("boxe") || s.contains("boxing") -> boxing
            s.contains("escalade") || s.contains("grimpe") || s.contains("climb") -> climbing
            s.contains("randonn") || s.contains("trek") || s.contains("hik") -> hiking
            s.contains("hiit") || s.contains("fractionn") -> hiit
            else -> null
        }
        return table?.get(level) ?: generic(sportName, level)
    }

    // ── Musculation ───────────────────────────────────────────────────────────
    private val strength = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Squat gobelet", "Goblet squat"),
                t("3 séries × 10-12 reps", "3 sets × 10-12 reps"),
                t("Apprentissage du mouvement de base pour les jambes et les fessiers.",
                    "Learning the fundamental lower-body and glute movement."),
                t("Pieds écartés largeur d'épaules, poitrine fière, descendre jusqu'à avoir les cuisses parallèles au sol sans décoller les talons. Pousser sur les talons pour remonter.",
                    "Feet shoulder-width apart, chest proud, descend until your thighs are parallel to the floor without lifting your heels. Drive through the heels to stand back up."),
                "goblet squat technique debutant",
                "QNPVhVsJS2A"
            ),
            ExerciseGuide(
                t("Développé couché haltères", "Dumbbell bench press"),
                t("3 séries × 10 reps", "3 sets × 10 reps"),
                t("Renforcement des pectoraux, des épaules et des triceps.",
                    "Strengthens the chest, shoulders and triceps."),
                t("Allongé sur un banc, haltères au niveau de la poitrine, pousser vers le haut en contrôlant la descente sur 2 secondes.",
                    "Lying on a bench, dumbbells at chest level, press upward while controlling the lowering phase over 2 seconds."),
                "developpe couche halteres technique",
                "HBXn_SAlPw0"
            ),
            ExerciseGuide(
                t("Tirage horizontal poulie", "Seated cable row"),
                t("3 séries × 12 reps", "3 sets × 12 reps"),
                t("Renforcement du dos et correction de la posture.",
                    "Builds the back and corrects posture."),
                t("Buste droit, tirer la poignée vers le nombril en resserrant les omoplates en fin de mouvement.",
                    "Torso upright, pull the handle toward your navel, squeezing the shoulder blades at the end of the movement."),
                "tirage horizontal poulie technique",
                "9A7cb5YjJew"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Squat barre", "Barbell back squat"),
                t("4 séries × 8 reps", "4 sets × 8 reps"),
                t("Prise de force et de masse sur le bas du corps.",
                    "Builds lower-body strength and size."),
                t("Barre calée sur les trapèzes, inspirer et gainer la ceinture abdominale avant de descendre.",
                    "Bar resting on the traps, inhale and brace your core before descending."),
                "squat barre libre technique",
                "B0Pki58hb50"
            ),
            ExerciseGuide(
                t("Développé couché barre", "Barbell bench press"),
                t("4 séries × 8 reps", "4 sets × 8 reps"),
                t("Exercice de référence pour la force du haut du corps.",
                    "The reference lift for upper-body strength."),
                t("Prise un peu plus large que les épaules, descendre la barre au milieu des pectoraux avec contrôle.",
                    "Grip slightly wider than shoulders, lower the bar to mid-chest under control."),
                "developpe couche barre execution",
                "SngodvMU0JA"
            ),
            ExerciseGuide(
                t("Tractions pronation", "Pull-ups"),
                t("4 séries × 6-8 reps", "4 sets × 6-8 reps"),
                t("Développement de la largeur du dos.",
                    "Develops back width."),
                t("Suspendu à la barre, tirer jusqu'à dépasser la barre avec le menton sans balancer le corps.",
                    "Hanging from the bar, pull until your chin clears it without swinging."),
                "tractions pronation technique",
                "b6KgEsSnhaY"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Soulevé de terre", "Deadlift"),
                t("5 séries × 5 reps", "5 sets × 5 reps"),
                t("Force globale, chaîne postérieure et gainage maximal.",
                    "Full-body strength, posterior chain and maximal bracing."),
                t("Barre collée aux tibias, dos parfaitement neutre, pousser le sol avec les jambes puis verrouiller le bassin.",
                    "Bar against the shins, back perfectly neutral, push the floor away with your legs then lock the hips."),
                "souleve de terre technique execution",
                "QYovptVFHgQ"
            ),
            ExerciseGuide(
                t("Développé militaire debout", "Standing overhead press"),
                t("4 séries × 6 reps", "4 sets × 6 reps"),
                t("Force des épaules et stabilité du tronc.",
                    "Shoulder strength and trunk stability."),
                t("Pieds ancrés, fessiers serrés, presser la barre au-dessus de la tête sans cambrer le bas du dos.",
                    "Feet planted, glutes squeezed, press the bar overhead without arching the lower back."),
                "developpe militaire debout technique",
                "G5lGF7znQYQ"
            ),
            ExerciseGuide(
                t("Dips lestés", "Weighted dips"),
                t("4 séries × 8 reps", "4 sets × 8 reps"),
                t("Intensité maximale sur les triceps et le bas des pectoraux.",
                    "Maximal intensity on triceps and lower chest."),
                t("Corps légèrement incliné vers l'avant, descendre jusqu'à 90 degrés aux coudes puis remonter explosivement.",
                    "Torso leaning slightly forward, lower to 90 degrees at the elbows then drive up explosively."),
                "dips technique musculation",
                "Dbih-r0vMmw"
            )
        )
    )

    // ── Tir à l'arc ───────────────────────────────────────────────────────────
    private val archery = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Position et ancrage", "Stance and anchor point"),
                t("10 volées × 3 flèches", "10 ends × 3 arrows"),
                t("Maîtrise de la posture stable et des repères au visage.",
                    "Mastering a stable stance and consistent facial reference points."),
                t("Pieds perpendiculaires à la cible, épaules basses, corde venant toucher le bout du nez et le coin des lèvres.",
                    "Feet perpendicular to the target, shoulders down, string touching the tip of your nose and the corner of your mouth."),
                "tir a l arc position debutant",
                "QlVipew4oMI"
            ),
            ExerciseGuide(
                t("Armement à l'élastique", "Resistance band draw"),
                t("3 séries × 15 répétitions", "3 sets × 15 reps"),
                t("Renforcement des rhomboïdes sans fatigue articulaire.",
                    "Strengthens the rhomboids without joint fatigue."),
                t("Fixer l'élastique à hauteur d'épaule, simuler l'armement en tirant avec le coude et les muscles du dos, jamais avec le bras.",
                    "Anchor the band at shoulder height and mimic the draw using your elbow and back muscles, never your arm."),
                "elastique entrainement tir a l arc",
                "E5_TdAvxAAk"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Visée et décoche", "Aiming and release"),
                t("12 volées × 3 flèches à 18 m", "12 ends × 3 arrows at 18 m"),
                t("Fluidité de la décoche et maintien de la visée après le départ.",
                    "A clean release and holding the aim through the shot."),
                t("Ne pas bouger le bras d'arc après la décoche, laisser l'arc basculer naturellement vers l'avant.",
                    "Keep the bow arm still after release and let the bow tip forward naturally."),
                "decoche tir a l arc technique",
                "VL9zpc1uav0"
            ),
            ExerciseGuide(
                t("Face pull", "Face pull"),
                t("4 séries × 12 reps", "4 sets × 12 reps"),
                t("Prévention des tendinites d'épaule et stabilité posturale.",
                    "Prevents shoulder tendinitis and improves postural stability."),
                t("Tirer la corde vers le visage en ouvrant les coudes vers le haut et l'arrière, sans hausser les épaules.",
                    "Pull the rope toward your face, opening the elbows up and back, without shrugging."),
                "face pull technique execution",
                "ljgqer1ZpXg"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Tir sous contrainte de temps", "Timed shooting"),
                t("15 volées de 3 flèches, 2 min au chrono", "15 ends of 3 arrows, 2 min on the clock"),
                t("Simuler la pression de compétition et automatiser la routine.",
                    "Simulates competition pressure and automates your shot routine."),
                t("Garder une respiration diaphragmatique calme pendant toute la séquence, sans raccourcir le temps de visée.",
                    "Keep calm diaphragmatic breathing throughout, without cutting your aiming time short."),
                "tir a l arc competition preparation",
                "yx_aFhKf-2U"
            ),
            ExerciseGuide(
                t("Gainage anti-rotation", "Anti-rotation core work"),
                t("4 séries × 45 s", "4 sets × 45 s"),
                t("Stabilité du tronc sous tension de corde lourde.",
                    "Trunk stability under heavy draw weight."),
                t("Gainage latéral avec élévation de jambe, puis Pallof press à la poulie en résistant à la rotation.",
                    "Side plank with leg raise, then cable Pallof press resisting rotation."),
                "pallof press gainage anti rotation",
                "Pz95Wgyt9MA"
            )
        )
    )

    // ── Course et marche ──────────────────────────────────────────────────────
    private val running = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Alternance marche / course", "Walk-run intervals"),
                t("30 min (2 min course / 1 min marche)", "30 min (2 min run / 1 min walk)"),
                t("Habituer les articulations et le système cardiovasculaire.",
                    "Lets your joints and cardiovascular system adapt."),
                t("Courir à une allure où l'on peut parler sans être essoufflé. Poser le pied sous le bassin plutôt que devant.",
                    "Run at a pace where you can still talk. Land with your foot under your hips, not ahead of them."),
                "debuter course a pied alternance marche",
                "Gm2UPZlMNyM"
            ),
            ExerciseGuide(
                t("Renforcement mollets", "Calf and ankle strengthening"),
                t("3 séries × 15 montées sur pointes", "3 sets × 15 calf raises"),
                t("Prévention des périostites tibiales.",
                    "Prevents shin splints."),
                t("Debout sur une marche, monter sur la pointe des pieds puis descendre lentement sous l'horizontale.",
                    "Standing on a step, rise onto your toes then lower slowly below horizontal."),
                "renforcement mollets course a pied",
                "4Z0K1Hxsxlo"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Fractionné 30/30", "30/30 intervals"),
                t("2 blocs de 8 × (30 s vite / 30 s trot)", "2 blocks of 8 × (30 s fast / 30 s jog)"),
                t("Augmentation de la VMA et de la capacité aérobie.",
                    "Raises maximal aerobic speed and aerobic capacity."),
                t("30 secondes à 90-95 % de VMA suivies de 30 secondes de trot lent, sans jamais marcher.",
                    "30 seconds at 90-95% of max aerobic speed followed by 30 seconds of easy jogging, never walking."),
                "fractionne 30 30 course a pied",
                "TOpI9SUWab8"
            ),
            ExerciseGuide(
                t("Sortie longue en endurance", "Long easy run"),
                t("50 à 60 min à allure modérée", "50 to 60 min at an easy pace"),
                t("Développement de la pompe cardiaque et de l'oxydation des lipides.",
                    "Develops cardiac output and fat oxidation."),
                t("Fréquence cardiaque à 70-75 % de la FCM. On doit pouvoir tenir une conversation sans forcer.",
                    "Heart rate at 70-75% of max. You should be able to hold a conversation comfortably."),
                "endurance fondamentale course a pied",
                "H2tOg-02M2Y"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Séance au seuil", "Threshold session"),
                t("3 × 2000 m allure 10 km, 2 min de repos", "3 × 2000 m at 10K pace, 2 min rest"),
                t("Recul du seuil d'accumulation des lactates.",
                    "Pushes back the lactate accumulation threshold."),
                t("Maintenir une allure constante sur chaque répétition, sans partir trop vite sur la première.",
                    "Hold an even pace on every repetition; do not go out too fast on the first one."),
                "seance seuil course a pied",
                "poUd80dAXhc"
            ),
            ExerciseGuide(
                t("Côtes courtes", "Short hill sprints"),
                t("10 × 100 m en côte, retour en trot", "10 × 100 m uphill, jog back down"),
                t("Puissance musculaire et foulée économique.",
                    "Builds muscular power and a more economical stride."),
                t("Pousser fort sur les cuisses, monter les genoux et engager activement les bras.",
                    "Drive hard through the thighs, lift the knees and use your arms actively."),
                "seance cotes course a pied",
                "yS-yHjazoVw"
            )
        )
    )

    // ── Cyclisme ──────────────────────────────────────────────────────────────
    private val cycling = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Sortie en endurance souple", "Easy endurance ride"),
                t("45 min à cadence élevée", "45 min at high cadence"),
                t("Habituer les jambes au pédalage et installer la position.",
                    "Gets your legs used to pedalling and settles your position."),
                t("Viser 80 à 90 tours par minute sur petit braquet. Le but est la fluidité, pas la force.",
                    "Aim for 80-90 rpm in an easy gear. The goal is smoothness, not force."),
                "debuter velo route cadence pedalage",
                "dFbdY_mWn08"
            ),
            ExerciseGuide(
                t("Réglage de la position", "Bike fit basics"),
                t("20 min d'ajustements", "20 min of adjustments"),
                t("Éviter les douleurs de genou et de dos dès le départ.",
                    "Avoids knee and back pain from the very start."),
                t("Jambe presque tendue en bas de course, genou à l'aplomb de l'axe de pédale, buste sans tension dans les épaules.",
                    "Leg almost straight at the bottom of the stroke, knee over the pedal spindle, shoulders relaxed."),
                "reglage position velo route",
                "UJyd9b_vBmk"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Travail au seuil", "Threshold intervals"),
                t("3 × 10 min soutenu, 5 min de récupération", "3 × 10 min hard, 5 min recovery"),
                t("Augmentation de la puissance maintenable sur la durée.",
                    "Raises the power you can sustain over time."),
                t("Allure où la phrase devient hachée. Rester assis et garder la cadence stable.",
                    "An effort where sentences become broken. Stay seated and keep cadence steady."),
                "seance seuil velo entrainement",
                "T9aH7mutI44"
            ),
            ExerciseGuide(
                t("Force en côte", "Low-cadence hill work"),
                t("6 × 3 min à 50-60 tr/min", "6 × 3 min at 50-60 rpm"),
                t("Renforcement spécifique des quadriceps et des fessiers.",
                    "Targeted strengthening of quads and glutes."),
                t("Gros braquet en montée, buste calme, tirer sur le haut de la pédale autant que pousser.",
                    "Big gear uphill, upper body quiet, pull over the top of the stroke as much as you push."),
                "travail de force velo cote",
                "TLuArRrt83Q"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Intervalles VO2max", "VO2max intervals"),
                t("5 × 4 min maximal, 4 min de repos", "5 × 4 min all-out, 4 min rest"),
                t("Développement de la puissance aérobie maximale.",
                    "Develops maximal aerobic power."),
                t("Partir contrôlé sur les 30 premières secondes pour tenir les 4 minutes entières.",
                    "Start controlled for the first 30 seconds so you can hold the full 4 minutes."),
                "intervalles vo2max velo",
                "DE5DYJGWDpU"
            ),
            ExerciseGuide(
                t("Sprints départ arrêté", "Standing start sprints"),
                t("8 × 15 s à fond, 3 min de repos", "8 × 15 s all-out, 3 min rest"),
                t("Puissance neuromusculaire et explosivité.",
                    "Neuromuscular power and explosiveness."),
                t("Départ en danseuse, se rasseoir dès que la cadence monte, relâcher le haut du corps.",
                    "Start out of the saddle, sit down as cadence rises, keep the upper body relaxed."),
                "sprint velo technique puissance",
                "NAtcA5rSRsc"
            )
        )
    )

    // ── Natation ──────────────────────────────────────────────────────────────
    private val swimming = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Respiration et équilibre", "Breathing and body position"),
                t("8 × 25 m avec matériel", "8 × 25 m with aids"),
                t("Se placer haut sur l'eau et respirer sans casser la nage.",
                    "Sitting high in the water and breathing without breaking your stroke."),
                t("Souffler entièrement sous l'eau par le nez, ne tourner que la tête pour inspirer, sans lever le menton.",
                    "Exhale fully underwater through the nose, rotate only the head to inhale, never lifting the chin."),
                "apprendre respiration crawl debutant",
                "3S0i9kroZEM"
            ),
            ExerciseGuide(
                t("Battements avec planche", "Kicking with a board"),
                t("8 × 25 m", "8 × 25 m"),
                t("Gainage et propulsion par les jambes.",
                    "Core stability and leg propulsion."),
                t("Battements partant des hanches et non des genoux, chevilles relâchées, amplitude faible et rythme régulier.",
                    "Kick from the hips rather than the knees, ankles loose, small amplitude and steady rhythm."),
                "battements jambes crawl planche",
                "z9B8vGB4OoY"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Éducatif rattrapé", "Catch-up drill"),
                t("10 × 50 m", "10 × 50 m"),
                t("Allongement de la nage et meilleur appui sur l'eau.",
                    "Lengthens the stroke and improves your catch."),
                t("Une main attend l'autre devant avant de démarrer la traction. Chercher à glisser le plus longtemps possible.",
                    "One hand waits for the other in front before starting the pull. Glide as long as possible."),
                "educatif crawl rattrape technique",
                "kBfnfhZjQbE"
            ),
            ExerciseGuide(
                t("Série en endurance", "Endurance set"),
                t("10 × 100 m, 20 s de repos", "10 × 100 m, 20 s rest"),
                t("Capacité aérobie spécifique et régularité du rythme.",
                    "Sport-specific aerobic capacity and pacing consistency."),
                t("Compter ses coups de bras par longueur et chercher à garder le même nombre du début à la fin.",
                    "Count your strokes per length and try to keep the same count from start to finish."),
                "seance natation endurance crawl",
                "jdYuC39ncEg"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Série au seuil", "Threshold set"),
                t("8 × 200 m soutenu, 30 s de repos", "8 × 200 m hard, 30 s rest"),
                t("Maintien d'une allure élevée sous fatigue.",
                    "Holding a high pace under fatigue."),
                t("Garder une nage longue malgré la fatigue ; c'est l'amplitude qui doit tenir, pas la fréquence.",
                    "Keep the stroke long despite fatigue: it is distance per stroke that must hold, not turnover."),
                "entrainement natation seuil",
                "p9LW22WvJug"
            ),
            ExerciseGuide(
                t("Virages et coulées", "Turns and underwaters"),
                t("16 × 25 m départ plongé", "16 × 25 m from a dive"),
                t("Gains sur les phases non nagées, souvent décisives.",
                    "Gains on the non-swimming phases, often decisive."),
                t("Coulée en position hydrodynamique, bras serrés derrière la tête, ondulations jusqu'à la reprise de nage.",
                    "Streamline off the wall, arms locked behind the head, dolphin kick until you break out."),
                "technique virage culbute natation",
                "lwOBe-G-4pc"
            )
        )
    )

    // ── Yoga ──────────────────────────────────────────────────────────────────
    private val yoga = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Salutation au soleil", "Sun salutation"),
                t("3 cycles lents", "3 slow rounds"),
                t("Enchaînement de base qui mobilise tout le corps.",
                    "The foundational sequence that mobilises the whole body."),
                t("Synchroniser chaque mouvement sur une inspiration ou une expiration. Ne jamais forcer sur les étirements.",
                    "Sync every movement with an inhale or an exhale. Never force a stretch."),
                "salutation au soleil yoga debutant",
                "STNry5WIBbM"
            ),
            ExerciseGuide(
                t("Chien tête en bas", "Downward facing dog"),
                t("5 respirations × 4", "5 breaths × 4"),
                t("Étirement de la chaîne postérieure et renforcement des épaules.",
                    "Stretches the posterior chain and strengthens the shoulders."),
                t("Genoux légèrement fléchis si les ischio-jambiers tirent, priorité au dos long plutôt qu'aux talons au sol.",
                    "Bend the knees slightly if your hamstrings pull; a long spine matters more than heels on the floor."),
                "chien tete en bas yoga technique",
                "dhhH0P7IFGw"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Postures du guerrier", "Warrior poses"),
                t("5 respirations par posture", "5 breaths per pose"),
                t("Force des jambes, ouverture des hanches et équilibre.",
                    "Leg strength, hip opening and balance."),
                t("Genou avant à l'aplomb de la cheville, hanches conscientes de leur orientation, regard posé sur un point fixe.",
                    "Front knee stacked over the ankle, hips consciously oriented, gaze fixed on one point."),
                "posture guerrier yoga enchainement",
                "BxIqe_D5kkI"
            ),
            ExerciseGuide(
                t("Équilibres sur une jambe", "Single-leg balances"),
                t("4 × 45 s par côté", "4 × 45 s per side"),
                t("Proprioception et gainage profond.",
                    "Proprioception and deep core control."),
                t("Fixer un point immobile, ancrer les orteils, respirer calmement plutôt que de bloquer la respiration.",
                    "Fix your gaze on a still point, spread the toes, and keep breathing rather than holding your breath."),
                "equilibre yoga arbre technique",
                "ad4CHIBAEQc"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Inversions", "Inversions"),
                t("5 × 30 s contre un mur", "5 × 30 s against a wall"),
                t("Force des épaules et contrôle du gainage.",
                    "Shoulder strength and core control."),
                t("Monter en contrôle, jamais en élan. Épaules à l'aplomb des coudes, côtes rentrées, bassin en rétroversion.",
                    "Enter under control, never by kicking up. Shoulders over elbows, ribs in, pelvis tucked."),
                "poirier yoga apprentissage progression",
                "ATL79J-9z3g"
            ),
            ExerciseGuide(
                t("Flexions arrière profondes", "Deep backbends"),
                t("5 respirations × 5", "5 breaths × 5"),
                t("Mobilité thoracique et ouverture des hanches.",
                    "Thoracic mobility and hip opening."),
                t("L'extension vient du haut du dos, pas des lombaires. Fessiers relâchés, pubis poussé vers l'avant.",
                    "The extension comes from the upper back, not the lumbar spine. Glutes soft, pubic bone forward."),
                "flexion arriere yoga pont technique",
                "19k0b11R65Q"
            )
        )
    )

    // ── Stretching ────────────────────────────────────────────────────────────
    private val stretching = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Routine complète", "Full-body routine"),
                t("8 postures × 30 s", "8 positions × 30 s"),
                t("Relâcher les tensions et gagner en amplitude générale.",
                    "Releases tension and builds general range of motion."),
                t("Étirer sans douleur, jusqu'à une tension supportable. Respirer lentement et ne jamais faire de à-coups.",
                    "Stretch to a bearable tension, never to pain. Breathe slowly and avoid bouncing."),
                "routine etirements complete debutant",
                "nMgxn1JkUkU"
            ),
            ExerciseGuide(
                t("Mobilité des hanches", "Hip mobility"),
                t("5 × 45 s par côté", "5 × 45 s per side"),
                t("Contrer les effets de la position assise prolongée.",
                    "Counters the effects of prolonged sitting."),
                t("Fente basse avec bassin en rétroversion pour sentir l'avant de la cuisse, sans cambrer le bas du dos.",
                    "Low lunge with the pelvis tucked to feel the front of the thigh, without arching the lower back."),
                "mobilite hanches etirement psoas",
                "8T49uDkpi7w"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Étirements activo-passifs", "Contract-relax stretching"),
                t("6 exercices × 3 contractions", "6 exercises × 3 contractions"),
                t("Gains d'amplitude durables via le relâchement neuromusculaire.",
                    "Lasting range gains through neuromuscular relaxation."),
                t("Tenir la position, contracter 6 secondes contre résistance, relâcher puis gagner quelques degrés.",
                    "Hold the position, contract for 6 seconds against resistance, release, then ease a few degrees deeper."),
                "etirement contracte relache technique",
                "VYDPMp1GRFo"
            ),
            ExerciseGuide(
                t("Mobilité thoracique", "Thoracic mobility"),
                t("4 × 10 répétitions par côté", "4 × 10 reps per side"),
                t("Indispensable pour les épaules et la posture assise.",
                    "Essential for shoulder health and seated posture."),
                t("En quadrupédie, main derrière la tête, ouvrir le coude vers le plafond en suivant du regard.",
                    "On all fours, hand behind the head, open the elbow toward the ceiling and follow it with your eyes."),
                "mobilite thoracique exercice",
                "zXD5wBcaSRs"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Travail du grand écart", "Splits progression"),
                t("4 séries × 60 s par position", "4 sets × 60 s per position"),
                t("Amplitude maximale des hanches et des ischio-jambiers.",
                    "Maximal hip and hamstring range."),
                t("Progresser sur plusieurs mois. Toujours échauffer avant, ne jamais chercher l'amplitude à froid.",
                    "Progress over months. Always warm up first; never chase range when cold."),
                "progression grand ecart etirement",
                "vib2Bra7hnw"
            ),
            ExerciseGuide(
                t("Mobilité épaules et poignets", "Shoulder and wrist mobility"),
                t("5 × 40 s", "5 × 40 s"),
                t("Prévention des blessures sur les appuis au sol.",
                    "Injury prevention for floor-based support work."),
                t("Rotations lentes et complètes, en cherchant les fins d'amplitude sans jamais entrer dans la douleur.",
                    "Slow full rotations, seeking end range without ever entering pain."),
                "mobilite epaules poignets exercices",
                "74h-SZk2Bqc"
            )
        )
    )

    // ── Football ──────────────────────────────────────────────────────────────
    private val football = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Conduite de balle", "Dribbling control"),
                t("6 × 30 m en slalom", "6 × 30 m slalom"),
                t("Contrôle du ballon en mouvement, les deux pieds.",
                    "Controlling the ball on the move, both feet."),
                t("Toucher le ballon à chaque appui avec l'extérieur du pied, tête relevée le plus souvent possible.",
                    "Touch the ball on every stride with the outside of the foot, head up as much as possible."),
                "conduite de balle football exercice",
                "fZjlT8Sorlg"
            ),
            ExerciseGuide(
                t("Passes contre un mur", "Wall passing"),
                t("5 × 2 min", "5 × 2 min"),
                t("Qualité du premier contact et précision de passe.",
                    "First-touch quality and passing accuracy."),
                t("Contrôle orienté vers l'espace libre, passe avec l'intérieur du pied et cheville verrouillée.",
                    "Take your first touch into space, pass with the inside of the foot and a locked ankle."),
                "controle passe football technique",
                "yZwKLRjYZcA"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Frappes de balle", "Shooting technique"),
                t("5 × 10 frappes", "5 × 10 shots"),
                t("Puissance et précision devant le but.",
                    "Power and accuracy in front of goal."),
                t("Pied d'appui à côté du ballon, frappe avec le cou-de-pied, regard sur le ballon au moment de l'impact.",
                    "Plant foot beside the ball, strike with the laces, eyes on the ball at contact."),
                "technique frappe de balle football",
                "b3s9UzsUwV8"
            ),
            ExerciseGuide(
                t("Vivacité et appuis", "Agility and footwork"),
                t("6 × 20 s à haute intensité", "6 × 20 s high intensity"),
                t("Explosivité sur les premiers mètres.",
                    "Explosiveness over the first few metres."),
                t("Appuis courts et rapides, centre de gravité bas, regard toujours relevé.",
                    "Short quick steps, low centre of gravity, head always up."),
                "exercice vivacite appuis football",
                "RgcK39U5v2Y"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Jeu en une touche", "One-touch play"),
                t("4 blocs de 5 min", "4 blocks of 5 min"),
                t("Vitesse de décision et qualité technique sous pression.",
                    "Decision speed and technical quality under pressure."),
                t("Préparer son contrôle avant réception en regardant autour de soi, orienter le corps à l'avance.",
                    "Scan before the ball arrives and open your body in advance."),
                "exercice jeu une touche football",
                "xdzKGTubhBY"
            ),
            ExerciseGuide(
                t("Répétition de sprints", "Repeated sprints"),
                t("10 × 30 m, 30 s de récupération", "10 × 30 m, 30 s recovery"),
                t("Capacité à répéter les efforts intenses en match.",
                    "The ability to repeat high-intensity efforts in a match."),
                t("Départ explosif, décélération contrôlée pour protéger les ischio-jambiers.",
                    "Explosive start, controlled deceleration to protect the hamstrings."),
                "repetition sprints football preparation",
                "OVYOtw_BtXs"
            )
        )
    )

    // ── Basketball ────────────────────────────────────────────────────────────
    private val basketball = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Dribble des deux mains", "Two-hand dribbling"),
                t("5 × 2 min", "5 × 2 min"),
                t("Aisance avec le ballon sans regarder au sol.",
                    "Handling the ball without looking down."),
                t("Pousser le ballon avec le bout des doigts, pas la paume. Main libre qui protège le ballon.",
                    "Push the ball with your fingertips, not the palm. Free hand protects the ball."),
                "apprendre dribble basket debutant",
                "DV_Y3-NHh1w"
            ),
            ExerciseGuide(
                t("Tir en course", "Lay-up"),
                t("4 × 10 de chaque côté", "4 × 10 each side"),
                t("Finition près du cercle, des deux mains.",
                    "Finishing at the rim with both hands."),
                t("Rythme deux appuis, monter le genou opposé à la main qui tire, déposer le ballon sur la planche.",
                    "Two-step rhythm, drive the knee opposite your shooting hand, lay the ball off the backboard."),
                "lay up basket technique",
                "wpt2VeIMXgI"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Mécanique de tir", "Shooting mechanics"),
                t("10 × 10 tirs à distance croissante", "10 × 10 shots at increasing range"),
                t("Régularité du geste et de la trajectoire.",
                    "Consistency of form and arc."),
                t("Coude sous le ballon, poignet cassé en fin de geste, jambes qui donnent la puissance.",
                    "Elbow under the ball, wrist snapped on follow-through, power coming from the legs."),
                "mecanique de tir basket technique",
                "Ian4YZzLIMM"
            ),
            ExerciseGuide(
                t("Défense et déplacements", "Defensive slides"),
                t("6 × 45 s", "6 × 45 s"),
                t("Position défensive basse et glissements latéraux.",
                    "Low defensive stance and lateral sliding."),
                t("Pieds toujours plus écartés que les épaules, ne jamais croiser les jambes, mains actives.",
                    "Feet wider than the shoulders, never cross your legs, hands active."),
                "defense basket deplacement lateral",
                "-g_zsC0yOC8"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Tir en sortie de dribble", "Pull-up jumper"),
                t("8 × 10 tirs", "8 × 10 shots"),
                t("Création de son propre tir sous fatigue.",
                    "Creating your own shot under fatigue."),
                t("Stopper net dans l'axe du panier, épaules face à la cible, tir déclenché à la montée.",
                    "Stop square to the rim, shoulders facing the target, release on the way up."),
                "tir sortie de dribble basket",
                "V2AyFZr08hQ"
            ),
            ExerciseGuide(
                t("Pliométrie et détente", "Plyometrics and vertical jump"),
                t("5 × 8 sauts", "5 × 8 jumps"),
                t("Gain de détente verticale.",
                    "Improves vertical jump."),
                t("Amortir en souplesse et enchaîner immédiatement. La qualité du contact au sol prime sur le nombre.",
                    "Land softly and rebound immediately. Ground contact quality matters more than volume."),
                "pliometrie detente verticale exercice",
                "x84r0G2gYII"
            )
        )
    )

    // ── Tennis et padel ───────────────────────────────────────────────────────
    private val tennis = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Coup droit", "Forehand"),
                t("5 × 20 balles", "5 × 20 balls"),
                t("Geste de base et point d'impact devant le corps.",
                    "The fundamental stroke and contact point in front of the body."),
                t("Préparation tôt, raquette qui part de bas vers le haut, finition au-dessus de l'épaule opposée.",
                    "Prepare early, swing low to high, finish above the opposite shoulder."),
                "coup droit tennis technique debutant",
                "xue9cSVZp-U"
            ),
            ExerciseGuide(
                t("Revers à deux mains", "Two-handed backhand"),
                t("5 × 20 balles", "5 × 20 balls"),
                t("Stabilité du côté faible.",
                    "Stability on your weaker side."),
                t("Les deux mains serrées, rotation des épaules qui commande le geste, transfert du poids vers l'avant.",
                    "Both hands firm, shoulder rotation driving the stroke, weight transferring forward."),
                "revers deux mains tennis technique",
                "0APgBmGB7xo"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Service", "Serve"),
                t("6 × 15 services", "6 × 15 serves"),
                t("Régularité du premier service et variation des zones.",
                    "First-serve consistency and target variation."),
                t("Lancer de balle constant et légèrement devant, jambes qui poussent, pronation au moment de l'impact.",
                    "Consistent toss slightly in front, legs driving up, forearm pronation at contact."),
                "service tennis technique pronation",
                "AVHnfvjFmXk"
            ),
            ExerciseGuide(
                t("Déplacements et replacement", "Movement and recovery"),
                t("6 × 90 s", "6 × 90 s"),
                t("Occuper le centre après chaque frappe.",
                    "Recovering to the centre after every shot."),
                t("Pas chassés vers le centre dès la frappe terminée, split step au moment où l'adversaire frappe.",
                    "Side-step back to centre as soon as you finish, split step as your opponent strikes."),
                "deplacement replacement tennis exercice",
                "awD24d5Thds"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Lift et contrôle de trajectoire", "Topspin and trajectory control"),
                t("8 × 15 balles", "8 × 15 balls"),
                t("Marge au-dessus du filet sans perdre en longueur.",
                    "Net clearance without losing depth."),
                t("Frappe brossée de bas en haut, raquette qui accélère dans l'impact, plan de frappe très en avant.",
                    "Brush low to high, racket accelerating through contact, strike well in front."),
                "effet lift tennis technique",
                "fCsQQru-eTE"
            ),
            ExerciseGuide(
                t("Jeu au filet", "Net play and volleys"),
                t("6 × 2 min", "6 × 2 min"),
                t("Finition rapide des points.",
                    "Finishing points quickly."),
                t("Volée bloquée sans armer, raquette devant le corps, jambes qui avancent vers la balle.",
                    "Block the volley without a backswing, racket in front, legs moving into the ball."),
                "volee tennis technique filet",
                "cgFLSdjpA94"
            )
        )
    )

    // ── Boxe ──────────────────────────────────────────────────────────────────
    private val boxing = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Garde et déplacements", "Guard and footwork"),
                t("5 × 2 min", "5 × 2 min"),
                t("La base de tout : position, équilibre, mobilité.",
                    "The foundation: stance, balance, mobility."),
                t("Mains hautes au niveau des pommettes, menton rentré, déplacements en pas glissés sans jamais croiser les pieds.",
                    "Hands high at cheekbone level, chin tucked, slide your steps and never cross your feet."),
                "garde deplacement boxe debutant",
                "5ePgze-wCvo"
            ),
            ExerciseGuide(
                t("Direct avant et direct arrière", "Jab and cross"),
                t("6 × 2 min au sac", "6 × 2 min on the bag"),
                t("Les deux coups fondamentaux.",
                    "The two fundamental punches."),
                t("Le coup part du sol : rotation du pied arrière, puis hanche, puis épaule. Le bras revient immédiatement en garde.",
                    "The punch starts from the floor: rear foot rotates, then hip, then shoulder. The hand returns to guard immediately."),
                "direct jab boxe technique",
                "fm1tL3RYiTY"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Enchaînements et crochets", "Combinations and hooks"),
                t("8 × 2 min", "8 × 2 min"),
                t("Fluidité et variation des angles.",
                    "Fluidity and varied angles."),
                t("Crochet coude à 90 degrés, rotation des appuis, ne jamais lancer deux fois le même coup au même endroit.",
                    "Hook with the elbow at 90 degrees, rotate on your feet, never throw the same punch twice to the same spot."),
                "crochet boxe technique enchainement",
                "Nxqazl8Azao"
            ),
            ExerciseGuide(
                t("Esquives et contres", "Slips and counters"),
                t("6 × 2 min", "6 × 2 min"),
                t("Défense active plutôt que subie.",
                    "Active rather than passive defence."),
                t("Esquive rotative en fléchissant les jambes, pas en cassant le dos. Contre immédiat dans la remontée.",
                    "Slip by bending the legs, not the back. Counter immediately as you come back up."),
                "esquive rotative boxe technique",
                "A17N3G8vlpE"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Sparring technique", "Technical sparring"),
                t("6 × 3 min", "6 × 3 min"),
                t("Application sous opposition réelle et contrôlée.",
                    "Applying skills under real, controlled opposition."),
                t("Travailler un objectif précis par round plutôt que de chercher à gagner l'échange.",
                    "Work one specific goal per round rather than trying to win the exchange."),
                "sparring boxe conseils technique",
                "UeMUVS_Qspw"
            ),
            ExerciseGuide(
                t("Conditionnement spécifique", "Boxing conditioning"),
                t("10 × 1 min à intensité maximale", "10 × 1 min all-out"),
                t("Tenir le rythme sur la durée d'un combat.",
                    "Holding the pace for a full bout."),
                t("Alterner sac lourd, corde à sauter et gainage sans récupération complète entre les blocs.",
                    "Alternate heavy bag, skipping and core work without full recovery between blocks."),
                "preparation physique boxe conditionnement",
                "83YdfEegHE8"
            )
        )
    )

    // ── Escalade ──────────────────────────────────────────────────────────────
    private val climbing = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Placement de pieds", "Footwork"),
                t("6 voies faciles", "6 easy routes"),
                t("Grimper avec les jambes et non avec les bras.",
                    "Climbing with your legs rather than your arms."),
                t("Poser la pointe du chausson précisément, regarder son pied jusqu'à la pose, puis pousser sur la jambe.",
                    "Place the toe of the shoe precisely, watch your foot until it lands, then push through the leg."),
                "placement de pieds escalade debutant",
                "6H8b9RSvEKs"
            ),
            ExerciseGuide(
                t("Grimper bras tendus", "Straight-arm climbing"),
                t("5 voies en travail", "5 working routes"),
                t("Réduire la fatigue des avant-bras.",
                    "Reduces forearm fatigue."),
                t("Grimper bras tendus au maximum, bassin proche du mur, ne plier les bras que pour attraper une prise.",
                    "Keep arms straight as much as possible, hips close to the wall, bend only to reach the next hold."),
                "escalade bras tendus economie",
                "SU6vAxJMR-o"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Lolotte et gainage latéral", "Drop knee and lateral core"),
                t("8 blocs techniques", "8 technical boulders"),
                t("Gagner de l'allonge sans force supplémentaire.",
                    "Gaining reach without extra strength."),
                t("Rotation de la hanche pour coller l'extérieur du pied au mur, épaule qui s'éloigne de la prise visée.",
                    "Rotate the hip to press the outside edge of the shoe into the wall, shoulder turning away from the target hold."),
                "lolotte escalade technique",
                "BR2leQOzYeg"
            ),
            ExerciseGuide(
                t("Continuité", "Continuous climbing"),
                t("4 × 6 min de grimpe continue", "4 × 6 min continuous"),
                t("Endurance de force des avant-bras.",
                    "Forearm strength endurance."),
                t("Rester en mouvement sans repos prolongé, respirer en continu et relâcher la main libre entre les prises.",
                    "Keep moving without long rests, breathe continuously and shake out the free hand between holds."),
                "continuite escalade endurance",
                "FfrcFa-Ew68"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Suspensions sur poutre", "Hangboard repeaters"),
                t("6 × 10 s de suspension, 3 min de repos", "6 × 10 s hangs, 3 min rest"),
                t("Force de doigts, avec beaucoup de précaution.",
                    "Finger strength, with great care."),
                t("Toujours après un échauffement complet. Épaules actives, jamais relâchées, et pas plus de deux séances par semaine.",
                    "Always after a full warm-up. Shoulders engaged, never passive, and no more than twice a week."),
                "poutre escalade protocole suspension",
                "ftSie8Psges"
            ),
            ExerciseGuide(
                t("Travail de mouvement dur", "Limit bouldering"),
                t("10 essais sur un bloc limite", "10 attempts on a limit boulder"),
                t("Résoudre des mouvements au-dessus de son niveau.",
                    "Solving moves above your current level."),
                t("Analyser la méthode avant de grimper, viser un essai propre plutôt que dix essais fatigués.",
                    "Work out the sequence before pulling on; aim for one clean attempt rather than ten tired ones."),
                "travail bloc difficile methode escalade",
                "mx1GbnPorLo"
            )
        )
    )

    // ── Randonnée ─────────────────────────────────────────────────────────────
    private val hiking = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Sortie progressive", "Progressive hike"),
                t("2 h avec 300 m de dénivelé", "2 h with 300 m of ascent"),
                t("Habituer le corps au dénivelé et au port du sac.",
                    "Adapting to elevation gain and carrying a pack."),
                t("Adopter dès le départ une allure où la conversation reste possible. Régler le sac pour que le poids porte sur les hanches.",
                    "Set a conversational pace from the start. Adjust the pack so the weight sits on your hips."),
                "debuter randonnee conseils allure",
                "1znMDhqZWXE"
            ),
            ExerciseGuide(
                t("Renforcement des jambes", "Leg strengthening"),
                t("3 séries × 15 fentes", "3 sets × 15 lunges"),
                t("Protéger les genoux en descente.",
                    "Protects the knees on the way down."),
                t("Fentes contrôlées, genou qui ne dépasse pas la pointe du pied, descente lente sur 3 secondes.",
                    "Controlled lunges, knee not past the toes, lowering over 3 seconds."),
                "renforcement jambes randonnee genoux",
                "oXjz_DYgV8g"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Sortie avec dénivelé soutenu", "Sustained climb"),
                t("4 h avec 800 m de dénivelé", "4 h with 800 m of ascent"),
                t("Endurance spécifique en montée.",
                    "Specific uphill endurance."),
                t("Rythme régulier, petits pas en forte pente, pauses courtes plutôt que longues.",
                    "Steady rhythm, short steps on steep ground, brief stops rather than long ones."),
                "randonnee denivele technique montee",
                "llJWiSODLkY"
            ),
            ExerciseGuide(
                t("Descente et bâtons", "Descending with poles"),
                t("1 h de travail en descente", "1 h of downhill work"),
                t("Préserver les articulations sur la partie la plus traumatisante.",
                    "Protecting the joints on the most damaging phase."),
                t("Bâtons réglés plus longs en descente, pose de pied à plat, genoux souples en permanence.",
                    "Lengthen the poles for descents, land flat-footed, keep the knees soft throughout."),
                "technique descente batons randonnee",
                "Cd6aKUK8DXY"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("Sortie longue en autonomie", "Long self-supported hike"),
                t("6 à 8 h avec 1500 m de dénivelé", "6 to 8 h with 1500 m of ascent"),
                t("Gestion de l'effort, de l'eau et de l'alimentation.",
                    "Managing effort, hydration and fuelling."),
                t("Manger toutes les heures avant d'avoir faim, boire régulièrement par petites quantités.",
                    "Eat every hour before you feel hungry, drink small amounts regularly."),
                "randonnee longue gestion effort autonomie",
                "rw5GS-BsUBk"
            ),
            ExerciseGuide(
                t("Terrain technique", "Technical terrain"),
                t("3 h sur sentier accidenté", "3 h on rough trail"),
                t("Pied sûr sur pierrier et passages exposés.",
                    "Sure footing on scree and exposed sections."),
                t("Regard porté deux à trois pas devant, appuis francs, centre de gravité au-dessus du pied porteur.",
                    "Look two or three steps ahead, commit to each placement, keep your weight over the supporting foot."),
                "progression terrain technique montagne",
                "1Akc5ZLrZkQ"
            )
        )
    )

    // ── HIIT ──────────────────────────────────────────────────────────────────
    private val hiit = mapOf(
        SportLevel.BEGINNER to listOf(
            ExerciseGuide(
                t("Circuit découverte", "Introductory circuit"),
                t("3 tours de 4 exercices, 30 s / 30 s", "3 rounds of 4 exercises, 30 s / 30 s"),
                t("Découvrir le format sans se blesser.",
                    "Learning the format without getting hurt."),
                t("Squats, pompes sur les genoux, montées de genoux, gainage. Priorité absolue à la qualité d'exécution.",
                    "Squats, knee push-ups, high knees, plank. Execution quality comes before everything else."),
                "hiit debutant seance 20 minutes",
                "QGbANkBOFJw"
            ),
            ExerciseGuide(
                t("Échauffement articulaire", "Joint warm-up"),
                t("8 min", "8 min"),
                t("Préparer les articulations à l'intensité.",
                    "Preparing the joints for intensity."),
                t("Mobilisation de toutes les articulations puis montée cardiaque progressive sur 3 minutes.",
                    "Mobilise every joint, then raise the heart rate progressively over 3 minutes."),
                "echauffement avant hiit mobilite",
                "fIKcsi91j3c"
            )
        ),
        SportLevel.INTERMEDIATE to listOf(
            ExerciseGuide(
                t("Tabata", "Tabata"),
                t("8 × (20 s à fond / 10 s repos) × 3 blocs", "8 × (20 s all-out / 10 s rest) × 3 blocks"),
                t("Intensité maximale sur format court.",
                    "Maximal intensity in a short format."),
                t("Choisir un exercice maîtrisé : la fatigue ne doit jamais dégrader la technique.",
                    "Pick a movement you own: fatigue must never degrade technique."),
                "tabata seance technique",
                "7R6HRYiRY40"
            ),
            ExerciseGuide(
                t("Circuit pleine puissance", "Full-body circuit"),
                t("5 tours de 5 exercices, 40 s / 20 s", "5 rounds of 5 exercises, 40 s / 20 s"),
                t("Endurance musculaire globale.",
                    "Whole-body muscular endurance."),
                t("Enchaîner haut du corps et bas du corps pour répartir la fatigue locale.",
                    "Alternate upper and lower body to spread local fatigue."),
                "circuit hiit full body seance",
                "tzbxIJudsqI"
            )
        ),
        SportLevel.ADVANCED to listOf(
            ExerciseGuide(
                t("EMOM 20 minutes", "20-minute EMOM"),
                t("20 × 1 min, un exercice par minute", "20 × 1 min, one exercise per minute"),
                t("Gestion de l'effort sous contrainte de temps.",
                    "Pacing under a time constraint."),
                t("Finir chaque bloc en 40 secondes pour conserver 20 secondes de récupération. Si ce n'est plus possible, réduire les répétitions.",
                    "Finish each block in 40 seconds to keep 20 seconds of rest. If you cannot, cut the reps."),
                "emom seance crossfit technique",
                "2Uv4dF7CssU"
            ),
            ExerciseGuide(
                t("Intervalles longs", "Long intervals"),
                t("6 × 3 min intense, 90 s de repos", "6 × 3 min hard, 90 s rest"),
                t("Puissance aérobie maximale.",
                    "Maximal aerobic power."),
                t("Allure tenable sur les 3 minutes entières : partir trop vite ruine la fin de série.",
                    "A pace you can hold for the full 3 minutes: going out too hard ruins the end of the set."),
                "intervalles longs hiit cardio",
                "J212vz33gU4"
            )
        )
    )

    // ── Repli pour un sport personnalise ─────────────────────────────────────
    private fun generic(sportName: String, level: SportLevel): List<ExerciseGuide> = when (level) {
        SportLevel.BEGINNER -> listOf(
            ExerciseGuide(
                t("Apprentissage des fondamentaux", "Learning the fundamentals"),
                t("30-40 min", "30-40 min"),
                t("Découverte des gestes de base et échauffement complet.",
                    "Discovering the basic movements with a full warm-up."),
                t("Prendre le temps d'assimiler la technique sans chercher l'intensité. Se filmer aide à progresser vite.",
                    "Take time to absorb the technique before chasing intensity. Filming yourself speeds up progress."),
                "tutoriel debutant $sportName"
            ),
            ExerciseGuide(
                t("Échauffement spécifique", "Sport-specific warm-up"),
                t("10-15 min", "10-15 min"),
                t("Préparation neuromusculaire et cardiovasculaire.",
                    "Neuromuscular and cardiovascular preparation."),
                t("Mobiliser les articulations sollicitées puis monter progressivement le rythme cardiaque.",
                    "Mobilise the joints involved, then raise the heart rate progressively."),
                "echauffement specifique $sportName"
            )
        )
        SportLevel.INTERMEDIATE -> listOf(
            ExerciseGuide(
                t("Séance structurée", "Structured session"),
                t("45-60 min", "45-60 min"),
                t("Développement de l'intensité et de l'endurance spécifique.",
                    "Building intensity and sport-specific endurance."),
                t("Alterner les phases techniques et les séquences à intensité modérée.",
                    "Alternate technical work with moderate-intensity blocks."),
                "entrainement intermediaire $sportName"
            ),
            ExerciseGuide(
                t("Travail de régularité", "Consistency work"),
                t("4 séries de 5 min", "4 sets of 5 min"),
                t("Répétition des gestes clés sous fatigue modérée.",
                    "Repeating key movements under moderate fatigue."),
                t("Se concentrer sur la fluidité et la constance plutôt que sur la performance.",
                    "Focus on smoothness and consistency rather than performance."),
                "exercices progression $sportName"
            )
        )
        SportLevel.ADVANCED -> listOf(
            ExerciseGuide(
                t("Séance à haute intensité", "High-intensity session"),
                t("60-75 min", "60-75 min"),
                t("Performance et dépassement du seuil habituel.",
                    "Performance work beyond your usual threshold."),
                t("Travailler par blocs intenses entrecoupés de récupérations incomplètes.",
                    "Work in hard blocks separated by incomplete recovery."),
                "entrainement avance $sportName"
            ),
            ExerciseGuide(
                t("Préparation physique spécifique", "Specific physical preparation"),
                t("40 min", "40 min"),
                t("Renforcement ciblé sur les points faibles du sport.",
                    "Targeted strengthening of the sport's weak points."),
                t("Identifier les groupes musculaires les plus sollicités et les renforcer en priorité.",
                    "Identify the muscle groups most used and strengthen them first."),
                "preparation physique $sportName"
            )
        )
    }
}
