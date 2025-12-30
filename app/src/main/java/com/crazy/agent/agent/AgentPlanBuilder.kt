package com.crazy.agent.agent

import javax.inject.Inject
import javax.inject.Singleton

/** Builds execution plans from natural language commands */
@Singleton
class AgentPlanBuilder @Inject constructor(private val commandParser: CommandParser) {

    /** Parse a natural language command into a list of executable steps */
    fun buildPlan(command: String): List<AgentStep> {
        val steps = mutableListOf<AgentStep>()

        // Extract entities from command
        val appName = commandParser.extractAppName(command)
        val packageName = appName?.let { commandParser.getPackageName(it) }

        if (packageName == null) {
            // Can't determine app, return empty plan
            return emptyList()
        }

        // Step 1: Open the app
        steps.add(
                AgentStep(
                        action = ActionType.OPEN_APP,
                        target = packageName,
                        description = "Opening ${appName.replaceFirstChar { it.uppercase() }}"
                )
        )

        // Step 2: Wait for app to load
        steps.add(
                AgentStep(
                        action = ActionType.WAIT,
                        target = "2000",
                        description = "Waiting for app to load"
                )
        )

        // Determine what action to perform based on command
        when {
            commandParser.isSendMessageCommand(command) -> {
                addMessageSteps(steps, command, appName)
            }
            commandParser.isSearchCommand(command) -> {
                addSearchSteps(steps, command, appName)
            }
        }

        return steps
    }

    /** Add steps for sending a message */
    private fun addMessageSteps(steps: MutableList<AgentStep>, command: String, appName: String) {
        val contactName = commandParser.extractContactName(command)
        val messageContent = commandParser.extractMessageContent(command) ?: "Hi"

        if (contactName != null) {
            // Step: Find and click search/new chat button
            when (appName.lowercase()) {
                "whatsapp" -> {
                    steps.add(
                            AgentStep(
                                    action = ActionType.FIND_ELEMENT,
                                    target = "New chat",
                                    description = "Finding new chat button"
                            )
                    )
                    steps.add(
                            AgentStep(
                                    action = ActionType.CLICK,
                                    target = "New chat",
                                    description = "Opening new chat"
                            )
                    )
                }
                "google chat" -> {
                    steps.add(
                            AgentStep(
                                    action = ActionType.FIND_ELEMENT,
                                    target = "Start chat",
                                    description = "Finding start chat button"
                            )
                    )
                    steps.add(
                            AgentStep(
                                    action = ActionType.CLICK,
                                    target = "Start chat",
                                    description = "Starting new chat"
                            )
                    )
                }
            }

            // Step: Search for contact
            steps.add(
                    AgentStep(
                            action = ActionType.FIND_ELEMENT,
                            target = "Search",
                            description = "Finding search field"
                    )
            )
            steps.add(
                    AgentStep(
                            action = ActionType.TYPE_TEXT,
                            target = "Search",
                            value = contactName,
                            description = "Searching for $contactName"
                    )
            )

            // Step: Wait for search results
            steps.add(
                    AgentStep(
                            action = ActionType.WAIT,
                            target = "1500",
                            description = "Waiting for search results"
                    )
            )

            // Step: Click on contact
            steps.add(
                    AgentStep(
                            action = ActionType.FIND_ELEMENT,
                            target = contactName,
                            description = "Finding contact $contactName"
                    )
            )
            steps.add(
                    AgentStep(
                            action = ActionType.CLICK,
                            target = contactName,
                            description = "Opening chat with $contactName"
                    )
            )

            // Step: Wait for chat to open
            steps.add(
                    AgentStep(
                            action = ActionType.WAIT,
                            target = "1000",
                            description = "Waiting for chat to open"
                    )
            )

            // Step: Find message input and type message
            steps.add(
                    AgentStep(
                            action = ActionType.FIND_ELEMENT,
                            target = "message",
                            description = "Finding message input"
                    )
            )
            steps.add(
                    AgentStep(
                            action = ActionType.TYPE_TEXT,
                            target = "message",
                            value = messageContent,
                            description = "Typing message: $messageContent"
                    )
            )

            // Step: Send message
            steps.add(
                    AgentStep(
                            action = ActionType.FIND_ELEMENT,
                            target = "Send",
                            description = "Finding send button"
                    )
            )
            steps.add(
                    AgentStep(
                            action = ActionType.CLICK,
                            target = "Send",
                            description = "Sending message"
                    )
            )
        }
    }

    /** Add steps for searching and playing content */
    private fun addSearchSteps(steps: MutableList<AgentStep>, command: String, appName: String) {
        val searchQuery = commandParser.extractSearchQuery(command)

        if (searchQuery != null) {
            when (appName.lowercase()) {
                "youtube" -> {
                    // Step: Find and click search button
                    steps.add(
                            AgentStep(
                                    action = ActionType.FIND_ELEMENT,
                                    target = "Search",
                                    description = "Finding search button"
                            )
                    )
                    steps.add(
                            AgentStep(
                                    action = ActionType.CLICK,
                                    target = "Search",
                                    description = "Opening search"
                            )
                    )

                    // Step: Type search query
                    steps.add(
                            AgentStep(
                                    action = ActionType.FIND_ELEMENT,
                                    target = "Search YouTube",
                                    description = "Finding search field"
                            )
                    )
                    steps.add(
                            AgentStep(
                                    action = ActionType.TYPE_TEXT,
                                    target = "Search YouTube",
                                    value = searchQuery,
                                    description = "Searching for: $searchQuery"
                            )
                    )

                    // Step: Wait for results
                    steps.add(
                            AgentStep(
                                    action = ActionType.WAIT,
                                    target = "2000",
                                    description = "Waiting for search results"
                            )
                    )

                    // Step: Click first result
                    steps.add(
                            AgentStep(
                                    action = ActionType.FIND_ELEMENT,
                                    target = searchQuery,
                                    description = "Finding video"
                            )
                    )
                    steps.add(
                            AgentStep(
                                    action = ActionType.CLICK,
                                    target = searchQuery,
                                    description = "Playing video"
                            )
                    )
                }
            }
        }
    }
}
