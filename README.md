# AI Keyboard (personal use)

Stage 1: basic English Android keyboard (InputMethodService + Jetpack Compose).

- No permissions, no internet, no analytics, no ads.
- Nothing typed is stored or sent anywhere.

## Build
- GitHub: Actions tab -> "Build debug APK" -> Run workflow -> download `ai-keyboard-debug-apk`.
- Android Studio: open this folder, let it sync, Build > Build APK(s).
  Output: `app/build/outputs/apk/debug/app-debug.apk`

## Install
1. Copy the APK to the phone and open it (allow "install unknown apps").
2. Open AI Keyboard -> "Open keyboard settings" -> enable AI Keyboard.
3. Tap "Choose AI Keyboard".
