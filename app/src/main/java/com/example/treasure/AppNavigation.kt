/*
* Hunter Clarke
* OSU CS 492
* */

package com.example.treasure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val timerState = remember { TimerState() }

    NavHost(navController = navController, startDestination = "permissionPage") {
        composable("permissionPage") { PermissionPage(navController) }
        composable("startPage") { StartPage(navController, timerState) }
        composable("cluePage") { CluePage(navController, timerState) }
        composable("cluePage2") { CluePage2(navController, timerState) }
        composable("clueSolvedPage") { ClueSolvedPage(navController, timerState) }
        composable("treasureHuntCompletedPage") { TreasureHuntCompletedPage(navController, timerState) }
    }
}