# Implementation Plan - Update Single Video Class Screen UI

We will update the single video class screen (such as `LearnTopicScreen.kt`) to match the user's reference design, incorporating the exact header styling, video player layout, channel info, action buttons (like, PDF notes, AI doubt solver, bookmark, download), and playlist tabs.

## Proposed Changes

### 1. Single Video Class Screen (`LearnTopicScreen.kt`)
- **Video Player / Thumbnail Header**:
  - Incorporate top overlay indicators.
  - Full-width video player with play/pause controls, progress bar, and duration badges.
- **Title & Metadata Section**:
  - Bold title matching reference styling.
  - View count, rating, and chapter indicators.
- **Creator / Channel Row**:
  - Channel avatar badge, name, class/board info, and mock test action chip.
- **Interactive Action Buttons**:
  - Row of action chips: Like, PDF Notes Download, AI Doubt Solver, Save/Bookmark, Offline Download.
- **Tabs Row**:
  - Tabs for Playlist, Smart Notes, Chapter MCQ.
- **Playlist Items List**:
  - List of playlist video cards matching the reference screenshot.

## Verification Plan

### Automated Tests
- Run `compile_applet` to verify successful compilation.
