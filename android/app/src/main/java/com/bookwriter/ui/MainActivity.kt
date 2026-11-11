package com.bookwriter.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bookwriter.viewmodel.BookTreeViewModel
import com.bookwriter.viewmodel.NotesViewModel
import com.bookwriter.service.TextProcessingService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BookWriterApp()
                }
            }
        }
    }
}

@Composable
fun BookWriterApp() {
    val notesVM: NotesViewModel = viewModel {
        NotesViewModel(TextProcessingService())
    }
    val bookTreeVM: BookTreeViewModel = viewModel {
        BookTreeViewModel()
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    BookWriterScaffold(selectedTab = selectedTab, onTabSelected = { selectedTab = it },
        notesVM = notesVM, bookTreeVM = bookTreeVM)
}