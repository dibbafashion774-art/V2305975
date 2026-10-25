package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CryptoPriceRow
import com.example.ui.theme.BinanceGold
import com.example.ui.theme.DarkBgPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.MarketCategory
import com.example.viewmodel.MarketViewModel
import com.example.viewmodel.SortType

@Composable
fun MarketsScreen(
    marketViewModel: MarketViewModel,
    onNavigateToPairDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by marketViewModel.searchQuery.collectAsState()
    val selectedCategory by marketViewModel.selectedCategory.collectAsState()
    val sortType by marketViewModel.sortType.collectAsState()
    val pairs by marketViewModel.filteredPairs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
    ) {
        // Top Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Markets",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${pairs.size} Pairs Available",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { marketViewModel.setSearchQuery(it) },
            placeholder = { Text("Search coin or symbol (e.g. BTC, ETH, SOL)", color = TextTertiary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { marketViewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("markets_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BinanceGold,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkCardBg,
                unfocusedContainerColor = DarkCardBg
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf(
                Pair(MarketCategory.ALL, "All Coins"),
                Pair(MarketCategory.FAVORITES, "★ Favorites"),
                Pair(MarketCategory.GAINERS, "Top Gainers"),
                Pair(MarketCategory.LOSERS, "Top Losers"),
                Pair(MarketCategory.VOLUME, "24h Volume")
            )
            items(categories) { (cat, label) ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BinanceGold else DarkCardBg)
                        .border(1.dp, if (isSelected) BinanceGold else DarkBorder, RoundedCornerShape(8.dp))
                        .clickable { marketViewModel.setCategory(cat) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("market_filter_${cat.name.lowercase()}")
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sorting Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Name / Vol",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                modifier = Modifier.clickable {
                    marketViewModel.setSortType(
                        if (sortType == SortType.NAME_ASC) SortType.DEFAULT else SortType.NAME_ASC
                    )
                }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Last Price",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (sortType == SortType.PRICE_DESC || sortType == SortType.PRICE_ASC) BinanceGold else TextTertiary,
                    modifier = Modifier
                        .clickable {
                            marketViewModel.setSortType(
                                if (sortType == SortType.PRICE_DESC) SortType.PRICE_ASC else SortType.PRICE_DESC
                            )
                        }
                        .padding(end = 24.dp)
                )
                Text(
                    text = "24h Change",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (sortType == SortType.CHANGE_DESC || sortType == SortType.CHANGE_ASC) BinanceGold else TextTertiary,
                    modifier = Modifier.clickable {
                        marketViewModel.setSortType(
                            if (sortType == SortType.CHANGE_DESC) SortType.CHANGE_ASC else SortType.CHANGE_DESC
                        )
                    }
                )
            }
        }

        // Pairs List
        if (pairs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedCategory == MarketCategory.FAVORITES) "No favorite coins saved yet.\nTap the star icon on any coin to add it to favorites." else "No cryptocurrencies found matching \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(pairs, key = { it.symbol }) { pair ->
                    CryptoPriceRow(
                        pair = pair,
                        onClick = { onNavigateToPairDetail(pair.symbol) },
                        onToggleFavorite = { marketViewModel.toggleFavorite(pair.symbol) },
                        showSparkline = true
                    )
                }
            }
        }
    }
}
