package com.jans.imageload

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import java.io.File

interface ImageLoader {
    fun load(imageView: ImageView, url: String?, options: ImageOptions? = null)
    fun load(imageView: ImageView, file: File?, options: ImageOptions? = null)
    fun load(imageView: ImageView, @RawRes @DrawableRes resId: Int, options: ImageOptions? = null)
    suspend fun download(context:Context,url:String): Result<Bitmap>
    fun clear(imageView: ImageView)
}

object DefaultImageLoader : ImageLoader by GlideImageLoader()