package com.example.alpai.model

import com.example.alpai.enmuclass.WasteCategory


object WasteMapper {

    fun mapLabelToCategory(label: String): WasteCategory {
        return when (label.lowercase()) {
            "trash", "biological" -> WasteCategory.RESIDUE
            "paper", "cardboard" -> WasteCategory.PAPER
            "plastic", "metal", "glass", "stereoform", "bubblewrap" ->
                WasteCategory.MIX_RECYCLING
            else -> WasteCategory.UNKNOWN
        }
    }
}