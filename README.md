<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/a3bc2ebd-f27b-4492-8564-baa60959b15a

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## Grid strategy notes

See [GRID_STRATEGY_README_UR.md](GRID_STRATEGY_README_UR.md) for the requested 0.01 / 0.02 / 0.03 / 0.05 basket rules and the critical MT5 execution limitation. The unsafe random-trade simulator has been removed. The Android Start control is intentionally blocked until a genuine MT5 execution adapter is supplied and verified.


## MetaApi bridge integration scaffold

A server-side MetaApi bridge has been added under `mt5-bridge/`. The Android source includes `MetaApiBridgeClient.kt` for calling that bridge over HTTPS. The MetaApi secret stays on the server, not in the APK. This bridge can provision/deploy an MT5 account and issue/read individual orders/positions, but the grid strategy runner is NOT yet connected to its live quote/position stream. Do not enable live trading until the strategy runner, net P/L accounting, persistence, duplicate-order protection and close-all confirmation are completed and demo-tested. See `mt5-bridge/README_UR.md`.
