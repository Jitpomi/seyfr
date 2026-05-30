package com.jitpomi.seyfr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jitpomi.seyfr.ui.components.AppLogo
import com.jitpomi.seyfr.ui.components.CustomSnackbar
import com.jitpomi.seyfr.ui.screens.ReceiveScreen
import com.jitpomi.seyfr.ui.screens.SendScreen
import com.jitpomi.seyfr.ui.screens.SupportScreen
import com.jitpomi.seyfr.ui.theme.SeyfrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // NDK initialization has been moved to SeyfrApplication

        enableEdgeToEdge()
        setContent {
            SeyfrTheme {
                val viewModel = remember { AppViewModel(applicationContext) }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "splash_screen") {
                    composable("splash_screen") {
                        com.jitpomi.seyfr.ui.screens.AnimatedSplashScreen(
                            onSplashComplete = {
                                navController.navigate("home_screen") {
                                    popUpTo("splash_screen") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("home_screen") {
                        SeyfrApp(
                            uiState = uiState,
                            onSend = viewModel::send,
                            onReceive = viewModel::receive,
                            onClearSend = viewModel::clearSend,
                            onSetDestination = viewModel::setDestination
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeyfrApp(
    uiState: AppUiState,
    onSend: (String) -> Unit,
    onReceive: (String) -> Unit,
    onClearSend: () -> Unit,
    onSetDestination: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.sendStatus) {
        val message = when (val status = uiState.sendStatus) {
            is TransferStatus.Success -> status.message
            is TransferStatus.Error -> status.message
            else -> null
        }
        message?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(uiState.receiveStatus) {
        val message = when (val status = uiState.receiveStatus) {
            is TransferStatus.Success -> status.message
            is TransferStatus.Error -> status.message
            else -> null
        }
        message?.let { snackbarHostState.showSnackbar(it) }
    }

    val onCopyTicket: (String) -> Unit = { ticket ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("ticket", ticket))
    }

    val onShareTicket: (String) -> Unit = { ticket ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, ticket)
        }
        context.startActivity(Intent.createChooser(intent, "Share ticket"))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                CustomSnackbar(snackbarData = data)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowUp,
                            contentDescription = "Send"
                        )
                    },
                    label = { Text("Send") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Receive"
                        )
                    },
                    label = { Text("Receive") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Support"
                        )
                    },
                    label = { Text("Support") }
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                AppLogo()

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> SendScreen(
                            uiState = uiState,
                            onSend = onSend,
                            onClearSend = onClearSend,
                            onCopyTicket = onCopyTicket,
                            onShareTicket = onShareTicket
                        )
                        1 -> ReceiveScreen(
                            uiState = uiState,
                            onReceive = onReceive,
                            onSetDestination = onSetDestination
                        )
                        2 -> SupportScreen()
                    }
                }
            }
        }
    }
}
