package com.phoenix.fitpro.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.phoenix.fitpro.R
import com.phoenix.fitpro.presentation.ui.coach.CoachProgramScreen
import com.phoenix.fitpro.presentation.ui.dashboard.DashboardScreen
import com.phoenix.fitpro.presentation.ui.nutrition.AddMealScreen
import com.phoenix.fitpro.presentation.ui.nutrition.NutritionScreen
import com.phoenix.fitpro.presentation.ui.profile.AchievementsScreen
import com.phoenix.fitpro.presentation.ui.profile.ProfileScreen
import com.phoenix.fitpro.presentation.ui.stats.StatsScreen
import com.phoenix.fitpro.presentation.ui.workout.AddWorkoutScreen
import com.phoenix.fitpro.presentation.ui.workout.SportLibraryScreen
import com.phoenix.fitpro.presentation.ui.workout.WorkoutScreen

// ── Route constants ───────────────────────────────────────────────────────────
object Routes {
    const val DASHBOARD       = "dashboard"
    const val WORKOUT         = "workout"
    const val COACH           = "coach"
    const val ADD_WORKOUT     = "add_workout"
    const val SPORT_LIBRARY   = "sport_library"
    const val NUTRITION       = "nutrition"
    const val ADD_MEAL        = "add_meal"
    const val STATS           = "stats"
    const val PROFILE         = "profile"
    const val ACHIEVEMENTS    = "achievements"
}

// ── Bottom navigation tabs ────────────────────────────────────────────────────
enum class BottomTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    DASHBOARD(Routes.DASHBOARD, R.string.nav_home, Icons.Rounded.Home, Icons.Rounded.Home),
    WORKOUT(Routes.WORKOUT, R.string.nav_workout, Icons.Rounded.FitnessCenter, Icons.Rounded.FitnessCenter),
    COACH(Routes.COACH, R.string.nav_coach, Icons.Rounded.AutoAwesome, Icons.Rounded.AutoAwesome),
    NUTRITION(Routes.NUTRITION, R.string.nav_nutrition, Icons.Rounded.Restaurant, Icons.Rounded.Restaurant),
    STATS(Routes.STATS, R.string.nav_stats, Icons.Rounded.BarChart, Icons.Rounded.BarChart),
    PROFILE(Routes.PROFILE, R.string.nav_profile, Icons.Rounded.Person, Icons.Rounded.Person)
}

@Composable
fun PhoenixNavHost() {
    val navController = rememberNavController()
    val bottomTabs = BottomTab.values()

    // Determine if current route is a bottom-level route
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showBottomBar = bottomTabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PhoenixBottomBar(
                    tabs = bottomTabs,
                    currentRoute = currentRoute,
                    onTabSelected = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / 4 },
                    animationSpec = tween(300)
                ) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(300)
                ) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(300)
                ) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it / 4 },
                    animationSpec = tween(300)
                ) + fadeOut(tween(300))
            }
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onAddWorkout = { navController.navigate(Routes.ADD_WORKOUT) },
                    onAddMeal = { navController.navigate(Routes.ADD_MEAL) },
                    onViewAchievements = { navController.navigate(Routes.ACHIEVEMENTS) }
                )
            }
            composable(Routes.WORKOUT) {
                WorkoutScreen(
                    onAddWorkout = { navController.navigate(Routes.ADD_WORKOUT) },
                    onOpenSportLibrary = { navController.navigate(Routes.SPORT_LIBRARY) }
                )
            }
            composable(Routes.COACH) {
                CoachProgramScreen()
            }
            composable(Routes.ADD_WORKOUT) {
                AddWorkoutScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SPORT_LIBRARY) {
                SportLibraryScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.NUTRITION) {
                NutritionScreen(
                    onAddMeal = { navController.navigate(Routes.ADD_MEAL) }
                )
            }
            composable(Routes.ADD_MEAL) {
                AddMealScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.STATS) {
                StatsScreen()
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onViewAchievements = { navController.navigate(Routes.ACHIEVEMENTS) }
                )
            }
            composable(Routes.ACHIEVEMENTS) {
                AchievementsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun PhoenixBottomBar(
    tabs: Array<BottomTab>,
    currentRoute: String?,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar(
        containerColor = com.phoenix.fitpro.presentation.theme.DarkSurface,
        tonalElevation = androidx.compose.ui.unit.Dp.Unspecified
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = stringResource(tab.labelRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = com.phoenix.fitpro.presentation.theme.ElectricBlueAlpha15,
                    unselectedIconColor = com.phoenix.fitpro.presentation.theme.TextSecondary,
                    unselectedTextColor = com.phoenix.fitpro.presentation.theme.TextSecondary
                )
            )
        }
    }
}
