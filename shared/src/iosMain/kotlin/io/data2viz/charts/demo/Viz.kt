package io.data2viz.charts.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import io.data2viz.viz.VizContainer
import io.data2viz.viz.VizContainerView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun Viz(
    modifier: Modifier,
    block: (VizContainer) -> Unit
) {
    UIKitView(
        factory = { VizContainerView() },
        modifier = modifier,
        update = { block(it.container) }
    )
}
