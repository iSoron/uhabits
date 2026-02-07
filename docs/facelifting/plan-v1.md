```
model: Gemini 3
```
____________________________________________________________________________

# Material 3 Migration Plan (v1)

This document outlines the step-by-step iterative migration of Loop Habit Tracker from Material 2 (M2) to Material 3 (M3).

## Goals
- Modernize the app's look and feel following Material 3 guidelines.
- Support Dynamic Color (Material You) on Android 12+.
- Maintain existing functionality and custom habit color features.
- Iterative approach: Component-by-component migration with visual and automated testing.

---

## Phase 1: Infrastructure & Foundation

### 1.1 Dependency Audit
- **Task**: Ensure `com.google.android.material:material` is at least version `1.11.0` or higher.
- **Verification**: Check `gradle/libs.versions.toml`. (Current: `1.12.0` - OK).

### 1.2 M3 Color System Definition
- **Task**: Create `res/values/m3_colors.xml` (and `-night`).
- **Details**: Define core M3 color roles (Primary, Secondary, Tertiary, Surface, Error, etc.). Use the [Material Theme Builder](https://m3.material.io/theme-builder) to generate a baseline.
- **Verification**: Colors are accessible in layouts via `?attr/colorPrimary`, etc.

### 1.3 M3 Theme Setup (Side-by-Side)
- **Task**: Create a new base theme `Theme.App.M3` inheriting from `Theme.Material3.DayNight.NoActionBar`.
- **Verification**: Temporarily switch one Activity to use this theme and ensure it doesn't crash.

---

## Phase 2: Global Theme Transition

### 2.1 Base Theme Switch
- **Task**: Update `AppBaseTheme` in `styles.xml` to inherit from `Theme.Material3.DayNight.NoActionBar`.
- **Warning**: This will immediately change the appearance of many components (e.g., larger buttons, different font sizes).
- **Verification**: Run the app and identify immediate visual regressions (e.g., text contrast issues).

---

## Phase 3: Component-by-Component Migration (Iterative)

Each iteration includes:
1. Update styles/layouts for the component.
2. Manual visual verification (Light/Dark mode).
3. Run existing UI tests (Espresso) to ensure no functional regressions.

### Iteration 1: Surfaces & Top App Bar
- **Components**: `Toolbar`, Activity backgrounds.
- **Changes**: 
    - Use `MaterialToolbar` with M3 styles.
    - Set window backgrounds to `?attr/colorSurface`.
    - Update status bar and navigation bar colors.

### Iteration 2: Buttons & Chips
- **Components**: `MaterialButton`, `Chip`, Floating Action Button (FAB).
- **Changes**: 
    - Migrate to M3 Button styles (Tonal, Outlined, Text).
    - Update FAB to M3 (Square-ish corners).

### Iteration 3: Cards & Lists
- **Components**: Habit list items, statistics cards.
- **Changes**:
    - Update `MaterialCardView` to M3 styles (increased corner radius, updated elevation/stroke).
    - Refine list item spacing and typography to match M3 "List" guidelines.

### Iteration 4: Dialogs & Bottom Sheets
- **Components**: Color picker, Habit editor dialogs, Confirmation dialogs.
- **Changes**:
    - Ensure all dialogs use `MaterialAlertDialog` with M3 theme.
    - Update layouts to use M3 padding and corner radii (typically 28dp).

### Iteration 5: Input Fields
- **Components**: `TextInputLayout`, `TextInputEditText`.
- **Changes**:
    - Migrate to M3 text field styles (Filled or Outlined).
    - Update error states and helper text styling.

---

## Phase 4: Polish & Advanced Features

### 4.1 Dynamic Color Integration
- **Task**: Implement `DynamicColors.applyToActivitiesIfAvailable()` in the `Application` class.
- **Details**: Allow the app to take on the user's wallpaper colors on Android 12+.

### 4.2 Typography Refinement
- **Task**: Map existing `android:textAppearance` to M3 equivalents (e.g., `TextAppearance.Material3.TitleLarge`).

### 4.3 Pure Black Theme (Amoled)
- **Task**: Update the `PureBlack` theme variant to work correctly with M3 color roles.

---

## Phase 5: Cleanup

### 5.1 Legacy Style Removal
- **Task**: Remove unused M2 styles and color definitions from `styles.xml` and `colors.xml`.
- **Verification**: Full app build and regression test.

---

## Testing Strategy

### Visual Testing
- **Checklist**:
    - Light Mode vs. Dark Mode.
    - Pure Black Mode.
    - Different screen sizes (Phone/Tablet).
    - Left-to-Right (LTR) vs. Right-to-Left (RTL) languages.

### Automated Testing
- **Command**: `./gradlew connectedDebugAndroidTest`
- **Focus**: Ensure that clicking buttons, opening dialogs, and navigation still work as expected despite the visual changes.
