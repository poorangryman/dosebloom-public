# Responsive and system-inset layer

DoseBloom uses a window-driven Compose layout rather than device-specific dimensions.

- `enableEdgeToEdge()` is enabled at the Activity boundary.
- Material 3 `TopAppBar`, `Scaffold`, and adaptive navigation own system-bar spacing.
- `ScaffoldDefaults.contentWindowInsets` prevents screen content from occupying unsafe system regions.
- `imePadding()` keeps interactive content accessible when the IME is visible.
- `NavigationSuiteScaffold` adapts primary navigation to the current window size.
- Main content is capped at 720dp on expanded windows instead of stretching phone layouts indefinitely.
- No status-bar or cutout heights are hard-coded.

Android's `WindowInsets.safeDrawing`/`displayCutout` model is the source of truth for unsafe display regions. Future custom edge-to-edge surfaces must use insets rather than fixed offsets.

This layer intentionally does not change repositories, Room, scheduling, notification, import/export, or widget data behavior.
