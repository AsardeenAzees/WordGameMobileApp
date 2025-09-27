# Word Guessing Game Mobile App Architecture

## Overview
The Word Guessing Game is an Android application written in Kotlin and powered by Jetpack Compose. It implements a MVVM (Model–View–ViewModel) architecture with a unidirectional data flow, allowing clear separation between UI, domain logic, and data sources. Navigation between onboarding, gameplay, stats, and leaderboard views is handled via Jetpack Navigation for Compose.

## Modules
- `app`: Android application module that bundles Compose UI, view models, and dependency wiring.
- `data`: Kotlin package inside `app` containing API clients, repositories, and local storage helpers.
- `domain`: Kotlin package for core game models, business rules, and use-cases.
- `ui`: Kotlin package for Compose screens, components, and navigation graph.

## Key Dependencies
- Jetpack Compose Material3 for UI components and theming.
- Kotlin Coroutines & Flow for asynchronous operations and reactive state updates.
- Retrofit & OkHttp for HTTP communication with the word provider API, rhyming/tip API, and Dreamlo leaderboard service.
- AndroidX DataStore and SharedPreferences interoperability for persisting player profiles, local high scores, and configuration.
- Koin (lightweight DI) to wire repositories, use cases, and view models.

## Data Flow
1. **Remote Data Sources**
   - `RandomWordService` fetches new target words and hints from a public API.
   - `ClueService` provides synonyms and rhymes for clue requests.
   - `LeaderboardService` sends and retrieves scores from Dreamlo.
2. **Local Data Source**
   - `PlayerPreferences` wraps SharedPreferences for storing player name, local best score, and onboarding completion flag.
3. **Repositories**
   - `WordRepository` coordinates word retrieval, fallback logic, and caching of the current round.
   - `LeaderboardRepository` handles Dreamlo score submissions and retrieval with failure resilience.
4. **Use Cases**
   - `StartGameUseCase`, `SubmitGuessUseCase`, `RequestClueUseCase`, `AdvanceLevelUseCase`, `PersistScoreUseCase`, etc., encapsulate discrete domain operations.
5. **View Models**
   - `OnboardingViewModel` manages player registration.
  - `GameViewModel` orchestrates gameplay state, timers, scoring, and clue consumption.
   - `LeaderboardViewModel` exposes UI state for remote leaderboard.
6. **UI Layer**
   - Screens collect state from view models via Compose state flows and render the corresponding UI. User actions trigger view model intents, which in turn invoke domain use cases.

## Navigation Flow
1. **Onboarding Screen** collects player name on first launch.
2. **Game Screen** hosts current level, guesses, timer, score, and clue actions.
3. **Result Bottom Sheet** appears after win/loss, offering replay and leaderboard submission.
4. **Leaderboard Screen** displays global standings and the player’s rank.
5. **Settings/About Screen** allows players to review instructions and reset progress.

## Error Handling Strategy
- Each repository surfaces domain-friendly error types (e.g., `NetworkUnavailable`, `ServiceRateLimited`).
- View models translate errors into UI messages and fallback behaviors (e.g., offline word list, local-only leaderboard).
- A retry policy is applied on API calls with exponential backoff up to three attempts before falling back to local defaults.

## Testing Plan
- Unit tests for use cases and view models covering scoring rules, clue penalties, timer handling, and fallback logic.
- Instrumented tests for SharedPreferences persistence and navigation flows (using Compose testing).
- Integration tests for API layer with mocked Retrofit responses.

## Deliverables Mapping
- **Source Code**: `app` module.
- **Documentation**: `docs/` (architecture, requirements, API references).
- **Testing**: `app/src/test/` and `app/src/androidTest/` suites.
- **Packaging**: Gradle tasks for assembling debug/release APK; CI-ready configuration stub in `.github/workflows/`.

