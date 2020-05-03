package com.jans.imageload.extension

import com.bumptech.glide.request.RequestOptions
import com.jans.societyoo.utils.imageload.transformation.RoundedCornersTransformation
import com.jans.societyoo.utils.imageload.transformation.Transformation

fun RequestOptions.transform(transformation: Transformation): RequestOptions =
    when (transformation) {
        is Transformation.RoundedCorner -> {
            transform(
                RoundedCornersTransformation(
                    transformation.radius, transformation.margin,
                    RoundedCornersTransformation.CornerType.ALL
                )
            )
            this
        }
        is Transformation.CircleCrop -> {
            circleCrop()
            this
        }
        is Transformation.None -> {
            //do nothing
            this
        }

    }
