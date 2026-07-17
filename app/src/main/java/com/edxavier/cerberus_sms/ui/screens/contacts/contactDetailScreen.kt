package com.edxavier.cerberus_sms.ui.screens.contacts

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.helpers.*
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailsScreen(
    viewModel: AppViewModel,
    navController: NavHostController
) {
    val myContext = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val contact = viewModel.selectedContact
    var favContact by remember { mutableStateOf(contact.favorite) }
    var showMenu by remember { mutableStateOf(false) }
    var clickNum by remember { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(true){
        viewModel.getContactNumbers(contact.id)
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title ={
                    Text(text =  contact.name, softWrap = false, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {
                        viewModel.setContactAsFav(!favContact, contact.id)
                        favContact = !favContact
                    }) {
                        if(favContact) {
                            Icon(
                                ImageVector.vectorResource(id = R.drawable.star_filled),
                                null, modifier = Modifier.size(26.dp)
                            )
                        }else{
                            Icon(ImageVector.vectorResource(id = R.drawable.favorite_outline), null)
                        }                    }
                    IconButton(onClick = {
                        val lookupUri = ContactsContract.Contacts.getLookupUri(contact.id.toLong(), contact.lookupKey)
                        val editIntent = Intent(Intent.ACTION_EDIT).apply {
                            setDataAndType(lookupUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE)
                        }
                        editIntent.putExtra("finishActivityOnSaveCompleted", true)
                        myContext.startActivity(editIntent)
                    }) {
                        Icon(Icons.Outlined.Edit, null)
                    }
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {
            state.contactNumbers.forEach { cNumber->
                Card(modifier = Modifier.padding(12.dp)) {
                    Column {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    showMenu = if (cNumber.number == clickNum)
                                        !showMenu
                                    else
                                        true
                                    clickNum = cNumber.number
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            if(cNumber.whatsapp){
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.whatsapp_com),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cNumber.number.clearPhoneString().toPhoneFormat(),
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                            cNumber.operator?.let { op ->
                                if(!op.operator.getOperatorString().startsWith("INTE")) {
                                    Text(
                                        text = op.operator.getOperatorString()
                                            .replaceFirstChar { c -> c.uppercase() },
                                        modifier = Modifier
                                            .background(
                                                color = Color(
                                                    op.operator.getOperatorColor(
                                                        LocalContext.current
                                                    )
                                                ),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(visible = (showMenu && clickNum == cNumber.number)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Row(
                                Modifier
                                    .padding(6.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                var locked by remember {
                                    mutableStateOf(BlockedNumberContract.isBlocked(myContext, cNumber.number))
                                }
                                val icon = if(locked) ImageVector.vectorResource(
                                    id = R.drawable.lock_on
                                )else ImageVector.vectorResource(
                                    id = R.drawable.lock_off
                                )
                                Surface(
                                    onClick = {
                                        if (locked) {
                                            BlockedNumberContract.unblock(myContext, cNumber.number)
                                        } else {
                                            viewModel.blockNumber(cNumber.number)
                                        }
                                        locked = !locked
                                    },
                                    shape = CircleShape,
                                    tonalElevation = 1.dp,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Box(modifier = Modifier.padding(12.dp)) {
                                        Icon(imageVector = icon, contentDescription = "Bloquear")
                                    }
                                }
                                Surface(
                                    onClick = { myContext.sendSms(cNumber.number) },
                                    shape = CircleShape,
                                    tonalElevation = 1.dp,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Box(modifier = Modifier.padding(12.dp)) {
                                        Icon(imageVector = ImageVector.vectorResource(id = R.drawable.chat), contentDescription = "SMS")
                                    }
                                }
                                if(cNumber.whatsapp){
                                    Surface(
                                        onClick = {
                                            val url = "https://api.whatsapp.com/send?phone=${cNumber.number.toPhoneFormat()}"
                                            try {
                                                val pm: PackageManager = myContext.packageManager
                                                val i = Intent(Intent.ACTION_VIEW)
                                                i.setPackage("com.whatsapp")
                                                i.data = Uri.parse(url)
                                                if (i.resolveActivity(pm) != null) {
                                                    myContext.startActivity(i)
                                                } else {
                                                    Toast.makeText(myContext, "Whatsapp no instalado", Toast.LENGTH_LONG).show()
                                                }
                                            } catch (e: PackageManager.NameNotFoundException) {
                                                Toast.makeText(myContext, "Whatsapp no instalado", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        shape = CircleShape,
                                        tonalElevation = 1.dp,
                                        color = Color(0xFF25D366).copy(alpha = 0.15f)
                                    ) {
                                        Box(modifier = Modifier.padding(12.dp)) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(id = R.drawable.whatsapp_com),
                                                contentDescription = "WhatsApp",
                                                tint = Color(0xFF25D366)
                                            )
                                        }
                                    }
                                }
                                Surface(
                                    onClick = { myContext.makeCall(cNumber.number) },
                                    shape = CircleShape,
                                    tonalElevation = 1.dp,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Box(modifier = Modifier.padding(12.dp)) {
                                        Icon(imageVector = Icons.Outlined.Phone, contentDescription = "Llamar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}