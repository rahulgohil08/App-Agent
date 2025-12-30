package com.crazy.agent.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.crazy.agent.accessibility.ScreenInteractionService
import com.crazy.agent.action.ActionResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandInputScreen(viewModel: CommandInputViewModel = hiltViewModel()) {
        val context = LocalContext.current
        val uiState by viewModel.uiState.collectAsState()
        val executedSteps by viewModel.executedSteps.collectAsState()
        var commandText by remember { mutableStateOf("") }

        // Check service status reactively
        var isServiceEnabled by remember {
                mutableStateOf(ScreenInteractionService.getInstance() != null)
        }

        // Update service status when screen resumes
        DisposableEffect(Unit) {
                val lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
                val observer =
                        androidx.lifecycle.LifecycleEventObserver { _, event ->
                                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                        isServiceEnabled =
                                                ScreenInteractionService.getInstance() != null
                                }
                        }
                lifecycleOwner?.lifecycle?.addObserver(observer)

                onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("Agent App") },
                                actions = {
                                        IconButton(
                                                onClick = {
                                                        val intent =
                                                                Intent(
                                                                        Settings.ACTION_ACCESSIBILITY_SETTINGS
                                                                )
                                                        context.startActivity(intent)
                                                }
                                        ) {
                                                Icon(
                                                        Icons.Default.Settings,
                                                        contentDescription = "Settings"
                                                )
                                        }
                                }
                        )
                }
        ) { paddingValues ->
                Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        // Service status card
                        if (!isServiceEnabled) {
                                Card(
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme
                                                                        .errorContainer
                                                )
                                ) {
                                        Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                Text(
                                                        "Accessibility Service Required",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                        "Please enable the Agent accessibility service in Settings → Accessibility → Agent",
                                                        style = MaterialTheme.typography.bodyMedium
                                                )
                                                Button(
                                                        onClick = {
                                                                val intent =
                                                                        Intent(
                                                                                Settings.ACTION_ACCESSIBILITY_SETTINGS
                                                                        )
                                                                context.startActivity(intent)
                                                        }
                                                ) { Text("Open Settings") }
                                        }
                                }
                        }

                        // Command input card
                        Card {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                        Text(
                                                "Enter Command",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                        )

                                        OutlinedTextField(
                                                value = commandText,
                                                onValueChange = { commandText = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                placeholder = {
                                                        Text(
                                                                "e.g., Open WhatsApp and send message to Crazy"
                                                        )
                                                },
                                                minLines = 2,
                                                maxLines = 4,
                                                enabled =
                                                        uiState !is CommandUiState.Executing &&
                                                                uiState !is CommandUiState.Parsing
                                        )

                                        Button(
                                                onClick = { viewModel.executeCommand(commandText) },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled =
                                                        commandText.isNotBlank() &&
                                                                uiState !is
                                                                        CommandUiState.Executing &&
                                                                uiState !is
                                                                        CommandUiState.Parsing &&
                                                                isServiceEnabled
                                        ) {
                                                Icon(
                                                        Icons.Default.PlayArrow,
                                                        contentDescription = null
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text("Execute")
                                        }

                                        // Example commands
                                        Text(
                                                "Example commands:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                ExampleCommand(
                                                        "Open WhatsApp and send message to Crazy"
                                                ) { commandText = it }
                                                ExampleCommand(
                                                        "Search for Despacito on YouTube and play"
                                                ) { commandText = it }
                                                ExampleCommand(
                                                        "Open Google Chat and find Crazy and send him Hi"
                                                ) { commandText = it }
                                        }
                                }
                        }

                        // Status card
                        AnimatedVisibility(visible = uiState !is CommandUiState.Idle) {
                                Card(
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor =
                                                                when (uiState) {
                                                                        is CommandUiState.Success ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primaryContainer
                                                                        is CommandUiState.Error ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .errorContainer
                                                                        else ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surfaceVariant
                                                                }
                                                )
                                ) {
                                        Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                when (val state = uiState) {
                                                        is CommandUiState.Parsing -> {
                                                                Row(
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically,
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                8.dp
                                                                                        )
                                                                ) {
                                                                        CircularProgressIndicator(
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                20.dp
                                                                                        )
                                                                        )
                                                                        Text("Parsing command...")
                                                                }
                                                        }
                                                        is CommandUiState.Executing -> {
                                                                Text(
                                                                        "Executing (${state.currentStep + 1}/${state.totalSteps})",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .titleSmall,
                                                                        fontWeight = FontWeight.Bold
                                                                )
                                                                Row(
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically,
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                8.dp
                                                                                        )
                                                                ) {
                                                                        CircularProgressIndicator(
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                20.dp
                                                                                        )
                                                                        )
                                                                        Text(
                                                                                state.currentDescription
                                                                        )
                                                                }
                                                        }
                                                        is CommandUiState.Success -> {
                                                                Row(
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically,
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                8.dp
                                                                                        )
                                                                ) {
                                                                        Icon(
                                                                                Icons.Default.Check,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onPrimaryContainer
                                                                        )
                                                                        Text(
                                                                                "Completed successfully!",
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold
                                                                        )
                                                                }
                                                                TextButton(
                                                                        onClick = {
                                                                                viewModel.reset()
                                                                        }
                                                                ) { Text("Reset") }
                                                        }
                                                        is CommandUiState.Error -> {
                                                                Row(
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically,
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                8.dp
                                                                                        )
                                                                ) {
                                                                        Icon(
                                                                                Icons.Default.Close,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onErrorContainer
                                                                        )
                                                                        Text(
                                                                                "Error: ${state.message}",
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold
                                                                        )
                                                                }
                                                                TextButton(
                                                                        onClick = {
                                                                                viewModel.reset()
                                                                        }
                                                                ) { Text("Reset") }
                                                        }
                                                        else -> {}
                                                }
                                        }
                                }
                        }

                        // Executed steps
                        if (executedSteps.isNotEmpty()) {
                                Text(
                                        "Execution Log",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                )

                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(executedSteps) { stepExecution ->
                                                StepExecutionCard(stepExecution)
                                        }
                                }
                        }
                }
        }
}

@Composable
fun ExampleCommand(text: String, onClick: (String) -> Unit) {
        TextButton(
                onClick = { onClick(text) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
                Text(
                        text = "• $text",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                )
        }
}

@Composable
fun StepExecutionCard(stepExecution: StepExecution) {
        Card(
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        when (stepExecution.result) {
                                                is ActionResult.Success ->
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                is ActionResult.Failure ->
                                                        MaterialTheme.colorScheme.errorContainer
                                                                .copy(alpha = 0.3f)
                                                else -> MaterialTheme.colorScheme.surface
                                        }
                        )
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Step number
                        Box(
                                modifier =
                                        Modifier.size(32.dp)
                                                .background(
                                                        color =
                                                                when (stepExecution.result) {
                                                                        is ActionResult.Success ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary
                                                                        is ActionResult.Failure ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .error
                                                                        else ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surfaceVariant
                                                                },
                                                        shape = RoundedCornerShape(16.dp)
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                                Text(
                                        text = "${stepExecution.index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color =
                                                when (stepExecution.result) {
                                                        is ActionResult.Success ->
                                                                MaterialTheme.colorScheme.onPrimary
                                                        is ActionResult.Failure ->
                                                                MaterialTheme.colorScheme.onError
                                                        else ->
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                },
                                        fontWeight = FontWeight.Bold
                                )
                        }

                        // Step description and result
                        Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                                Text(
                                        text = stepExecution.step.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                )
                                when (val result = stepExecution.result) {
                                        is ActionResult.Success -> {
                                                Text(
                                                        text = "✓ ${result.message}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        }
                                        is ActionResult.Failure -> {
                                                Text(
                                                        text = "✗ ${result.error}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.error
                                                )
                                        }
                                        else -> {}
                                }
                        }
                }
        }
}
