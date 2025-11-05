package com.example.number

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    private var ready = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            val ok = status == TextToSpeech.SUCCESS
            val lang = tts?.setLanguage(Locale.JAPAN)
            ready = ok && (lang == TextToSpeech.LANG_AVAILABLE || lang == TextToSpeech.LANG_COUNTRY_AVAILABLE)
            tts?.setSpeechRate(1.5f)
            tts?.setPitch(2.3f)
        }

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    NumberGrid((0..30).toList()) { n -> speak(n) }
                }
            }
        }
    }

    private fun speak(n: Int) {
        if (!ready) return
        val text = jp(n)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "no_$n")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun NumberGrid(nums: List<Int>, onTap: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(nums) { n ->
            Button(onClick = { onTap(n) }, modifier = Modifier.padding(8.dp)) {
                Text(n.toString())
            }
        }
    }
}

/** 0〜30の日本語読み */
private fun jp(n: Int): String = when (n) {
    0 -> "ぜろ"
    1 -> "いち"
    2 -> "に"
    3 -> "さん"
    4 -> "よん"
    5 -> "ご"
    6 -> "ろく"
    7 -> "なな"
    8 -> "はち"
    9 -> "きゅう"
    10 -> "じゅう"
    in 11..19 -> "じゅう" + jp(n - 10)
    20 -> "にじゅう"
    in 21..29 -> "にじゅう" + jp(n - 20)
    30 -> "さんじゅう"
    else -> n.toString()
}
