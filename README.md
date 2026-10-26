# ReelCounter 📱
> Minimalist, privacy-first Instagram Reels counter for Android built with Kotlin, Jetpack Compose, AccessibilityService, and Room.

## Features
- **Zero Internet / Privacy-First**: Does not have `android.permission.INTERNET`. Zero network calls, zero telemetry, zero analytics. Never reads or stores usernames, captions, comments, audio names, or messages.
- **Dedicated AccessibilityService**: Scopes strictly to `com.instagram.android`. Employs a 700ms debouncing window across `TYPE_VIEW_SCROLLED` and `TYPE_WINDOW_CONTENT_CHANGED` to ensure each swipe registers once.
- **Isolated ReelDetector Class**: Detection heuristics isolated in one file (`ReelDetector.kt`) with explicit instructions on updating resource IDs and scroll thresholds when Instagram updates their UI.
- **Draggable Floating Overlay**: Compact bubble (`TYPE_APPLICATION_OVERLAY`) displaying real-time counter over Instagram. Automatically hides when Instagram moves to the background and remembers coordinates.
- **Room Local Persistence**: Daily count automatically resets at midnight based on local ISO date (`YYYY-MM-DD`).
- **Clean MVVM + Hilt**: Modern Android architecture with unidirectional data flow via Kotlin Coroutines and `StateFlow`.

## Build and Install
```bash
# Build Debug APK
./gradlew assembleDebug

# Install via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch App
adb shell am start -n com.reelcounter.app/.ui.MainActivity
```
## Build without Android Studio (GitHub Actions)
Push this folder to a GitHub repo. The workflow in `.github/workflows/build.yml` builds the APK.
Download it from Actions > latest run > Artifacts > ReelCounter-debug-apk.
(No gradlew is included; the workflow installs Gradle itself. Android Studio will regenerate the wrapper if you open the project.)
