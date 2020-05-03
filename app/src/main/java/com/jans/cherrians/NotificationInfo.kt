package com.jans.cherrians


data class NotificationInfo (
    val title: String="",
    val desc: String="",
    val img: String?=null,
    val webUrl: String?=null,
    val layoutType: Int=0
)