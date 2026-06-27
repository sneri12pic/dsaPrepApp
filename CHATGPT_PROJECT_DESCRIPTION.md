# DSA Prep Android App - Project Description for ChatGPT

Use this document as context when helping with this project. The project is a native Android app named **DSA Prep**. It helps a developer keep a simple daily routine for data structures and algorithms interview preparation, project work, technical review, and career/application work.

The app is intentionally local-first and small in scope. It uses Jetpack Compose for the UI, Room for local persistence, ViewModels for screen state, Kotlin coroutines/Flow for reactive updates, and Navigation Compose for the bottom-tab app structure.

## High-Level Product Summary

**DSA Prep** is a personal study tracker for interview preparation. The app gives the user:

- A daily checklist of default study tasks.
- A prominent "Up next" task so the user always knows the next action.
- A daily check-in form for energy, focus, and notes.
- A month calendar showing active study/check-in days.
- A two-week roadmap timeline for structured DSA prep.
- A progress dashboard showing streaks, total study days, weekly completion, and check-in details.

The current app has three bottom-navigation tabs:

1. **Today**
2. **Plan**
3. **Progress**

The app currently does not use a backend, authentication, remote sync, or network APIs. All persisted data is stored locally with Room.

## Repository and Module Structure

The repository is a standard single-module Android project:

```text
dsaPrepApp/
  build.gradle.kts
  settings.gradle.kts
  gradle.properties
  gradlew
  gradlew.bat
  app/
    build.gradle.kts
    proguard-rules.pro
    schemas/
      com.stepandemianenko.dsaprep.data.StudyDatabase/
        1.json
    src/main/
      AndroidManifest.xml
      java/com/stepandemianenko/dsaprep/
        MainActivity.kt
        StudyPlanApplication.kt
        data/
        navigation/
        ui/
        viewmodel/
      res/
        values/
        drawable/
        mipmap-*/
```

The package namespace and application ID are:

```text
com.stepandemianenko.dsaprep
```

The app label shown to users is:

```text
DSA Prep
```

## Technology Stack

The project uses:

- **Kotlin**
- **Android Gradle Plugin 8.11.2**
- **Kotlin 2.0.21**
- **KSP 2.0.21-1.0.27**
- **Jetpack Compose**
- **Material 3**
- **Navigation Compose**
- **Lifecycle ViewModel + lifecycle-aware state collection**
- **Room**
- **Kotlin coroutines and Flow**
- **Java time APIs**, especially `LocalDate`, `YearMonth`, and `DateTimeFormatter`

Important Android settings:

```kotlin
compileSdk = 36
minSdk = 26
targetSdk = 36
jvmTarget = "17"
sourceCompatibility = JavaVersion.VERSION_17
targetCompatibility = JavaVersion.VERSION_17
```

Important dependencies in `app/build.gradle.kts` include:

- `androidx.activity:activity-compose`
- `androidx.compose.foundation:foundation`
- `androidx.compose.material3:material3`
- `androidx.compose.material:material-icons-extended`
- `androidx.compose.ui:ui`
- `androidx.lifecycle:lifecycle-runtime-compose`
- `androidx.lifecycle:lifecycle-viewmodel-compose`
- `androidx.navigation:navigation-compose`
- `androidx.room:room-runtime`
- `androidx.room:room-ktx`
- `androidx.room:room-compiler` through KSP

Room schema export is enabled:

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

## App Entry Point

### `MainActivity.kt`

`MainActivity` is a simple `ComponentActivity`. It calls `setContent`, applies `DsaPrepTheme`, and renders `AppNavigation`.

The entry point looks conceptually like this:

```kotlin
setContent {
    DsaPrepTheme {
        AppNavigation()
    }
}
```

### `StudyPlanApplication.kt`

The custom `Application` class owns the app-wide repository:

```kotlin
class StudyPlanApplication : Application() {
    val repository: StudyRepository by lazy {
        StudyRepository(StudyDatabase.getDatabase(this).studyDao())
    }
}
```

This is the current dependency injection strategy. There is no Hilt, Koin, or other DI framework. Screens obtain the repository from `LocalContext.current.applicationContext as StudyPlanApplication`, then create ViewModels with custom factories.

## Navigation

Navigation lives in:

```text
app/src/main/java/com/stepandemianenko/dsaprep/navigation/AppNavigation.kt
```

The app uses a `Scaffold` with a Material 3 `NavigationBar`.

Bottom navigation destinations:

| Route | Label | Screen |
|---|---|---|
| `today` | Today | `TodayScreen` |
| `plan` | Plan | `PlanScreen` |
| `progress` | Progress | `ProgressScreen` |

The app starts on the `today` route.

Navigation behavior:

- Uses `rememberNavController`.
- Uses `NavHost`.
- Uses `popUpTo(navController.graph.findStartDestination().id)` with `saveState = true`.
- Uses `launchSingleTop = true`.
- Uses `restoreState = true`.

Each route creates its own ViewModel using `viewModel(factory = ...)`, collects UI state with `collectAsStateWithLifecycle`, and passes callbacks into the screen.

## Architecture

The project follows a lightweight MVVM structure:

```text
UI Composables
  -> ViewModels
    -> StudyRepository
      -> StudyDao
        -> Room database
```

The main boundaries are:

- `data/`: Room entities, DAO, database, repository, domain/data models.
- `viewmodel/`: UI state models and ViewModels.
- `ui/screens/`: screen-level Compose functions.
- `ui/components/`: reusable Compose components.
- `ui/theme/`: Material theme.
- `navigation/`: bottom navigation and route wiring.

State generally flows one way:

1. Room emits `Flow` data through DAO queries.
2. `StudyRepository` maps entities to app models and combines flows where needed.
3. ViewModels collect repository flows or expose `StateFlow` UI state.
4. Compose screens collect ViewModel state and render stateless UI where possible.
5. User interactions call callbacks, which delegate back to ViewModel methods.

## Data Layer

Data files live in:

```text
app/src/main/java/com/stepandemianenko/dsaprep/data/
```

### Room Database

The Room database is `StudyDatabase`.

Database name:

```text
study_plan_database
```

Database version:

```text
1
```

Entities:

- `StudyTaskEntity`
- `DailyCheckInEntity`

Type converters:

- `Converters`, which stores `LocalDate` as an ISO date string via `date.toString()` and reads it with `LocalDate.parse(value)`.

The database is created with:

```kotlin
Room.databaseBuilder(
    context.applicationContext,
    StudyDatabase::class.java,
    "study_plan_database"
).build()
```

There are no migrations yet because the database is still version 1.

### `StudyTaskEntity`

Room table:

```text
study_tasks
```

Fields:

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Primary key, auto-generated |
| `title` | `String` | Task title |
| `category` | `String` | Category label |
| `date` | `LocalDate` | Stored through Room converter |
| `isCompleted` | `Boolean` | Completion state |

Domain model:

```kotlin
data class StudyTask(
    val id: Long,
    val title: String,
    val category: String,
    val date: LocalDate,
    val isCompleted: Boolean
)
```

`StudyTaskEntity.toStudyTask()` maps the Room entity into the domain model.

### `DailyCheckInEntity`

Room table:

```text
daily_check_ins
```

Fields:

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Primary key, auto-generated |
| `date` | `LocalDate` | Unique indexed date |
| `energyLevel` | `String` | Currently values are `Low`, `Medium`, or `High` |
| `focusLevel` | `String` | Currently values are `Low`, `Medium`, or `High` |
| `note` | `String?` | Optional free-text note |
| `completedAtLeastOneTask` | `Boolean` | Whether the day counts as active because at least one task was completed |

The `date` column has a unique index, so there should be at most one check-in per day. Saving a check-in uses `OnConflictStrategy.REPLACE`.

Domain model:

```kotlin
data class DailyCheckIn(
    val id: Long,
    val date: LocalDate,
    val energyLevel: String,
    val focusLevel: String,
    val note: String?,
    val completedAtLeastOneTask: Boolean
)
```

### DAO

The DAO is `StudyDao`.

Important queries and operations:

- `getTasksForDate(date: LocalDate): Flow<List<StudyTaskEntity>>`
- `getTaskCountForDate(date: LocalDate): Int`
- `insertTasks(tasks: List<StudyTaskEntity>)`
- `updateTaskCompletion(taskId: Long, isCompleted: Boolean)`
- `getAllTasks(): Flow<List<StudyTaskEntity>>`
- `getCompletedTasks(): Flow<List<StudyTaskEntity>>`
- `insertOrUpdateCheckIn(checkIn: DailyCheckInEntity)`
- `getCheckInByDate(date: LocalDate): Flow<DailyCheckInEntity?>`
- `getRecentCheckIns(limit: Int): Flow<List<DailyCheckInEntity>>`
- `getAllCheckIns(): Flow<List<DailyCheckInEntity>>`

### Repository

The repository is `StudyRepository`.

Responsibilities:

- Map Room entities into app/domain models.
- Seed default tasks for today if no tasks exist for the current date.
- Update task completion.
- Save or update a daily check-in.
- Fetch recent check-ins.
- Compute progress summary statistics.
- Provide the static two-week study roadmap used by the Plan tab.

#### Default Daily Tasks

When `TodayViewModel` starts, it calls:

```kotlin
repository.seedDefaultTasksIfNeeded(today)
```

If today's task count is zero, the repository inserts four default tasks:

1. `Solve 1-2 Neetcode / algorithm problems`
2. `Work on fitness app project`
3. `Review one technical concept`
4. `Apply / network / improve portfolio`

Categories:

- `Algorithms / Neetcode`
- `Main project`
- `Computer science fundamentals`
- `Applications / CV / GitHub`

#### Progress Calculation

`getProgressSummary(today: LocalDate = LocalDate.now())` combines:

- Completed tasks.
- All check-ins.
- Five recent check-ins.

It creates a set of completed/active dates from:

- Dates of completed tasks.
- Dates of check-ins where `completedAtLeastOneTask == true`.

It then calculates:

- `currentStreak`
- `longestStreak`
- `completedStudyDays`
- `weeklyCompletionPercentage`
- `completedDates`
- `recentCheckIns`
- `checkInsByDate`

Current streak logic:

- If today is active, the streak starts today.
- Else if yesterday is active, the streak starts yesterday.
- Else current streak is zero.
- Then it walks backward day by day while dates are active.

Longest streak logic:

- Sort all completed dates.
- Count consecutive date sequences.
- Return the largest sequence length.

Weekly completion percentage:

- Looks at today and the previous six days.
- Counts how many of those seven dates are active.
- Converts that to an integer percentage using `(weekCompletedDays * 100) / 7`.

### Static Weekly Plan

The Plan tab uses a fixed in-code roadmap returned by `StudyRepository.getWeeklyPlan()`.

This roadmap is **not persisted**. Only checked/expanded UI state for this screen is tracked in memory by `PlanViewModel`.

The roadmap has two weeks:

#### Week A - Foundations

- Mon: Arrays & hashing - Solve 2 problems
- Tue: Two pointers + sliding window - Solve 2 problems
- Wed: Stack + binary search - Solve 2 problems
- Thu: Review & redo - 4 earlier problems
- Fri: Timed OA - 1 HackerRank set
- Sat: Fitness app - Build a feature + 10-min walkthrough prep
- Sun: Behavioural + rest - 2 STAR stories, light review

#### Week B - Structures

- Mon: Linked lists - Solve 2 problems
- Tue: Trees BFS / DFS - Solve 2 problems
- Wed: Heaps + intervals - Solve 2 problems
- Thu: Graph basics - Solve 2 problems
- Fri: Timed practice - 1 HackerRank set + 1 LeetCode mock
- Sat: Fitness app - 2 drills + a debugging deep-dive
- Sun: Behavioural + refresh - 2 STAR stories, weak-spot refresh

## ViewModels and UI State

ViewModels live in:

```text
app/src/main/java/com/stepandemianenko/dsaprep/viewmodel/
```

### Shared UI Models

`StudyUiModels.kt` contains:

- `StudyTaskUiModel`
- `PlanCategoryUiModel`
- `RecentCheckInUiModel`
- `CalendarDayUiModel`

Note: `PlanCategoryUiModel` and `PlanCategory` exist but do not appear central to the current Plan timeline implementation.

### `TodayViewModel`

`TodayViewModel` controls the Today tab.

It owns:

- Today's date.
- Today's tasks.
- The first incomplete task as `nextTask`.
- Completed/total counts.
- A progress fraction.
- Energy and focus selections.
- The check-in note.
- Whether the saved message is visible.
- A month calendar model for the current month.
- Active/completed dates from the progress summary.

Important UI state:

```kotlin
data class TodayUiState(
    val todayDateText: String = "",
    val focusOfDayText: String = "Focus of the day: one algorithm, one project step, one review.",
    val motivationText: String = "A calm, finished session beats a perfect plan.",
    val tasks: List<StudyTaskUiModel> = emptyList(),
    val nextTask: StudyTaskUiModel? = null,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progress: Float = 0f,
    val selectedEnergyLevel: String = "Medium",
    val selectedFocusLevel: String = "Medium",
    val note: String = "",
    val isLoading: Boolean = false,
    val checkInSavedMessageVisible: Boolean = false,
    val calendar: TodayCalendarUiModel = TodayCalendarUiModel(),
    val activeDates: Set<LocalDate> = emptySet()
)
```

Important behavior:

- Seeds default tasks for today if needed.
- Collects today's tasks and updates checklist/progress.
- Collects `ProgressSummary` so the Today calendar can mark active dates.
- Collects today's check-in so existing energy/focus/note values are restored.
- `toggleTask(taskId)` flips completion for a task.
- `setEnergyLevel(level)` updates selected energy and hides the saved message.
- `setFocusLevel(level)` updates selected focus and hides the saved message.
- `updateNote(note)` updates the note and hides the saved message.
- `saveCheckIn()` stores today's check-in with `completedAtLeastOneTask = completedCount > 0`.

The Today calendar is Monday-first, built for the current month, and marks:

- Today.
- Completed/active days.
- Past days.
- Future days.

### `PlanViewModel`

`PlanViewModel` controls the Plan tab.

It loads the static roadmap from `StudyRepository.getWeeklyPlan()`.

It tracks in-memory UI-only state:

- Checked roadmap day IDs.
- Expanded week IDs.

Important UI state:

```kotlin
data class PlanUiState(
    val weeks: List<PlanWeekUiModel> = emptyList()
)
```

Each week exposes:

- `id`
- `title`
- `subtitle`
- `days`
- `isExpanded`
- `completedCount`
- `totalCount`
- `progressFraction`

Each day exposes:

- `id`
- `dayLabel`
- `topic`
- `detail`
- `isChecked`
- `isToday`

Important behavior:

- All weeks are expanded by default.
- Checking roadmap items does not write to Room.
- Expansion state does not write to Room.
- The current weekday is marked as "today" only in the first week.
- `toggleItem(itemId)` checks/unchecks a day.
- `toggleExpand(weekId)` expands/collapses a week.

### `ProgressViewModel`

`ProgressViewModel` controls the Progress tab.

It collects `repository.getProgressSummary()` and maps it into:

- Streak statistics.
- Weekly completion percentage.
- Motivation text.
- Recent check-ins.
- A seven-day calendar strip model.
- A full month calendar grid.
- A map of exact check-in details by date.

Important UI state:

```kotlin
data class ProgressUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completedStudyDays: Int = 0,
    val weeklyCompletionPercentage: Int = 0,
    val motivationText: String = "",
    val calendarDays: List<CalendarDayUiModel> = emptyList(),
    val recentCheckIns: List<RecentCheckInUiModel> = emptyList(),
    val monthLabel: String = "",
    val weekdayHeaders: List<String> = emptyList(),
    val monthDays: List<ProgressCalendarDay> = emptyList(),
    val checkInDetailsByDate: Map<LocalDate, CheckInDetailUiModel> = emptyMap(),
    val isLoading: Boolean = true
)
```

The Progress month calendar is Monday-first. It includes leading and trailing placeholder cells so the grid is aligned to full weeks.

Motivation text depends on weekly completion:

- `>= 70`: "Strong week. Keep the rhythm steady."
- `>= 40`: "Good momentum. One more focused day will lift the week."
- `> 0`: "You have started. Protect the next small session."
- `0`: "Begin with one task today and let the streak follow."

## Screens

Screen files live in:

```text
app/src/main/java/com/stepandemianenko/dsaprep/ui/screens/
```

### Today Screen

File:

```text
TodayScreen.kt
```

Main sections:

1. `TodayHeader`
2. `UpNextCard`
3. "Today's tasks" checklist
4. `TodayCalendar`
5. `CheckInCard`

The screen uses a `LazyColumn` with 20 dp padding and 20 dp vertical spacing.

`TodayHeader` shows:

- Formatted current date.
- "Today's study plan" title.
- Today's completed/total count.
- A `LinearProgressIndicator`.
- A motivation line based on task completion.

`UpNextCard` shows:

- The first incomplete task.
- A prominent "Mark done" button.
- If all tasks are complete, an "All done for today" state.

The task list renders `TaskCard` for each task. If tasks are empty and loading is false, it displays "No tasks for today yet."

The check-in card uses:

- Energy selector.
- Focus selector.
- Optional note field.
- Save button.
- Saved confirmation text.

### Plan Screen

File:

```text
PlanScreen.kt
```

Main sections:

1. `OverallProgressHeader`
2. `PlanTimeline`

`OverallProgressHeader` shows:

- "Prep roadmap"
- "Weekly timeline"
- Overall completed/total count.
- Short instruction text.
- Overall progress indicator.

`PlanTimeline` renders the two-week roadmap as a vertical timeline with:

- Week headers.
- Per-week progress.
- Expand/collapse animation.
- Day rows with topics and details.
- Today badge on the matching weekday in the first week.
- Check marks for completed roadmap items.

### Progress Screen

File:

```text
ProgressScreen.kt
```

Main sections:

1. Header text: "Momentum" and "Progress"
2. `WeeklyHeroCard`
3. Three compact `ProgressSummaryCard` stats
4. `ProgressCalendar`
5. "Check-in detail" section
6. Selected check-in detail card or empty message

The screen locally tracks `explicitSelection` with `remember`.

Selection behavior:

- Defaults to today's check-in if it exists.
- Otherwise defaults to the most recent check-in date.
- Tapping a marked day in the month calendar selects that date.
- Only dates with check-ins are selectable.

`WeeklyHeroCard` shows:

- A circular completion indicator for this week.
- Current streak text.
- Motivation text.

The stat row shows:

- Current streak.
- Longest streak.
- Total study days.

## Reusable UI Components

Components live in:

```text
app/src/main/java/com/stepandemianenko/dsaprep/ui/components/
```

### `TaskCard`

Displays one daily task.

Behavior:

- Whole card is clickable.
- Checkbox also toggles the task.
- Completed tasks use a softer background, lower elevation, and strikethrough text.
- Accessibility semantics describe marking the task complete/incomplete.

### `UpNextCard`

Displays the first incomplete task as the primary action for the Today screen.

States:

- Task available: primary-colored card with task title, category, and "Mark done" button.
- No task available: all-done card.

### `CheckInCard`

Displays the daily reflection form.

Inputs:

- Energy level: Low, Medium, High.
- Focus level: Low, Medium, High.
- Optional note.

Actions:

- Save check-in.

Visual state:

- Shows a different supporting message if today is already active.
- Shows "Check-in saved" after save.

### `TodayCalendar`

Displays a compact current-month grid on the Today screen.

It receives a stateless month model from `TodayViewModel`.

It marks:

- Active days.
- Today.
- Past/future days with different emphasis.

It also shows the active-day count for the current month.

### `PlanTimeline`

Displays the static two-week study roadmap.

Features:

- Vertical rail with dots.
- Week header rows.
- Animated expand/collapse.
- Per-week progress bars.
- Checkable day rows.
- Today badge.
- Warm accent colors per week.

### `ProgressCalendar`

Displays a full current-month calendar on the Progress screen.

Features:

- Weekday headers.
- Day-of-month numbers.
- Check-in markers.
- Activity markers.
- Today outline.
- Selected date state.
- Legend for check-in/activity/today.

Only days with check-ins are clickable.

### `ProgressSummaryCard`

Compact statistic tile used in the Progress screen.

### `CalendarStrip`

This component renders a last-seven-days strip. It is present in the codebase but the current `ProgressScreen` uses the full `ProgressCalendar` instead.

## Theme and Visual Design

Theme file:

```text
app/src/main/java/com/stepandemianenko/dsaprep/ui/theme/Theme.kt
```

The app uses a custom light Material 3 color scheme. The palette is described in code as "peach + creamy yellow + pink".

Important color roles:

- `primary`: rose pink, used for main highlights and actions.
- `secondary`: peach, used for supporting warmth.
- `tertiary`: creamy golden yellow, used sparingly for accents like today's outline.
- `background`: light seashell cream.
- `surface`: near-white.
- Warm grey values are used for text and outlines.

The UI style is card-based, soft, warm, and motivational. Cards generally use rounded corners around 18-26 dp, with light elevation.

## Current User Flow

### First Launch / Today Tab

1. App opens to the Today tab.
2. `TodayViewModel` seeds default tasks if today has no tasks.
3. User sees four default tasks.
4. The first incomplete task appears in the `UpNextCard`.
5. User can mark a task done from the up-next card or from the full checklist.
6. Progress count and progress bar update automatically.
7. User can fill in energy, focus, and an optional note.
8. Saving the check-in writes one `DailyCheckInEntity` for today.
9. Today becomes active if at least one task has been completed.

### Plan Tab

1. User sees a two-week DSA prep roadmap.
2. User can expand/collapse weeks.
3. User can tick day rows as complete.
4. Per-week and overall progress update immediately.
5. This Plan state is currently in-memory only, so checked roadmap items are reset when the ViewModel is recreated.

### Progress Tab

1. User sees weekly completion percentage and current streak.
2. User sees current, longest, and total study-day stats.
3. User sees a full month calendar.
4. Days with check-ins are marked and tappable.
5. Tapping a check-in day shows its exact energy, focus, and note.
6. If no check-in exists, an empty message is shown.

## Persistence Details

Persisted:

- Daily task rows for each date.
- Completion status for daily tasks.
- Daily check-ins.

Not persisted:

- Plan tab checked roadmap items.
- Plan tab expanded/collapsed week state.
- Progress screen currently selected calendar day.

The app currently creates default daily tasks only for the date passed to `seedDefaultTasksIfNeeded`, which is currently today. There is no UI for changing dates or generating tasks for arbitrary future/past dates.

## Known Limitations and Gaps

These are important when proposing future changes:

- There are no automated tests in the current project.
- There is no dependency injection framework.
- There is no remote sync or account system.
- There is no screen for editing, deleting, or creating custom tasks.
- The Plan roadmap is hardcoded in `StudyRepository`.
- Plan completion state is not persisted.
- The app only seeds the current day's default tasks.
- There is no dark theme.
- There are no Room migrations yet because the database is version 1.
- The repository currently mixes persisted task/check-in operations with the static Plan roadmap.
- Some models/components appear unused or legacy, such as `PlanCategory`, `PlanCategoryUiModel`, and possibly `CalendarStrip`.
- Build configuration includes debug Compose tooling, but no explicit test dependencies.

## Important Implementation Conventions

When editing this project, follow these conventions:

- Keep MVVM boundaries clear.
- Put persistent data changes in `data/`.
- Put screen state and state transformations in `viewmodel/`.
- Keep screens mostly stateless by passing state and callbacks.
- Prefer adding focused reusable composables in `ui/components/`.
- Use `collectAsStateWithLifecycle` when collecting ViewModel state in Compose.
- Prefer `StateFlow` in ViewModels.
- Prefer Room `Flow` queries for reactive UI updates.
- Use `LocalDate` for date-based features and update Room converters/migrations carefully if date storage changes.
- If changing Room entities, increment the database version and add a migration or explicitly handle destructive migration if that is acceptable.
- Preserve the app's warm Material 3 visual language unless the task explicitly asks for a redesign.
- Avoid adding a backend unless the user explicitly asks for sync, accounts, or multi-device support.

## File Map for Common Changes

Use this map when deciding where to make changes:

| Change Needed | Likely Files |
|---|---|
| Add/edit daily tasks behavior | `StudyRepository.kt`, `StudyDao.kt`, `StudyTaskEntity.kt`, `TodayViewModel.kt`, `TodayScreen.kt` |
| Add task creation UI | `TodayScreen.kt`, new component in `ui/components/`, `TodayViewModel.kt`, `StudyRepository.kt`, `StudyDao.kt` |
| Persist Plan roadmap completion | Add Room entity/DAO methods, update `StudyDatabase.kt`, update `PlanViewModel.kt`, add migration/schema |
| Change static roadmap content | `StudyRepository.getWeeklyPlan()` |
| Change Today UI | `TodayScreen.kt`, `TaskCard.kt`, `UpNextCard.kt`, `TodayCalendar.kt`, `CheckInCard.kt` |
| Change Progress stats | `StudyRepository.getProgressSummary()`, `ProgressViewModel.kt`, `ProgressScreen.kt` |
| Change Progress calendar behavior | `ProgressViewModel.kt`, `ProgressCalendar.kt`, `ProgressScreen.kt` |
| Change theme/colors | `ui/theme/Theme.kt` |
| Add new bottom tab | `AppNavigation.kt`, new screen, new ViewModel |
| Change app label | `res/values/strings.xml` |
| Change database schema | Data entity files, `StudyDatabase.kt`, schema/migration handling |

## Suggested Future Improvements

Potential next improvements:

- Add a custom task creation/editing flow.
- Add task templates by day or topic.
- Persist Plan roadmap completion.
- Add dark theme support.
- Add Room migrations before schema changes.
- Add unit tests for streak and weekly completion logic.
- Add ViewModel tests with fake repositories.
- Add Compose UI tests for task toggling and check-in saving.
- Add an onboarding or settings screen for custom DSA goals.
- Add date navigation so users can inspect or edit past days.
- Separate the static plan roadmap into its own source or repository if it grows.
- Add a backup/export feature for local data.

## Best Mental Model for ChatGPT

Think of this project as a compact native Android study habit tracker. It is not a generic to-do app and not a large production platform. The core product idea is:

> Help the user complete a small daily interview-prep routine, record how the day felt, and see momentum over time.

When suggesting code changes, keep the app simple and consistent with the existing architecture. Prefer small, direct changes over broad refactors. If a change touches persistence, be careful with Room schema versioning. If a change touches UI, preserve the warm Compose Material 3 style and the three-tab navigation model.
