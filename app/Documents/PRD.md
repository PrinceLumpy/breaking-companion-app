# BreakPractice - Product Requirements Document

**Platform:** Android  
**Language:** Kotlin

---

## 1. Goal

Help dancers store, organize, and practice their moves by:

- Logging moves with custom moveTags
- Randomly or structurally generating one combo at a time for practice
- Managing a personal moveTag library

---

## 2. Core Features

### 2.1 Move Management

- Add a new move (fields: **name**, **moveTags**)
- Assign one or more moveTags to each move
- Edit or delete existing moves
- View all moves in a **scrollable list of cards**
    - Card shows name and moveTags
    - Edit button on each card

### 2.2 Tag Management

- Default moveTags on first launch: `"Toprock"`, `"Footwork"`, `"Freeze"`, `"Power"`
- Users can **add, edit, or delete moveTags**
- Tags persist across sessions
- Separate **Tag List tab** displays all moveTags
- Clicking a moveTag shows all moves with that moveTag
- Tag editing options:
    - Rename moveTag
    - Delete moveTag (removes it from all moves)
- Tags are selectable when adding or editing moves

### 2.3 Combo Generator

- Generates **one combo at a time**
- Two generation modes:
    - **Random:** pick a random length (2–5) and random moves from selected moveTags
    - **Structured:** user specifies a moveTag sequence (2–5 moveTags, e.g. {tag1} → {tag2} →
      {tag3})
        - Picks one move from each specified moveTag
- Ask user: **How many moves long? (2–5)**
    - Default: random length between 2 and 5
- After generating a combo:
    - Display clearly
    - Offer **"Save this combo"** button
    - Saved combos appear in a separate tab ("Saved Combos")
- Encourage exporting combos to an SRS flashcard app or daily practice

### 2.4 Flashcard Mode

- For moves with excluded moveTags (like “get downs”)
- Prompt-style question: “Can you do this [move] in three different ways in and out of it?”
- Randomly select from moves tagged as excluded from combos

---

## 3. UI/UX Requirements

- **Menu navigation** to switch between:
    - Move List
    - Combo History
    - Saved Combos
    - Tag List

- **Landing screen (Move List):**
    - List of moves as cards
    - Button: “Add Move”
    - Button: “Generate Combo”

- **Add/Edit Move screen:**
    - Input: Move name
    - Multi-select dropdown of existing moveTags
    - Field to create new moveTag

- **Tag List screen:**
    - List of all user moveTags
    - Click moveTag to view associated moves
    - Buttons: Edit moveTag name, Delete moveTag

- **Combo Generator screen:**
    - Input: Combo length (2–5)
    - Mode toggle: Random or Structured
    - If Structured: fields to choose 2–5 moveTags in order
    - Generate button
    - After generation: "Save this combo" button

- **Saved Combos screen:**
    - List of saved combos
    - Delete option on each combo

- **Flashcard screen:**
    - Shows random move with excluded moveTag
    - “Next” button

**Design Goals:**

- Clean, minimal, modern
- Easy and fast data entry
- Visually satisfying cards (rounded corners, subtle shadows)

---

## 4. Data Model

**Move Object**

```kotlin
data class Move(
    val id: String, // UUID
    val name: String,
    val moveTags: List<String>
)

Todo

Please read through and do all of this, read all files necessary consume as much data as you need: