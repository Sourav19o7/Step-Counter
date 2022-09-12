package com.level.stepcounter

import java.util.concurrent.TimeUnit

data class Days(
    var day: String,
    var steps: String,
    var time: TimeUnit,
    var distance: Int,
    var calories: Float
)
