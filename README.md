# DAFTAR
**Offline-first student study planner — Arabic RTL**

DAFTAR is a privacy-first study organizer. Study records stay in the browser's local IndexedDB; the app does not send them to a server.

## Current features
- Organize subjects and lectures.
- Track completed lectures and overall progress.
- Add notes to lectures.
- Persistent light/dark appearance.
- Local document library for PDF, DOCX, and PPTX with in-app preview.
- Automatic local persistence using IndexedDB.
- JSON backup export and restore with validation and confirmation.
- One-time migration attempt from the earlier `daftar-v1` localStorage key; the old key is not deleted.
- Installable PWA shell with a service worker and offline cache.

## Data safety
1. Export a JSON backup regularly and before changing devices or clearing browser/app data.
2. Keep the backup outside the device (for example, Drive or a computer).
3. Restore from the Import button.
4. Do not uninstall/clear app data until you have a verified backup.

The GitHub repository contains application source only. It does not contain any user's private study data.

## Run locally
Serve this directory over HTTP (service workers do not work from `file://`):
```bash
python3 -m http.server 8080
```
Open http://localhost:8080.

## Deploy
GitHub Actions deploys the site to GitHub Pages when enabled for this repository (Settings → Pages → Build and deployment → GitHub Actions).

## Update and Android installation
The PWA updates its cached shell when a new service-worker version is deployed; users should reopen the app while online to receive it. Imported documents are stored in a separate local IndexedDB store and are not included in the JSON study-data backup. Export or copy important documents separately. PDF pages are rendered by Android's native PdfRenderer. DOCX and PPTX parsing uses a locally bundled JSZip library, so preview works offline. PPTX preview currently extracts slide text and does not reproduce all original slide layouts, charts, or animations. Legacy .doc and .ppt files are not supported. This web app is not yet a signed Android APK. APK upgrades require a persistent Android application ID and the same release signing key for every release. Never rotate or regenerate that key between releases. Do not distribute a debug-signed APK as a production update.

## Privacy
All study data is stored locally in the user's browser profile. Clearing site data, uninstalling an app, or losing the device can erase it; backups are therefore essential.

## Signed Android update setup

The release APK workflow intentionally stops if the persistent signing credentials are missing. Configure these four **GitHub Actions repository secrets** under **Settings → Secrets and variables → Actions**:

- `DAFTAR_KEYSTORE_BASE64` — the existing release keystore encoded as Base64
- `DAFTAR_STORE_PASSWORD` — keystore password
- `DAFTAR_KEY_ALIAS` — alias of the existing release key
- `DAFTAR_KEY_PASSWORD` — password for that key

**Important:** use the exact keystore and alias that signed the APK already installed on users' devices. Creating a new key will prevent Android from installing the new APK as an update over an app signed with the old key. Keep an encrypted backup of the keystore outside GitHub; never commit it to this repository.

The unsigned/debug build is for testing only and is not a safe substitute for an update-compatible release APK.


## DAFTAR 1.5.0 development
- Tasks and assignments with due dates, priorities, and completion tracking.
- Weekly lecture timetable with day, time, subject, and location.
- Exam calendar with countdowns and upcoming exam overview.
- Full JSON backup now embeds local document files (PDF, DOCX, PPTX); backup files can be large, up to the app's import limit.
- Database schema migrated to version 5 while preserving existing subjects, lectures, sessions, and document records.

- Focus/break timer (25/5), persisted across app restarts, with per-subject study-time analytics.
- Gradebook with assessment records and subject averages.
- Manual flashcards with active recall, reveal-answer flow and mastery counters.
- Global search across study records and indexed DOCX/PPTX text.
- Native Android share sheet for subject summaries and lecture notes.
- Paginated PDF viewer with last-page persistence, page bookmarks and per-page notes; PDF attachments are included in backups and can be rendered after restore.
- Android WorkManager reminders for task deadlines, exams, class sessions and timer completion. WorkManager timing is battery-optimized and may not be exact to the minute; Android 13+ notification permission is required.

- Local recovery history increased to 100 snapshots (previously 30).
- Tasks support subtasks and daily, weekly or monthly recurrence; recurring items are generated when the current task is marked complete.
- Grade target calculator estimates the score needed across remaining marks to reach a chosen percentage.
