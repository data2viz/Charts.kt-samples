package io.data2viz.charts.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.data2viz.charts.chart.Chart
import io.data2viz.charts.chart.mark.domainSpecific.PriceMovement
import io.data2viz.charts.layout.sizeManager
import samples.candleStick
import samples.volumeHistogram

@Composable
fun DualCharts(modifier: Modifier = Modifier) {

	var priceChart: Chart<PriceMovement>? by remember { mutableStateOf(null) }
	var volumeChart: Chart<PriceMovement>? by remember { mutableStateOf(null) }
	val verticalSync = remember { sizeManager().vSynchro() }

	Column(modifier) {
		Viz(modifier = Modifier
			.fillMaxWidth()
			.weight(.8f)) {
			priceChart = it.candleStick()
		}
		Viz(modifier = Modifier
			.fillMaxWidth()
			.weight(.2f)) {
			volumeChart = it.volumeHistogram()
		}
	}

	LaunchedEffect(priceChart, volumeChart) {
		val firstChart = priceChart ?: return@LaunchedEffect
		val secondChart = volumeChart ?: return@LaunchedEffect
		verticalSync.addAllCharts(firstChart, secondChart)

		firstChart.onZoom { secondChart.zoom(it.zoomAction) }
		firstChart.onPan { secondChart.pan(it.panAction) }
		firstChart.onHighlight { event ->
			secondChart.highlight(event.data)
			event.selectedData.firstOrNull()?.let { secondChart.setCursorFor(it) }
		}
	}
}
