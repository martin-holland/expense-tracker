package com.example.expensetracker.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import com.example.expensetracker.viewmodel.DashBoardViewModel
import com.example.theme.com.example.expensetracker.LocalAppColors
import kotlin.text.category

@Composable
fun DashboardScreen(
    viewModel: DashBoardViewModel = viewModel { DashBoardViewModel() }
){

    val appColors = LocalAppColors.current
    val uiState = viewModel.uiState

    val totalSpent = uiState.expense.sumOf{it.amount}
    val chosenCurrenty = Currency.USD
    val currentMonth = "October 2025"

    val categorySumMap = uiState.expense.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toSortedMap(compareBy<ExpenseCategory> { it.displayName })

    println(categorySumMap)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
            .statusBarsPadding() // Add padding for system status bar
    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Header()
            MonthlySpendCard(totalSpent,currentMonth,chosenCurrenty)

            ExpenseBreakdownCard(categorySumMap)
            Text("This is overview")
            Text("This is pie chart")
            Text("This is graph chart")
        }
    }
}

@Composable
private fun Header(){
    val appColors = LocalAppColors.current
    val headerPadding by animateDpAsState(
        targetValue =  16.dp
    )
    val titleSize by animateFloatAsState(
        targetValue = 1f
    )

    Surface (
        modifier = Modifier.fillMaxWidth(),
        color = appColors.background,
        shadowElevation = 0.dp
    ){
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = headerPadding, bottom = headerPadding)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ){
                Box(
                    modifier = Modifier
                        .size(48.dp * titleSize)
                        .clip(CircleShape)
                        .background(Color(0xFF00BCD4)), // Teal color
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "App Icon",
                        tint = appColors.primaryForeground,
                        modifier = Modifier.size(24.dp * titleSize)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Expense Dashboard",
                        style =
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = appColors.foreground
                    )

                    Text(
                        text = "Expense Tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.mutedForeground
                    )

                }
            }
        }

    }
}

@Composable
fun MonthlySpendCard(spend: Double, month: String, currency: Currency) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00BFAE)),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Monthly Spend",
                color = Color.White,
                modifier = Modifier.padding(4.dp))
            Text("${retrieveCurrencySymbol(currency)}${spend}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                modifier = Modifier.padding(4.dp))
            Text(month, color = Color.White, modifier = Modifier.padding(4.dp))
        }
    }
}

private fun retrieveCurrencySymbol(currency: Currency):String{
    return currency.symbol
}

@Composable
fun ExpenseBreakdownCard(categorySumMap:Map<ExpenseCategory,Double>){
    Card (
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .border(BorderStroke(2.dp,Color(0xFFececf0)))
    ){
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Expense Breakdown", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            CategoryGrid(categorySumMap)

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryGrid(categorySumMap: Map<ExpenseCategory, Double>) {
    val categories = categorySumMap.keys.toList()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            CategorySummaryCard(
                category = category,
                amount = categorySumMap[category] ?: 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }
    }
}

@Composable
fun CategorySummaryCard(
    category: ExpenseCategory,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFececf0), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.displayName,
                    tint = Color.Unspecified
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(category.displayName, style = MaterialTheme.typography.bodyMedium)
                Text("$${amount.toInt()}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private fun <K,V> Map<K,V>.toSortedMap(comparator:Comparator< in K>):Map<K,V>{
    return this.entries
        .sortedWith(compareBy(comparator){ it.key })
        .associate { it.toPair() }
}
