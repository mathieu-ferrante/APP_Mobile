# Phoenix

Phoenix est une application Android de suivi fitness et bien-être. Elle permet de suivre ses séances, sa nutrition, ses statistiques et sa progression, avec un coach assisté par IA.

## Fonctionnalités

- Tableau de bord avec résumé quotidien, séries de jours et progression
- Création et historique de séances sportives
- Bibliothèque de sports personnalisée
- Suivi des repas et des apports nutritionnels
- Statistiques hebdomadaires, mensuelles et annuelles
- Profil, objectifs et badges de progression
- Synchronisation cloud avec Firebase et connexion Google
- Coach IA et génération de programmes avec Gemini

## Technologies

- Kotlin et Jetpack Compose
- Material 3 et Navigation Compose
- Hilt pour l'injection de dépendances
- Room et DataStore pour le stockage local
- Firebase Authentication, Firestore et Analytics
- Retrofit, OkHttp et Gson pour les appels réseau
- WorkManager pour les tâches en arrière-plan

## Prérequis

- Android Studio récent
- JDK 17
- Android SDK 35
- Un appareil ou émulateur Android API 26 ou supérieur
- Un projet Firebase associé à l'application

## Configuration

1. Ouvrir le projet dans Android Studio.
2. Ajouter le fichier `app/google-services.json` depuis la console Firebase.
3. Créer ou compléter `local.properties` à la racine du projet :

```properties
sdk.dir=C:\\Users\\<utilisateur>\\AppData\\Local\\Android\\Sdk
gemini.api.key=VOTRE_CLE_GEMINI
gemini.model=gemini-3.5-flash-lite
```

`local.properties` est ignoré par Git et ne doit jamais être commitée.

## Compilation et lancement

Depuis la racine du projet :

```powershell
./gradlew.bat assembleDebug
./gradlew.bat installDebug
```

Le variant debug utilise l'identifiant d'application `com.phoenix.fitpro.phoenix.debug`.

Pour générer une version release :

```powershell
./gradlew.bat assembleRelease
```

## Structure

```text
app/src/main/java/com/phoenix/fitpro/
├── data/          # Sources locales, réseau et implémentations des repositories
├── di/            # Modules Hilt
├── domain/        # Modèles, services IA et contrats métier
└── presentation/  # Navigation, écrans, ViewModels et thème Compose
```

## Sécurité

Les clés API et les paramètres propres à la machine doivent rester dans `local.properties`. Le fichier Firebase peut contenir des identifiants publics de projet, mais les règles Firebase et les clés serveur doivent être protégées côté console.
