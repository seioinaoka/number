package com.example.number

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.activity.compose.setContent
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NumberApp() }
    }
}

@Composable
fun NumberApp(vm: CounterViewModel = viewModel()) {
    val context = LocalContext.current

    // TextToSpeech を状態で保持（null許容）
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    // 初期化（Composeスコープ外で確実に）
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.JAPANESE
                tts?.setSpeechRate(0.9f)
                tts?.setPitch(1.0f)
            }
        }
    }
    // 破棄
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Color.White) {
            Column(Modifier.fillMaxSize()) {
                Text("Number v1.1.1", modifier = Modifier.padding(12.dp), color = Color.Black)

                NumberGrid(
                    numbers = (0..30).toList(),
                    onClick = { n ->
                        vm.onNumberClicked(n)
                        // 読み上げ（null安全）
                        tts?.speak(
                            n.toString(),
                            TextToSpeech.QUEUE_FLUSH,
                            Bundle().apply {
                                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                            },
                            "speak-$n"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                HorizontalDivider()

                CharacterGrid(
                    count = vm.current,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}


/** 数字ボタン群 */
@Composable
fun NumberGrid(
    numbers: List<Int>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        items(numbers) { n ->
            Button(onClick = { onClick(n) }) { Text(n.toString()) }
        }
    }
}

/** 横10×縦3（合計30体）で並べるキャラクター表示 */
@Composable
fun CharacterGrid(
    count: Int,
    modifier: Modifier = Modifier
) {
    val face = painterResource(id = R.drawable.char_face_10) // ← 10x10画像をdrawableに置く

    // 常に30マスを描画（空白は非表示）
    val totalSlots = 30
    val indices = remember { List(totalSlots) { it } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(text = "表示中：$count 体", modifier = Modifier.padding(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(10), // 横10
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.heightIn(min = 150.dp)
        ) {
            items(indices) { index ->
                AnimatedVisibility(
                    visible = index < count,
                    enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
                    exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center)
                ) {
                    Image(
                        painter = face,
                        contentDescription = "char",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/** カウント増減ロジック */
class CounterViewModel : ViewModel() {
    var current by mutableStateOf(0)
        private set
    private var target by mutableStateOf(0)
    private var job: Job? = null

    fun onNumberClicked(n: Int) {
        target = n.coerceIn(0, 30)
        startAnimation()
    }

    private fun startAnimation() {
        job?.cancel()
        job = viewModelScope.launch {
            while (isActive && current != target) {
                current += if (target > current) 1 else -1
                delay(120L)
            }
        }
    }
}
