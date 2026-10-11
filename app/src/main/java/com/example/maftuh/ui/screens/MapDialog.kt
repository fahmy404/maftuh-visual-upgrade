package com.example.maftuh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maftuh.data.LocationId
import com.example.maftuh.ui.theme.AmberGold
import com.example.maftuh.ui.theme.ForensicGreen
import com.example.maftuh.ui.theme.NoirDark
import com.example.maftuh.ui.theme.NoirSurface
import com.example.maftuh.ui.theme.NoirSurfaceVariant

@Composable
fun MapDialog(
    currentLocation: LocationId,
    onSelectLocation: (LocationId) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("map_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NoirSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberGold)
                        Text(
                            text = "خريطة التحرك في القاهرة (مدينة نصر)",
                            color = AmberGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_map_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LocationId.values().forEach { loc ->
                    val isCurrent = (loc == currentLocation)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable(enabled = !isCurrent) { onSelectLocation(loc) }
                            .testTag("map_loc_${loc.name}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) AmberGold.copy(alpha = 0.15f) else NoirSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) AmberGold else Color.DarkGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = when (loc) {
                                    LocationId.APARTMENT -> "🏢"
                                    LocationId.STREET -> "🚗"
                                    LocationId.STATION -> "👮‍♂️"
                                },
                                fontSize = 24.sp
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = loc.displayNameAr,
                                    color = if (isCurrent) AmberGold else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = loc.subtitleAr,
                                    color = Color.LightGray,
                                    fontSize = 10.5.sp
                                )
                            }

                            if (isCurrent) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ForensicGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "موقعك الحالي",
                                        color = ForensicGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DirectionsWalk,
                                    contentDescription = "انتقال",
                                    tint = AmberGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
