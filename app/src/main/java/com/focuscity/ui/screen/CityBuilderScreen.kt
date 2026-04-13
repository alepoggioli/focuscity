package com.focuscity.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import com.focuscity.ui.component.PixelGrid
import com.focuscity.ui.theme.*
import com.focuscity.ui.viewmodel.CityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityBuilderScreen(
    viewModel: CityViewModel = viewModel()
) {
    val cityState by viewModel.cityState.collectAsState()
    var showShop by remember { mutableStateOf(false) }
    var showBuildingInfo by remember { mutableStateOf(false) }
    var showMoveMode by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show errors as snackbar
    LaunchedEffect(cityState.errorMessage) {
        cityState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY CITY",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gold
                )
                CoinDisplay(coins = cityState.coins)
            }

            Spacer(Modifier.height(12.dp))

            // ── Grid ──
            PixelGrid(
                buildings = cityState.buildings,
                selectedBuilding = cityState.selectedBuilding,
                isPlacing = cityState.placingType != null,
                onCellTap = { x, y ->
                    if (cityState.placingType != null) {
                        // Placing mode: try to place
                        viewModel.placeBuilding(x, y)
                    } else if (showMoveMode && cityState.selectedBuilding != null) {
                        // Move mode: move selected building
                        viewModel.moveBuilding(x, y)
                        showMoveMode = false
                    } else {
                        // Selection mode
                        val building = viewModel.getBuildingAt(x, y)
                        if (building != null) {
                            viewModel.selectBuilding(building)
                            showBuildingInfo = true
                        } else {
                            viewModel.deselectBuilding()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ── Placing mode banner ──
            AnimatedVisibility(visible = cityState.placingType != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MidBlue)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap a cell to place ${cityState.placingType?.displayName ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    IconButton(onClick = { viewModel.cancelPlacing() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = ErrorRed)
                    }
                }
            }

            // ── Move mode banner ──
            AnimatedVisibility(visible = showMoveMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MidBlue)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap a new location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    IconButton(onClick = {
                        showMoveMode = false
                        viewModel.deselectBuilding()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = ErrorRed)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Build button ──
            if (cityState.placingType == null && !showMoveMode) {
                Button(
                    onClick = { showShop = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGreen)
                ) {
                    Text(
                        text = "BUILD",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Building Shop Bottom Sheet ──
    if (showShop) {
        ModalBottomSheet(
            onDismissRequest = { showShop = false },
            containerColor = DarkBlue
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BUILDING SHOP",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gold
                )
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
                                viewModel.startPlacing(type)
                                showShop = false
                            }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ── Building Info Dialog ──
    if (showBuildingInfo && cityState.selectedBuilding != null) {
        val building = cityState.selectedBuilding!!
        val type = try { BuildingType.valueOf(building.type) } catch (_: Exception) { null }

        if (type != null) {
            AlertDialog(
                onDismissRequest = {
                    showBuildingInfo = false
                    viewModel.deselectBuilding()
                },
                title = {
                    Text(type.displayName, style = MaterialTheme.typography.headlineSmall)
                },
                text = {
                    Column {
                        Text("Size: ${type.width}×${type.height}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Position: (${building.gridX}, ${building.gridY})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        if (type != BuildingType.HALL) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Refund: \uD83E\uDE99 ${type.refund}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoinGold
                            )
                        }
                    }
                },
                confirmButton = {
                    if (type != BuildingType.HALL) {
                        Row {
                            TextButton(onClick = {
                                showBuildingInfo = false
                                showMoveMode = true
                            }) {
                                Text("Move", color = SkyBlue)
                            }
                            TextButton(onClick = {
                                viewModel.deleteBuilding()
                                showBuildingInfo = false
                            }) {
                                Text("Delete", color = ErrorRed)
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showBuildingInfo = false
                        viewModel.deselectBuilding()
                    }) {
                        Text("Close", color = TextMuted)
                    }
                },
                containerColor = SurfaceLight
            )
        }
    }
}
