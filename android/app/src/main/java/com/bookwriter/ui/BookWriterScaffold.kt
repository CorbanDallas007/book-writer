package com.bookwriter.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.*
import androidx.navigation.compose.navigation
import com.bookwriter.model.Chapter
import com.bookwriter.model.Note
import com.bookwriter.model.StoryContext
import com.bookwriter.viewmodel.BookTreeViewModel
import com.bookwriter.viewmodel.NotesViewModel

@Composable
fun BookWriterScaffold(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    notesVM: NotesViewModel,
    bookTreeVM: BookTreeViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "record",
        destinationRouteMap = mapOf(
            "record" to Unit::class.java,
            "book" to Unit::class.java,
            "lore" to Unit::class.java
        )
    ) {
        composable("record") {
            RecordScreen(navController = navController, notesVM = notesVM)
        }

        composable("book") {
            BookScreen(navController = navController, bookTreeVM = bookTreeVM, notesVM = notesVM)
        }

        composable("lore") {
            LoreScreen(navController = navController, bookTreeVM = bookTreeVM)
        }
    }

    BottomNavigation(
        modifier = Modifier.fillMaxWidth(),
        selectedIndex = selectedTab,
        onSelect = onTabSelected
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Mic, contentDescription = null) },
            label = { Text("Запись") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0); navController.navigate("record") }
        )

        BottomNavigationItem(
            icon = { Icon(Icons.Default.Book, contentDescription = null) },
            label = { Text("Книга") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1); navController.navigate("book") }
        )

        BottomNavigationItem(
            icon = { Icon(Icons.Default.BookOpen, contentDescription = null) },
            label = { Text("Лор") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2); navController.navigate("lore") }
        )
    }