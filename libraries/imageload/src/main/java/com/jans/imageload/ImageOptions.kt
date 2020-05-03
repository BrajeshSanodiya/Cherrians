package com.jans.imageload

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import com.jans.societyoo.utils.imageload.transformation.Transformation

data class ImageOptions(
    @DrawableRes val placeholder: Int = 0,
    val placeholderDrawable:Drawable?=null,
    @DrawableRes val error: Int = 0,
    @FloatRange(from = 0.0, to = 1.0) val sizeMultiplier: Float = 1F,
    val diskCacheStrategy: CacheStrategy = CacheStrategy.AUTOMATIC,
    val priority: Priority = Priority.NORMAL,
    val transformation: Transformation = Transformation.None
)