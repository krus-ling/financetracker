package com.example.financetracker.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

/**
 * Централизованные анимации переходов между экранами для [androidx.navigation3.ui.NavDisplay].
 *
 * Обеспечивают единое направление движения при открытии экранов (справа налево с параллаксом)
 * и при возврате как по кнопке в интерфейсе, так и через системный жест Predictive Back (слева направо).
 */
object NavigationTransitions {

    private const val PUSH_DURATION_MS = 320
    private const val POP_DURATION_MS = 280
    private const val PREDICTIVE_POP_DURATION_MS = 210
    private const val PARALLAX_OFFSET_FRACTION = 0.28f
    private const val UNDERLYING_ALPHA = 0.88f

    /**
     * Анимация перехода вперед.
     *
     * Новый экран выезжает на 100%, а текущий слегка сдвигается влево с легким затемнением.
     */
    fun <S> horizontalPush(): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        val slideSpec = tween<IntOffset>(
            durationMillis = PUSH_DURATION_MS,
            easing = FastOutSlowInEasing
        )
        val alphaSpec = tween<Float>(
            durationMillis = PUSH_DURATION_MS,
            easing = FastOutSlowInEasing
        )

        val enter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = slideSpec
        )
        val exit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = slideSpec,
            targetOffset = { (it * PARALLAX_OFFSET_FRACTION).toInt() }
        ) + fadeOut(
            animationSpec = alphaSpec,
            targetAlpha = UNDERLYING_ALPHA
        )

        enter togetherWith exit
    }

    /**
     * Анимация возврата назад по стрелке в TopAppBar или нажатию системной кнопки "назад".
     */
    fun <S> horizontalPop(): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        horizontalPopTransform(
            durationMillis = POP_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    }

    /**
     * Анимация возврата назад при жесте Predictive Back от края экрана
     *
     * Использует [androidx.compose.animation.core.LinearEasing], чтобы экран следовал за пальцем 1:1.
     */
    fun <S> horizontalPredictivePop(): AnimatedContentTransitionScope<S>.(Int) -> ContentTransform = {
        horizontalPopTransform(
            durationMillis = PREDICTIVE_POP_DURATION_MS,
            easing = LinearEasing
        )
    }

    private fun <S> AnimatedContentTransitionScope<S>.horizontalPopTransform(
        durationMillis: Int,
        easing: Easing
    ): ContentTransform {
        val slideSpec = tween<IntOffset>(
            durationMillis = durationMillis,
            easing = easing
        )

        val alphaSpec = tween<Float>(
            durationMillis = durationMillis,
            easing = easing
        )

        val enter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = slideSpec,
            initialOffset = { (it * PARALLAX_OFFSET_FRACTION).toInt() }
        ) + fadeIn(
            animationSpec = alphaSpec,
            initialAlpha = UNDERLYING_ALPHA
        )

        val exit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = slideSpec
        )

        return enter togetherWith exit
    }
}