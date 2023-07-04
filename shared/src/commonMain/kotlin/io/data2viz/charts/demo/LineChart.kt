package io.data2viz.charts.demo

import io.data2viz.charts.chart.chart
import io.data2viz.charts.chart.mark.MarkCurves
import io.data2viz.charts.chart.mark.line
import io.data2viz.charts.chart.quantitative
import io.data2viz.charts.chart.temporal
import io.data2viz.charts.core.PanMode
import io.data2viz.charts.core.TriggerMode
import io.data2viz.charts.core.ZoomMode
import io.data2viz.viz.VizContainer
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun VizContainer.lineChart() {
    chart(transactions) {
        title = "Join account amount - March 2023"
        config {
            events {
                triggerMode = TriggerMode.Column
                zoomMode = ZoomMode.X
                panMode = PanMode.X
            }
        }

        val timeDimension = temporal({ domain.date }) {
            name = "Date of operation"
        }
        val amount = quantitative({ domain.amount }) {
            name = "Account total amount"
        }

        line(timeDimension, amount) {
            showMarkers = true
            curve = MarkCurves.Curved

            tooltip {
                this.formatter = {
                    "${domain.name} (${domain.date.toLocalDateTime(TimeZone.UTC).date}): ${domain.amount}€"
                }
            }

            y {
                enableAlternateBackground = true
            }
        }
    }
}

private data class Transaction(
    val account: String,
    val name: String,
    val date: Instant,
    val amount: Double
)

private val transactions = listOf(
    Transaction("Join account", "Highway fees", Instant.parse(("2023-03-01T22:19:44.475Z")), 7863.60),
    Transaction("Join account", "Gasoline", Instant.parse(("2023-03-06T12:19:44.475Z")), 7786.30),
    Transaction("Join account", "Sandwich", Instant.parse(("2023-03-06T14:19:44.475Z")), 7726.30),
    Transaction("Join account", "Incoming payment", Instant.parse(("2023-03-06T22:19:44.475Z")), 7936.30),
    Transaction("Join account", "Books", Instant.parse(("2023-03-08T22:19:44.475Z")), 7122.68),
    Transaction("Join account", "Flowers", Instant.parse(("2023-03-09T16:19:44.475Z")), 6932.66),
    Transaction("Join account", "Books", Instant.parse(("2023-03-09T22:19:44.475Z")), 6858.42),
    Transaction("Join account", "Salary", Instant.parse(("2023-03-11T22:19:44.475Z")), 8600.40),
    Transaction("Join account", "Bakery", Instant.parse(("2023-03-13T22:19:44.475Z")), 8596.30),
    Transaction("Join account", "Food", Instant.parse(("2023-03-19T22:19:44.475Z")), 8530.40),
    Transaction("Join account", "Gift", Instant.parse(("2023-03-19T23:19:44.475Z")), 8212.40),
    Transaction("Join account", "Gasoline", Instant.parse(("2023-03-20T16:19:44.475Z")), 7963.40),
    Transaction("Join account", "Food", Instant.parse(("2023-03-20T23:19:44.475Z")), 7859.40),
    Transaction("Join account", "Bakery", Instant.parse(("2023-03-21T12:19:44.475Z")), 7850.40),
    Transaction("Join account", "Books", Instant.parse(("2023-03-22T08:19:44.475Z")), 7630.40),
    Transaction("Join account", "Flat rent", Instant.parse(("2023-03-26T12:19:44.475Z")), 5622.40),
    Transaction("Join account", "Shoes", Instant.parse(("2023-03-26T14:19:44.475Z")), 5499.40),
    Transaction("Join account", "Food", Instant.parse(("2023-03-28T23:19:44.475Z")), 5463.40),
)
