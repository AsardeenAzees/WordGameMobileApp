# Word Guessing Game (Android)

An Android mobile application for the MAD Word Guessing Game assignment (ITE 2152). Players guess randomly generated words within ten attempts while maintaining a score that penalises incorrect guesses and clue usage. The app supports onboarding with persistent player names, time tracking, multi-level progression, and an optional Dreamlo-powered global leaderboard.

## Highlights
- Kotlin + Jetpack Compose UI with Material 3 styling.
- Word retrieval from Random Word API with Datamuse synonyms/rhymes for tips.
- SharedPreferences-backed onboarding and profile persistence.
- Scoring, timer, and clue mechanics implemented in a reusable domain engine.
- Leaderboard integration via Dreamlo (configurable BuildConfig keys).
- Compose Navigation for onboarding, game, leaderboard, and settings flows.
- Unit tests for critical game engine logic.

## Project Structure
```
WordGameMobileApp/
+-- app/                     # Android application module
¦   +-- src/main/java/com/example/wordgame/
¦   ¦   +-- data/            # Local/remote data sources and repositories
¦   ¦   +-- domain/          # Game models and GameEngine
¦   ¦   +-- presentation/    # ViewModels per feature
¦   ¦   +-- ui/              # Compose UI and navigation
¦   +-- src/test/            # Unit tests
+-- docs/                    # Project documentation
+-- build.gradle.kts         # Root Gradle configuration
+-- settings.gradle.kts
```

## Getting Started
1. Open the project in Android Studio Giraffe+.
2. Replace `BuildConfig.DREAMLO_PUBLIC_CODE` and `BuildConfig.DREAMLO_PRIVATE_CODE` in `app/build.gradle.kts` with your Dreamlo leaderboard codes (optional).
3. Sync Gradle. If the Gradle wrapper JAR is missing, run `gradle wrapper` or let Android Studio recreate it.
4. Build and run on an emulator or Android 9+ physical device.

## Testing
Run unit tests from Android Studio or via:
```bash
./gradlew test
```

## Configuration
- Word API: `https://random-word-api.herokuapp.com/`
- Clue API: `https://api.datamuse.com/`
- Leaderboard: `https://dreamlo.com/lb/`

Update or mock these endpoints as needed for offline demos.

## License
Educational use for ITE 2152 coursework.
