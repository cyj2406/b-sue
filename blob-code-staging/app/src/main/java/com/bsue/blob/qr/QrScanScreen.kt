package com.bsue.blob.qr

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bsue.blob.ui.components.HazardTape
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1
import com.bsue.blob.ui.theme.TapeYellow
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

// 실물 카드 QR 스캔 (CameraX 카메라 + ML Kit QR 읽기, 인터넷 없이 폰 안에서 동작)
@Composable
fun QrScanScreen(
    onResult: (BlobQr) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }
    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }
    var unknownCode by remember { mutableStateOf(false) } // 우리 QR이 아닌 걸 비췄을 때

    BackHandler(onBack = onClose)

    Box(Modifier.fillMaxSize().background(BlobBlack)) {
        if (hasPermission) {
            CameraQrPreview(
                onQr = { raw ->
                    val qr = BlobQr.parse(raw)
                    if (qr != null) onResult(qr) else unknownCode = true
                },
            )
            ScanFrame(Modifier.align(Alignment.Center))
        } else {
            Text(
                "카메라를 허락해줘야\nQR을 볼 수 있어!",
                fontFamily = BlackHanSans, fontSize = 24.sp, color = BlobCream, textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // 위: 안내
        Column(Modifier.fillMaxWidth().statusBarsPadding()) {
            HazardTape(Modifier.fillMaxWidth().height(14.dp))
            Text(
                text = if (unknownCode) "음… 블롭 카드 QR이 아닌 것 같아" else "카드 뒷면 QR을 네모 안에 비춰줘",
                fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = if (unknownCode) TapeYellow else BlobCream,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }

        // 아래: 닫기
        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp)
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlobOrange, contentColor = BlobCream),
        ) {
            Text("닫기", fontFamily = BlackHanSans, fontSize = 18.sp)
        }
    }
}

// 카메라 미리보기 + QR 읽기
@Composable
private fun CameraQrPreview(onQr: (String?) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
    }
    var handled by remember { mutableStateOf(false) } // 한 번 읽으면 그만
    var lastUnknown by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage == null || handled) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(image)
                    .addOnSuccessListener(mainExecutor) { barcodes ->
                        val raw = barcodes.firstOrNull()?.rawValue ?: return@addOnSuccessListener
                        if (handled) return@addOnSuccessListener
                        if (BlobQr.parse(raw) != null) {
                            handled = true
                            onQr(raw)
                        } else if (raw != lastUnknown) {
                            lastUnknown = raw
                            onQr(raw) // 우리 QR 아님 → 안내만
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            }
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, mainExecutor)

        onDispose {
            if (providerFuture.isDone) providerFuture.get().unbindAll()
            analysisExecutor.shutdown()
            scanner.close()
        }
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

// 노란 모서리 네모 (여기에 QR을 맞춰요)
@Composable
private fun ScanFrame(modifier: Modifier) {
    Canvas(modifier.size(240.dp)) {
        val len = size.width * 0.18f
        val stroke = 8.dp.toPx()
        val w = size.width
        val h = size.height
        listOf(
            Offset(0f, 0f) to listOf(Offset(len, 0f), Offset(0f, len)),
            Offset(w, 0f) to listOf(Offset(w - len, 0f), Offset(w, len)),
            Offset(0f, h) to listOf(Offset(len, h), Offset(0f, h - len)),
            Offset(w, h) to listOf(Offset(w - len, h), Offset(w, h - len)),
        ).forEach { (corner, ends) ->
            ends.forEach { end -> drawLine(TapeYellow, corner, end, strokeWidth = stroke, cap = StrokeCap.Square) }
        }
    }
}
