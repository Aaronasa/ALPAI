package com.example.alpai.model

import com.example.alpai.enmuclass.WasteCategory

data class WasteResult(
    val label: String,
    val confidence: Float,
    val category: WasteCategory
)