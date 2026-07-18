# AGENTS.md - MarcadorLlamadas

## Project overview
Android dialer replacement app (package `com.edxavier.cerberus_sms`). Compiled against SDK 37, minSdk 24. Single-module Gradle project with Kotlin + Compose. Windows dev environment (use `gradlew.bat`).

## Architecture

### Two entry points (legacy + Compose)
- `ui.MainActivity` (Compose-based) is the real launcher declared in `AndroidManifest.xml`.
- `MainActivity` at root package is old Fragment/XML-based code — do not modify or confuse with the active one.
- The app is a dialer replacement: it requests `ROLE_DIALER` and implements `InCallService` (`CallService.kt`).

### Key directories
| Path | Purpose |
|---|---|
| `app/src/main/java/.../ui/MainActivity.kt` | Launcher activity (Compose) |
| `app/src/main/java/.../ui/screens/` | Compose screens (calls, contacts, dialer, settings, incall) |
| `app/src/main/java/.../services/CallService.kt` | Telecom InCallService |
| `app/src/main/java/.../helpers/` | Extensions, call state manager, event bus |
| `app/src/main/java/.../data/` | Room DB, models, DAOs, repos |
| `app/src/main/java/.../navigation/` | Compose nav routes and bottom bar |

### Mixed UI approach
The codebase has both Compose and legacy View/Fragment code. New UI work goes in `ui/screens/` using Compose. Legacy classes (`ScopeActivity`, `ScopeFragment`, old `CallsFragment`, old `ContactsFragment`) are still present but the launcher path is fully Compose.

### Custom coroutine scope base classes
`ScopeActivity` and `ScopeFragment` live under namespace `com.nicrosoft.consumoelectrico` (copied from another project). They provide `CoroutineScope` (Main dispatcher) to subclasses via `launch {}`.

### State management
A single `UiState` data class (`ui/core/states/UiState.kt`) holds all screen state. `AppViewModel` (in `ui/calls/`) manages it and is shared across all Compose screens via the single-activity architecture.

## Build

### Commands
```bash
# Assemble debug APK
.\gradlew.bat assembleDebug

# Assemble release APK
.\gradlew.bat assembleRelease

# Clean
.\gradlew.bat clean

# Run unit tests (placeholder tests only)
.\gradlew.bat test

# Run connected android tests
.\gradlew.bat connectedAndroidTest
```

### Build quirks
- **Room uses kapt**, not KSP. Do not switch to KSP without also migrating `apply plugin: 'kotlin-kapt'`.
- **google-services plugin must be applied at the bottom** of `app/build.gradle` (line 122: `apply plugin: 'com.google.gms.google-services'`). Adding it in the `plugins {}` block will break the build.
- `gradle.properties` contains billing key and AdMob ad unit IDs. Do not commit changes to these values.
- Keystore: `app/cerberos_keystore.jks`
- Version bump in `app/build.gradle` → `defaultConfig.versionCode` and `versionName`.

## Firebase & AdMob
- Firebase Crashlytics, Analytics, and AdMob are integrated.
- AdMob IDs differ between debug (test ads) and release (production ads), set via `resValue` in build types.
- `google-services.json` at app root.

## Permissions
The app requires: `READ_CALL_LOG`, `READ_PHONE_STATE`, `READ_PHONE_NUMBERS`, `READ_CONTACTS`, `CALL_PHONE`, `MANAGE_OWN_CALLS`. It also requests default dialer role at first launch.

## Testing
Only placeholder tests exist (`ExampleUnitTest`, `ExampleInstrumentedTest`). No meaningful test coverage. When adding Room tests, note the database uses `fallbackToDestructiveMigration()`.
