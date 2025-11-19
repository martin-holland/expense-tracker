# Voice-to-Expense Proof of Concept - Step-by-Step Guide

**Approach:** Start with Native Android SpeechRecognizer → Evaluate Results → Decide on AI Enhancement

**Status:** ✅ **Phase 1 & 2 COMPLETE** - Ready for Testing!

---

## 🎯 Goal

Build a working proof-of-concept to:

1. Test Android's native speech recognition quality
2. Evaluate what data we can extract with basic parsing
3. Determine if native solution is "good enough"
4. Make data-driven decision on whether AI enhancement is needed

---

## 📋 Implementation Phases

### ✅ Phase 1: Native Speech Recognition (COMPLETED)

- ✅ Integrate Android SpeechRecognizer
- ✅ Display transcribed text
- ✅ Collect real-world samples

### ✅ Phase 2: Basic Data Extraction (COMPLETED)

- ✅ Rule-based parsing for amount, currency, category
- ✅ Show extracted data in UI
- ✅ Test with various input patterns

### 📊 Phase 3: Evaluation & Testing (NEXT STEP - Days 5-7)

- ⏳ Test with 20+ real voice inputs
- ⏳ Measure accuracy rates
- ⏳ Document what works / what doesn't

### 🎯 Phase 4: Decision Point (Day 8)

- ⏳ Review results
- ⏳ Decide: Keep native or add AI enhancement

---

## 🚀 Phase 1: Native Speech Recognition ✅ COMPLETED

### ✅ Step 1.1: Add Required Permissions

**File:** `composeApp/src/androidMain/AndroidManifest.xml`

The RECORD_AUDIO permission already exists, but verify it's there:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

### ✅ Step 1.2: Create Speech Recognition Service

**File:** `composeApp/src/androidMain/kotlin/com/example/expensetracker/service/SpeechRecognizerService.android.kt`

```kotlin
package com.example.expensetracker.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.Locale

/**
 * Service for Android speech recognition using SpeechRecognizer API
 * This is a native, on-device solution (no API costs)
 */
class AndroidSpeechRecognizerService(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    // Channels for streaming results
    private val _partialResults = Channel<String>(Channel.BUFFERED)
    private val _finalResult = Channel<SpeechRecognitionResult>(Channel.BUFFERED)

    val partialResults: Flow<String> = _partialResults.receiveAsFlow()
    val finalResult: Flow<SpeechRecognitionResult> = _finalResult.receiveAsFlow()

    /**
     * Start speech recognition
     * @return true if started successfully
     */
    fun startListening(): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            println("❌ Speech recognition not available on this device")
            return false
        }

        if (isListening) {
            println("⚠️ Already listening")
            return false
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                // Get confidence scores
                putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true)
            }

            speechRecognizer?.startListening(intent)
            isListening = true
            println("🎤 Speech recognition started")
            return true
        } catch (e: Exception) {
            println("❌ Error starting speech recognition: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * Stop speech recognition
     */
    fun stopListening() {
        if (!isListening) {
            println("⚠️ Not currently listening")
            return
        }

        try {
            speechRecognizer?.stopListening()
            isListening = false
            println("⏹️ Speech recognition stopped")
        } catch (e: Exception) {
            println("❌ Error stopping speech recognition: ${e.message}")
        }
    }

    /**
     * Cancel speech recognition
     */
    fun cancel() {
        try {
            speechRecognizer?.cancel()
            isListening = false
            println("🚫 Speech recognition cancelled")
        } catch (e: Exception) {
            println("❌ Error cancelling speech recognition: ${e.message}")
        }
    }

    /**
     * Cleanup resources
     */
    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
            _partialResults.close()
            _finalResult.close()
            println("🗑️ Speech recognizer destroyed")
        } catch (e: Exception) {
            println("❌ Error destroying speech recognizer: ${e.message}")
        }
    }

    /**
     * Create recognition listener for handling speech events
     */
    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            println("✅ Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            println("🗣️ User started speaking")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Audio level changed - could use for visualization
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Raw audio buffer - not needed for our use case
        }

        override fun onEndOfSpeech() {
            println("🔇 User stopped speaking")
            isListening = false
        }

        override fun onError(error: Int) {
            val errorMessage = getErrorMessage(error)
            println("❌ Speech recognition error: $errorMessage")

            _finalResult.trySend(
                SpeechRecognitionResult.Error(errorMessage)
            )
            isListening = false
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val confidenceScores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

            if (matches != null && matches.isNotEmpty()) {
                val bestMatch = matches[0]
                val confidence = confidenceScores?.getOrNull(0) ?: 0f

                println("✅ Final result: \"$bestMatch\" (confidence: $confidence)")

                // Send all alternatives for analysis
                val alternatives = matches.mapIndexed { index, text ->
                    SpeechAlternative(
                        text = text,
                        confidence = confidenceScores?.getOrNull(index) ?: 0f
                    )
                }

                _finalResult.trySend(
                    SpeechRecognitionResult.Success(
                        text = bestMatch,
                        confidence = confidence,
                        alternatives = alternatives
                    )
                )
            } else {
                println("⚠️ No results received")
                _finalResult.trySend(
                    SpeechRecognitionResult.Error("No results")
                )
            }
            isListening = false
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION
            )
            if (matches != null && matches.isNotEmpty()) {
                val partialText = matches[0]
                println("📝 Partial result: \"$partialText\"")
                _partialResults.trySend(partialText)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Additional events - not needed for basic use
        }
    }

    /**
     * Convert error codes to human-readable messages
     */
    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error ($error)"
        }
    }
}

/**
 * Result from speech recognition
 */
sealed class SpeechRecognitionResult {
    data class Success(
        val text: String,
        val confidence: Float,
        val alternatives: List<SpeechAlternative>
    ) : SpeechRecognitionResult()

    data class Error(val message: String) : SpeechRecognitionResult()
}

/**
 * Alternative speech recognition result
 */
data class SpeechAlternative(
    val text: String,
    val confidence: Float
)
```

---

### ✅ Step 1.3: Update VoiceInputViewModel

**File:** `composeApp/src/commonMain/kotlin/com/example/expensetracker/viewmodel/VoiceInputViewModel.kt`

Add speech recognition capabilities to the existing ViewModel:

```kotlin
package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.service.SpeechRecognitionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceInputViewModel : ViewModel() {
    // ... existing code ...

    // NEW: Speech recognition state
    private val _speechRecognitionState = MutableStateFlow<SpeechRecognitionState>(
        SpeechRecognitionState.Idle
    )
    val speechRecognitionState: StateFlow<SpeechRecognitionState> =
        _speechRecognitionState.asStateFlow()

    private val _partialTranscription = MutableStateFlow("")
    val partialTranscription: StateFlow<String> = _partialTranscription.asStateFlow()

    sealed class SpeechRecognitionState {
        object Idle : SpeechRecognitionState()
        object Listening : SpeechRecognitionState()
        data class Success(
            val transcription: String,
            val confidence: Float,
            val alternatives: List<String>
        ) : SpeechRecognitionState()
        data class Error(val message: String) : SpeechRecognitionState()
    }

    /**
     * Start speech recognition (NEW)
     */
    fun startSpeechRecognition() {
        viewModelScope.launch {
            _speechRecognitionState.value = SpeechRecognitionState.Listening
            _partialTranscription.value = ""

            // Platform-specific implementation will be called here
            // See next step for Android implementation
        }
    }

    /**
     * Stop speech recognition (NEW)
     */
    fun stopSpeechRecognition() {
        // Platform-specific implementation
    }

    /**
     * Handle partial transcription updates (NEW)
     */
    fun onPartialTranscription(text: String) {
        _partialTranscription.value = text
    }

    /**
     * Handle final transcription result (NEW)
     */
    fun onSpeechResult(result: SpeechRecognitionResult) {
        when (result) {
            is SpeechRecognitionResult.Success -> {
                _speechRecognitionState.value = SpeechRecognitionState.Success(
                    transcription = result.text,
                    confidence = result.confidence,
                    alternatives = result.alternatives.map { it.text }
                )
            }
            is SpeechRecognitionResult.Error -> {
                _speechRecognitionState.value = SpeechRecognitionState.Error(result.message)
            }
        }
    }

    /**
     * Reset speech recognition state (NEW)
     */
    fun resetSpeechRecognition() {
        _speechRecognitionState.value = SpeechRecognitionState.Idle
        _partialTranscription.value = ""
    }
}
```

---

### ✅ Step 1.4: Create Platform-Specific Integration

**File:** `composeApp/src/androidMain/kotlin/com/example/expensetracker/viewmodel/VoiceInputViewModel.android.kt`

```kotlin
package com.example.expensetracker.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.service.AndroidSpeechRecognizerService
import kotlinx.coroutines.launch

/**
 * Android-specific extension for VoiceInputViewModel
 * Manages the speech recognizer lifecycle
 */
class AndroidVoiceInputHelper(
    private val context: Context,
    private val viewModel: VoiceInputViewModel
) {
    private var speechRecognizerService: AndroidSpeechRecognizerService? = null

    fun startSpeechRecognition() {
        if (speechRecognizerService == null) {
            speechRecognizerService = AndroidSpeechRecognizerService(context)

            // Collect partial results
            viewModel.viewModelScope.launch {
                speechRecognizerService?.partialResults?.collect { partial ->
                    viewModel.onPartialTranscription(partial)
                }
            }

            // Collect final results
            viewModel.viewModelScope.launch {
                speechRecognizerService?.finalResult?.collect { result ->
                    viewModel.onSpeechResult(result)
                }
            }
        }

        speechRecognizerService?.startListening()
    }

    fun stopSpeechRecognition() {
        speechRecognizerService?.stopListening()
    }

    fun cleanup() {
        speechRecognizerService?.destroy()
        speechRecognizerService = null
    }
}
```

---

### ✅ Step 1.5: Update AddExpenseScreen UI

**File:** `composeApp/src/commonMain/kotlin/com/example/expensetracker/view/AddExpenseScreen.kt`

Add speech recognition UI to the existing voice section:

```kotlin
// Inside AddExpenseScreen composable, in the voice section:

@Composable
fun AddExpenseScreen(viewModel: AddExpenseViewModel = viewModel()) {
    val voiceViewModel: VoiceInputViewModel = viewModel<VoiceInputViewModel>()

    // ... existing code ...

    // NEW: Speech recognition state
    val speechState by voiceViewModel.speechRecognitionState.collectAsState()
    val partialTranscription by voiceViewModel.partialTranscription.collectAsState()

    // ... existing voice section ...

    // NEW: Add below the existing recording buttons
    if (showVoiceSection) {
        Spacer(Modifier.height(16.dp))

        // Speech Recognition Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = appColors.muted.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Live Transcription (POC)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                // Transcription display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    when (val state = speechState) {
                        is VoiceInputViewModel.SpeechRecognitionState.Idle -> {
                            Text(
                                "Tap 'Start Live Transcription' to begin",
                                color = appColors.mutedForeground,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        is VoiceInputViewModel.SpeechRecognitionState.Listening -> {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = accentGreen
                                    )
                                    Text(
                                        "Listening...",
                                        color = accentGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (partialTranscription.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        partialTranscription,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                        is VoiceInputViewModel.SpeechRecognitionState.Success -> {
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "✓ Transcribed",
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "Confidence: ${(state.confidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = appColors.mutedForeground
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    state.transcription,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )

                                // Show alternatives if available
                                if (state.alternatives.size > 1) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Alternatives:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = appColors.mutedForeground
                                    )
                                    state.alternatives.drop(1).take(2).forEach { alt ->
                                        Text(
                                            "• $alt",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = appColors.mutedForeground,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                        is VoiceInputViewModel.SpeechRecognitionState.Error -> {
                            Column {
                                Text(
                                    "❌ Error",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start/Stop button
                    Button(
                        onClick = {
                            when (speechState) {
                                is VoiceInputViewModel.SpeechRecognitionState.Idle,
                                is VoiceInputViewModel.SpeechRecognitionState.Success,
                                is VoiceInputViewModel.SpeechRecognitionState.Error -> {
                                    // Create helper and start
                                    val helper = AndroidVoiceInputHelper(
                                        context = LocalContext.current,
                                        viewModel = voiceViewModel
                                    )
                                    helper.startSpeechRecognition()
                                }
                                is VoiceInputViewModel.SpeechRecognitionState.Listening -> {
                                    // Stop listening
                                    // (helper.stopSpeechRecognition())
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (speechState) {
                                is VoiceInputViewModel.SpeechRecognitionState.Listening ->
                                    Color(0xFFFF6B6B)
                                else -> accentGreen
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            when (speechState) {
                                is VoiceInputViewModel.SpeechRecognitionState.Listening ->
                                    Icons.Default.Stop
                                else -> Icons.Default.Mic
                            },
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when (speechState) {
                                is VoiceInputViewModel.SpeechRecognitionState.Listening ->
                                    "Stop"
                                else -> "Start Live Transcription"
                            },
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
```

---

## ✅ Phase 1 Complete - Test It!

At this point, you should be able to:

1. Tap "Voice Input" toggle
2. Tap "Start Live Transcription"
3. Speak: _"I spent 50 euros on lunch at Subway"_
4. See the text appear in real-time
5. See the final transcription with confidence score

**Test Cases to Try:**

```
1. "Food category, 50 euros, lunch at Subway"
2. "I spent fifty dollars on lunch"
3. "Travel expense, 100 euros for train ticket"
4. "Subway, 15 euros"
5. "Utilities bill, 75 dollars"
```

**Document Your Results:**
Create a simple table to track accuracy:

| Input            | Transcription | Accuracy  | Confidence |
| ---------------- | ------------- | --------- | ---------- |
| "50 euros lunch" | ...           | Good/Poor | 85%        |
| ...              | ...           | ...       | ...        |

---

## 🔍 Phase 2: Basic Data Extraction ✅ COMPLETED

Once you have transcription working, add simple parsing.

### ✅ Step 2.1: Create Expense Parser

**File:** `composeApp/src/commonMain/kotlin/com/example/expensetracker/service/ExpenseParser.kt`

```kotlin
package com.example.expensetracker.service

import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory

/**
 * Simple rule-based parser for extracting expense data from text
 * This is a POC - we'll evaluate if it's "good enough" or needs AI
 */
object ExpenseParser {

    /**
     * Parse transcribed text into expense data
     */
    fun parse(text: String): ParsedExpenseData {
        val lowerText = text.lowercase()

        return ParsedExpenseData(
            amount = extractAmount(lowerText),
            currency = extractCurrency(lowerText),
            category = extractCategory(lowerText),
            description = extractDescription(text, lowerText),
            rawText = text
        )
    }

    /**
     * Extract amount from text
     * Handles: "50", "50.00", "fifty", "50 euros"
     */
    private fun extractAmount(text: String): Double? {
        // Pattern 1: Numeric amounts
        val numericPattern = Regex("""(\d+(?:[.,]\d{1,2})?)""")
        val numericMatch = numericPattern.find(text)
        if (numericMatch != null) {
            return numericMatch.value.replace(",", ".").toDoubleOrNull()
        }

        // Pattern 2: Spelled out numbers (basic)
        val wordToNumber = mapOf(
            "one" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0, "five" to 5.0,
            "six" to 6.0, "seven" to 7.0, "eight" to 8.0, "nine" to 9.0, "ten" to 10.0,
            "fifteen" to 15.0, "twenty" to 20.0, "thirty" to 30.0, "forty" to 40.0,
            "fifty" to 50.0, "sixty" to 60.0, "seventy" to 70.0, "eighty" to 80.0,
            "ninety" to 90.0, "hundred" to 100.0
        )

        for ((word, value) in wordToNumber) {
            if (text.contains(word)) {
                return value
            }
        }

        return null
    }

    /**
     * Extract currency from text
     * Handles: "euros", "dollars", "pounds", "€", "$", "£"
     */
    private fun extractCurrency(text: String): Currency? {
        return when {
            // Symbols
            text.contains("€") -> Currency.EUR
            text.contains("$") -> Currency.USD
            text.contains("£") -> Currency.GBP
            text.contains("¥") -> Currency.JPY

            // Words
            text.contains("euro") -> Currency.EUR
            text.contains("dollar") -> Currency.USD
            text.contains("buck") -> Currency.USD
            text.contains("pound") -> Currency.GBP
            text.contains("yen") -> Currency.JPY
            text.contains("franc") -> Currency.CHF

            else -> null
        }
    }

    /**
     * Extract category from text
     * Looks for category keywords
     */
    private fun extractCategory(text: String): ExpenseCategory? {
        // FOOD keywords
        val foodKeywords = listOf(
            "food", "lunch", "dinner", "breakfast", "meal", "restaurant",
            "cafe", "coffee", "snack", "grocery", "groceries",
            "subway", "mcdonald", "burger", "pizza"
        )

        // TRAVEL keywords
        val travelKeywords = listOf(
            "travel", "transport", "gas", "petrol", "fuel", "parking",
            "train", "bus", "flight", "taxi", "uber", "car"
        )

        // UTILITIES keywords
        val utilitiesKeywords = listOf(
            "utilities", "utility", "electric", "electricity", "water",
            "internet", "phone", "bill", "subscription"
        )

        return when {
            foodKeywords.any { text.contains(it) } -> ExpenseCategory.FOOD
            travelKeywords.any { text.contains(it) } -> ExpenseCategory.TRAVEL
            utilitiesKeywords.any { text.contains(it) } -> ExpenseCategory.UTILITIES
            else -> null
        }
    }

    /**
     * Extract description from text
     * Tries to clean up the text into a readable description
     */
    private fun extractDescription(originalText: String, lowerText: String): String {
        // Remove category mentions
        var cleaned = originalText

        val categoryWords = listOf(
            "food category", "travel category", "utilities category",
            "category", "amount", "expense"
        )

        categoryWords.forEach { word ->
            cleaned = cleaned.replace(word, "", ignoreCase = true)
        }

        // Remove currency mentions
        val currencyWords = listOf("euros", "dollars", "pounds", "bucks")
        currencyWords.forEach { word ->
            cleaned = cleaned.replace(word, "", ignoreCase = true)
        }

        // Remove amount if at the beginning
        cleaned = cleaned.replace(Regex("""^\d+[.,]?\d*\s*"""), "")

        // Clean up whitespace
        cleaned = cleaned.trim().replace(Regex("""\s+"""), " ")

        // Capitalize first letter
        return cleaned.replaceFirstChar { it.uppercaseChar() }
    }
}

/**
 * Result of parsing transcribed text
 */
data class ParsedExpenseData(
    val amount: Double?,
    val currency: Currency?,
    val category: ExpenseCategory?,
    val description: String,
    val rawText: String
) {
    /**
     * Calculate a simple "completeness" score
     * 1.0 = all fields extracted, 0.0 = nothing extracted
     */
    val completeness: Float
        get() {
            var score = 0f
            if (amount != null) score += 0.4f  // Most important
            if (currency != null) score += 0.2f
            if (category != null) score += 0.2f
            if (description.isNotBlank()) score += 0.2f
            return score
        }

    /**
     * Is this data "good enough" to show to user?
     */
    val isUsable: Boolean
        get() = amount != null && description.isNotBlank()
}
```

---

### ✅ Step 2.2: Integrate Parser with ViewModel

**Update:** `composeApp/src/commonMain/kotlin/com/example/expensetracker/viewmodel/VoiceInputViewModel.kt`

```kotlin
// Add to VoiceInputViewModel:

private val _parsedExpenseData = MutableStateFlow<ParsedExpenseData?>(null)
val parsedExpenseData: StateFlow<ParsedExpenseData?> = _parsedExpenseData.asStateFlow()

/**
 * Parse the transcription into expense data
 */
fun parseTranscription(transcription: String) {
    val parsed = ExpenseParser.parse(transcription)
    _parsedExpenseData.value = parsed

    println("""
        📊 Parsing Results:
        Raw: "${parsed.rawText}"
        Amount: ${parsed.amount}
        Currency: ${parsed.currency}
        Category: ${parsed.category}
        Description: "${parsed.description}"
        Completeness: ${(parsed.completeness * 100).toInt()}%
        Usable: ${parsed.isUsable}
    """.trimIndent())
}
```

---

### ✅ Step 2.3: Update UI to Show Parsed Data

**Update:** `AddExpenseScreen.kt`

```kotlin
// Add after the transcription display:

val parsedData by voiceViewModel.parsedExpenseData.collectAsState()

// Show parsed data when available
parsedData?.let { data ->
    Spacer(Modifier.height(12.dp))

    // Parse button (when we have a successful transcription)
    if (speechState is VoiceInputViewModel.SpeechRecognitionState.Success) {
        Button(
            onClick = {
                val state = speechState as VoiceInputViewModel.SpeechRecognitionState.Success
                voiceViewModel.parseTranscription(state.transcription)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.primary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Extract Expense Data")
        }

        Spacer(Modifier.height(12.dp))
    }

    // Show parsed results
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                data.completeness >= 0.8f -> Color(0xFF10B981).copy(alpha = 0.1f)
                data.completeness >= 0.5f -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                else -> Color(0xFFEF4444).copy(alpha = 0.1f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Extracted Data",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "Completeness: ${(data.completeness * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors.mutedForeground
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            // Show each field
            ParsedField("Amount", data.amount?.toString() ?: "Not found")
            ParsedField("Currency", data.currency?.code ?: "Not found")
            ParsedField("Category", data.category?.displayName ?: "Not found")
            ParsedField("Description", data.description.ifBlank { "Not found" })

            // If usable, show button to populate form
            if (data.isUsable) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        // TODO: Populate AddExpense form with this data
                        // viewModel.populateFromParsedData(data)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Text("✓ Use This Data", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ParsedField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.mutedForeground
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (value != "Not found") FontWeight.Medium else FontWeight.Normal,
            color = if (value != "Not found") appColors.foreground
                   else appColors.mutedForeground
        )
    }
}
```

---

## 📊 Phase 3: Evaluation & Testing (Days 5-7)

### Test Protocol

Create a spreadsheet to track results:

**File:** `docs/features/VOICE_INPUT_TEST_RESULTS.md`

```markdown
# Voice Input POC Test Results

## Test Date: [Date]

## Tester: [Name]

## Device: [Android device model]

### Test Cases

| #   | Input Spoken                                 | Transcription | Amount | Currency | Category | Description | Notes |
| --- | -------------------------------------------- | ------------- | ------ | -------- | -------- | ----------- | ----- |
| 1   | "Food category, 50 euros, lunch at Subway"   |               |        |          |          |             |       |
| 2   | "I spent fifty dollars on lunch"             |               |        |          |          |             |       |
| 3   | "Subway, 15 euros"                           |               |        |          |          |             |       |
| 4   | "Travel expense, 100 euros for train ticket" |               |        |          |          |             |       |
| 5   | "Utilities bill, 75 dollars"                 |               |        |          |          |             |       |
| 6   | "Gas, 40 euros"                              |               |        |          |          |             |       |
| 7   | "Coffee at Starbucks, 5 fifty"               |               |        |          |          |             |       |
| 8   | "Dinner at Italian restaurant, 80 pounds"    |               |        |          |          |             |       |
| 9   | "Parking, 10 dollars"                        |               |        |          |          |             |       |
| 10  | "Internet bill, 50 euros"                    |               |        |          |          |             |       |

### Accuracy Summary

- Transcription Accuracy: \_\_\_% (correct transcriptions / total)
- Amount Extraction: \_\_\_% (correct amounts / total)
- Currency Extraction: \_\_\_% (correct currencies / total)
- Category Extraction: \_\_\_% (correct categories / total)
- Overall Usability: \_\_\_% (usable results / total)

### Observations

**What Works Well:**

- **What Doesn't Work:**

- **Surprising Findings:**

-

### Recommendation

[ ] Native solution is good enough - proceed with polish
[ ] Native solution needs AI enhancement - proceed with Option 3
[ ] Need more testing
```

---

## 🎯 Phase 4: Decision Point (Day 8)

### Decision Criteria

**Proceed with NATIVE solution if:**

- ✅ Transcription accuracy > 85%
- ✅ Amount extraction > 80%
- ✅ Category extraction > 70%
- ✅ Overall "usable" rate > 70%
- ✅ User satisfaction is high

**Proceed with AI ENHANCEMENT if:**

- ❌ Transcription accuracy < 80%
- ❌ Amount extraction < 75%
- ❌ Category extraction < 60%
- ❌ Users find it frustrating
- ❌ Too many failed extractions

### Next Steps Based on Results

#### If Native is Good Enough:

1. Polish the UI
2. Add form auto-population
3. Add error handling
4. Ship it!

#### If AI Enhancement Needed:

1. Review Option 3 specs (Cloud STT + LLM)
2. Set up OpenAI/Anthropic accounts
3. Implement hybrid approach:
   - Try native first
   - Fall back to AI if confidence < 70%
4. A/B test both approaches

---

## 🚀 Quick Start Summary

### Day 1-2: Get Transcription Working

```bash
# 1. Copy AndroidSpeechRecognizerService.android.kt
# 2. Update VoiceInputViewModel
# 3. Add UI to AddExpenseScreen
# 4. Test transcription
```

### Day 3-4: Add Basic Parsing

```bash
# 1. Create ExpenseParser
# 2. Integrate with ViewModel
# 3. Add parsed data display
# 4. Test extraction
```

### Day 5-7: Evaluate Results

```bash
# 1. Test with 20+ real inputs
# 2. Document accuracy rates
# 3. Gather user feedback
```

### Day 8: Make Decision

```bash
# Review results
# Choose: Polish native OR Add AI enhancement
```

---

## 💡 Tips for Success

### For Better Transcription:

- Test in quiet environment
- Speak clearly
- Use natural phrasing
- Try different input patterns

### For Better Extraction:

- Look for patterns in failed parses
- Add keywords as you discover them
- Test edge cases (unusual amounts, multiple currencies)

### For Evaluation:

- Be honest about accuracy
- Consider user experience, not just numbers
- Think about production reliability

---

## 📞 Questions?

As you implement this POC, track:

1. **What transcription errors occur most?**
2. **What parsing patterns fail?**
3. **Is it faster than manual entry?**
4. **Do users like it?**

These answers will guide whether to:

- ✅ Ship the native solution
- 🤖 Add AI enhancement
- 🔄 Iterate on parsing rules

---

**Start with Phase 1 and let the data guide your decisions! 🚀**

Good luck with the POC! Report back with your findings and we can decide next steps.

---

## 📝 Implementation Summary

### ✅ What Was Implemented

**Phase 1 - Native Speech Recognition:**

1. ✅ Created `AndroidSpeechRecognizerService.android.kt` - Native Android speech recognition service
2. ✅ Updated `VoiceInputViewModel.kt` - Added speech recognition state management
3. ✅ Created `VoiceInputViewModel.android.kt` - Android-specific helper for managing speech recognizer lifecycle
4. ✅ Updated `AddExpenseScreen.kt` - Added live transcription UI with real-time feedback
5. ✅ Created `AddExpenseScreen.android.kt` - Platform-specific speech recognition button

**Phase 2 - Basic Data Extraction:**

1. ✅ Created `ExpenseParser.kt` - Rule-based parser for extracting expense data from transcribed text
   - Amount extraction (numeric and spelled-out numbers)
   - Currency detection (symbols and words)
   - Category identification (keywords for FOOD, TRAVEL, UTILITIES)
   - Description cleaning
2. ✅ Integrated parser with VoiceInputViewModel
3. ✅ Added parsed data display UI with completeness scoring

### 🎯 Files Created/Modified

**New Files:**

- `composeApp/src/androidMain/kotlin/com/example/expensetracker/service/SpeechRecognizerService.android.kt`
- `composeApp/src/androidMain/kotlin/com/example/expensetracker/viewmodel/VoiceInputViewModel.android.kt`
- `composeApp/src/androidMain/kotlin/com/example/expensetracker/view/AddExpenseScreen.android.kt`
- `composeApp/src/commonMain/kotlin/com/example/expensetracker/service/ExpenseParser.kt`

**Modified Files:**

- `composeApp/src/commonMain/kotlin/com/example/expensetracker/viewmodel/VoiceInputViewModel.kt`
- `composeApp/src/commonMain/kotlin/com/example/expensetracker/view/AddExpenseScreen.kt`

### 🧪 How to Test

1. **Open the app** on an Android device or emulator
2. **Navigate to** Add Expense screen
3. **Tap "Voice Input"** in the Quick Input section
4. **Tap "Start Live Transcription"** button
5. **Speak** an expense (e.g., "I spent 50 euros on lunch at Subway")
6. **Watch** the real-time transcription appear
7. **Tap "Extract Expense Data"** to parse the transcription
8. **Review** the extracted amount, currency, category, and description

### 📊 Next Steps: Phase 3 - Testing & Evaluation

Now that the implementation is complete, proceed to **Phase 3: Evaluation & Testing**:

1. **Test with real voice inputs** (see test cases in the guide above)
2. **Document accuracy** for each component (transcription, amount, currency, category)
3. **Calculate success rates** to determine if native solution is sufficient
4. **Make data-driven decision** on whether to proceed with native or add AI enhancement

**Testing Template Created:** Ready to fill in `VOICE_INPUT_TEST_RESULTS.md` with your findings!

---

**Implementation Date:** November 14, 2025
**Status:** ✅ Phase 1 & 2 Complete - Ready for Testing
**Next:** Begin Phase 3 Testing
