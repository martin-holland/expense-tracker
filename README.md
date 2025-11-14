# Expense Tracker

Kotlin Multiplatform Compose project targeting Android and iOS.

## Features

- 📊 **Expense History** - View and manage all your expenses
- 🏷️ **Categories** - Food, Travel, Utilities, and Other
- 💱 **Multi-Currency** - Support for 12 major currencies
- 🗄️ **Room Database** - Persistent storage using Room KMP
- 🔄 **Reactive UI** - Real-time updates with Kotlin Flow
- 📱 **Cross-Platform** - Shared codebase for Android & iOS

## Project Layout

- `composeApp/`: shared Kotlin code (UI and logic)
- `composeApp/src/commonMain`: code shared by all targets
- `composeApp/src/androidMain`: Android-specific code (entry activity)
- `composeApp/src/iosMain`: iOS-specific code (root view controller)
- `iosApp/iosApp`: Xcode project (iOS app entry point and SwiftUI wrapper)
- `docs/`: comprehensive documentation

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

## Architecture

The app follows **MVVM** (Model-View-ViewModel) architecture with **Repository Pattern**:

- **View Layer**: Jetpack Compose UI (shared across platforms)
- **ViewModel Layer**: State management and business logic
- **Repository Layer**: Data access abstraction
- **Data Layer**: Room database (KMP) for persistent storage

```
View (Compose) → ViewModel → Repository → Room DAO → Database
```

## Database Implementation

This app uses **Room KMP** for cross-platform persistent storage.

### Key Features:
- ✅ Shared database for Android & iOS
- ✅ Automatic seeding with sample data on first launch
- ✅ Reactive data updates via Kotlin Flow
- ✅ Type-safe queries
- ✅ Repository pattern for clean architecture

### Quick Start (For Developers):

```kotlin
// Get repository instance
val repository = ExpenseRepository.getInstance()

// Observe all expenses (reactive)
viewModelScope.launch {
    repository.getAllExpenses()
        .collect { expenses ->
            // Update UI
        }
}

// Add/Update expense
viewModelScope.launch {
    repository.insertExpense(expense)
}

// Delete expense
viewModelScope.launch {
    repository.deleteExpense(expense)
}
```

**📖 Full Documentation:** See [ROOM_DATABASE_IMPLEMENTATION.md](docs/ROOM_DATABASE_IMPLEMENTATION.md) for complete guide including:
- Architecture details
- Platform-specific setup
- Usage examples
- Migration strategies
- Best practices

## Where to Add Code

- Shared UI/logic: `composeApp/src/commonMain/kotlin`
- Android-only: `composeApp/src/androidMain/kotlin`
- iOS-only: `composeApp/src/iosMain/kotlin` and Swift in `iosApp/iosApp`
- Database: `composeApp/src/commonMain/kotlin/com/example/expensetracker/data/`

## Documentation

- [Room Database Implementation Guide](docs/ROOM_DATABASE_IMPLEMENTATION.md) - Complete database usage guide
- [Room KMP iOS Updates](docs/ROOM_KMP_IOS_UPDATES.md) - Latest iOS best practices & updates
- [Room Advanced Features](docs/ROOM_ADVANCED_FEATURES.md) - Transactions, limitations & future features **NEW!**
- [Implementation Status](docs/ROOM_IMPLEMENTATION_STATUS.md) - Current platform status
- [Expense History Implementation](docs/EXPENSE_HISTORY_IMPLEMENTATION.md) - UI implementation details

## 🚀 Getting Started

**Quick Start:** See [Android Studio Setup Guide](docs/ANDROID_STUDIO_SETUP.md) for step-by-step instructions!

📊 **[Build Status & Verification](BUILD_STATUS.md)** - Latest build results and setup verification

### Run on Android:
1. Open project in Android Studio
2. Select "composeApp" configuration
3. Click Run (▶️)
4. Database auto-seeds with sample data on first launch

### Run on iOS:
- Code is ready and configured
- Waiting on Room KMP stable release (expected Q1 2026)
- See [setup guide](docs/ANDROID_STUDIO_SETUP.md) for details

## Helpful Links

- **[Android Studio Setup Guide](docs/ANDROID_STUDIO_SETUP.md)** - How to run the app ⭐
- Kotlin Multiplatform basics: https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
- Room KMP Documentation: https://developer.android.com/kotlin/multiplatform/room
- Compose Multiplatform: https://www.jetbrains.com/lp/compose-multiplatform/

##
