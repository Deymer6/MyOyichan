# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project location

The Gradle project root is the **`Projects/`** subdirectory, not the repository root. All Gradle commands must be run from `Projects/`. The Android module is `:app` (`Projects/app/`).

## Build & test commands

Run from `Projects/`. On Windows use `.\gradlew.bat`; the examples below use the Unix wrapper.

```bash
./gradlew assembleDebug            # build debug APK
./gradlew installDebug             # build + install on a connected device/emulator
./gradlew clean                    # clean build outputs
./gradlew lint                     # Android lint
./gradlew test                     # JVM unit tests (src/test)
./gradlew connectedAndroidTest     # instrumented tests (needs device/emulator, src/androidTest)
```

Run a single unit test class/method:

```bash
./gradlew testDebugUnitTest --tests "com.upn.myoyichan.SomeTest"
./gradlew testDebugUnitTest --tests "com.upn.myoyichan.SomeTest.someMethod"
```

Notes:
- The Gradle **wrapper auto-downloads Gradle** (8.10.2) — never install Gradle manually. `gradle.properties`' `org.gradle.java.home` was pinned to a non-existent `D:` path (from another machine) and is now **commented out**, so Gradle uses `JAVA_HOME` (on this machine: `C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot`). See the `build-run-setup` memory for device/SDK details.
- Toolchain is JDK 21; `compileOptions`/`kotlinOptions` target JVM 17. minSdk 26, target/compile SDK 35.
- The only existing test (`ExampleInstrumentedTest`) is under package `com.example.myoyichan` while app code is `com.upn.myoyichan` — a leftover scaffold, not a real suite.

## Architecture

Single-module Android app (Kotlin, View-based UI with **ViewBinding**, no Jetpack Compose). MVVM with a Repository layer. **No DI framework** — dependencies are wired manually.

Layers, all under `Projects/app/src/main/java/com/upn/myoyichan/`:

- **`data/local/`** — Room. `AppDatabase` (singleton, version 1) with entities `Usuario`, `Medicamento`, `SignoVital`, `ContactoSos` and matching DAOs. The DB is **encrypted with SQLCipher** using a hardcoded passphrase in `AppDatabase.kt`, and uses `fallbackToDestructiveMigration()` — any schema change wipes the DB rather than migrating.
- **`data/remote/`** — Retrofit client (`RetrofitClient`, base URL `https://api.fda.gov/`) for the OpenFDA drug-label API (`OpenFdaApiService`).
- **`repository/`** — thin wrappers over DAOs; expose `LiveData` for queries and `suspend` functions for writes.
- **`ui/`** — feature packages (`auth`, `main`, `home`, `medicamentos`, `vitales`, `contactos`, `recordatorios`, `profile`, `settings`), each with Activities/Fragments, an adapter, and a `ViewModel`.
- **`utils/`** — cross-cutting managers (see below).

### Key cross-cutting patterns

- **Manual DI via `ui/ViewModelFactory`.** One factory takes all four repositories as nullable constructor args. Each Fragment/Activity builds its own repository from `AppDatabase.getInstance(context).xxxDao()`, passes only the repo it needs to `ViewModelFactory`, and resolves the ViewModel. There is no application-level graph; follow this per-screen wiring when adding a screen.
- **User scoping via `SessionManager`** (`utils/SessionManager`, SharedPreferences `myoyichan_session`). It stores the logged-in `usuarioId`; nearly every query is filtered by it (`getByUsuarioId`). There are no real foreign-key constraints — `usuarioId` is just an `Int` column.
- **App settings via `PreferencesManager`** (`utils/PreferencesManager`, SharedPreferences `myoyichan_settings`, separate from the session). Holds voice speed/pitch, voice-assistant toggle, notifications toggle, and font-size index. Configured in `SettingsActivity`; applied by `VoiceManager` (reads speed/pitch on init), `MainActivity` (hides the voice FAB when disabled), and `AlarmReceiver` (skips notifications when disabled).
- **All Activities extend `ui/BaseActivity`, not `AppCompatActivity`.** `BaseActivity.attachBaseContext` applies the saved font-size scale globally. When adding an Activity, extend `BaseActivity` so the font setting takes effect there too.
- **Auth.** `LoginActivity` is the launcher activity (see `AndroidManifest.xml`). Passwords are hashed with unsalted SHA-256 in `utils/PasswordUtils`. If `SessionManager.isLoggedIn()` is false, `MainActivity` redirects to login.
- **Navigation is hybrid.** `MainActivity` hosts a `NavHostFragment` + bottom nav over `res/navigation/nav_graph.xml` with exactly 4 fragment destinations (home, medicamentos, vitales, contactos). Everything else — auth, forms, profile, settings — is a **separate Activity launched via `Intent`**, passing data as primitive extras (e.g. `"MEDICAMENTO_ID"`, `"nombre_medicamento"`, `"tipo_vital"`).
- **Reminders reuse the medication model.** The `recordatorios` feature has no entity/repository of its own; `RecordatorioViewModel` wraps `MedicamentoRepository` and toggles the `activo` flag on `MedicamentoEntity`. Scheduled notifications fire through `utils/AlarmReceiver` (a `BroadcastReceiver` driven by `AlarmManager` exact alarms).

### Domain-specific features

- **Voice assistant** (`utils/VoiceManager` + `MainActivity.handleVoiceCommand`). Wraps Android `SpeechRecognizer` and `TextToSpeech` in Spanish (`es-PE`). Commands are accent-normalized and matched against Spanish regex patterns to navigate, open forms, or trigger an emergency call. Add new commands as `when` branches in `handleVoiceCommand`.
- **Medication info lookup** (`ui/medicamentos/InfoMedicamentoActivity`). Queries OpenFDA with fallback search fields (brand → generic → active ingredient), then translates the English result to Spanish **on-device** via `utils/TranslatorManager` (Google ML Kit), running the field translations in parallel with `async`. The translation is wrapped in **`withTimeoutOrNull(10s)` with an English fallback** — on a device without Google Play Services (e.g. Huawei) or a slow model download, it shows the original English instead of hanging. `TranslatorManager` rethrows `CancellationException` so the timeout cancels cleanly; the ML Kit model (~30 MB) downloads on first use (any network).
- **SOS emergency call.** `MainActivity.llamarEmergencia()` looks up the principal `ContactoSos` for the user and dials it via `Intent.ACTION_CALL` (needs `CALL_PHONE` permission).

## Conventions

- **The codebase is in Spanish** — class names, entity/column names, comments, and all user-facing strings (e.g. `MedicamentoEntity`, `ContactoSosRepository`, `frecuencia`, `horario`). Match this when adding code.
- **Fragments** use the nullable `_binding` / `binding` ViewBinding pattern and null it out in `onDestroyView()`.
- **ViewModels** expose query results as `LiveData` and report write outcomes via a `MutableLiveData<Result<Unit>>` (`operationResult`); writes run in `viewModelScope.launch` with try/catch.
- Sensitive values are currently hardcoded (DB passphrase in `AppDatabase.kt`) and `usesCleartextTraffic="true"` is set in the manifest — be aware when touching the data/network layers.
- **Form/text colors (gotcha):** Material3's defaults render `TextInputEditText` input text in purple and filled-button text/icons in indigo. Set colors explicitly: `android:textColor="@color/text_primary"` on each `TextInputEditText` (the `materialThemeOverlay` alone does **not** darken input text); the `Widget.MyOyichan.Button.*` styles force white text/`iconTint` (filled) or green (outlined). Spinners that need dark text use a custom `ArrayAdapter` (see `configurarSpinnerEnfermedad()` in `RegisterActivity`/`ProfileActivity`) instead of `android:entries`.

## Recent changes & open work (session 2026-06-24 → 06-25)

**Done this session:**
- **Settings screen implemented.** `SettingsActivity` now persists & applies voice speed/pitch (with a live `VoiceManager` preview), the voice-assistant toggle, notifications toggle, and font size. Added `utils/PreferencesManager` + `ui/BaseActivity`; all 10 Activities migrated to extend `BaseActivity` (global font scaling).
- **Legibility fixes** (purple/low-contrast text): explicit dark `android:textColor` on every form `TextInputEditText` (register, profile, contacto, vital, medicamento); white button text/icons in `themes.xml`; dark-text custom spinner adapter for `Enfermedad` in register/profile.
- **INFO FDA robustness:** translation timeout + English fallback (see Medication info lookup above).

Verified on a physical **Huawei JNY-LX2** (Android 10, no Google services). The app installs/runs; ML Kit translation falls back to English and `SpeechRecognizer` voice input is unavailable there (TTS works).

**Open / pending (pick up next):**
- Make the text-field **cursor green** app-wide (`colorControlActivated` → `@color/primary` in `Base.Theme.MyOyichan`) — still slightly purple.
- Apply the **dark-text spinner adapter** to the remaining spinners: `Relación` (contacto), `Tipo` (vital), `Frecuencia` (medicamento) — they still show light text.
- Cosmetic: OpenFDA English text still shows raw field prefixes ("Purpose", "WARNINGS") in `limpiarYFormatearTexto`.
