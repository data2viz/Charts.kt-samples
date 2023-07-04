package io.data2viz.charts.demo

import io.data2viz.charts.chart.chart
import io.data2viz.charts.chart.constant
import io.data2viz.charts.chart.discrete
import io.data2viz.charts.chart.mark.dot
import io.data2viz.charts.chart.quantitative
import io.data2viz.charts.chart.temporal
import io.data2viz.charts.core.PanMode
import io.data2viz.charts.core.ZoomMode
import io.data2viz.charts.core.formatToSI
import io.data2viz.math.pct
import io.data2viz.random.RandomDistribution
import io.data2viz.shape.Symbols
import io.data2viz.timeFormat.format
import io.data2viz.viz.VizContainer
import kotlinx.datetime.Instant

fun VizContainer.bubbleChart() {
    chart(samples) {
        config {
            events {
                zoomMode = ZoomMode.XY
                panMode = PanMode.XY
            }
            tooltip {
                fontSize = config.tooltip.fontSize + 2.0
            }
            cursor {
                show = true
            }
        }

        tooltip {
            formatter =
                { "${format("At %X")(domain.timestamp)}, pressure = ${domain.pressure.formatToSI()} mb." }
        }

        val timeDimension = temporal({ domain.timestamp })
        val pressureDimension = quantitative({ domain.pressure })
        series = discrete({ domain.batchCode })

        dot(timeDimension, pressureDimension) {
            marker = constant(Symbols.Circle)
            size = discrete({ domain.temperature * 6 })
            strokeColor = discrete({ config.mark.strokeColors[indexOfSeries].withAlpha(60.pct) })
        }
    }
}

private val randomGenerator = RandomDistribution(42).normal(100.0, 18.0)

private data class Sample(
    val sampleIndex: Int,
    val batchCode: String,
    val timestamp: Instant,
    val temperature: Double,
    val pressure: Double
)

private val samples = generateSamples(30)

private fun generateSamples(numSamples: Int) = (0 until numSamples).map {
    val batchIndex = 1 + (it % 4)
    val pressure: Double = randomGenerator() * 1000
    val temp: Double = randomGenerator() * batchIndex * pressure / 100000
    val ts = Instant.fromEpochMilliseconds(1611150127144L + (it * 8632L))
    Sample(it, "Batch #$batchIndex", ts, temp, pressure)
}
