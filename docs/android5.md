# Build launcher for Android 5

This guide is for the opt-in `codex/android5` branch. The normal launcher build still targets Android 8.0 (API 26) and newer. Use `-PlegacyAndroid=true` to build the Android 5 variant (minimum API 21); Android 5.0 is API 21 and Android 5.1 is API 22.

The compatibility build was installed and checked on an IV3012A7 running Android 5.1.1. The checks covered launching the app, opening it with the physical Home button, and starting the box again after a reboot. Other Android 5 devices and firmware are not yet verified.

## Requirements

- JDK 17
- Android SDK Platform 34 and Build Tools 34.0.0
- ADB for installing and checking a device

On Windows PowerShell, set the Java and SDK paths if they are not already configured:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:ANDROID_HOME = 'C:\Android-SDK'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
```

Use the equivalent JDK and SDK paths on macOS or Linux.

## Build and test

From the repository's `launcher` directory, run:

```powershell
.\gradlew.bat --no-daemon -PlegacyAndroid=true testReleaseUnitTest lintRelease assembleRelease
```

On macOS or Linux, use `./gradlew` in place of `gradlew.bat`.

The APK is written to `app/build/outputs/apk/release/app-release.apk` (from the repository root, that is `launcher/app/build/outputs/apk/release/app-release.apk`). The legacy property lowers `minSdk` only for that build and enables Java library desugaring for APIs used by the launcher. Without the property, the regular build remains at `minSdk 26`.

## Install and verify

Check the device's Android API before installing; API 21 or 22 is expected for Android 5:

```sh
adb shell getprop ro.build.version.sdk
```

Install and start the launcher:

```sh
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
```

If Android shows a Home-app chooser, select the new launcher and choose **Always**. Test navigation and press the physical Home button from another app. Reboot once and confirm that the new launcher opens at startup and ADB reconnects before changing the stock launcher state.

On the IV3012A7 used for this branch, the original launcher (`com.example.binhbt.panodicdemo`) started a competing screen at boot. It was disabled only after the new launcher was installed, selected as Home, and verified. If the same package is present on that device and you need to reverse that change, use:

```sh
adb shell pm enable --user 0 com.example.binhbt.panodicdemo
```

Do not copy that package name to another TV. Identify its stock launcher and back up its current state first. Never disable Android core, TV input, tuner, remote, or settings packages as a way to fix Home.

## Device-specific settings and limits

This branch's `config.xml` contains settings for the IV3012A7: it hides the Inputs button, redirects the stock launcher's Home screen, and maps remote scancode 102 because this firmware reports the Home button as the vendor key `VEGA_HOME`. Those values are not suitable defaults for other TVs.

This is a local compatibility branch. It lowers the minimum API only when opted in, but the built APK is currently 719,269 bytes, above the repository's 480 KiB size limit. The repository also does not accept lowering `minSdk` in its regular launcher line, so this branch is not ready for an upstream pull request.

## Commit changes

Review the working tree and staged diff, then commit the files you changed:

```sh
git status --short
git add launcher/app docs/android5.md README.vi.md
git diff --cached --check
git commit -m "docs: add Android 5 build guide"
```

Use a `Launcher:` commit title when the commit contains launcher code rather than documentation alone.
