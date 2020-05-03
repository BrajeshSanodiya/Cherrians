package com.jans.imageload

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.jans.imageload.extension.transform
import java.io.File


class GlideImageLoader :
    ImageLoader {

    override fun load(imageView: ImageView, url: String?, options: ImageOptions?) {
        val requestBuilder = Glide.with(imageView).load(url)
        options?.let { requestBuilder.apply(buildRequestOptions(it)) }
        requestBuilder.into(imageView)
    }
    override fun load(imageView: ImageView, file: File?, options: ImageOptions?) {
        val requestBuilder = Glide.with(imageView).load(file)
        options?.let { requestBuilder.apply(buildRequestOptions(it)) }
        requestBuilder.into(imageView)
    }

    override fun load(
        imageView: ImageView, @RawRes @DrawableRes resId: Int,
        options: ImageOptions?
    ) {
        val requestBuilder = Glide.with(imageView).load(resId)
        options?.let { requestBuilder.apply(buildRequestOptions(it)) }
        requestBuilder.into(imageView)
    }

    override suspend fun download(context: Context, url: String): Result<Bitmap> {
        return tryCatching {
            Glide.with(context).asBitmap().load(url).submit().get()
                ?: throw NullPointerException("Downloaded bitmap is null : $url")
        }
    }

    private fun buildRequestOptions(options: ImageOptions): RequestOptions {
        val diskCacheStrategy = when (options.diskCacheStrategy) {
            CacheStrategy.ALL -> DiskCacheStrategy.ALL
            CacheStrategy.NONE -> DiskCacheStrategy.NONE
            CacheStrategy.ORIGINAL -> DiskCacheStrategy.DATA
            CacheStrategy.DECODED -> DiskCacheStrategy.RESOURCE
            CacheStrategy.AUTOMATIC -> DiskCacheStrategy.AUTOMATIC
        }
        val priority = when (options.priority) {
            Priority.HIGH -> com.bumptech.glide.Priority.HIGH
            Priority.NORMAL -> com.bumptech.glide.Priority.NORMAL
            Priority.LOW -> com.bumptech.glide.Priority.LOW
        }

        if(options.placeholderDrawable==null){
            return RequestOptions().placeholder(options.placeholder)
                .error(options.error)
                .sizeMultiplier(options.sizeMultiplier)
                .diskCacheStrategy(diskCacheStrategy)
                .transform(options.transformation)
                .priority(priority)
        }else{
            return RequestOptions().placeholder(options.placeholderDrawable)
                .error(options.error)
                .sizeMultiplier(options.sizeMultiplier)
                .diskCacheStrategy(diskCacheStrategy)
                .transform(options.transformation)
                .priority(priority)
        }
    }

    override fun clear(imageView: ImageView) {
        Glide.with(imageView).clear(imageView)
    }
}