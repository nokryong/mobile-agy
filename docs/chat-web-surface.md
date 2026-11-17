# Visible Chat surface

Chat opens `ChatWebActivity`, a visible official ChatGPT WebView. `FLAG_ACTIVITY_REORDER_TO_FRONT` retains both this Activity and MainActivity when switching. Ordinary Chat uses the official composer, model picker and history. Mobile Codex does not send ordinary Chat messages or copy answers into its Codex UI. The earlier hidden transport, request observer, server requery, local transcript renderer, and standalone requirements probe have been removed. Existing on-device local transcript data is left untouched but is no longer read by the app.

Only `chat-web-custom.js` runs on exact `https://chatgpt.com` pages to add a compact sidebar mode switch. Mobile-agy removes the character renderer and its asset routes. The page's colors, sidebar scrolling, sections and model controls are unchanged. Authentication pages receive neither script. There is no Android JavaScript interface on the remote WebView. The one intercepted `mobilecodex://mode/codex` main-frame link only reopens MainActivity. External HTTPS links use the system handler. System insets are handled once by SafeWebViewLayout.


Android Back first closes a detected sidebar, then navigates web history or returns to Codex. A native loading indicator appears during navigation; Codex and reload actions appear only on a main-page load error. Android Back remains a return path if the web sidebar structure changes. System file selection is connected. Microphone/camera capture and other browser-dependent features are not claimed as verified.

Verification: jsdom fixtures cover switch injection, SPA replacement, no changes to web composer/history, non-ChatGPT origin rejection and repeated injection. Android unit tests cover URL boundaries. CI runs packaged layout checks and build/lint. These tests do not constitute a live signed-in ChatGPT WebView test. Phone control was disabled during implementation.
