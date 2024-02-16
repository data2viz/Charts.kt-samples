package samples

import io.data2viz.charts.chart.Chart
import io.data2viz.charts.chart.mark.*
import io.data2viz.charts.chart.mark.internal.*
import io.data2viz.charts.config.AxisConfig
import io.data2viz.charts.config.EventConfig
import io.data2viz.charts.config.MarkConfig
import io.data2viz.charts.core.*
import io.data2viz.charts.dimension.*
import io.data2viz.charts.event.EventZone
import io.data2viz.charts.layout.DrawingZone
import io.data2viz.color.ColorOrGradient
import io.data2viz.geom.Point
import io.data2viz.geom.Rect
import io.data2viz.geom.RectGeom
import kotlinx.datetime.Instant
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Add a [VariableColumn] with a quantitative X axis to the current [Chart].
 *
 * @param x The [Quantitative] dimension for the X axis
 * @param y The [Quantitative] dimension for the Y axis
 * @param init The initialization lambda for this [Mark].
 */
public fun <DOMAIN> Chart<DOMAIN>.variableColumn(
    x: Quantitative<DOMAIN>,
    y: Quantitative<DOMAIN>,
    init: VariableColumn<DOMAIN, Double?, Double?>.() -> Unit = {}): Unit =

    addMark { markIndex -> VariableColumn(markIndex, dataset, x, y, init, config.events, config.mark, config.x, config.y) }


/**
 * Add a [VariableColumn] with a temporal X axis to the current [Chart].
 *
 * @param x The [Temporal] dimension for the X axis
 * @param y The [Quantitative] dimension for the Y axis
 * @param init The initialization lambda for this [Mark].
 */
public fun <DOMAIN> Chart<DOMAIN>.variableColumn(
    x: Temporal<DOMAIN>,
    y: Quantitative<DOMAIN>,
    init: VariableColumn<DOMAIN, Instant, Duration>.() -> Unit = {}): Unit =

    addMark { markIndex -> VariableColumn(markIndex, dataset, x, y, init, config.events, config.mark, config.x, config.y) }

/**
 * "Variable column mark" is a column mark but with a continuous X axis instead of a discrete one, the width
 * (or thickness) of each column is determined by the thickness dimension.
 *
 * The generic XVAL type can be:
 *  - a Double? (when used with a quantitative dimension)
 *  - an [Instant] (when used with a temporal dimension).
 *
 * The "thickness" of the columns is determined by the thickness dimension and its generic TVAL type is:
 *  - a Double? (when used with a quantitative dimension)
 *  - a [Duration] (when used with a temporal dimension).
 *
 * @see BarMark
 */
@ChartDSL
@Suppress("UNCHECKED_CAST")
public class VariableColumn<DOMAIN, XVAL, TVAL> (
    private val markIndex: Int,
    dataset: Dataset<DOMAIN>,
    xDimension: ContinuousDimension<DOMAIN, XVAL>,
    yDimension: Quantitative<DOMAIN>,
    init: VariableColumn<DOMAIN, XVAL, TVAL>.() -> Unit,
    eventConfig: EventConfig,
    config: MarkConfig,
    override val xAxisConfig: AxisConfig,
    override val yAxisConfig: AxisConfig
) :
    HasFill<DOMAIN> by FillDelegate(dataset, config),
    HasStrokeWidth<DOMAIN> by StrokeWidthDelegate(dataset, config),
    HasStrokeColor<DOMAIN> by StrokeColorDelegate(dataset, config),
    HasMarkDecorator<DOMAIN>,
    HasHighlightDecorator<DOMAIN>,
    HasSelectionDecorator<DOMAIN>,
    HasLegendDecorator<DOMAIN>,
    HasCustomTooltip<DOMAIN>,
    HasHighlightMode by HighlightModeDelegate(eventConfig),
    HasSelectionMode by SelectionModeDelegate(eventConfig),

    // Constructor
    MarkXY<DOMAIN, XVAL, Double?>{

    override val x: Axis<DOMAIN, XVAL> = Axis(dataset.data, xDimension, xAxisConfig.copy(config, config), false)
    override val y: Axis<DOMAIN, Double?> = Axis(dataset.data, yDimension, yAxisConfig.copy(config, config), false)

    override val defaultMarkDecorator: DatumDecorator<DOMAIN> = { datum, _, drawingZone ->
        createBar(datum)?.let { drawingZone.drawBar(it, strokeColor(datum), strokeWidth(datum), fill(datum)) }
    }
    override val defaultHighlightDecorator: DatumDecorator<DOMAIN> = { datum, _, drawingZone ->
        createBar(datum)?.let { drawingZone.drawBar(it, strokeColorHighlight(datum), strokeWidthHighlight(datum), fillHighlight(datum)) }
    }
    override val defaultSelectionDecorator: DatumDecorator<DOMAIN> = { datum, _, drawingZone ->
        createBar(datum)?.let { drawingZone.drawBar(it, strokeColorSelect(datum), strokeWidthSelect(datum), fillSelect(datum)) }
    }
    override val defaultLegendDecorator: DatumDecorator<DOMAIN> = { datum, position, drawingZone ->
        val bar = RectGeom(position.x - 5.0, position.y - 6.0, 10.0, 10.0)
        drawingZone.drawBar(bar, strokeColor(datum), strokeWidth(datum), fill(datum))
    }

    override var markDecorator: DatumDecorator<DOMAIN> = defaultMarkDecorator
    override var highlightDecorator: DatumDecorator<DOMAIN> = defaultHighlightDecorator
    override var selectionDecorator: DatumDecorator<DOMAIN> = defaultSelectionDecorator
    override var legendDecorator: DatumDecorator<DOMAIN> = defaultLegendDecorator

    /**
     * The thickness of each column.
     * When used with a Quantitative Dimension, this value represents a Double?.
     * When used with a Temporal Dimension, this value represents a duration.
     */
    public var thickness: Dimension<DOMAIN, TVAL> = (
        if (xDimension is Quantitative)
            Constant<DOMAIN, Double?>(1.0)
        else
            Constant(1.0.seconds)
        ) as Dimension<DOMAIN, TVAL>

    private lateinit var quantity: Axis<DOMAIN, Double?>
    private lateinit var category: Axis<DOMAIN, XVAL>

    private val eventZones: MutableList<EventZone<DOMAIN>> = mutableListOf()

    private lateinit var categories: Map<XVAL, List<Datum<DOMAIN>>>
    private lateinit var stackValues: Map<XVAL, List<Double>>

    init {
        init()
    }

    override fun prepare(dataset: Dataset<DOMAIN>) {

        quantity = y
        category = x

        /**
         * ************************ PREPARE STACKING VALUES*******************************
         *
         * To avoid recomputing stacked values each time, let's do it once and for all when preparing the chart.
         */

        var stackMax = Double.NEGATIVE_INFINITY
        categories = dataset.data.groupBy { category.dimension(it) }
        categories.values.forEach { category ->
            stackMax = max(stackMax, category.sumOf { quantity.dimension.accessor(it) ?: Double.NEGATIVE_INFINITY })
        }
        val seriesCount = dataset.dataBySeries.size
        stackValues = categories.mapValues { categoryKV ->
            var stackValue = .0
            (0 .. seriesCount).map { seriesIndex ->
                val ret = stackValue
                if (seriesIndex < seriesCount) {
                    val firstOrNull = categoryKV.value.firstOrNull { it.indexOfSeries == seriesIndex }
                    stackValue += firstOrNull?.let { quantity.dimension.accessor(it) } ?: .0
                }
                ret
            }
        }

        if  (quantity.end == null) quantity.end = stackMax

        /**
         * ************************ COMPUTE MIN/MAX *******************************
         *
         * As "thickness" is a dimension that may not be constant, we must check for each value the thickness,
         * add or remove it to compute the min and max of the axes
         */

        if (category.start == null) {
            if (category.dimension is Quantitative) {
                (category as Axis<DOMAIN, Double?>).start = dataset.data.minOfOrNull {
                    ((category.dimension as Quantitative).accessor(it) ?: Double.POSITIVE_INFINITY)
                    -(((thickness(it) as Double?) ?: .0) / 2.0)
                }
            }
            if (category.dimension is Temporal) {
                val min = dataset.data.minOfOrNull {
                    (category.dimension as Temporal).accessor(it) - ((thickness(it) as Duration) / 2.0)
                }
                min?.let { (category as Axis<DOMAIN, Instant>).start = min }
            }
        }
        if (category.end == null) {
            if (category.dimension is Quantitative) {
                (category as Axis<DOMAIN, Double?>).end = dataset.data.maxOfOrNull {
                    ((category.dimension as Quantitative).accessor(it) ?: Double.NEGATIVE_INFINITY)
                    +(((thickness(it) as Double?) ?: .0) / 2.0)
                }
            }
            if (category.dimension is Temporal) {
                val max = dataset.data.maxOfOrNull {
                    (category.dimension as Temporal).accessor(it) + ((thickness(it) as Duration) / 2.0)
                }
                max?.let { (category as Axis<DOMAIN, Instant>).end = max }
            }
        }

    }

    override fun drawMark(drawingZone: DrawingZone, dataset: Dataset<DOMAIN>) {
        drawingZone.clear()
        eventZones.clear()
        dataset.data.map { datum ->
            val xPos = x.getPosition(datum)
            val yPos = y.getPosition(datum)
            if (xPos != null && yPos != null) markDecorator(datum, Point(xPos, yPos), drawingZone)
            createBar(datum)?.let { eventZones.add(EventZone(datum, RectZone(it, Point.origin), markIndex)) }
        }
    }

    override fun drawHighlight(drawingZone: DrawingZone, highlightedData: Collection<Datum<DOMAIN>>) {
        drawingZone.clear()
        highlightedData.map { datum ->
            val xPos = x.getPosition(datum)
            val yPos = y.getPosition(datum)
            if (xPos != null && yPos != null) highlightDecorator(datum, Point(xPos, yPos), drawingZone)
        }
    }

    override fun drawSelection(drawingZone: DrawingZone, selectedData: Collection<Datum<DOMAIN>>) {
        drawingZone.clear()
        selectedData.map { datum ->
            val xPos = x.getPosition(datum)
            val yPos = y.getPosition(datum)
            if (xPos != null && yPos != null) selectionDecorator(datum, Point(xPos, yPos), drawingZone)
        }
    }


    private fun getToPosition(datum: Datum<DOMAIN>): Double? {
        val catValue = category.values[datum.indexInData]
        return catValue?.let { stackValues[catValue]?.get(datum.indexOfSeries + 1) }?.let { quantity.scaleValue(it) }
    }

    private fun getFromPosition(datum: Datum<DOMAIN>): Double? {
        val catValue = category.values[datum.indexInData]
        return catValue?.let { stackValues[catValue]?.get(datum.indexOfSeries) }?.let { quantity.scaleValue(it) }
    }

    private fun getThickness(datum: Datum<DOMAIN>): Double? {
        val from = category.getPosition(datum)
        val to = when (category.dimension) {
            is Quantitative ->  {
                category.values[datum.indexInData]?.let {
                    val value = (it as Double) + ((thickness(datum) as Double?) ?: .0)
                    (category as Axis<DOMAIN, Double?>).scaleValue(value)
                }
            }
            is Temporal     -> {
                category.values[datum.indexInData]?.let {
                    val value = (it as Instant) + (thickness(datum) as Duration)
                    (category as Axis<DOMAIN, Instant>).scaleValue(value)
                }
            }
            else            -> error("Dimension type is not handled by this Mark: ${category.dimension}")
        }
        if (from == null || to == null) return null
        val thick = abs(to - from)
        return thick
    }

    private fun createBar(datum: Datum<DOMAIN>): Rect? {
        val width = getThickness(datum)
        val fromX = category.getPosition(datum)
        val from = getFromPosition(datum)
        val to = getToPosition(datum)
        return if (fromX != null && width != null && from != null && to != null) {
            RectGeom(fromX - (width / 2.0), min(from, to), width, abs(from - to))
        } else null
    }

    private fun DrawingZone.drawBar(bar: Rect, stroke: ColorOrGradient?, strokeWidth: Double?, fill: ColorOrGradient?) : Rect? {
        if (bar.x < -bar.width || bar.y < -bar.height || bar.x > contentWidth || bar.y > contentHeight) return null
        val clipX = max(.0, min(contentWidth, bar.x))
        val clipY = max(.0, min(contentHeight, bar.y))
        val clipWidth = bar.width + (bar.x - clipX)
        val clipHeight = bar.height + (bar.y - clipY)
        val rect = rect {
            x = clipX
            y = clipY
            width = if (clipX + clipWidth > contentWidth) contentWidth - clipX else clipWidth
            height = if (clipY + clipHeight > contentHeight) contentHeight - clipY else clipHeight

            this.fill = fill
            this.strokeColor = stroke
            this.strokeWidth = strokeWidth
        }
        return rect
    }

    override fun getEventZones(drawingZone: DrawingZone, dataset: Dataset<DOMAIN>): Collection<EventZone<DOMAIN>> = eventZones

    override fun getTooltipPosition(datum: Datum<DOMAIN>, drawingZone: DrawingZone): TooltipPosition? =
        createBar(datum)?.let { bar -> TooltipPosition(RectZone(bar), LayoutPosition.Right) }

}
