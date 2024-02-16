package samples

import io.data2viz.charts.chart.*
import io.data2viz.charts.chart.mark.candleStick
import io.data2viz.charts.chart.mark.domainSpecific.PriceMovement
import io.data2viz.charts.chart.mark.line
import io.data2viz.charts.core.*
import io.data2viz.charts.viz.VizContainer
import io.data2viz.color.Colors
import io.data2viz.color.col
import io.data2viz.format.Locale
import io.data2viz.format.Type
import io.data2viz.format.formatter
import io.data2viz.random.RandomDistribution
import io.data2viz.timeFormat.defaultLocale
import io.data2viz.viz.RichTextBuilder
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.Duration.Companion.hours

val from = Instant.parse("2023-03-12T16:00:00.000Z")
val randomGenerator =
	RandomDistribution(Clock.System.now().nanosecondsOfSecond).normal(.0, PI / 20.0)
val interval = 1.hours
val intervals = 40
val startPrice = 39477.4
var closePrice = startPrice
val dataset = (1..intervals).map {
	val p1 = closePrice + (sin(randomGenerator() * it) * 100)
	val p2 = closePrice + (sin(randomGenerator() * it) * 100)
	val p3 = closePrice + (sin(randomGenerator() * it) * 100)
	val prices = listOf(p1, p2, p3)
	val high = prices.max()
	val low = prices.min()
	val open = closePrice
	val close = prices.first { it != high && it != low }
	val volume = 4000 + (sin(randomGenerator() * it) * 3500)
	closePrice = close
	PriceMovement(
		timestamp = from.plus(interval * it),
		interval = interval,
		open = open,
		close = close,
		high = high,
		low = low,
		volume = volume,
		turnover = 40000000.0,
		// mean = (open+close) / 2.0,
		// toto = .0
	)
}

private val locale = Locale()
private val amountFormatter = locale.formatter(type = Type.FIXED_POINT, precision = 1)
private val changeFormatter = locale.formatter(type = Type.PERCENT, precision = 2)
private val volumeFormatter = locale.formatter(type = Type.DECIMAL_WITH_SI, precision = 1)

public fun VizContainer.candleStick(): Chart<PriceMovement> {
    return chart(dataset) {

        config {
            events {
                triggerMode = TriggerMode.Column
                selectionMode = SelectionMode.None
                zoomMode = ZoomMode.X
                panMode = PanMode.X
            }
            cursor {
                show = true
                mode = CursorMode.StickOnX
                display = CursorDisplay.AxisXY
            }
        }

        val temporalDimension = temporal( { domain.timestamp } )
        val meanPriceDimension = quantitative( { (domain.open + domain.close) / 2.0 } )

        tooltip {
//            formatter = {
//                "Time   ${domain.timestamp.formatToDateTime(defaultLocale)}\n" +
//                    "Open   ${amountFormatter(domain.open)}\n" +
//                    "High   ${amountFormatter(domain.high)}\n" +
//                    "Low   ${amountFormatter(domain.low)}\n" +
//                    "Close   ${amountFormatter(domain.close)}\n" +
//                    "Chg   ---\n" +
//                    "%chg   ---%\n" +
//                    "Range   ---%\n" +
//                    "Amount   ${domain.volume.formatSmart(locale)}\n" +
//                    "Turnover   ${domain.turnover.formatSmart(locale)}"
//            }
            tooltipBuilder = { selectedData, ttp, defaultTitle, defaultText, displayRatio, defaultFont, drawingZone ->
                RichTextBuilder {
                    val domain = selectedData?.datum?.domain ?: return@RichTextBuilder
                    val change = (domain.close / domain.open) - 1.0
                    text("Time    ", bold = true); text(domain.timestamp.formatToDateTime(defaultLocale))
                    newLine()
                    text("Open    ", bold = true); text(amountFormatter(domain.open))
                    newLine()
                    text("High    ", bold = true); text(amountFormatter(domain.high))
                    newLine()
                    text("Low     ", bold = true); text(amountFormatter(domain.low))
                    newLine()
                    text("Close   ", bold = true); text(amountFormatter(domain.close))
                    newLine()
                    text("Chg.    ", bold = true); text(changeFormatter(change), textColor = if (change < 0) "#CA3F66".col else "#25A750".col)
                }
            }
        }

        // The candlestick
        candleStick(temporalDimension, meanPriceDimension, { domain }) {
            strokeWidth = constant(2.0)

            x {
                enableGridLines = true
                enableTicks = false
                enableAxisLine = false
            }
            y {
                enableGridLines = true
                enableTicks = false
                enableAxisLine = false
            }
        }

        // The "mean" line
        line(temporalDimension, meanPriceDimension) {
            strokeWidth = constant(2.0)
            strokeColor = constant(Colors.Web.black)
            highlightMode = HighlightMode.Disabled
        }
    }
}

public fun VizContainer.volumeHistogram(): Chart<PriceMovement> {
    return chart(dataset) {

        config {
            events {
                triggerMode = TriggerMode.Column
                selectionMode = SelectionMode.None
                zoomMode = ZoomMode.X
                panMode = PanMode.X
            }
            cursor {
                show = true
                mode = CursorMode.StickOnX
                type = CursorType.Vertical
            }
            tooltip {
                show = false
            }
        }

        val temporalDimension = temporal( { domain.timestamp } )
        val volumeDimension = quantitative( { domain.volume } ) {
            formatter = { this?.let { volumeFormatter(it) } ?: "" }
        }

        variableColumn(temporalDimension, volumeDimension) {
            thickness = discrete( { domain.interval * .8 } )
            strokeColor = discrete( { if (domain.close < domain.open) "#CA3F66".col else "#25A750".col } )
            strokeColorHighlight = discrete({ strokeColor(this)?.brighten(1.0) } )
            fill = discrete( { if (domain.close < domain.open) "#CA3F66".col else "#25A750".col } )
            fillHighlight = discrete({ fill(this)?.brighten(1.0) } )

            x {
                enableGridLines = true
                enableTicks = false
                enableAxisLine = false
            }
            y {
                enableGridLines = true
                enableTicks = false
                enableAxisLine = false
                start = .0
            }
        }
    }
}
