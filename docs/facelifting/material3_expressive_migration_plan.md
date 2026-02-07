```
model: Gemini 3
```
____________________________________________________________________________

# Material 3 Expressive Migration Plan for UHabits Android

This document outlines a step-by-step iterative plan for migrating the UHabits Android application's UI from Material Design 2 to Material Design 3 Expressive.

**Estimated Complexity:** High. The migration from Material Design 2 to Material Design 3 (and then applying Material 3 Expressive principles) involves updating dependencies, themes, color systems, and individual components. Given the number of XML layout files, this will require careful, iterative work.

---

### Step-by-Step Iterative Migration Plan:

**Phase 1: Preparation & Foundation (One-time setup)**

1.  **Update Material Dependency:**
    *   **Action:** In `gradle/libs.versions.toml`, update the `material` version from `"1.12.0"` to the latest stable Material 3 version (e.g., `1.x.x` or `1.y.z`). Then, in `uhabits-android/build.gradle.kts`, ensure the `implementation(libs.material)` correctly points to the updated version.
    *   **Reasoning:** This is the foundational step to enable Material 3 components and styling.

2.  **Update Root Application Theme:**
    *   **Action:** In `uhabits-android/src/main/res/values/themes.xml` (or `styles.xml` if `themes.xml` doesn't exist or is not the primary theme file), change the parent of your main application theme. For example, replace `Theme.MaterialComponents.DayNight.NoActionBar` with `Theme.Material3.DayNight.NoActionBar`. Also, update any custom styles to inherit from `Widget.Material3.*` instead of `Widget.MaterialComponents.*` where appropriate.
    *   **Reasoning:** This applies the default Material 3 styling to your entire application, affecting colors, shapes, and typography of most components automatically.

3.  **Adopt Material 3 Color System (Initial):**
    *   **Action:** Use an online tool like the [Material Theme Builder](https://m3.material.io/theme-builder) to generate a Material 3 color palette based on your app's current primary colors. Then, update your `uhabits-android/src/main/res/values/colors.xml` file with the new color attributes (e.g., `primary`, `onPrimary`, `primaryContainer`, etc.) and ensure your theme references these new colors.
    *   **Reasoning:** Material 3 has an expanded color system. Aligning with it is crucial for a cohesive M3 look.

---

**Phase 2: Iterative Component Migration (Screen by Screen / Component by Component)**

This is the core iterative process. You will pick one UI element, migrate it, test it, and then move to the next.

1.  **Identify a Target UI Element:**
    *   **Action:** Choose a single, contained UI element to migrate. Start with simpler ones like a button, a card, or a specific dialog, then move to more complex screens (e.g., `activity_edit_habit.xml`, `show_habit.xml`).
    *   **Reasoning:** Breaking down the task into smaller, manageable chunks allows for continuous testing and reduces the risk of overwhelming visual regressions.

2.  **Replace/Update Component in XML:**
    *   **Action:** For the chosen UI element, update its XML definition.
        *   **Buttons:** If using `com.google.android.material.button.MaterialButton`, it might automatically adapt to M3 styling under a `Theme.Material3` theme. Otherwise, explicitly use Material 3 button styles or replace generic `<Button>` with `<com.google.android.material.button.MaterialButton>`.
        *   **FABs:** Material 3 FABs have new shapes and styles. Adjust `app:shapeAppearanceOverlay` or use appropriate Material 3 FAB classes.
        *   **Cards, Chips, Top App Bars, Bottom Navigation:** These components have significant visual updates. Update their XML attributes (e.g., shapes, elevation, colors) to reflect Material 3.
        *   **General Views:** For standard Android Views (like `TextView`, `ImageView`), ensure their styling (colors, typography) looks correct under the new Material 3 theme.
    *   **Reasoning:** This directly updates the visual appearance of the component.

3.  **Refine Styling and Attributes:**
    *   **Action:** Adjust specific attributes in the XML (e.g., `app:cornerRadius`, `app:elevation`, `android:textColor`, `android:padding`) or custom styles to fine-tune the Material 3 Expressive look. Consider using Material 3 design tokens for shapes, elevations, and typography.
    *   **Reasoning:** Material 3 Expressive emphasizes unique shapes, dynamic color, and a broader range of motion.

4.  **Review and Test:**
    *   **Action:** Run the application, navigate to the migrated UI element, and visually inspect it. Test its functionality to ensure no regressions were introduced.
    *   **Reasoning:** Immediate feedback helps catch issues early.

---

**Phase 3: Global Refinement (After most components are migrated)**

1.  **Typography Integration:**
    *   **Action:** Define a comprehensive Material 3 typography scale in your themes and apply it consistently across your app to match the expressive design. This might involve creating custom text appearance styles.
    *   **Reasoning:** Material 3 offers a rich typography system that can contribute significantly to the "expressive" feel.

2.  **Motion and Animation:**
    *   **Action:** Introduce Material 3 motion principles, such as expressive transitions between screens, shared element transitions, and subtle animations for state changes.
    *   **Reasoning:** Motion is a key aspect of Material 3 Expressive, adding delight and clarity to user interactions.

3.  **Dynamic Color (Optional):**
    *   **Action:** Implement dynamic color functionality for devices running Android 12 and above, allowing the app's colors to adapt to the user's wallpaper.
    *   **Reasoning:** This is a hallmark feature of Material 3 and enhances personalization.

4.  **Accessibility Review:**
    *   **Action:** Conduct a thorough accessibility review to ensure all updated components and their interactions meet accessibility guidelines (e.g., sufficient contrast, proper content descriptions).
    *   **Reasoning:** Accessibility is paramount for any user interface.

5.  **Comprehensive Testing:**
    *   **Action:** Perform full end-to-end testing, including UI tests, integration tests, and user acceptance testing, to ensure overall stability and quality.
    *   **Reasoning:** Verifies the complete application works as expected after all changes.
