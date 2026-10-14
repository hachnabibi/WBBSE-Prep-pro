# Modularizing Admin Dashboard Screens

Refactor the monolithic 8,700+ line `AdminPanelScreen.kt` into 10 clean, focused, maintainable screen composables inside `com.example.ui.screens.admin/`, while keeping the coordinator shell with drawer, tabs, and action messaging.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed based on user feedback:
> - **Unified Curriculum & Topics**: Chapter and Topic CRUD is consolidated inside `AdminCurriculumScreen.kt` using clean sub-tabs ("বোর্ড ও শ্রেণি" / "অধ্যায় ও টপিক").
> - **Navigation Shell Retention**: `AdminPanelScreen.kt` continues to serve as the master coordinator containing the `ModalNavigationDrawer`, the top horizontal chip row, TopAppBar actions, and action message toast/banner, delegating body content to each dedicated screen composable.
> - **Zero Loss of Features**: All Firestore CRUD, bulk upload logic, test creation, live exam tools, podcast manager, and index links are preserved identically.

- **Confirmed Decision 1**: Structure 10 screen files under `com.example.ui.screens.admin/` corresponding to Tabs 0 through 9.
- **Confirmed Decision 2**: Combine Board/Class and Chapter/Topic management into sub-tabs inside `AdminCurriculumScreen.kt`.
- **Confirmed Decision 3**: Retain the Bengali language interface and responsive Drawer + Tab Row navigation.

---

## 1. Overview & Core Concept

Currently, `AdminPanelScreen.kt` houses all admin functionality in a single 8,700+ line file, making maintenance, code reading, and feature enhancements difficult. This refactoring breaks the admin system into 10 modular files:

| File Name | Tab Index | Description |
|---|---|---|
| `AdminDashboardScreen.kt` | ট্যাব ০ | ক্লাউড ব্যাকআপ, সিস্টেম স্ট্যাটাস ও ড্যাশবোর্ড সামারি |
| `AdminAddQuestionScreen.kt` | ট্যাব ১ | নতুন প্রশ্ন সংযোজন (বোর্ড, ক্লাস, সাবজেক্ট ও চ্যাপ্টার সিলেকশন) |
| `AdminQuestionBankScreen.kt` | ট্যাব ২ | প্রশ্ন ব্যাংক ম্যানেজমেন্ট ও ফিল্টারিং (CMS) |
| `AdminCustomTestScreen.kt` | ট্যাব ৩ | কাস্টম মক টেস্ট ক্রিয়েটর ও প্রশ্ন নির্বাচন |
| `AdminLiveExamScreen.kt` | ট্যাব ৪ | লাইভ এক্সাম ও স্ট্রাকচার্ড টেস্ট শিডিউলিং |
| `AdminTestTemplatesScreen.kt` | ট্যাব ৫ | টেস্ট টেমপ্লেট ও কোয়েশ্চেন পেপার ব্যবস্থাপনা |
| `AdminBulkImportScreen.kt` | ট্যাব ৬ | বাল্ক ইমপোর্ট (JSON / CSV প্রশ্ন ও সিলেবাস) |
| `AdminCurriculumScreen.kt` | ট্যাব ৭ | কারিকুলাম ম্যানেজার (বোর্ড, ক্লাস, বিষয়, অধ্যায় ও টপিক) |
| `AdminIndexManagerScreen.kt` | ট্যাব ৮ | ফায়ারস্টোর ইনডেক্স ম্যানেজার ও ১-ক্লিক কনসোল লিংক |
| `AdminPodcastScreen.kt` | ট্যাব ৯ | অডিও পডকাস্ট আপলোড, এডিট ও ক্লাউড ম্যানেজমেন্ট |

---

## 2. User Experience & Visual Design

- **Seamless Transition**: The user experiences no visual degradation or abrupt UI changes. The TopAppBar, navigation drawer, and top horizontal filter chip row remain instant and smooth.
- **Responsive Navigation**: Switching between tabs instantly loads the lightweight isolated composable without recomposing irrelevant sections of other tabs.
- **Unified Feedback**: The success/error banner (`displayMessage` with `CorrectGreen` or `WrongRed`) at the top of the coordinator shell communicates status updates from any child screen uniformly via `onAction: (Boolean, String) -> Unit`.
- **Consistent Material 3 Styling**: All child screens adopt M3 Card, Surface, FilterChip, OutlinedTextField, and Button standards with 8.dp/12.dp corner radii and Bengali typography.

---

## 3. Key Product Decisions & Trade-Offs

- **Coordinator Pattern (`AdminPanelScreen.kt`)**:
  - *Chosen Approach*: `AdminPanelScreen` acts as the single controller managing `selectedTab`, `drawerState`, `actionMessage`, and top bar actions. It delegates the content area cleanly to the 10 screen composables.
  - *Why*: Avoids duplicated scaffold boilerplate, navigation drawers, and state synchronization across 10 screens.
- **Preserved Existing Tabs (`AdminDashboardHomeTab`, `AdminCurriculumCRUDTab`)**:
  - *Chosen Approach*: Reuse existing modularized files where appropriate and cleanly expose them through `AdminDashboardScreen.kt` and `AdminCurriculumScreen.kt`.
- **Shared Helpers & Dialogs**:
  - Reusable badges (like `AdminStatGradientBadge`), test delete dialogs, and admin profile edit dialogs remain accessible without cyclic imports.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                     AdminPanelScreen                        │
│    (TopAppBar + ModalNavigationDrawer + FilterChip Row)     │
└───────────────┬─────────────────────────────────────────────┘
                │
                ├─► Tab 0: AdminDashboardScreen
                ├─► Tab 1: AdminAddQuestionScreen
                ├─► Tab 2: AdminQuestionBankScreen
                ├─► Tab 3: AdminCustomTestScreen
                ├─► Tab 4: AdminLiveExamScreen
                ├─► Tab 5: AdminTestTemplatesScreen
                ├─► Tab 6: AdminBulkImportScreen
                ├─► Tab 7: AdminCurriculumScreen (Board/Class + Topics)
                ├─► Tab 8: AdminIndexManagerScreen
                └─► Tab 9: AdminPodcastScreen
```

### Module Responsibilities & Signature Contracts

1. **`AdminDashboardScreen`**:
   - Parameters: `viewModel: StudyViewModel`, `backupState: Any`, `isLoading: Boolean`, `isBengali: Boolean`, `onAction: (Boolean, String) -> Unit`, `modifier: Modifier`.
2. **`AdminAddQuestionScreen`**:
   - Contains all board, class, subject, chapter, option, explanation form fields and Firestore insert logic.
3. **`AdminQuestionBankScreen`**:
   - CMS view: Question listing, search, subject filtering, edit dialog, delete with confirmation.
4. **`AdminCustomTestScreen`**:
   - Step-by-step test creator: title, duration, marks, question selector from question bank, test saving.
5. **`AdminLiveExamScreen`**:
   - Scheduled live exams: start time, duration, leaderboards, batch targeting.
6. **`AdminTestTemplatesScreen`**:
   - Pre-configured test templates management, test deletion, view question list.
7. **`AdminBulkImportScreen`**:
   - Multi-format question parser, bulk upload to Firestore, sample templates download/copy.
8. **`AdminCurriculumScreen`**:
   - Sub-tab 0: Board & Class setup (`AdminBoardClassTab`).
   - Sub-tab 1: Chapter & Topic CRUD (`AdminCurriculumCRUDTab`).
9. **`AdminIndexManagerScreen`**:
   - Firestore composite indexes list with 1-click open deep links to Firebase Console.
10. **`AdminPodcastScreen`**:
    - Audio lecture metadata, streaming URL link, class/subject categorization, Firestore persistence.

### Verification Plan

- Run `compile_applet` after creating all files and updating `AdminPanelScreen.kt` to ensure complete build and type safety without missing symbols.
