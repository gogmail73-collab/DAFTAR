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
