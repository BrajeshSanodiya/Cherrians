package com.jans.societyoo.utils.imageload.transformation

sealed class Transformation {
    data class RoundedCorner(val radius: Int, val margin: Int) : Transformation()
    object CircleCrop : Transformation()
    object None : Transformation()
}