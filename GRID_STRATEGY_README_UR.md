# Grid Basket Strategy (Demo-first)

## Implemented decision rules

- First position: 0.01 lot.
- If the current 0.01 position's net P/L reaches +$1, the strategy emits `CloseAll`; after the broker confirms all positions are closed, call `onBasketClosed()` and the next cycle begins at 0.01.
- If price moves adversely by one configured grid step, add 0.02.
- If it moves another one step adversely, add 0.03.
- If it moves two further steps adversely from the 0.03 entry, add 0.05.
- At the 0.02 or 0.03 stage, close the basket when the combined net P/L reaches +$1.
- At the 0.05 stage, close the basket when combined net P/L reaches +$3.
- If the combined net P/L of all positions managed by this strategy reaches -$50, emit close-all and halt. A manual reset is required.
- P/L must be net of commission, swap and fees. BUY/SELL must use separate strategy instances/baskets if they are to run independently. The emergency stop should be account-wide across both directions in the production adapter.

## Grid distances

- XAUUSD: one step = $1.00 price movement; final 0.03-to-0.05 gap = $2.00.
- Forex: one step = 10 pips; final gap = 20 pips. The broker adapter must convert pips to price using the symbol's digits and contract specification.

## Important execution limitation

`GridBasketStrategy` is a deterministic decision core. It returns `Open`, `CloseAll`, or `Halt` actions and contains no broker networking. The old random BUY/SELL and random P/L simulator has been removed. The app's Start action is deliberately blocked until a verified MT5 execution adapter is integrated. The existing broker/VPS socket checks are NOT MT5 authentication and cannot place orders. Do not enter a live account password into this build or use it for live trading.

To complete live/demo execution, a real MT5-compatible bridge/service is still required to provide authenticated account state, quotes, order placement/close confirmation, position tickets, contract sizing and net P/L. The Android app alone cannot connect to arbitrary MT5 retail accounts through the generic host/port test in this project.

## Current build status

This ZIP contains the Android project and the strategy decision core, not a finished tradable APK. No real broker quotes are currently connected; the old fabricated random quote movement was removed. The initial prices displayed in the symbol list are placeholders only. The project has no Gradle wrapper script in the supplied ZIP, so build it by opening the folder in Android Studio/AIDE with compatible Gradle and Android SDK installed. The Kotlin strategy source was syntax-checked with the Kotlin compiler; a full Android Gradle build could not be run in this environment.


## MetaApi bridge scaffold added

`mt5-bridge/` mein Node.js server-side adapter diya gaya hai aur Android mein `MetaApiBridgeClient.kt` hai. Yeh account provisioning/deploy, account info, positions, market order aur individual position close ke endpoints provide karta hai. MetaApi token sirf server `.env` mein rakhein. Yeh abhi complete automated strategy loop nahi hai; quotes, basket P/L aggregation, BUY/SELL basket state, retry/idempotency aur close-all confirmations integrate/test hone tak Start ko live ke liye unlock na karein.
