package com.example.presentation.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.presentation.ui.auth.AuthScreen
import com.example.presentation.ui.automation.AutomationScreen
import com.example.presentation.ui.chat.ChatScreen
import com.example.presentation.ui.drawer.SaraDrawerContent
import com.example.presentation.ui.home.HomeScreen
import com.example.presentation.ui.memory.MemoryScreen
import com.example.presentation.ui.notifications.NotificationsScreen
import com.example.presentation.ui.permissions.PermissionsScreen
import com.example.presentation.ui.personalities.PersonalitiesScreen
import com.example.presentation.ui.profile.ProfileScreen
import com.example.presentation.ui.imagegen.ImageGenScreen
import com.example.presentation.ui.models.ModelSelectorScreen
import com.example.presentation.ui.prompts.PromptLibraryScreen
import com.example.presentation.ui.assistant.AssistantToolsScreen
import com.example.presentation.ui.qrocr.QrOcrToolsScreen
import com.example.presentation.ui.search.SearchScreen
import com.example.presentation.ui.security.SecurityCenterScreen
import com.example.presentation.ui.sync.CloudSyncBackupScreen
import com.example.presentation.ui.privacy.PrivacyLegalScreen
import com.example.presentation.ui.companion.CompanionHubScreen
import com.example.presentation.ui.achievements.AchievementsScreen
import com.example.presentation.ui.performance.PerformanceDashboardScreen
import com.example.presentation.ui.help.HelpAboutScreen
import com.example.presentation.ui.settings.SettingsScreen
import com.example.presentation.ui.splash.SplashScreen
import com.example.presentation.ui.vision.VisionScreen
import com.example.presentation.ui.vision.VisionCameraScreen
import com.example.presentation.ui.documents.DocumentAiScreen
import com.example.presentation.ui.planner.PlannerRoutinesScreen
import com.example.presentation.ui.notes.NotesTasksScreen
import com.example.presentation.ui.history.HistoryDownloadsScreen
import com.example.presentation.ui.quickpanel.QuickPanelWidgetScreen
import com.example.presentation.ui.voice.VoiceScreen
import com.example.presentation.ui.workspace.WorkspaceScreen
import com.example.presentation.viewmodel.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Chat : BottomNavItem("chat", "Chat", Icons.Default.Chat)
    object Workspace : BottomNavItem("workspace", "Workspace", Icons.Default.Workspaces)
    object Voice : BottomNavItem("voice", "Voice", Icons.Default.Mic)
    object Memory : BottomNavItem("memory", "Memory", Icons.Default.Psychology)
    object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun SaraNavHost(
    mainViewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
    val userProfile by mainViewModel.currentUserProfile.collectAsState()
    val activePersonality by mainViewModel.activePersonality.collectAsState()
    val unreadNotificationsCount by mainViewModel.unreadNotificationsCount.collectAsState()

    val chatViewModel: ChatViewModel = viewModel(factory = SimpleViewModelFactory {
        ChatViewModel(mainViewModel.chatRepository)
    })

    val voiceViewModel: VoiceViewModel = viewModel(factory = SimpleViewModelFactory {
        VoiceViewModel(mainViewModel.chatRepository)
    })

    val memoryViewModel: MemoryViewModel = viewModel(factory = SimpleViewModelFactory {
        MemoryViewModel(mainViewModel.memoryRepository)
    })

    val personalitiesViewModel: PersonalitiesViewModel = viewModel(factory = SimpleViewModelFactory {
        PersonalitiesViewModel(mainViewModel.personalityRepository)
    })

    val profileViewModel: ProfileViewModel = viewModel(factory = SimpleViewModelFactory {
        ProfileViewModel(mainViewModel.authRepository)
    })

    val authViewModel: AuthViewModel = viewModel(factory = SimpleViewModelFactory {
        AuthViewModel(mainViewModel.authRepository)
    })

    val automationViewModel: AutomationViewModel = viewModel(factory = SimpleViewModelFactory {
        AutomationViewModel(mainViewModel.automationRepository)
    })

    val memoryItems by memoryViewModel.memoryItems.collectAsState()
    val recentMessages by chatViewModel.messages.collectAsState()

    var initialPromptForChat by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SaraDrawerContent(
                userProfile = userProfile,
                activePersonality = activePersonality,
                onNavigate = { route ->
                    coroutineScope.launch { drawerState.close() }
                    navController.navigate(route)
                },
                onLogout = {
                    coroutineScope.launch { drawerState.close() }
                    profileViewModel.logout()
                    navController.navigate("auth") {
                        popUpTo(0)
                    }
                }
            )
        }
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = currentRoute in listOf("home", "chat", "voice", "memory", "settings")

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = SurfaceDark,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    ) {
                        val bottomNavItems = listOf(
                            BottomNavItem.Home,
                            BottomNavItem.Chat,
                            BottomNavItem.Workspace,
                            BottomNavItem.Voice,
                            BottomNavItem.Memory,
                            BottomNavItem.Settings
                        )

                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title, fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AmoledBlack,
                                    selectedTextColor = NeonPurpleBright,
                                    indicatorColor = NeonPurpleBright,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_${item.route}")
                            )
                        }
                    }
                }
            },
            containerColor = AmoledBlack
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("splash") {
                    SplashScreen(
                        onSplashComplete = {
                            if (isLoggedIn) {
                                navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                            } else {
                                navController.navigate("auth") { popUpTo("splash") { inclusive = true } }
                            }
                        }
                    )
                }

                composable("auth") {
                    AuthScreen(
                        viewModel = authViewModel,
                        onAuthSuccess = {
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    )
                }

                composable("home") {
                    HomeScreen(
                        userProfile = userProfile,
                        activePersonality = activePersonality,
                        recentMessages = recentMessages,
                        memoryCount = memoryItems.size,
                        unreadNotificationsCount = unreadNotificationsCount,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onNavigateToChat = { prompt ->
                            initialPromptForChat = prompt
                            navController.navigate("chat")
                        },
                        onNavigateToVoice = { navController.navigate("voice") },
                        onNavigateToMemory = { navController.navigate("memory") },
                        onNavigateToPersonalities = { navController.navigate("personalities") },
                        onNavigateToNotifications = { navController.navigate("notifications") }
                    )
                }

                composable("chat") {
                    ChatScreen(
                        viewModel = chatViewModel,
                        activePersonality = activePersonality,
                        userLanguage = userProfile.language,
                        initialPrompt = initialPromptForChat,
                        onNavigateToVision = { navController.navigate("vision") }
                    )
                }

                composable("voice") {
                    com.example.presentation.ui.voice.ZoyaVoiceWebViewScreen()
                }

                composable("memory") {
                    MemoryScreen(viewModel = memoryViewModel)
                }

                composable("personalities") {
                    PersonalitiesScreen(
                        viewModel = personalitiesViewModel,
                        voiceViewModel = voiceViewModel
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onLogoutComplete = {
                            navController.navigate("auth") { popUpTo(0) }
                        }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        onNavigateToPermissions = { navController.navigate("permissions") },
                        onNavigateToSecurity = { navController.navigate("security") },
                        onNavigateToCloudSync = { navController.navigate("cloudsync") },
                        onNavigateToPrivacy = { navController.navigate("privacy") }
                    )
                }

                composable("security") {
                    SecurityCenterScreen(
                        onNavigateToPrivacy = { navController.navigate("privacy") },
                        onNavigateToCloudSync = { navController.navigate("cloudsync") }
                    )
                }

                composable("cloudsync") {
                    CloudSyncBackupScreen()
                }

                composable("privacy") {
                    PrivacyLegalScreen(
                        onNavigateToPermissions = { navController.navigate("permissions") }
                    )
                }

                composable("companion") {
                    CompanionHubScreen(
                        onNavigateToChat = { navController.navigate("chat") },
                        onNavigateToVoice = { navController.navigate("voice") }
                    )
                }

                composable("achievements") {
                    AchievementsScreen()
                }

                composable("performance") {
                    PerformanceDashboardScreen()
                }

                composable("help") {
                    HelpAboutScreen()
                }

                composable("automation") {
                    AutomationScreen(viewModel = automationViewModel)
                }

                composable("permissions") {
                    PermissionsScreen()
                }

                composable("vision") {
                    VisionScreen(
                        chatViewModel = chatViewModel,
                        activePersonality = activePersonality,
                        userLanguage = userProfile.language,
                        onNavigateToChat = { navController.navigate("chat") }
                    )
                }

                composable("notifications") {
                    NotificationsScreen(viewModel = automationViewModel)
                }

                composable("livevision") {
                    VisionCameraScreen(
                        onSendToChat = { prompt ->
                            initialPromptForChat = prompt
                            navController.navigate("chat")
                        }
                    )
                }

                composable("planner") {
                    PlannerRoutinesScreen(
                        onSendToChat = { prompt ->
                            initialPromptForChat = prompt
                            navController.navigate("chat")
                        }
                    )
                }

                composable("notestasks") {
                    NotesTasksScreen(
                        onSendToChat = { prompt ->
                            initialPromptForChat = prompt
                            navController.navigate("chat")
                        }
                    )
                }

                composable("history") {
                    HistoryDownloadsScreen()
                }

                composable("quickpanel") {
                    QuickPanelWidgetScreen(
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable("documents") {
                    DocumentAiScreen(
                        onSendToChat = { prompt ->
                            initialPromptForChat = prompt
                            navController.navigate("chat")
                        }
                    )
                }

                composable("assistant") {
                    AssistantToolsScreen()
                }

                composable("qrocr") {
                    QrOcrToolsScreen(
                        onSendToChat = { prompt ->
                            initialPromptForChat = prompt
                            navController.navigate("chat")
                        }
                    )
                }

                composable("models") {
                    ModelSelectorScreen(chatViewModel = chatViewModel)
                }

                composable("imagegen") {
                    ImageGenScreen()
                }

                composable("prompts") {
                    PromptLibraryScreen(
                        onSendToChat = { prompt ->
                            initialPromptForChat = prompt
                            navController.navigate("chat")
                        }
                    )
                }

                composable("workspace") {
                    WorkspaceScreen(
                        onSendPromptToChat = { prompt ->
                            initialPromptForChat = prompt
                            navController.navigate("chat")
                        }
                    )
                }

                composable("search") {
                    SearchScreen(
                        chatViewModel = chatViewModel,
                        memoryViewModel = memoryViewModel,
                        onNavigateToChat = { prompt ->
                            initialPromptForChat = prompt
                            navController.navigate("chat")
                        }
                    )
                }
            }
        }
    }
}

class SimpleViewModelFactory<T : androidx.lifecycle.ViewModel>(
    private val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}
