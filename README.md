# DoseBloom

**A simple, private Android medication tracker.**

I want to start with an honest disclaimer: **I have no professional experience in Android application development.**

DoseBloom was created as a personal project for my own needs. I wanted a straightforward way to keep track of medications and scheduled doses without unnecessary complexity, advertising, subscriptions, or mandatory cloud services.

So I decided to make one myself.

I relied **heavily on ChatGPT throughout the development process** — from the initial project structure and implementation to debugging, UI improvements, and solving Android-specific issues. This is my first serious Android application, so experienced developers will almost certainly find things that could be done better.

I'm publishing the project openly because I thought it might be useful to someone else as well. I'm also hoping that people with more experience can help me improve it.

## What is DoseBloom?

DoseBloom is a lightweight Android application for tracking medications and scheduled doses locally on your device.

The main idea is simple: create medication schedules, record doses, and use reminders to keep track of what has been taken.

### Features

- Create and manage medication profiles and schedules
- Track today's doses and review medication history
- Notifications and scheduled reminders for upcoming doses
- Home-screen widget for quick access to medication information
- JSON data export and import
- Multiple user profiles
- Local data storage
- Dark theme
- Settings for notification and exact-alarm permissions
- **Russian and English language support**
- In-app language selection with a system-default option
- No mandatory account or cloud service
- No advertising

DoseBloom is a personal tracking tool and **is not a substitute for medical advice, diagnosis, or professional healthcare**. Always follow the instructions provided by your doctor or medication packaging.

## Why does it exist?

I wanted a practical medication tracker that focused on the things I actually needed instead of trying to become a large healthcare platform.

DoseBloom started as a personal project and gradually grew into a complete Android application.

## Community feedback and contributions

If you use DoseBloom, **feedback, bug reports, feature requests, and contributions are welcome**.

- [Report a bug or discuss DoseBloom](https://github.com/poorangryman/dosebloom-public/issues/1)
- [Open a new issue](https://github.com/poorangryman/dosebloom-public/issues/new)

For bug reports, please include your Android version, device model, DoseBloom version, steps to reproduce the problem, and screenshots or logs when possible.

If you are an Android developer, or have experience with Kotlin/Java, testing, security, architecture, notifications, or UI/UX, constructive suggestions and pull requests are especially welcome.

DoseBloom handles potentially sensitive medication information. Please avoid posting personal medical information, prescriptions, or other private data in public issues.

## A note about the code

This project is my first serious attempt at creating an Android application, and I'm learning as I go.

**ChatGPT was heavily involved in the development of DoseBloom.** The application would not have reached its current state without it.

I'm publishing the source code openly because I believe that sharing a real, imperfect project can be more useful than pretending it was written by an experienced developer from the beginning.

If you're an experienced Android developer and notice something that could be significantly improved, constructive feedback is welcome.

**I'm also very open to suggestions and contributions from the community.** If you have ideas for new features, improvements, bug fixes, UI/UX changes, or anything else that could make DoseBloom better, feel free to share them.

If you have experience with Android development, Kotlin/Java, UI/UX, testing, security, architecture, notifications, or any other area relevant to the project, **any help, advice, constructive criticism, or contribution would be greatly appreciated.** I'm still learning, so there is definitely a lot I can improve.

## Building

Open the project in Android Studio and allow Gradle to synchronize the project.

For a release build, use the Gradle `assembleRelease` task or the corresponding Android Studio build option. The repository also contains a GitHub Actions workflow that can build a release APK automatically.

The production signing key is intentionally not included in this repository. If you need to update an existing installation that was signed with the original release key, you must use the same signing key when creating the release.

## Privacy and data

DoseBloom is designed around local data storage. The application does not require an account or mandatory cloud synchronization.

Exported JSON files may contain sensitive medication information. Store exported files securely and do not share them publicly unless you are comfortable disclosing their contents.

## License

See [LICENSE](LICENSE).

## A note about this README

**I also used ChatGPT to write and polish this README.** My English isn't good enough to express all of this clearly and naturally on my own, so I relied on ChatGPT to help translate and formulate my thoughts. The ideas and information about the project are mine; ChatGPT helped me put them into proper English.
