package io.data2viz.charts.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.data2viz.viz.VizContainer
import io.data2viz.viz.VizContainerView


@Composable
actual fun Viz(
    modifier: Modifier,
    block: (VizContainer) -> Unit
) {
    AndroidView(
        factory = { VizContainerView(it) },
        modifier = modifier,
        update = { block(it) }
    )
}
