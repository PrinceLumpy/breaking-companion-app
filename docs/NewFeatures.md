# Practice Mode Expansion: Design Document

This document outlines the architectural and UI changes required to implement the **Goals**, **Timer**, and **Foundational Drills** features.

---

## 1. Goals & Progress Tracking

### Concept
Users can create high-level goals (e.g., "Master Power Moves") and define specific "Stages" or drills required to achieve them. Unlike a sequential video game level, these stages are open tasks. The user tracks reps/sets for each stage to reach a target.

### Database Schema Changes

#### New Entity: `Goal`
Represents the parent container for a set of tracking tasks.
```kotlin
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)
```

#### New Entity: `GoalStage`
Represents a single trackable item within a goal (e.g., "200 Windmills").
```kotlin
@Entity(
    tableName = "goal_stages",
    foreignKeys = [
        ForeignKey(entity = Goal::class, parentColumns = ["id"], childColumns = ["goalId"], onDelete = CASCADE)
    ]
)
data class GoalStage(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val goalId: String, // Foreign Key
    val name: String,   // e.g., "Flare Drills"
    val currentCount: Int = 0,
    val targetCount: Int,
    val unit: String = "reps" // e.g., "reps", "minutes", "sets"
)
```

### UI/UX Design

#### A. Goals Dashboard (New Tab)
*   **List View:** Displays active goals.
*   **Card Content:** Goal Title, specific count of active stages (e.g., "3 active drills"), and an overall completion percentage.
*   **FAB:** "Create New Goal".

#### B. Goal Details Screen (The "Tracker")
This is where the user spends their practice time.
*   **Header:** Goal Title & Description.
*   **Visuals:** A master progress bar for the entire goal.
*   **Stage List:** A list of `GoalStage` items.

**Stage Item Layout (Row):**
1.  **Left:** Stage Name (e.g., "Halos").
2.  **Middle:**
    *   **Progress Bar:** Linear progress indicator showing `current / target`.
    *   **Text:** `45 / 100`.
3.  **Right (Controls):**
    *   **`-` Button:** Decrement by 1 (Quick correction).
    *   **`+` Button:** Increment by 1 (Quick add).
    *   **`Edit/Add` Button:** Opens a dialog.

**"Add Reps" Dialog:**
*   Triggered by clicking the stage or the specific "Add" button.
*   **Input Field:** Numeric input (e.g., user did a set of 10, types "10").
*   **Action:** Adds input to `currentCount`.
*   **Reasoning:** Doing 50 reps and tapping `+` 50 times is bad UX.

---

## 2. In-App Timer

### Concept
A utility to help with footwork drills, freezes, or HIIT sets without leaving the app.

### Database Changes
*   None required initially. (Future: Save session logs).

### UI/UX Design

#### Timer Screen (New Tab or Utility Button)
*   **Two Modes:**
    1.  **Stopwatch:** Simple Start/Stop/Reset. Used for "How long can I hold this freeze?".
    2.  **Countdown/Interval:**
        *   Input: Duration (mm:ss).
        *   Controls: Start, Pause, Reset.
        *   Visuals: Large text, circular progress indicator.
        *   Audio: Beep on completion (requires `MediaPlayer` or `ToneGenerator`).

---

## 3. Foundational Drills Library

### Concept
A persistent checklist or library of "Basics" (e.g., 6-step, CCs, Pushups) that a user wants to keep track of separately from their creative "Moves" list.

### Database Schema Changes

#### New Entity: `Drill`
Separating this from `Move` prevents the "Creative Move Library" (Combo Generator) from getting cluttered with conditioning exercises.

```kotlin
@Entity(tableName = "drills")
data class Drill(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // e.g., "Conditioning", "Footwork", "Freeze"
    val description: String
)
```

### UI/UX Design

#### Drills Library Screen
*   **List View:** Alphabetical list of drills.
*   **Functionality:**
    *   Add/Edit/Delete Drills.
    *   **"Add to Goal" Action:** Long-pressing a drill allows the user to instantly create a `GoalStage` in an existing goal using this drill's name.

---

## 4. Navigation & Architecture Updates

### Navigation Structure
The "Practice" section is becoming feature-rich. To avoid a clutter of 6+ bottom tabs, we should reorganize:

**Current Bottom Bar:**
1.  Moves (Library)
2.  Saved Combos
3.  Tags
4.  Battle
5.  Settings

**Proposed Bottom Bar:**
1.  **Library** (Moves & Tags & Drills) -> Tab Layout at top?
2.  **Lab** (Combo Gen & Saved Combos)
3.  **Tracker** (Goals & Timer) -> **NEW**
4.  **Battle**
5.  **Settings**

*Alternatively, strictly for "Practice Mode" request:*
Keep existing tabs but add a **"Goals"** tab and put the Timer/Drills inside it or as sub-features.

### Recommended Implementation Order
1.  **Database Layer:** Create Entities (`Goal`, `GoalStage`, `Drill`) and DAOs.
2.  **Goals UI:** Build the Goal List and Detail screens.
3.  **Drills UI:** Build the simple CRUD list.
4.  **Timer UI:** Build the utility screen.
