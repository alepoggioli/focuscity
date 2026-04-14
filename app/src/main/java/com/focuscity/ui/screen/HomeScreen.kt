package com.focuscity.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuscity.data.model.BuildingType
import com.focuscity.ui.component.BuildingCard
import com.focuscity.ui.component.CoinDisplay
import com.focuscity.ui.component.DayDotGrid
import com.focuscity.ui.component.IsometricCityGrid
import com.focuscity.ui.theme.*
import com.focuscity.ui.viewmodel.CityViewModel
import com.focuscity.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onStartSession: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    cityViewModel: CityViewModel = viewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        if (page == 0) {
            CityPage(homeViewModel, cityViewModel, onStartSession)
        } else {
            StatsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityPage(
    homeViewModel: HomeViewModel,
    cityViewModel: CityViewModel,
    onStartSession: () -> Unit
) {
    val profile by homeViewModel.userProfile.collectAsState()
    val todayMinutes by homeViewModel.todayMinutes.collectAsState()
    
    val cityState by cityViewModel.cityState.collectAsState()
    var showShop by remember { mutableStateOf(false) }
    var showBuildingInfo by remember { mutableStateOf(false) }
    var showMoveMode by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(cityState.errorMessage) {
        cityState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            cityViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkNavy
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background Map
            IsometricCityGrid(
                buildings = cityState.buildings,
                gridSize = 50,
                selectedBuilding = cityState.selectedBuilding,
                isPlacing = cityState.placingType != null,
                onCellTap = { x, y ->
                    if (cityState.placingType != null) {
                        cityViewModel.placeBuilding(x, y)
                    } else if (showMoveMode && cityState.selectedBuilding != null) {
                        cityViewModel.moveBuilding(x, y)
                        showMoveMode = false
                    } else {
                        val building = cityViewModel.getBuildingAt(x, y)
                        if (building != null) {
                            cityViewModel.selectBuilding(building)
                            showBuildingInfo = true
                        } else {
                            cityViewModel.deselectBuilding()
                        }
                    }
                }
            )

            // Overlays (HUD)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top HUD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    CoinDisplay(coins = profile.coins, large = true)
                    
                    // Simple settings/sound placeholder
                    IconButton(onClick = {}) {
                        Text("⚙", style = MaterialTheme.typography.titleLarge)
                    }
                }

                // Temporary placement banners
                AnimatedVisibility(visible = cityState.placingType != null || showMoveMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MidBlue.copy(alpha = 0.9f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val txt = if (showMoveMode) "Tap a new location" else "Tap a cell to place ${cityState.placingType?.displayName}"
                        Text(text = txt, style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = {
                            if (showMoveMode) {
                                showMoveMode = false
                                cityViewModel.deselectBuilding()
                            } else {
                                cityViewModel.cancelPlacing()
                            }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = ErrorRed)
                        }
                    }
                }

                // Middle HUD: Floating Focus Calendar
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceLight.copy(alpha = 0.8f))
                        .padding(16.dp)
                ) {
                    // Since it's quick buttons, we just put the summary text and let them swipe for details.
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Today: $todayMinutes min", color = TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("Swipe left for Stats", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Bottom HUD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Shop button
                    Button(
                        onClick = { showShop = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("🛒", style = MaterialTheme.typography.titleLarge)
                    }

                    // Main Focus Button
                    Button(
                        onClick = onStartSession,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .height(64.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantPink)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(text = "FOCUS!", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }

    // Building Shop Bottom Sheet
    if (showShop) {
        ModalBottomSheet(
            onDismissRequest = { showShop = false },
            containerColor = DarkBlue
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BUILDING SHOP", style = MaterialTheme.typography.headlineMedium, color = Gold)
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(BuildingType.purchasable) { type ->
                        BuildingCard(
                            buildingType = type,
                            coins = cityState.coins,
                            onClick = {
                                cityViewModel.startPlacing(type)
                                showShop = false
                            }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // Building Info Dialog
    if (showBuildingInfo && cityState.selectedBuilding != null) {
        val building = cityState.selectedBuilding!!
        val type = try { BuildingType.valueOf(building.type) } catch (_: Exception) { null }

        if (type != null) {
            AlertDialog(
                onDismissRequest = {
                    showBuildingInfo = false
                    cityViewModel.deselectBuilding()
                },
                title = { Text(type.displayName) },
                text = {
                    Column {
                        Text("Position: (${building.gridX}, ${building.gridY})")
                    }
                },
                confirmButton = {
                    if (type != BuildingType.HALL) {
                        Row {
                            TextButton(onClick = {
                                showBuildingInfo = false
                                showMoveMode = true
                            }) { Text("Move", color = SkyBlue) }
                            TextButton(onClick = {
                                cityViewModel.deleteBuilding()
                                showBuildingInfo = false
                            }) { Text("Delete", color = ErrorRed) }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showBuildingInfo = false
                        cityViewModel.deselectBuilding()
                    }) { Text("Close", color = TextMuted) }
                },
                containerColor = SurfaceLight
            )
        }
    }
}
