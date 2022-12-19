package com.f0x1d.foxbin.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.f0x1d.foxbin.R
import com.f0x1d.foxbin.extensions.*
import com.f0x1d.foxbin.model.Screen
import com.f0x1d.foxbin.network.model.response.FoxBinNote
import com.f0x1d.foxbin.viewmodel.NotesViewModel
import com.f0x1d.foxbin.viewmodel.base.LoadingState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(navController: NavController, viewModel: NotesViewModel) {
    val loadingState by viewModel.loadingStateFlow.collectAsState()
    val accessToken by viewModel.accessTokenFlow.collectAsState(initial = null)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            LargeTopAppBar(
                title = { Text(text = stringResource(id = R.string.notes)) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        onClick = {
                            if (loadingState.isLoading) return@IconButton

                            if (accessToken == null)
                                navController.navigate(Screen.Login.route)
                            else
                                viewModel.logout()
                        }
                    ) {
                        Icon(painter = painterResource(id = if (accessToken == null) R.drawable.ic_login else R.drawable.ic_logout), contentDescription = null)
                    }
                }
            )

            loadingState.DefaultIfLoading()
            loadingState.DefaultIfError { viewModel.reload() }

            loadingState.IfLoaded {
                LazyColumn(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 88.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val notes = (loadingState as LoadingState.LOADED<List<FoxBinNote>>).data

                    items(
                        notes,
                        key = { note -> note.slug.hashCode() }
                    ) { note ->
                        NoteItem(
                            note = note,
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Screen.Publish.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.NoteItem(note: FoxBinNote, navController: NavController, viewModel: NotesViewModel) {
    val context = LocalContext.current

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateItemPlacement(),
        onClick = { navController.navigate("${Screen.Note.route}/${note.slug}") }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        start = 20.dp
                    ),
                    text = note.slug,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    modifier = Modifier.padding(
                        start = 20.dp,
                        bottom = 20.dp
                    ),
                    text = Date(note.date).toLocaleString()
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            Column(
                modifier = Modifier
                    .padding(
                        top = 5.dp,
                        end = 5.dp,
                        bottom = 5.dp
                    )
            ) {
                IconButton(onClick = { context.shareNote(note.slug) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { viewModel.delete(note.slug) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}