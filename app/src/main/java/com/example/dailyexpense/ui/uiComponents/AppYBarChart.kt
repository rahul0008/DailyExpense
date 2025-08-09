package com.example.dailyexpense.ui.uiComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle
import com.example.dailyexpense.ui.presenter.expenseReport.ChartDataEntry

@Composable
fun AppYBarChart(
    title: String,
    chartDataEntries: List<ChartDataEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    axisLabelColor: Color = MaterialTheme.colorScheme.onBackground,
    chartHeight: Dp = 250.dp
) {
    if (chartDataEntries.isEmpty()) {
        AppText( // Assuming AppText is defined and accessible
            text = "No data available for $title.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(16.dp)
        )
        return
    }

    val individualBarsData: List<BarData> = chartDataEntries.mapIndexed { index, entry ->
        BarData(
            point = Point(x = index.toFloat(), y = entry.value),
            label = entry.label,
            color = barColor,
            description = "Bar for ${entry.label}, value ${entry.value}"
        )
    }

    val maxYValue = chartDataEntries.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f // Ensure maxYValue is at least 1 for sensible division, or handle 0 case
    val yAxisStepCount = 5 // Number of intervals

    val xAxisData = AxisData.Builder()
        .axisStepSize(100.dp)
        .steps(if (chartDataEntries.size > 1) chartDataEntries.size - 1 else 0)
        .bottomPadding(16.dp)
        .labelData { index -> chartDataEntries.getOrNull(index)?.label ?: "" }
        .axisLabelAngle(if (chartDataEntries.any { it.label.length > 7 }) 30f else 0f)
        .labelAndAxisLinePadding(15.dp)
        .axisLineColor(axisLabelColor)
        .axisLabelColor(axisLabelColor)
        .build()

    // 4. Configure Y-Axis - CORRECTED
    val yAxisData = AxisData.Builder()
        .steps(yAxisStepCount) // Number of intervals. Will result in `yAxisStepCount + 1` labels.
        .labelData { index -> // index is an Int from 0 to yAxisStepCount
            // Calculate the data value for this specific Y-axis tick mark
            val stepValue = maxYValue / yAxisStepCount
            val labelValueAtTick = stepValue * index
            "%.0f".format(labelValueAtTick) // Format the calculated Float
        }
        .axisLineColor(axisLabelColor)
        .axisLabelColor(axisLabelColor)
        // .maxRange(maxYValue) // Consider explicitly setting maxRange if labels don't align perfectly
        .build()

    val barChartData = BarChartData(
        chartData = individualBarsData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        backgroundColor = MaterialTheme.colorScheme.surface,
        barStyle = BarStyle(
            barWidth = 20.dp,
            cornerRadius = 4.dp,
            isGradientEnabled = false
        ),
        paddingEnd = 16.dp,
        paddingTop = 16.dp,
        horizontalExtraSpace = 10.dp,
    )

    Column(modifier = modifier) {
        AppText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
        )
        BarChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            barChartData = barChartData
        )
    }
}

// Dummy AppText for preview - ensure this is present or your actual AppText is accessible
@Composable
fun AppText(text: String, style: androidx.compose.ui.text.TextStyle, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = text, style = style, modifier = modifier)
}


@Preview(showBackground = true, name = "Bar Chart Preview")
@Composable
fun AppYBarChartPreview() {
    MaterialTheme { // Or your app's specific theme like DailyExpenseTheme
        Surface(modifier = Modifier.fillMaxSize()) {
            AppYBarChart(
                title = "Monthly Expenses Preview",
                chartDataEntries = listOf(
                    ChartDataEntry("Jan", 120f),
                    ChartDataEntry("Feb", 200f),
                    ChartDataEntry("Mar", 150f),
                    ChartDataEntry("Apr", 80f),
                    ChartDataEntry("May", 220f),
                    ChartDataEntry("Jun", 180f)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Other previews (Empty, SingleItem) as before...
@Preview(showBackground = true, name = "Bar Chart Preview - Empty Data")
@Composable
fun AppYBarChartEmptyPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppYBarChart(
                title = "Monthly Expenses - No Data",
                chartDataEntries = emptyList(),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Bar Chart Preview - Single Item")
@Composable
fun AppYBarChartSingleItemPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppYBarChart(
                title = "Single Expense Category",
                chartDataEntries = listOf(
                    ChartDataEntry("Groceries", 350.75f)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
