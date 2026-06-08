@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.messaging.ui.conversation.messagedetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.messaging.R
import com.android.messaging.ui.conversation.messagedetails.model.MessageDetailsUiState

@Composable
internal fun MessageDetailsScreen(
    conversationId: String,
    messageId: String,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    screenModel: MessageDetailsScreenModel = hiltViewModel<MessageDetailsViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(conversationId, messageId, screenModel) {
        screenModel.onArguments(
            conversationId = conversationId,
            messageId = messageId,
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back),
                        )
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.message_details_title))
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            MessageDetailsBody(
                uiState = uiState,
            )
        }
    }
}

@Composable
private fun MessageDetailsBody(uiState: MessageDetailsUiState) {
    when (uiState) {
        MessageDetailsUiState.Loading -> CircularProgressIndicator()

        MessageDetailsUiState.Unavailable -> {
            Text(text = stringResource(id = R.string.message_details_title))
        }

        is MessageDetailsUiState.Content -> {
            Text(text = uiState.preview.text ?: uiState.details.type.name)
        }
    }
}
