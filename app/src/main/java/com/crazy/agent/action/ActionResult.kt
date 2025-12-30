package com.crazy.agent.action

/** Represents the result of an action execution */
sealed class ActionResult {
    data class Success(val message: String = "Action completed successfully") : ActionResult()
    data class Failure(val error: String) : ActionResult()
    data class InProgress(val status: String) : ActionResult()
}
