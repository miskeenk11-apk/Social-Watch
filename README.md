# SOCIAL WATCH — APP STEP 4

STEP 4 hardens the native Android shell without changing the existing SOCIAL WATCH UI/service behavior.

## Added in this step
- Native Android `MainActivity` so the Android project has an actual launcher activity.
- Strict HTTPS allow-list for YouTube, Facebook and TikTok only.
- Non-platform navigation is blocked.
- Android Back intentionally returns to SOCIAL WATCH Home instead of becoming a general browser history.
- JavaScript, DOM storage and media playback support are enabled for the external platforms.
- Instagram remains removed.
- Existing search URLs and platform destinations remain unchanged.
- Old Android target remains `minSdk 21` (Android 5.0).

## Compatibility truth
The app shell itself can target Android 5.0+, but the current YouTube/Facebook/TikTok websites may require newer WebView capabilities. This cannot be solved honestly by HTML alone. A future legacy build can use an older compatible engine where licensing, security and platform requirements permit it.

## Build
Open `android` in Android Studio and build the APK. For Windows, run `npm install` and then `npm run dist:win` from the package root.
