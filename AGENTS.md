# Vaani — Agent Summary

## Goal
Replace the rule-based TranslateEngine with NLLB-200-distilled-600M AI model via ONNX Runtime on Android, keeping TranslateEngine as fallback, while building out the full product.

## Constraints & Preferences
- Must run entirely offline on Android (no internet/cloud API calls)
- Must support Hinglish → English only
- Must work on old Android phones (minSdk 21, 2 GB RAM)
- ONNX Runtime kept as inference engine (not switching to TFLite)
- Model download on first launch via HuggingFace (~868 MB), not bundled in APK
- Keep GrammarEngine.kt for post-processing and TranslateEngine.kt as fallback
- Contribution workflow: issues → branches → commits → PRs → code reviews on GitHub; avoid forced equal-priority patterns

## Progress

### Sprint 1 (AI Verification & Hardening) — 7 issues merged
- Issue #85: Removed stale ProGuard rules for non-existent `ModelLoader` and `TranslationPipeline` classes
- Issue #80: Auto-detect ONNX tensor names via `OrtSession.getInputName()/getOutputName()` at load time; fuzzy fallback matching; KV-cache `present.X` → `past_key_values.X` mapping; `scripts/inspect_onnx.py`
- Issue #84: Detailed inference timing logs (tokenizer, encoder, per-step decoder, total); `Timing` helper using `System.nanoTime()`
- Issue #82: SHA256 integrity verification in `ModelManager.verifyModels()`; streaming 8KB buffer; `ModelFile.expectedSha256`; new `Callback.onVerify()`; corrupt files auto-delete
- Issue #83: Download resilience — 3 retry attempts with exponential backoff (2s, 4s, 8s); per-file progress with KB/s speed; `onOverallProgress()`; `onVerify(fileName)`
- Issue #81: DJL SentencePiece JNI fallback → pure-Kotlin `KotlinSentencePiece` (reads `.bpe.model` protobuf directly, varint field parser, greedy longest-prefix encoding)
- Issue #86: `NllbTranslatorTest.kt` — 6 end-to-end AI translation tests with `Assume.assumeTrue()` guard

### Sprint 2 (Testing Infrastructure) — 4 file PRs merged + 2 direct commits
- Direct commit: `testOptions { unitTests.isReturnDefaultValues = true; isIncludeAndroidResources = true }` in `app/build.gradle.kts`
- Direct commit: CI unit test runner + test results artifact upload in `.github/workflows/android-ci.yml`
- **TranslateEngineTest.kt** (PR #94): 30+ test cases covering phrasebook categories, regex templates, word-by-word fallback, edge cases, GrammarEngine integration
- **GrammarEngineTest.kt** (PR #95): All 8 cleanup passes tested (whitespace, contractions, S-V agreement, spelling, articles, redundancy, tense, capitalization)
- **TransliteratorTest.kt** (PR #96): Vowels, consonants, matras, full words, halant, mixed alphanumeric, case insensitivity
- **NllbTokenizerTest.kt** (PR #97): Constants, error handling, synthetic `.bpe.model` binary for roundtrip tests

### Sprint 3 (Translation Quality) — 3 PRs merged
- **Phrasebook expansion** (Issue #98, PR #101): 250+ new Hinglish→English entries across food/restaurant (ordering, dishes, cooking), travel (hotel booking, tickets, auto/taxi), tech/social media (WhatsApp status, Instagram, YouTube, apps), celebrations (30+ festival greetings including regional), relationships (boyfriend, fiancé), weather (humidity, hail, rainbow, storm, umbrella), shopping (try-on, UPI/GPay, QR codes, refunds, sales), emotions (excited, nervous, jealous, grateful), health (X-ray, blood test, sprain, fracture), time (clock times, durations). Fixed leading space bug in `hawa chal rahi hai`.
- **Regex template fixes** (Issue #99, PR #102): Moved generic `kya aap/tum (.+)` patterns to end of template list so specific patterns (`sakte hain`, `sakti hain`) match first. Added 10+ new templates: past-tense questions (`kya aapne/tumne`), "let's" patterns (`chalo/chale`), present perfect (`ho gaya hai`), "have done" (`kar liya/li`), obligation (`karna hai`), refusal (`nahi karna`), and specific `kya aapne` + verb patterns (liya/kiya/dekha/suna).
- **Transliterator improvements** (Issue #100, PR #103): Added ऋ vowel (ri/r̥) and ॠ (ru) to lookup. Extended matraMap with `ri`→`ृ` for consonant+ri matra formation. Added conjuncts: dwa, dnya, dha, tta, hna, hya, hva, mna. Added 15+ word-specific entries with proper conjuncts (kripya→कृपया, swagat→स्वागत, prayas→प्रयास, vidyalay→विद्यालय, etc.).

### Code Audit (PR #104) — 5 files fixed
- **NllbTranslator**: CRITICAL tensor shape bugs (input_ids [N]→[1,N], decoder scalar→[1,1]), OrtSession.Result leak (64 per translation), encoder exception leak, TOCTOU race on isLoaded()
- **TranslateEngine**: kya aapne template ordering fixed (specific before generic), duplicate phrasebook entries removed
- **GrammarEngine**: Pronunciation-aware article fix (a university → NOT an university), non-BMP safe capitalizeFirst, case-insensitive 'a an' cleanup
- **Transliterator**: Fixed 'a' in matraMap causing wrong long-ā, trailing halant on final consonants
- **ModelManager**: MAX_RETRIES 3→4 for correct 3-retry with 2s/4s/8s, @Volatile cancelled flag, file delete logging

### Sprint 4 (Hardening & UX) — 9 files changed
- **NllbTranslator.kt**: Removed `logits.close()` (ONNX Runtime Java anti-pattern causing premature native deallocation). Wrapped inference with `withTimeout(30s)` catching `TimeoutCancellationException`. Added `MAX_INPUT_CHARS=512` guard against OOM on 2GB devices.
- **NllbTokenizer.kt**: Added `@Synchronized` to `encode()` and `decode()` for thread safety on concurrent `translate()` calls.
- **ModelManager.kt**: Added `Context` constructor param. Added network check (`ConnectivityManager`) and storage check (`freeSpace >= 1GB`) before starting download. Fixed speed formula from `totalBytes/elapsed` to `(totalBytes*1000)/(elapsed*1024)` for correct KB/s reporting.
- **Prefs.kt**: Added `modelVersion: Int` — incremented on each successful download so keyboard service can detect model refresh.
- **VaaniKeyboardService.kt**: AI preview in `updatePreview()` — rule-based shown instantly, then AI result replaces it when ready (stale input discarded). `warmUpTranslator()` changed to `retryWarmUp()` with 3 attempts × 5s delay. Re-init check in `onStartInput()` — if model downloaded but `nllbTranslator==null`, retry warm-up. `incrementTranslationCount()` moved inside success branch only. `nllbTokenizer` set to null in `onDestroy()`.
- **DashboardActivity.kt**: Passes `this` to ModelManager constructor. Uses `model_download_success` string on completion. Increments `modelVersion` on success.
- **SettingsActivity.kt**: Same changes as DashboardActivity.
- **AGENTS.md**: Updated with Sprint 4 summary.
- No changes to layouts or strings.xml (existing `model_download_success` string now used).

## Blocked
- Real-device verification not possible in this environment — needs APK build + Android device/emulator
- ONNX tensor name auto-detection and DJL fallback both need runtime verification with actual model files
- SHA256 hashes will be filled in after downloading model files on real device
- No Java runtime in this environment — cannot run unit tests

## Key Decisions
- **Natural contribution workflow**: Direct commits for config/infrastructure; issues + PRs + reviews for code changes; not forcing equal priority on every issue
- **Protobuf parser for SentencePiece fallback**: `KotlinSentencePiece` reads SentencePiece binary `.model` file using field-number-based protobuf parsing (field 1 = pieces, wire type 2 = length-delimited strings)
- **Tensor name auto-detection**: `NllbTranslator.load()` inspects `OrtSession.getInputName(i)` / `getOutputName(i)` at load time; `mapName()` uses exact match → fuzzy substring → first available; KV-cache mapping via `present.X` → `past_key_values.X` suffix matching
- **Sprint ordering**: Sprint 1 (AI hardening) → Sprint 2 (testing) → Sprint 3 (quality) → Code audit → Sprint 4 (hardening)
- **Test infrastructure**: JUnit 4 + Kotlin Coroutines Test; Espresso for optional instrumentation; GitHub Actions CI
- **withTimeout for blocking ONNX calls**: Timeout only fires at coroutine suspension points; blocking `session.run()` won't be interrupted but the pattern documents intent and catches any future non-blocking paths.
- **AI preview strategy**: Rule-based result shown immediately on keystroke; AI result replaces it asynchronously when ready. Stale results (input changed since) are discarded.

## Next Steps
1. Build APK and test on real device: verify ONNX tensor names, DJL JNI loading, inference latency, model download with retry, OOM handling on 2GB RAM devices
2. Fill in SHA256 hashes after downloading model files on device
3. Consider Sprint 5: device testing, bug fixes found in real usage, potential `withTimeout` → `ExecutorService` timeout improvement for truly interrupting hung ONNX calls

## Relevant Files
- `app/src/test/java/com/vaani/keyboard/util/TranslateEngineTest.kt`
- `app/src/test/java/com/vaani/keyboard/util/GrammarEngineTest.kt`
- `app/src/test/java/com/vaani/keyboard/util/TransliteratorTest.kt`
- `app/src/test/java/com/vaani/keyboard/util/NllbTokenizerTest.kt`
- `app/src/test/java/com/vaani/keyboard/util/NllbTranslatorTest.kt`
- `app/src/main/java/com/vaani/keyboard/util/NllbTranslator.kt`
- `app/src/main/java/com/vaani/keyboard/util/NllbTokenizer.kt`
- `app/src/main/java/com/vaani/keyboard/util/ModelManager.kt`
- `app/src/main/java/com/vaani/keyboard/util/TranslationResult.kt`
- `app/src/main/java/com/vaani/keyboard/ime/VaaniKeyboardService.kt`
- `app/src/main/java/com/vaani/keyboard/ui/DashboardActivity.kt`
- `app/src/main/java/com/vaani/keyboard/ui/SettingsActivity.kt`
- `app/src/main/java/com/vaani/keyboard/util/Prefs.kt`
- `scripts/inspect_onnx.py`
- `.github/workflows/android-ci.yml`
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
