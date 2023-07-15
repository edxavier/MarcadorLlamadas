package com.edxavier.cerberus_sms.ui.screens.dialer

import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.helpers.getOperatorColor
import com.edxavier.cerberus_sms.helpers.getOperatorString

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun NumberInput(
    valueText:String = "",
    cursorPos: Int = 0,
    operator: Operator? = null,
    onBackSpace:(value:String, cursorPos:Int) -> Unit,
    onCursorPosChange:(cursorPos:Int) -> Unit
) {
    var cPos by remember { mutableStateOf(cursorPos) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        operator?.let {op ->
            if(!op.operator.getOperatorString().startsWith("INTE")) {
                val opStr = if(op.operator.getOperatorString().startsWith("LINEA")){
                    "${op.area} ${op.country}"
                }
                else{
                    op.operator.getOperatorString().replaceFirstChar { c -> c.uppercase() }
                }
                Text(
                    text = opStr,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            color = Color(
                                op.operator.getOperatorColor(LocalContext.current)
                            )
                        )
                        .padding(horizontal = 8.dp),
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)) {
            val textColor = MaterialTheme.colorScheme.onSurface
            AndroidView(
                factory = {
                    AppCompatEditText(it).apply {
                        isFocusable = true
                        isFocusableInTouchMode = true
                        showSoftInputOnFocus = false
                        setTextColor(textColor.toArgb())
                        textSize = 24f
                        textAlignment =  View.TEXT_ALIGNMENT_CENTER
                        setOnFocusChangeListener { view, _ ->
                            val tView = view as AppCompatEditText
                            cPos = tView.selectionStart
                            onCursorPosChange(cPos)
                        }
                        setOnClickListener { v ->
                            val tView = v as AppCompatEditText
                            cPos = tView.selectionStart
                            onCursorPosChange(cPos)
                        }
                    }
                },
                update = {
                    it.setText(valueText)
                    if(valueText.isNotEmpty() && (cPos >=0 && cPos < valueText.length)) {
                        it.setSelection(cPos)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.delete),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = {
                            if (cPos > 0 && cPos < valueText.length) {
                                val tmp = valueText
                                    .removeRange(
                                        cPos - 1,
                                        cPos
                                    )
                                cPos -= 1
                                onBackSpace(tmp, cPos)
                            } else {
                                if (valueText.isNotEmpty()) {
                                    cPos = valueText.length - 1
                                    onBackSpace(valueText.substring(0, valueText.length - 1), cPos)
                                } else {
                                    cPos = -1
                                    onBackSpace("", -1)
                                }
                            }
                        },
                        onLongClick = {
                            cPos = -1
                            onBackSpace("", -1)
                        }
                    )
                    .padding(8.dp)
            )
        }
    }
}