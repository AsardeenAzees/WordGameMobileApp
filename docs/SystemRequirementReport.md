# System Requirement Report

**Project Title:** Word Guessing Game (Mobile Application)  
**Course:** ITE 2152 - Introduction to Mobile Application Development  
**Assignment:** MAD Word Guessing Game  
**Date:** September 2025

---

## 1. Introduction
The Word Guessing Game is an Android mobile application that challenges players to deduce hidden words supplied by an online API. Each round begins with 100 points and 10 attempts. Players lose points for incorrect guesses and when consuming clues, while fast and accurate guesses yield bonuses. The experience includes onboarding, persistent player identity, multi-level difficulty, timers, and a global leaderboard backed by the Dreamlo service.

The solution is implemented with cost-free tooling and cloud services. Core technologies include Kotlin, Jetpack Compose, Retrofit, and SharedPreferences, ensuring an accessible stack for students and educators.

## 2. Objectives
- Deliver a casual yet competitive word guessing experience optimised for mobile form factors.
- Encourage strategic play via penalty-based scoring and optional clues.
- Measure completion time per word to unlock speed bonuses and leaderboard bragging rights.
- Provide a persistent player profile and integrate with a shared online leaderboard.
- Maintain cross-device compatibility with a responsive Compose UI.

## 3. System Scope
- Target platform: Android smartphones and tablets (Android 9.0 Pie or newer).
- External services: Random Word API, Datamuse for synonyms/rhymes, and Dreamlo for leaderboard storage.
- Local persistence: Player name and best score stored via SharedPreferences.
- Artefacts for submission: source code, signed APK/aab, presentation deck, and demonstration video.

## 4. Functional Requirements
### 4.1 Onboarding
- Prompt the user for a display name on first launch.  
- Persist the name locally and reuse it across sessions.  
- Skip onboarding automatically once the name exists.

### 4.2 Word Guessing Loop
- Fetch a random word from the configured API on every round.  
- Allow up to ten guesses per round.  
- Deduct 10 points for each incorrect guess.  
- Reset to level one and 100 points after failing a round.  
- Increase word difficulty/length after successful guesses.

### 4.3 Clue Mechanics
- Word length clue (-5 points).  
- Letter occurrence clue revealing count for a randomly selected letter (-5 points).  
- Word tip (synonym or rhyme) unlocked after five failed guesses (-5 points).  
- Prevent duplicate use of the same clue type within a round.

### 4.4 Timer & Scoring
- Run a per-round timer in seconds.  
- Award a speed bonus (+15 points by default) for solving a word within 30 seconds.  
- Display current score, attempts remaining, elapsed time, and best score.

### 4.5 Leaderboard
- Submit the current score to Dreamlo with the player's saved name (optional if keys are not configured).  
- Retrieve and display leaderboard entries ordered by score.  
- Handle offline scenarios gracefully by showing cached or empty state messaging.

### 4.6 Settings
- Allow updating the player name.  
- Display current best score.  
- Provide an option to reset local progress (name, best score, onboarding flag).

## 5. Non-Functional Requirements
- **Usability:** Compose UI with clear typography, instructions, and accessible button sizes.
- **Performance:** API requests should respond within three seconds; local fallbacks trigger otherwise.
- **Compatibility:** Works on Android 9+ devices with varying screen sizes using responsive Compose layouts.
- **Reliability:** Graceful degradation when APIs fail, with fallback word lists and error messaging.
- **Security:** Use HTTPS endpoints; no sensitive data retained beyond player nickname and score.
- **Maintainability:** MVVM architecture with repositories, tests for core logic, and clear documentation.

## 6. Tools & Technologies (Free Versions)
- **IDE:** Android Studio (latest stable).  
- **Language & Frameworks:** Kotlin 1.9, Jetpack Compose, AndroidX Navigation.  
- **Networking:** Retrofit + OkHttp logging interceptor.  
- **Local Storage:** SharedPreferences wrapper via `PlayerPreferences`.  
- **Backend Services:** Random Word API, Datamuse API, Dreamlo leaderboard.  
- **Design:** Figma (wireframes), Material 3 design system.  
- **Version Control:** GitHub (private repo).  
- **Presentation:** OBS Studio, PowerPoint / Google Slides for demos.

## 7. System Requirements
### 7.1 Development Workstation
- CPU: Intel Core i5 (4 cores) or equivalent.  
- RAM: Minimum 8 GB (16 GB recommended).  
- Storage: 10 GB free for Android SDKs and project assets.  
- OS: Windows 10/11, macOS 12+, or Ubuntu 22.04+.  
- Tools: Android Studio, Gradle 8.x, JDK 17 (bundled).

### 7.2 Test Devices
- Android device/emulator running Android 9 (API 28) or newer.  
- Minimum 2 GB RAM and 100 MB free storage per device.  
- Reliable Wi-Fi connection for API and leaderboard usage.

## 8. Error Handling & Testing Strategy
- Validate non-empty guesses before processing.  
- Catch network exceptions; revert to local fallback word list when remote services fail.  
- Reset round state robustly after app restarts.  
- Unit tests covering scoring rules, clue penalties, and timer bonus (`GameEngineTest`).  
- Instrumentation roadmap: Compose UI tests and repository integration tests with mocked APIs.  
- Manual QA across emulators (different DPIs) and at least one physical device.

## 9. Deliverables
1. Android Studio project with source code and Gradle files.  
2. Signed APK (or AAB) and zipped source bundle.  
3. Demonstration video showcasing onboarding, gameplay, clues, timer, and leaderboard update.  
4. Presentation slides summarising architecture, features, and test evidence.  
5. This System Requirement Report (PDF or Markdown).

## 10. Future Enhancements
- Add daily challenges and streak tracking.  
- Introduce multi-language word packs.  
- Implement offline leaderboard sync with queued submissions.  
- Provide accessibility improvements (dynamic font scaling, haptic feedback).

---
Prepared by the MAD Word Guessing Game project team, September 2025.
