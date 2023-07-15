package com.edxavier.cerberus_sms.ui.screens.incall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.helpers.MyCallsManager
import com.edxavier.cerberus_sms.ui.screens.dialer.DialKeyboard
import com.edxavier.cerberus_sms.ui.screens.dialer.KeyContent
import com.edxavier.cerberus_sms.ui.ui.theme.green_700
import com.edxavier.cerberus_sms.ui.ui.theme.red_700
import com.edxavier.cerberus_sms.ui.ui.theme.white_50
import kotlinx.coroutines.launch

@Composable
fun CallDialPad(
    viewModel: InCallViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        val dialKeys = listOf(
            KeyContent("1", ""),
            KeyContent("2", ""),
            KeyContent("3", ""),
            KeyContent("4", ""),
            KeyContent("5", ""),
            KeyContent("6", ""),
            KeyContent("7", ""),
            KeyContent("8", ""),
            KeyContent("9", ""),
            KeyContent("*", ""),
            KeyContent("0", ""),
            KeyContent("#", ""),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center
        ){
            items(dialKeys){kContent->
                DialKeyboard(
                    kContent.mainText,
                    kContent.secText,
                    onKeyPress = { char ->
                        viewModel.playDTMFTone(char.first())
                    },
                    textColor = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }


    }
}//