# Agent App

An Android application that uses natural language processing to automate interactions with other apps on your device. The Agent app leverages Android's Accessibility Service to interpret user commands and perform actions like sending messages, searching content, and navigating between apps.

## Features

- **Natural Language Commands**: Control your phone using simple, natural language instructions
- **Cross-App Automation**: Seamlessly interact with popular apps like WhatsApp, YouTube, Google Chat, Gmail, Chrome, Maps, Instagram, Facebook, Twitter, Telegram, Spotify, and Netflix
- **Accessibility Service Integration**: Uses Android's Accessibility Service to interact with UI elements in other applications
- **Real-time Execution Feedback**: Visual progress tracking of command execution steps
- **Error Handling**: Comprehensive error reporting and recovery mechanisms

## Supported Commands

The app currently supports various types of commands:

### Messaging
- "Open WhatsApp and send message to John saying 'Hello there!'"
- "Open Google Chat and find Jane and send her 'Meeting at 3 PM'"
- "Open Telegram and send 'Good morning' to Alex"

### Media & Content
- "Search for Despacito on YouTube and play"
- "Find 'Stranger Things' on Netflix and play first episode"
- "Search for 'How to make pizza' on YouTube and play"

## How It Works

1. **Command Parsing**: Natural language commands are parsed to extract app names, actions, and parameters
2. **Plan Building**: The system creates a sequence of executable steps based on the parsed command
3. **Action Execution**: Steps are executed using the Accessibility Service to interact with UI elements
4. **Feedback**: Execution progress is displayed in real-time with success/failure indicators

## Architecture

The app follows a modern Android architecture with:

- **Jetpack Compose**: For modern, declarative UI
- **Hilt**: For dependency injection
- **MVVM**: Model-View-ViewModel pattern for separation of concerns
- **Coroutines**: For asynchronous operations
- **Accessibility Service**: To interact with other apps' UI elements

### Key Components

- `CommandInputScreen`: Main UI for entering and executing commands
- `CommandInputViewModel`: Handles command execution logic
- `AgentPlanBuilder`: Parses natural language commands into executable steps
- `ActionExecutor`: Executes steps using the accessibility service
- `ScreenInteractionService`: Accessibility service that performs UI interactions

## Setup

1. Install the app on your Android device
2. Enable the Agent accessibility service:
   - Go to Settings → Accessibility → Agent
   - Toggle the service to enable it
3. Grant necessary permissions when prompted
4. Start using natural language commands in the app

## Permissions

- `BIND_ACCESSIBILITY_SERVICE`: Required for the accessibility service to function
- Package visibility queries for various popular apps (WhatsApp, YouTube, etc.)

## Example Usage

```
"Open WhatsApp and send 'Meeting postponed to 4 PM' to the group 'Team Chat'"
```

This command would:
1. Launch WhatsApp
2. Find and open the "Team Chat" group
3. Type "Meeting postponed to 4 PM" in the message field
4. Send the message

## Technologies Used

- Kotlin
- Jetpack Compose
- Android Architecture Components
- Hilt (Dependency Injection)
- Coroutines
- Timber (Logging)

## Limitations

- Requires accessibility service permission which may be seen as sensitive
- Performance depends on UI element detection accuracy
- May not work with all apps due to varying UI structures
- Requires apps to be installed on the device

## Security

The app only interacts with other applications through Android's Accessibility Service framework, which provides a secure way to automate UI interactions. No personal data is collected or stored by the Agent app itself.