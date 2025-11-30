package com.example.alpai.viewmodel

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class ClassifierHelper(val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    init {
        // 1. Load Model & Labels
        val model = FileUtil.loadMappedFile(context, "model_unquant.tflite")
        val options = Interpreter.Options()
        interpreter = Interpreter(model, options)
        labels = FileUtil.loadLabels(context, "labels.txt")
    }

    fun classify(bitmap: Bitmap): String {
        // 2. Pre-process Gambar (Resize ke 224x224 sesuai standar Teachable Machine)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Normalize pixel value
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 3. Siapkan Output Buffer
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, labels.size), DataType.FLOAT32)

        // 4. Jalankan Inference
        interpreter?.run(tensorImage.buffer, outputBuffer.buffer.rewind())

        // 5. Cari probabilitas tertinggi
        val confidences = outputBuffer.floatArray
        var maxPos = 0
        var maxConfidence = 0f
        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxPos = i
            }
        }

        return "${labels[maxPos]} (${String.format("%.1f%%", maxConfidence * 100)})"
    }

    fun close() {
        interpreter?.close()
    }
}