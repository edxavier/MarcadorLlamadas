package com.edxavier.cerberus_sms.ui.calls

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.ui.core.ui.MyBannerAd
import com.edxavier.cerberus_sms.ui.screens.calls.CallLogEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallsScreen(
    callLog: List<CallsLog>,
    navCtrl: NavHostController,
    viewModel: AppViewModel,
    listState: LazyListState
) {
    val grouped = groupByDate(callLog)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState,
    ) {
        item {
            MyBannerAd()
        }
        grouped.forEach { (label, items) ->
            stickyHeader {
                Surface(
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
            items(items = items, key = { it.id }) { call ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                            slideInVertically(
                                animationSpec = tween(durationMillis = 300)
                            ) { it / 4 }
                ) {
                    CallLogEntry(
                        call = call,
                        navCtrl = navCtrl,
                        viewModel = viewModel,
                        timeOnly = true
                    )
                }
            }
        }
    }
}

private fun groupByDate(calls: List<CallsLog>): List<Pair<String, List<CallsLog>>> {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val groups = mutableMapOf<String, MutableList<CallsLog>>()
    for (call in calls) {
        val label = when {
            call.callDate.timeInMillis >= today.timeInMillis -> "Hoy"
            call.callDate.after(yesterday) -> "Ayer"
            else -> dateFormat.format(call.callDate.timeInMillis)
        }
        groups.getOrPut(label) { mutableListOf() }.add(call)
    }
    return groups.toList()
}
