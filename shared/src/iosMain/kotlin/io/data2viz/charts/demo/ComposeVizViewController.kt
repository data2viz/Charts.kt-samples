package io.data2viz.charts.demo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController

fun ComposeVizViewController() = ComposeUIViewController {
    VizApp(Modifier.fillMaxSize())
}
