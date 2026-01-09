package com.example.alpai.enmuclass

import com.example.alpai.R

enum class WasteCategory(
    val displayName: String,
    val binImage: Int?
) {
    RESIDUE("Residue", R.drawable.bin_residue),
    PAPER("Paper", R.drawable.bin_paper),
    MIX_RECYCLING("Mix Recycling", R.drawable.bin_mix),
    HUMAN("Manusia (Bukan Sampah)", R.drawable.human),
    UNKNOWN("Tidak dapat diklasifikasikan", null)
}