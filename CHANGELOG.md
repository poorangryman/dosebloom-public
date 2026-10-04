# Changelog

## DoseBloom v2.0.1

- Data Integrity: fixed PRN ("Take now") intake loss during JSON import by supporting repeated intake suffixes (`HH:mm (2)`) in `Schedule.validTime`
- Reliability: added `USE_EXACT_ALARM` permission so Android 13+ devices receive exact medication reminders without requiring manual system settings toggling
- Battery & Background: added battery optimization check and direct settings navigation to prevent OEM power managers (MIUI/OneUI/EMUI) from killing background reminder alarms
- In-App Updater: improved lifecycle and UX with `DownloadState` tracking, download verification, cached APK handling, and direct install action
- Architecture: modularized monolithic `DoseBloomScreen.kt` into dedicated composables (`TodayScreen`, `HistoryScreen`, `MedicinesScreen`, `MedicineEditor`, `ProfileDialog`, `SettingsDialog`, `Components`)
- Testing: added unit test coverage for PRN timestamp suffix formats and v2.0.1 version comparisons

## DoseBloom v2.0.0

- Architecture: eliminated `stateIn` subscription churn in ViewModel by exposing cold intake flows
- Architecture: unified UI state with `DoseBloomUiState`
- Performance: marked `Medicine` and `Intake` domain models as `@Immutable` for optimal Compose skipping
- Reliability: added `goAsync()` to `BootReceiver` and `NextDoseWidget` to prevent Android process termination during background execution
- Concurrency: synchronized `Scheduler` cancel and reschedule operations via `Mutex` to prevent alarm state collisions
- Data: added `fallbackToDestructiveMigrationFrom(1)` to safeguard upgrades from legacy v1 database schemas
- Data: resolved medication name collisions when migrating medicines upon profile deletion
- Accessibility: replaced undersized touch targets with standard 48x48dp interactive controls
- Accessibility: added TalkBack `contentDescription` for calendar month switchers, search clearing, and profile deletion
- UX: added numeric keyboard types for supply and threshold input fields
- Testing: added comprehensive unit tests for `UpdateManager`, `Schedule`, and `DoseBloomUiState`

## DoseBloom v1.5.0

- Permanent RSA 4096-bit release signing keystore configuration
- In-App update checker in Settings dialog with changelog display and one-tap installation via DownloadManager and FileProvider
- Added `INTERNET` and `REQUEST_INSTALL_PACKAGES` permissions with `androidx.core.content.FileProvider`

## DoseBloom v1.4.9

- Modern botanical UI with pure white cards and soft 20dp corners
- Added daily adherence progress card to the Today screen
- Added quick stock replenishment (+10, +30, +50) directly from medication cards
- Added search and filter chips (All, Scheduled, As needed, Low stock) in the medicines list

## DoseBloom v1.4.8

- Added ability to undo an accidentally recorded dose (restores medicine stock)
- Added "Take now" for as-needed medication intake
- Clicking the home-screen widget now opens the application
- Critical reliability fixes for scheduling and alarms

## DoseBloom v1.4.7

- Fixed status bar and camera notch collisions on modern Android versions
- Added keyboard padding (`imePadding`) so input fields are not hidden by the keyboard
- Restored complete UI localization

## DoseBloom v1.4.6

- Complete responsive architecture for window sizes and system insets
- Adaptive navigation that switches to a rail on tablets and large screens
- Constrained main content width for better readability on large displays

## DoseBloom v1.4.5

- Replaced legacy edge-to-edge layout with standard Android approaches
- Restored the monthly history calendar with intake markers

## DoseBloom v1.4.3

- Added intake history to JSON data export
- Widget now properly shows the next dose for the currently active profile

## DoseBloom v1.4.0

- Added English language support.
- Added in-app language selection: System default, Russian, and English.
- Localized medication reminders and notification actions.
- Localized home-screen widget text.
- Improved locale-aware date and month formatting.
- Localized medication/profile/settings UI and validation messages.
- Preserved user-entered medication and profile data without automatic translation.
