package io.data2viz.charts.demo

import io.data2viz.viz.VizContainerView
import platform.UIKit.UIView

fun createBubbleChartView(): UIView = VizContainerView().apply {
    container.also { it.bubbleChart() }
}

fun createLineChartView(): UIView = VizContainerView().apply {
    container.also { it.lineChart() }
}
