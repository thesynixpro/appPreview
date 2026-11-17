package com.mobileide.studio.model

import com.mobileide.studio.screens.BuildStatus

data class Project(
    val name: String,
    val path: String,
    val language: String = "Kotlin",
    val lastModified: String = "",
    val buildStatus: BuildStatus = BuildStatus.None
)
