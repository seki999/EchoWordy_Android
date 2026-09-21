# EchoWordy

**EchoWordy** is a fast, offline-first Android app for memorizing English vocabulary. Each card
is shown for a short, configurable duration (2.0 seconds by default) and its English word is
pronounced automatically in American English the instant the card appears — no waiting for
speech to finish before the timer starts.

## Overview

You provide the complete content of every vocabulary card (word, IPA, translations, example
sentences — whatever you like). EchoWordy never looks anything up: there is no dictionary API,
translation API, or LLM involved. It only stores, parses, displays, and pronounces the cards you
give it. Everything lives in a local Room database; the app has no network permission and works
entirely offline.

## Main Features

- **Vocabulary lists** — create, rename, delete, and browse any number of lists.
- **Bulk paste import** — paste many complete cards at once, separated by blank lines.
- **Rapid review** — cards auto-advance every N seconds (configurable), with the English word
  pronounced the moment each card appears.
- **Pause / Resume** — pause mid-review without losing your place; the current card stays put.
- **Previous / Next** — step through cards manually, forward or backward.
- **Stop** — end a session early with a confirmation dialog; progress marked so far is kept.
- **Mark as Unknown** — flag difficult cards during a session (most naturally while paused).
- **Automatic "Unknown Words" lists** — finishing or stopping a review with marked cards
  generates a new list containing the complete original cards, in their original order, with a
  clean, non-duplicating name (`List - Unknown Words`, `List - Unknown Words 2`, …).
- **Settings** — configurable card duration and American English voice selection.

## Screens

1. **Home** — list of vocabulary lists with card counts; Add List.
2. **List Detail** — list name, card count, numbered card list; Start Review, Edit Cards,
   Rename, Delete.
3. **Paste Cards** — large text editor for bulk-pasting/editing a list's cards, with a live
   "Detected cards: N" count.
4. **Review** — one card at a time, large word display, auto-advance, Previous / Pause-Resume /
   Next / Stop, Mark as Unknown.
5. **Review Complete / Review Stopped** — session summary, with a link to the generated Unknown
   Words list if one was created.
6. **Settings** — card duration and American English voice.

## Technology Stack

- Kotlin
- Jetpack Compose + Material 3
- MVVM (ViewModel + `StateFlow`)
- Room (local persistence)
- Android `TextToSpeech` (offline, American English)
- Kotlin Coroutines
- Navigation Compose
- DataStore Preferences (settings)

No dependency injection framework is used — the app is small enough that a single hand-written
`EchoWordyApplication` dependency container (see `EchoWordyApplication.kt`) is simpler than
adding Hilt/Koin ceremony.

## Architecture

Clean-ish MVVM, split into three layers:

- **`data`** — Room entities/DAOs/database, DataStore-backed settings, and the repository
  implementation that maps entities to domain models.
- **`domain`** — plain Kotlin models, repository interfaces, and pure use cases (`CardParser`,
  `UnknownListNameGenerator`) with zero Android dependencies, so they're trivial to unit test.
- **`ui`** — one package per screen, each with a `ViewModel` (owns a `StateFlow<UiState>` and all
  business logic) and a stateless `@Composable` screen that renders it.
- **`tts`** — `TtsController` interface wrapping `android.speech.tts.TextToSpeech`, with a real
  Android implementation and a fake used in tests.

### Project Structure

```
app/src/main/java/com/seki999/echowordy/
├── EchoWordyApplication.kt        # manual DI container
├── MainActivity.kt
├── data/
│   ├── local/                     # Room entities, DAOs, AppDatabase, DataStore settings
│   └── repository/                # VocabularyRepositoryImpl
├── domain/
│   ├── model/                     # VocabularyList, VocabularyCard, ...
│   ├── repository/                # VocabularyRepository, SettingsRepository interfaces
│   └── usecase/                   # CardParser, UnknownListNameGenerator
├── tts/                           # TtsController, AndroidTtsController
└── ui/
    ├── navigation/                # NavHost, routes
    ├── theme/                     # Material 3 theme
    ├── home/
    ├── listdetail/
    ├── edit/                      # Paste Cards screen
    ├── review/
    └── settings/
```

## Requirements

- **Android Studio**: Koala (2024.1) or newer
- **JDK**: 17
- **Gradle**: 8.9 (via the included wrapper)
- **Android Gradle Plugin**: 8.5.2
- **Kotlin**: 2.0.20
- **Minimum Android SDK**: 26 (Android 8.0)
- **Target / Compile SDK**: 35

## Build Instructions

```bash
./gradlew assembleDebug
```

## Run Instructions

Open the project in Android Studio and run the `app` configuration on a device or emulator
running Android 8.0 (API 26) or later, or from the command line:

```bash
./gradlew installDebug
```

Android 8.0+ ships with a Google TTS engine that supports American English out of the box, so no
extra setup is normally required. If a device has no American English voice installed, EchoWordy
keeps working — cards still display and auto-advance — and shows a small non-blocking warning
instead of pronouncing words.

## Card Import Format

Paste one or more complete cards into the "Paste Cards" screen. Cards are separated by one or
more blank lines. Within a card, the **first non-empty line is the word** (the only part that is
pronounced); every other line is the body, shown exactly as typed.

### Example Card

```
abstain
英式 /əbˈsteɪn/
美式 /əbˈsteɪn/
意思 : 戒除；避免（尤指酒、食物等）
abstain from alcohol 戒酒
```

Pasting two cards at once:

```
abstain
英式 /əbˈsteɪn/
美式 /əbˈsteɪn/
意思 : 戒除；避免（尤指酒、食物等）
abstain from alcohol 戒酒

inaugural
英式 /ɪˈnɔːɡjərəl/
美式 /ɪˈnɔːɡjərəl/
意思 : 就职的；首次的
inaugural address 就职演说
```

The parser is tolerant of extra/trailing blank lines, Windows (`\r\n`) and Unix (`\n`) line
endings, and whitespace-only "blank" lines, and preserves Unicode (Chinese, Japanese, IPA
symbols, punctuation) exactly as entered.

## Review Behavior

- Starting a review shows the first card and speaks its word immediately; the card then stays
  on screen for the configured duration (**pronunciation happens inside that window, not after
  it** — the timer and the speech both start the moment the card appears).
- After the duration elapses, the app automatically advances to the next card and repeats.
- **Pause** freezes the current card and stops the timer without ending the session.
- **Resume** continues from exactly where you left off.
- **Previous / Next** jump cards manually; while paused, they move to the new card without
  restarting auto-play (you must press Resume for that).
- **Stop** asks for confirmation, then ends the session immediately, preserving any cards marked
  Unknown.

## Unknown Words Feature

While reviewing (most naturally while paused), tap **Mark as Unknown** to flag the current card;
tap **Remove from Unknown** to unflag it. Marking is idempotent — a card can only be marked once,
even if you revisit it. When a review ends (either normally or via Stop) with at least one
marked card, EchoWordy creates a new list containing the **complete original cards** (not just
the words) in their original order. The new list's name is generated to avoid collisions and
never doubles up:

- `TOEIC 01` → `TOEIC 01 - Unknown Words`
- again → `TOEIC 01 - Unknown Words 2`, then `TOEIC 01 - Unknown Words 3`, ...
- generating again *from* an Unknown Words list continues the same sequence rather than
  appending another suffix (`TOEIC 01 - Unknown Words` → `TOEIC 01 - Unknown Words 2`, never
  `... - Unknown Words - Unknown Words`).

Generated lists behave exactly like any other list: open, review, rename, delete, edit cards, or
generate another Unknown Words list from them.

## TextToSpeech Behavior

EchoWordy uses Android's built-in `TextToSpeech` engine with `Locale.US`, initialized once and
reused for the app's whole lifetime (see `AndroidTtsController`). Every new card speaks with
`QUEUE_FLUSH`, so switching cards immediately cuts off any speech still in progress instead of
queuing it up. If the engine fails to initialize or American English isn't available, the app
falls back to silent card display with a one-line warning, instead of crashing.

## Offline Usage

EchoWordy requests no `INTERNET` permission and makes no network calls. All lists, cards, and
settings are stored locally (Room + DataStore) and persist across app restarts.

## Known Limitations

- Review session state is not restored after process death (e.g. the OS killing the app in the
  background mid-review); restarting the review is required. Everyday configuration changes
  (like rotation, though the app is portrait-locked) do not lose state.
- Per-card editing (reordering, editing a single card in place) is not implemented; editing a
  list means re-pasting its full content, which replaces all of its cards.
- Voice selection lists whatever American English voices the device's TTS engine reports; on
  devices with only one such voice, the Settings screen simply shows "System Default".
