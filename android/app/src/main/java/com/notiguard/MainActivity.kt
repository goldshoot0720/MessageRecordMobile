package com.notiguard

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notiguard.service.ListenerAccess
import com.notiguard.ui.AppDetailViewModel
import com.notiguard.ui.HomeViewModel
import com.notiguard.ui.NotiGuardViewModelFactory
import com.notiguard.ui.RecordDetailViewModel
import com.notiguard.ui.SearchViewModel
import com.notiguard.ui.screens.SearchScreen
import com.notiguard.ui.screens.AppDetailScreen
import com.notiguard.ui.screens.HomeScreen
import com.notiguard.ui.screens.PermissionScreen
import com.notiguard.ui.screens.RecordDetailScreen
import com.notiguard.ui.theme.NotiGuardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            NotiGuardTheme {
                NotiGuardNavHost()
            }
        }
    }
}

private object Route {
    const val PERMISSION = "permission"
    const val HOME = "home"
    const val APP_DETAIL = "app/{pkg}/{label}"
    const val RECORD_DETAIL = "record/{id}"
    const val SEARCH = "search?pkg={pkg}&label={label}"
    fun search(pkg: String = "", label: String = "") = "search?pkg=${Uri.encode(pkg)}&label=${Uri.encode(label)}"

    fun appDetail(pkg: String, label: String) = "app/${Uri.encode(pkg)}/${Uri.encode(label)}"
    fun recordDetail(id: Long) = "record/$id"
}

@Composable
private fun NotiGuardNavHost() {
    val context = LocalContext.current
    val app = context.applicationContext as NotiGuardApp
    val repo = app.repository
    val identity = app.appIdentity
    val nav = rememberNavController()

    // 使用者可能在系統設定裡開了權限就回來，每次重組時重新判斷。
    var skippedPermission by remember { mutableStateOf(false) }
    val granted = ListenerAccess.isGranted(context)
    val start = if (granted || skippedPermission) Route.HOME else Route.PERMISSION

    NavHost(navController = nav, startDestination = start) {

        composable(Route.PERMISSION) {
            PermissionScreen(
                onOpenSettings = { context.startActivity(ListenerAccess.settingsIntent(context)) },
                onSkip = {
                    skippedPermission = true
                    nav.navigate(Route.HOME) { popUpTo(Route.PERMISSION) { inclusive = true } }
                },
            )
        }

        composable(Route.HOME) {
            val vm: HomeViewModel = viewModel(factory = NotiGuardViewModelFactory(repo))
            val state by vm.state.collectAsStateWithLifecycle()
            HomeScreen(
                state = state,
                iconFor = { pkg -> identity.icon(pkg) },
                onToggleMaster = vm::setMaster,
                onOpenSearch = { nav.navigate(Route.search()) },
                onOpenApp = { nav.navigate(Route.appDetail(it.packageName, it.appLabel)) },
                onOpenSettings = { context.startActivity(ListenerAccess.settingsIntent(context)) },
            )
        }

        composable(
            route = Route.APP_DETAIL,
            arguments = listOf(
                navArgument("pkg") { type = NavType.StringType },
                navArgument("label") { type = NavType.StringType },
            ),
        ) { entry ->
            val pkg = entry.arguments?.getString("pkg").orEmpty()
            val label = entry.arguments?.getString("label").orEmpty()
            val vm: AppDetailViewModel = viewModel(
                factory = NotiGuardViewModelFactory(repo, packageName = pkg, appLabel = label),
            )
            val state by vm.state.collectAsStateWithLifecycle()
            var pendingJson by remember { mutableStateOf<String?>(null) }
            val saver = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("application/json"),
            ) { uri ->
                val json = pendingJson
                pendingJson = null
                if (uri != null && json != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray())
                    }
                }
            }
            AppDetailScreen(
                state = state,
                icon = identity.icon(pkg),
                onBack = { nav.popBackStack() },
                onSetFilter = vm::setFilter,
                onOpenSearch = { nav.navigate(Route.search(pkg, label)) },
                onSetBlocking = vm::setBlocking,
                onOpenRecord = { nav.navigate(Route.recordDetail(it.id)) },
                onExport = {
                    vm.buildExport { fileName, json ->
                        pendingJson = json
                        saver.launch(fileName)
                    }
                },
            )
        }

        composable(
            route = Route.SEARCH,
            arguments = listOf(
                navArgument("pkg") { type = NavType.StringType; defaultValue = "" },
                navArgument("label") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { entry ->
            val pkg = entry.arguments?.getString("pkg").orEmpty()
            val label = entry.arguments?.getString("label").orEmpty()
            val vm: SearchViewModel = viewModel(factory = NotiGuardViewModelFactory(repo, packageName = pkg))
            val query by vm.query.collectAsStateWithLifecycle()
            val filter by vm.filter.collectAsStateWithLifecycle()
            val results by vm.results.collectAsStateWithLifecycle()
            SearchScreen(label, query, filter, results, vm::setQuery, vm::setFilter, vm::retry,
                onBack = { nav.popBackStack() },
                onOpenRecord = { nav.navigate(Route.recordDetail(it.id)) },
                iconFor = { identity.icon(it) })
        }

        composable(
            route = Route.RECORD_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val vm: RecordDetailViewModel = viewModel(
                factory = NotiGuardViewModelFactory(repo, recordId = id),
            )
            val record by vm.record.collectAsStateWithLifecycle()
            val blocking by vm.blocking.collectAsStateWithLifecycle()
            RecordDetailScreen(
                record = record,
                blocking = blocking,
                icon = record?.packageName?.let { identity.icon(it) },
                onBack = { nav.popBackStack() },
                onAllowApp = vm::allowApp,
                onBlockApp = vm::blockApp,
                onForgetApp = {
                    vm.forgetApp {
                        nav.popBackStack(Route.HOME, inclusive = false)
                    }
                },
            )
        }
    }
}
