package com.edxavier.cerberus_sms.ui.screens.incall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edxavier.cerberus_sms.ui.screens.dialer.DialKeyboard
import com.edxavier.cerberus_sms.ui.screens.dialer.KeyContent

@Composable
fun CallDialPad(viewModel: InCallViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            items(dialKeys) { kContent ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DialKeyboard(
                        mainText = kContent.mainText,
                        secondaryText = kContent.secText,
                        textColor = Color.White,
                        fontSize = 22.sp,
                        keySize = 56.dp,
                        onKeyPress = { char ->
                            viewModel.playDTMFTone(char.first())
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            onClick = { viewModel.showDialPad() },
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.10f),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Cerrar teclado",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ocultar",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private val dialKeys = listOf(
    KeyContent("1", ""),
    KeyContent("2", "ABC"),
    KeyContent("3", "DEF"),
    KeyContent("4", "GHI"),
    KeyContent("5", "JKL"),
    KeyContent("6", "MNO"),
    KeyContent("7", "PQRS"),
    KeyContent("8", "TUV"),
    KeyContent("9", "WXYZ"),
    KeyContent("*", ""),
    KeyContent("0", "+"),
    KeyContent("#", ""),
)
