# Implementation Plan - Mock Tests & Practice Sets Home Screen Section

We will create a dedicated `MockTestsHomeSection` composable component and integrate it into `HomeScreen.kt`. This section will display available practice sets for each subject in a horizontal scrolling card format, complete with progress bars for completed tests and a direct "Start Quiz" button.

## Proposed Changes

### 1. New Composable: `MockTestsHomeSection` in `HomeScreen.kt`
- Display section header ("মক টেস্ট ও প্র্যাকটিস সেট" / "Mock Tests & Practice Sets") with "সব দেখুন" (See All) action.
- Horizontal scrolling `LazyRow` of subject mock test cards.
- Each card will display:
  - Subject name (e.g., Mathematics, Bengali).
  - Total available practice sets and completed test count.
  - Animated progress bar indicating overall mastery or completion percentage.
  - Direct "Start Quiz" button with primary accent color and test icon.

### 2. Integration in `HomeScreen.kt`
- Insert `MockTestsHomeSection` into the main `LazyColumn` of `HomeScreen.kt` below the subject grid or daily quiz banner.

## Verification Plan
- Compile the app using `compile_applet` to ensure there are no syntax or resolution errors.
