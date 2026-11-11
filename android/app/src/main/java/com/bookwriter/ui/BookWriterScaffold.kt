package com.bookwriter.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bookwriter.model.Note
import com.bookwriter.viewmodel.BookTreeViewModel
import com.bookwriter.viewmodel.NotesViewModel

@Composable
fun BookWriterScaffold(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    notesVM: NotesViewModel,
    bookTreeVM: BookTreeViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        when (selectedTab) {
            0 -> RecordScreen(notesVM = notesVM)
            1 -> BookScreen(bookTreeVM = bookTreeVM, notesVM = notesVM)
            2 -> LoreScreen(bookTreeVM = bookTreeVM)
        }

        Spacer(modifier = Modifier.weight(1f))

        BottomNavigation(
            selectedTabIndex = selectedTab,
            onTabSelected = { onTabSelected(it) },
            modifier = Modifier.fillMaxWidth()
        ) {
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Mic, contentDescription = null) },
                label = { Text("Запись") },
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Book, contentDescription = null) },
                label = { Text("Книга") },
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Default.BookOpen, contentDescription = null) },
                label = { Text("Лор") },
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
fun RecordScreen(notesVM: NotesViewModel) {
    var transcribedText by remember { mutableStateOf("Готов к записи…") }
    var isRecording by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = transcribedText,
            style = MaterialTheme.typography.bodyLarge,
            color = if (transcribedText == "Готов к записи…") Color(0xFF8E8E93) else Color(0xFF1D1D1F),
            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp).background(Color(0xFFF2F2F7)).padding(16.dp).clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { isRecording = !isRecording },
            colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color(0xFFFF3B30) else Color(0xFF007AFF)),
            shape = CircleShape,
            modifier = Modifier.size(100.dp)
        ) {
            Icon(imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null, tint = Color.White)
        }

        if (isRecording) {
            PulseAnimation()
        }

        Button(
            onClick = {
                val note = Note(title = "Запись", rawText = transcribedText, correctedText = transcribedText, processingStyle = "literal")
                notesVM.addNote(note)
            },
            enabled = transcribedText != "Готов к записи…"
        ) {
            Text(text = "Сохранить заметку")
        }

        Button(
            onClick = {
                transcribedText = "ну короче ГГ зашел в таверну типа Таинственный Дракон и там сидел этот как бы эльф Элронд"
            }
        ) {
            Text(text = "Демо-ввод")
        }
    }
}

@Composable
fun PulseAnimation() {
    var animation by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            animation = 2f
            delay(1500)
            animation = 1f
            delay(1500)
        }
    }
    Canvas(modifier = Modifier.size(140.dp)) {
        val center = size.center
        val radius = 70f * animation
        drawCircle(
            color = Color(0xFFFF3B30).copy(alpha = 0.4f),
            center = center,
            radius = radius,
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

@Composable
fun BookScreen(bookTreeVM: BookTreeViewModel, notesVM: NotesViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(bookTreeVM.chapters, key = { it.id }) { chapter ->
            val chapterNotes = notesVM.notes.filter { it.chapterId == chapter.id }
            Column {
                Text(
                    text = "${chapter.name} (#${chapter.order})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1D1D1F)
                )
                items(chapterNotes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F7))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = note.title, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = note.rawText.take(80) + (if (note.rawText.length > 80) "…" else ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "${note.processingStyle}",
                                style = MaterialTheme.typography.captionSmall,
                                color = Color(0xFF007AFF)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoreScreen(bookTreeVM: BookTreeViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(bookTreeVM.storyContexts, key = { it.id }) { context ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = context.keyword, style = MaterialTheme.typography.titleMedium)
                        if (context.details.isNotEmpty()) {
                            Text(text = context.details, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E8E93))
                        }
                    }
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))) {
                        Text(text = "Удалить")
                    }
                }
            }
        }
    }
}