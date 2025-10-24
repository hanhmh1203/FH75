# FH75Demo Android Project - Build Configuration

## Overview
This Android project has been successfully configured and modernized to work with current Android development tools.

## Changes Made

### 1. Updated Gradle Configuration
- **Root build.gradle**: Updated Android Gradle Plugin from 2.1.3 to 8.1.4
- **App build.gradle**: Updated compile SDK to 34, target SDK to 34, minimum SDK to 21
- **Repositories**: Added Google repository and Maven Central
- **Dependencies**: Migrated from deprecated `compile` to `implementation`
- **AndroidX**: Enabled AndroidX support

### 2. AndroidX Migration
- Migrated all `android.support.v4` imports to `androidx.fragment.app`
- Updated dependency from support-v4 to androidx.appcompat and androidx.legacy
- All Java files have been updated to use AndroidX classes

### 3. Android Manifest Updates
- Removed deprecated `package` attribute (now defined in build.gradle)
- Updated minSdkVersion from 8 to 21, targetSdkVersion from 17 to 34
- Added required `android:exported` attributes for activities (Android 12+ requirement)

### 4. Build Environment
- Updated Gradle wrapper to use Gradle 8.5
- Created `gradle.properties` with recommended settings
- Created `proguard-rules.pro` with appropriate rules for the project
- Made gradlew executable

## Project Structure
```
FH75Demo/
├── app/
│   ├── build.gradle (modernized)
│   ├── proguard-rules.pro (created)
│   ├── src/main/
│   │   ├── AndroidManifest.xml (updated)
│   │   ├── java/ (AndroidX migrated)
│   │   └── res/
│   └── libs/
│       └── org.apache.http.legacy.jar
├── build.gradle (modernized)
├── gradle.properties (created)
├── gradle/wrapper/
└── gradlew (made executable)
```

## Build Commands

### Clean the project:
```bash
./gradlew clean
```

### Build debug APK:
```bash
./gradlew assembleDebug
```

### Build release APK:
```bash
./gradlew assembleRelease
```

### Install debug APK to connected device:
```bash
./gradlew installDebug
```

## Output Location
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

## Requirements
- Android SDK 34 (Android 14)
- Java 8 or higher
- Gradle 8.5

## Notes
- The project targets modern Android versions (API 21+) for better security and features
- All deprecated APIs have been updated to current standards
- The app maintains compatibility with the original HTTP legacy library
- Build warnings about deprecated Gradle features are expected and don't affect functionality

## Bluetooth Permissions
The app includes Bluetooth permissions for handheld reader functionality:
- `BLUETOOTH`
- `BLUETOOTH_ADMIN`

## Build Status
✅ Project successfully builds and generates APK
✅ All AndroidX migrations completed
✅ Modern Android SDK compliance achieved