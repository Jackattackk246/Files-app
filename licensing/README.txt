# ============================================================================
# 🔒 ARCHITECT NOTICE & LICENSING CHARTER
# Project: Files v2.4.6
# Author / Architect: Jack Lawton | Repository: Jackattackk246/Files
# ============================================================================

TERMS OF OPEN-SOURCE REUSE & CREATIVITY:
1. If you are here to upgrade the app feel free to, but if you are just copy and pasting don't bother.
2. Feel free to use the security system in another app, feel free to just credit.
3. NOTE: This application contains an automated Single-Line Creativity Pass. Direct clones or cosmetic name/icon swaps with zero code/layout modifications will trigger an immediate UI freeze, displaying: "Lacking creativity. You don't get the app."

HOW TO DECOUPLE THE VERIFICATION HOOKS LEGITIMATELY:
- A legitimate developer customizing or extending the codebase can safely comment out or delete the 'verifyCreativityPass()' call inside MainActivity.kt.
- All core security package classes (com.example.security.DeveloperSecurityEngine) are cleanly decoupled from the main file explorer execution loop.
- Offline sandboxed local storage, local query engine, and custom themes will continue operating at 100% offline stability.
