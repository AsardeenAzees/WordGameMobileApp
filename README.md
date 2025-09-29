# Word Guessing Game (Android) - Detailed Setup Guide

An Android mobile application for the MAD Word Guessing Game assignment (ITE 2152). Players guess randomly generated words within ten attempts while maintaining a score that penalises incorrect guesses and clue usage. The app supports onboarding with persistent player names, time tracking, multi-level progression, and an optional Dreamlo-powered global leaderboard.

## Features

- **Game Mechanics**: Guess words with limited attempts, score tracking, and penalty system
- **Progressive Difficulty**: Level-based challenges with increasing word complexity
- **Clue System**: Request hints at the cost of points (word length, letter occurrences, tips)
- **Clue Stacking**: All clues are displayed together in a list/stack
- **Level-Based Word Reveal**: 
  - Level 1: Reveal 2 random letters
  - Level 2: Reveal 1 random letter
  - Level 3+: Reveal 0 letters (all underscores)
- **Leaderboard**: Global ranking system powered by Dreamlo
- **Modern UI**: Jetpack Compose with Material 3 styling and animations
- **Persistent Profiles**: Player names and best scores saved locally

## Highlights
- Kotlin + Jetpack Compose UI with Material 3 styling.
- Word retrieval from Random Word API with Datamuse synonyms/rhymes for tips.
- SharedPreferences-backed onboarding and profile persistence.
- Scoring, timer, and clue mechanics implemented in a reusable domain engine.
- Leaderboard integration via Dreamlo (configurable BuildConfig keys).
- Compose Navigation for onboarding, game, leaderboard, and settings flows.
- Unit tests for critical game engine logic.

## Prerequisites

### For Android Studio:
- Android Studio Giraffe or newer
- Android SDK API Level 34 (Android 14)
- JDK 17 or newer
- Minimum Android 9 (API Level 28) for target devices

### For Local Development:
- Android SDK Command-line Tools
- JDK 17 or newer
- ADB (Android Debug Bridge) properly configured
- USB debugging enabled on Android device
- USB driver for your Android device (if needed)

## Project Structure
```
WordGameMobileApp/
+-- app/                     # Android application module
   +-- src/main/java/com/example/wordgame/
      +-- data/            # Local/remote data sources and repositories
      +-- domain/          # Game models and GameEngine
      +-- presentation/    # ViewModels per feature
      +-- ui/              # Compose UI and navigation
   +-- src/test/            # Unit tests
+-- docs/                    # Project documentation
+-- build.gradle.kts         # Root Gradle configuration
+-- settings.gradle.kts
```

## Getting Started

### Method 1: Using Android Studio (Recommended)

1. **Download the Project**
   - Download the ZIP file from the repository
   - Extract the ZIP file to your desired location

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the extracted folder and select it

3. **Configure Build Settings** (Optional)
   - Open `app/build.gradle.kts`
   - Replace `BuildConfig.DREAMLO_PUBLIC_CODE` and `BuildConfig.DREAMLO_PRIVATE_CODE` with your Dreamlo leaderboard codes if you want to use a custom leaderboard

4. **Sync Project**
   - Android Studio will automatically prompt to sync the project
   - If not prompted, click "Sync Now" when you see the notification
   - Wait for Gradle sync to complete

5. **Build and Run**
   - Connect an Android device via USB OR start an emulator
   - Ensure USB debugging is enabled on your device
   - Select your target device from the device dropdown
   - Click the "Run" button (green play icon) or press `Shift + F10`

### Running with Android Studio Emulator

1. **Create an Emulator** (if you don't have one already)
   - In Android Studio, go to `Tools` > `Device Manager`
   - Click the `Create device` button
   - Select a hardware profile (e.g., Pixel 4 or similar)
   - Select a system image (recommend API Level 30 or higher with Google APIs)
   - Click `Next` and then `Finish` to create the emulator

2. **Start the Emulator**
   - In Device Manager, click the play button next to your emulator
   - Wait for the emulator to fully boot up

3. **Run the App**
   - Select your emulator from the device dropdown in the toolbar
   - Click the "Run" button (green play icon) or press `Shift + F10`
   - The app will be installed and launched on the emulator

### Method 2: Using Command Line (Local Development)

1. **Download the Project**
   - Download the ZIP file from the repository
   - Extract the ZIP file to your desired location

2. **Navigate to Project Directory**
   ```bash
   cd path/to/WordGameMobileApp
   ```

3. **Connect Android Device**
   - Connect your Android device via USB
   - Enable USB debugging in Developer Options
   - Verify connection:
     ```bash
     adb devices
     ```
   - You should see your device listed

4. **Build the Project**
   ```bash
   # On Windows
   .\gradlew assembleDebug
   
   # On macOS/Linux
   ./gradlew assembleDebug
   ```

5. **Install on Device**
   ```bash
   # On Windows
   .\gradlew installDebug
   
   # On macOS/Linux
   ./gradlew installDebug
   
   # Or manually install the APK
   adb install app\build\outputs\apk\debug\app-debug.apk
   ```

6. **Launch the App**
   ```bash
   adb shell am start -n com.example.wordgame.debug/com.example.wordgame.MainActivity
   ```

## Testing
Run unit tests from Android Studio or via:
```bash
# On Windows
.\gradlew test

# On macOS/Linux
./gradlew test
```

## Configuration
- Word API: `https://random-word-api.herokuapp.com/`
- Clue API: `https://api.datamuse.com/`
- Leaderboard: `https://dreamlo.com/lb/`

Update or mock these endpoints as needed for offline demos.

## Troubleshooting

### Common Issues:

1. **Gradle Sync Issues**
   - Ensure you have JDK 17 installed
   - Try "File" > "Sync Project with Gradle Files"
   - Delete `.gradle` folder and re-sync

2. **Device Not Detected**
   - Check USB cable connection
   - Ensure USB debugging is enabled
   - Install appropriate USB drivers for your device
   - Try different USB ports

3. **App Crashes on Launch**
   - Check logcat for error messages
   - Ensure all dependencies are properly downloaded
   - Clean and rebuild the project

4. **Network Issues**
   - Verify internet connection
   - Check if APIs are accessible
   - Ensure proper permissions in AndroidManifest

### Useful ADB Commands:
```bash
# Check connected devices
adb devices

# View logcat
adb logcat

# View only errors
adb logcat *:E

# Uninstall app
adb uninstall com.example.wordgame.debug

# Clear app data
adb shell pm clear com.example.wordgame.debug
```

## License
Educational use for ITE 2152 coursework.