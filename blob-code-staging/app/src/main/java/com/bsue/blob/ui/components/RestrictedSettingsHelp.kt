package com.bsue.blob.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.GothicA1
import com.bsue.blob.ui.theme.TapeYellow

// APK 파일로 설치한 앱은 안드로이드 13부터 접근성·알림 읽기가 '제한된 설정'으로 잠겨요.
// 그걸 푸는 방법 안내 (누르면 펼쳐짐). 온보딩과 설정 화면에서 같이 써요.
// forNotifications = true 면 '알림 읽기' 기준으로 안내
@Composable
fun RestrictedSettingsHelp(
    modifier: Modifier = Modifier,
    startExpanded: Boolean = false,
    forNotifications: Boolean = false,
) {
    val settingName = if (forNotifications) "알림 읽기" else "접근성"
    val listName = if (forNotifications) "기기 및 앱 알림" else "설치된 앱"
    val openSettings: (Context) -> Unit =
        if (forNotifications) ::openNotificationListenerSettings else ::openAccessibilitySettings
    val context = LocalContext.current
    var expanded by rememberSaveable { mutableStateOf(startExpanded) }
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TapeYellow)
            .border(2.dp, BlobBlack, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp),
        ) {
            Text(
                text = "⚠ 켜려는데 '제한된 설정'이라고 막혀?",
                fontFamily = GothicA1,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BlobBlack,
                modifier = Modifier.weight(1f),
            )
            Text(if (expanded) "접기" else "푸는 법", fontFamily = BlackHanSans, fontSize = 13.sp, color = BlobBlack)
        }

        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                Step(1, "[$settingName 열기] → '$listName' → '블롭 출동'을 한 번 눌러봐. '제한된 설정' 창이 뜨면 [확인].")
                Step(2, "[앱 정보 열기] → 오른쪽 위 ⋮ → '제한된 설정 허용' → 잠금 풀기(PIN·지문).")
                Step(3, "다시 [$settingName 열기] → '$listName' → '블롭 출동' → 켜기!")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "APK 파일로 설치했을 때만 생기는 잠금이야. 1번을 먼저 해야 2번 메뉴가 나타나.",
                    fontFamily = GothicA1,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = BlobBlack.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { openSettings(context) }, modifier = Modifier.weight(1f)) {
                        Text("$settingName 열기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
                    }
                    OutlinedButton(onClick = { openAppInfo(context) }, modifier = Modifier.weight(1f)) {
                        Text("앱 정보 열기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
                    }
                }
            }
        }
    }
}

@Composable
private fun Step(number: Int, text: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text("$number", fontFamily = BlackHanSans, fontSize = 15.sp, color = BlobBlack)
        Spacer(Modifier.width(8.dp))
        Text(text, fontFamily = GothicA1, fontSize = 13.sp, lineHeight = 19.sp, color = BlobBlack)
    }
}

fun openAccessibilitySettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}

fun openNotificationListenerSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
}

fun openAppInfo(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
    )
}
