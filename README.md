# DoseBloom

**A simple, private Android medication tracker.**

I want to start with an honest disclaimer: **I have no professional experience in Android application development.**

DoseBloom was created as a personal project for my own needs. I wanted a straightforward way to keep track of medications and scheduled doses without unnecessary complexity, advertising, subscriptions, or mandatory cloud services.

So I decided to make one myself.

I relied **heavily on ChatGPT throughout the development process** — from the initial project structure and implementation to debugging, UI improvements, and solving Android-specific issues. This is my first serious Android application, so experienced developers will almost certainly find things that could be done better.

I'm publishing the project openly because I thought it might be useful to someone else as well. I'm also hoping that people with more experience can help me improve it.

## Current version

**1.4.3** (`versionCode 11`)

`applicationId`: `com.dosebloom.app`

Minimum Android version: **API 26 (Android 8.0)**  
Target SDK: **35**  
Compile SDK: **35**

## What is DoseBloom?

DoseBloom is a lightweight Android application for tracking medications and scheduled doses locally on your device.

The main idea is simple: create medication schedules, record doses, and use reminders to keep track of what has been taken.

### Features

- Create and manage medication schedules
- Track today's doses and review medication history with a calendar
- Record taken and skipped doses
- Medication courses with start and end dates
- As-needed medications
- Medication stock tracking and low-stock warnings
- Multiple user profiles
- Notifications and scheduled reminders
- Notification actions: **Taken**, **+10 min**, and **Skip**
- Automatic schedule restoration after device reboot and time/time-zone changes
- Home-screen widget showing the next scheduled dose for the active profile
- JSON data export and import, including actual intake history
- Light and dark theme with adapted semantic status and calendar colors
- Scrollable medication add/edit form for small screens
- Russian and English language support
- In-app language selection with a system-default option
- No mandatory account or cloud service
- No advertising

DoseBloom is a personal tracking tool and **is not a substitute for medical advice, diagnosis, or professional healthcare**. Always follow the instructions provided by your doctor or medication packaging.

## Export and import

DoseBloom can export application data to JSON and restore it through the Android system file picker.

The export format includes:

- user profiles;
- medications;
- dosage and units;
- schedules;
- course dates;
- notes;
- stock and low-stock threshold;
- as-needed status;
- actual intake history, including date, planned time, actual time, and status.

Older JSON files without the `intakes` field remain compatible; they simply do not contain intake history to restore.

## Reminders and exact alarms

DoseBloom uses Android `AlarmManager` for scheduled reminders. It attempts to use exact alarms when the required system access is available and falls back to a non-exact alarm when it is not.

On Android 12 and newer, exact alarms require the system's **Alarms & reminders** access. Depending on the Android version and installation state, this access may need to be enabled manually for the most precise reminders.

The application recreates its schedule after device reboot and after time or time-zone changes.

## Widget

DoseBloom includes a home-screen widget that displays the next scheduled dose.

The widget searches for the nearest scheduled dose within the next seven days and uses the profile currently selected in the application.

## Technology

- Kotlin 2.0.21
- Jetpack Compose
- Material 3
- AndroidX
- SQLite via `SQLiteOpenHelper`
- Android `AlarmManager` and `BroadcastReceiver`
- Android App Widgets
- JSON export/import
- Java 17
- Android Gradle Plugin 8.7.3
- Gradle 8.9

## Building

Open the project in Android Studio and allow Gradle to synchronize the project.

For a release build, use the Gradle `assembleRelease` task or the corresponding Android Studio build option. The repository also contains a GitHub Actions workflow that builds and verifies a release APK automatically on repository changes.

The production signing key is intentionally not included in this repository. If you need to update an existing installation that was signed with the original release key, you must use the same signing key when creating the release.

The private repository uses the production signing configuration through protected GitHub Actions secrets. This public repository does not contain the release keystore or signing passwords.

## Privacy and data

DoseBloom is designed around local data storage. The application does not require an account or mandatory cloud synchronization.

Exported JSON files may contain sensitive medication information. Store exported files securely and do not share them publicly unless you are comfortable disclosing their contents.

## Known tasks for the current version

1. Update the Android/Compose/Kotlin/Gradle stack as a separate technical change after verifying compatibility.
2. Add automated tests for the database, import/export, scheduling, and notification handling.
3. Continue improving the application based on testing and feedback.

## Versioning rule

For every code change in the project:

1. increment `versionName` and `versionCode`;
2. update the README in the private repository;
3. synchronize the code changes with `dosebloom-public`;
4. update the public README to the same version;
5. check the GitHub Actions release build.

## Why does it exist?

I wanted a practical medication tracker that focused on the things I actually needed instead of trying to become a large healthcare platform.

DoseBloom started as a personal project and gradually grew into a complete Android application.

## Community feedback and contributions

If you use DoseBloom, **feedback, bug reports, feature requests, and contributions are welcome**.

- [Open a new issue](https://github.com/poorangryman/dosebloom-public/issues/new)

For bug reports, please include your Android version, device model, DoseBloom version, steps to reproduce the problem, and screenshots or logs when possible.

If you are an Android developer, or have experience with Kotlin/Java, testing, security, architecture, notifications, or UI/UX, constructive suggestions and pull requests are especially welcome.

DoseBloom handles potentially sensitive medication information. Please avoid posting personal medical information, prescriptions, or other private data in public issues.

## A note about the code

This project is my first serious attempt at creating an Android application, and I'm learning as I go.

**ChatGPT was heavily involved in the development of DoseBloom.** The application would not have reached its current state without it.

I'm publishing the source code openly because I believe that sharing a real, imperfect project can be more useful than pretending it was written by an experienced developer from the beginning.

If you're an experienced Android developer and notice something that could be significantly improved, constructive feedback is welcome.

## License

See [LICENSE](LICENSE).
