package com.example.storyefun.ui.screens
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.storyefun.utils.rememberTextToSpeech
import java.util.*
//
//@Composable
//fun rememberTextToSpeech(context: Context): TextToSpeech? {
//    val ttsRef = remember { mutableStateOf<TextToSpeech?>(null) }
//
//    DisposableEffect(Unit) {
//        var tts: TextToSpeech? = null
//
//        tts = TextToSpeech(context) { status ->
//            if (status == TextToSpeech.SUCCESS) {
//                val localeVN = Locale("vi", "VN")
//                val result = tts?.setLanguage(localeVN)
//
//                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                    Toast.makeText(context, "Không hỗ trợ tiếng Việt hoặc thiếu dữ liệu", Toast.LENGTH_SHORT).show()
//                    val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
//                    context.startActivity(installIntent)
//                } else {
//                    ttsRef.value = tts
//                }
//            } else {
//                Toast.makeText(context, "TTS khởi tạo thất bại", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        onDispose {
//            tts?.stop()
//            tts?.shutdown()
//        }
//    }
//
//    return ttsRef.value
//}

@Composable
fun TestScreen() {
    val context = LocalContext.current
    val tts = rememberTextToSpeech(context)
    val vietnameseText = "english hua"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = {
            tts?.speak(vietnameseText, TextToSpeech.QUEUE_FLUSH, null, null)
        }) {
            Text("Đọc văn bản")
        }
    }
}
