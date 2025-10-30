Kotlin Multiplatform Compose project targeting Android and iOS.

Project layout

- `composeApp/`: shared Kotlin code (UI and logic)
- `composeApp/src/commonMain`: code shared by all targets
- `composeApp/src/androidMain`: Android-specific code (entry activity)
- `composeApp/src/iosMain`: iOS-specific code (root view controller)
- `iosApp/iosApp`: Xcode project (iOS app entry point and SwiftUI wrapper)

Prerequisites

- Android: Android Studio Koala+ with Android SDKs, a device or emulator
- iOS: Xcode 15+ with a simulator or a provisioned device
- Java: JDK 17 installed and selected in your IDE

Run the Android app

1. Open the project in Android Studio
2. Let Gradle sync finish
3. Choose an Android device/emulator
4. Select the `composeApp` Android run configuration and click Run

Terminal build (optional)

```bash
./gradlew :composeApp:assembleDebug
```

The APK will be under `composeApp/build/outputs/apk/debug/`.

Run the iOS app
Option A (Xcode)

1. Open `iosApp/iosApp/iosApp.xcodeproj` in Xcode
2. In the navigator, select the `iosApp` project → select the `iosApp` target
3. Go to “Signing & Capabilities”
4. Check “Automatically manage signing”
5. Select your Apple ID under “Team”
6. Ensure the “Bundle Identifier” is unique (e.g., `com.yourdomain.expensetracker`)
7. Select a Simulator (e.g., iPhone 15) and press Run (⌘R)

Option B (Android Studio)

1. Open the project in Android Studio (KMP supported)
2. Run → Edit Configurations… → select the iOS “Xcode Application” config
3. In Options, set “Development team” to your Apple ID team
4. Choose a Simulator device and Run

If you still see signing errors

- Add your Apple ID in Xcode: Xcode → Settings… → Accounts → “+” → Apple ID
- Clean build: Product → Clean Build Folder (Shift+Cmd+K), then run again
- If needed, delete Derived Data: Xcode → Settings… → Locations → Derived Data → delete

Notes

- On first iOS build, Gradle compiles the Kotlin framework consumed by Xcode
- If code-signing is required for device, set your team in Xcode project settings

Where to add code

- Shared UI/logic: `composeApp/src/commonMain/kotlin`
- Android-only: `composeApp/src/androidMain/kotlin`
- iOS-only: `composeApp/src/iosMain/kotlin` and Swift in `iosApp/iosApp`

Helpful links

- Kotlin Multiplatform basics: https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html

###
