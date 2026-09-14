package com.example.myeduapp.core.ui.theme

import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

object BigBridzAnimation {
    const val FastMs = 150
    const val NormalMs = 200
    const val SlowMs = 250

    val FastSpec: DurationBasedAnimationSpec<Float> = tween(durationMillis = FastMs, easing = FastOutSlowInEasing)
    val NormalSpec: DurationBasedAnimationSpec<Float> = tween(durationMillis = NormalMs, easing = FastOutSlowInEasing)
    val SlowSpec: DurationBasedAnimationSpec<Float> = tween(durationMillis = SlowMs, easing = FastOutSlowInEasing)
}
