package com.crazy.agent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crazy.agent.action.ActionExecutor
import com.crazy.agent.action.ActionResult
import com.crazy.agent.agent.AgentPlanBuilder
import com.crazy.agent.agent.AgentStep
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for command input and execution */
@HiltViewModel
class CommandInputViewModel
@Inject
constructor(private val planBuilder: AgentPlanBuilder, private val actionExecutor: ActionExecutor) :
        ViewModel() {

    private val _uiState = MutableStateFlow<CommandUiState>(CommandUiState.Idle)
    val uiState: StateFlow<CommandUiState> = _uiState.asStateFlow()

    private val _executedSteps = MutableStateFlow<List<StepExecution>>(emptyList())
    val executedSteps: StateFlow<List<StepExecution>> = _executedSteps.asStateFlow()

    /** Execute a natural language command */
    fun executeCommand(command: String) {
        if (command.isBlank()) return

        viewModelScope.launch {
            // Reset state
            _executedSteps.value = emptyList()
            _uiState.value = CommandUiState.Parsing

            // Build plan
            val steps = planBuilder.buildPlan(command)

            if (steps.isEmpty()) {
                _uiState.value =
                        CommandUiState.Error("Could not understand command. Please try again.")
                return@launch
            }

            _uiState.value =
                    CommandUiState.Executing(
                            currentStep = 0,
                            totalSteps = steps.size,
                            currentDescription = steps.first().description
                    )

            // Execute steps
            actionExecutor.executePlan(steps) { index, step, result ->
                // Update executed steps
                val execution = StepExecution(step = step, result = result, index = index)
                _executedSteps.value = _executedSteps.value + execution

                // Update UI state
                if (result is ActionResult.Failure) {
                    _uiState.value = CommandUiState.Error(result.error)
                } else if (index < steps.size - 1) {
                    _uiState.value =
                            CommandUiState.Executing(
                                    currentStep = index + 1,
                                    totalSteps = steps.size,
                                    currentDescription = steps[index + 1].description
                            )
                } else {
                    _uiState.value = CommandUiState.Success
                }
            }
        }
    }

    /** Reset to idle state */
    fun reset() {
        _uiState.value = CommandUiState.Idle
        _executedSteps.value = emptyList()
    }
}

/** UI state for command execution */
sealed class CommandUiState {
    object Idle : CommandUiState()
    object Parsing : CommandUiState()
    data class Executing(
            val currentStep: Int,
            val totalSteps: Int,
            val currentDescription: String
    ) : CommandUiState()
    object Success : CommandUiState()
    data class Error(val message: String) : CommandUiState()
}

/** Represents a step execution with its result */
data class StepExecution(val step: AgentStep, val result: ActionResult, val index: Int)
