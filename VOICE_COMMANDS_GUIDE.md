# Live Insight Mode - Voice Commands and Integrations

## Voice Commands

The following voice commands are now supported in Live Insight Mode:

### Core Commands
- **"show insights"** - Display recent AI insights and interactions
- **"read latest"** - Use text-to-speech to read the most recent AI response aloud
- **"copy"** - Copy the latest AI response to clipboard
- **"save [content]"** - Save custom content or note
- **"pause"** - Pause voice recognition temporarily

### Integration Commands
- **"share slack"** - Share latest AI insight to Slack
- **"share discord"** - Share latest AI insight to Discord
- **"save notion"** - Save latest insight in Notion-compatible format
- **"save obsidian"** - Save latest insight in Obsidian markdown format
- **"calendar context"** - Get current date/time context
- **"pip mode"** - Trigger Picture-in-Picture mode for Zoom/Teams

## App Integrations

### Slack Integration
- Automatically detects if Slack is installed
- Shares content directly to Slack app
- Falls back to generic share dialog if Slack unavailable

### Discord Integration  
- Automatically detects if Discord is installed
- Shares content directly to Discord app
- Falls back to generic share dialog if Discord unavailable

### Notion Auto-Save
- Creates text files compatible with Notion import
- Includes timestamps and formatting
- Attempts to open with Notion app if installed
- Saves to local files directory as backup

### Obsidian Auto-Save
- Creates markdown files with Obsidian-compatible formatting
- Includes metadata tags (#aura #ai-insight)
- Attempts to open with Obsidian if installed
- Saved in app's external files directory

### Calendar Context
- Provides current date and time information
- Can be extended to integrate with calendar apps
- Useful for time-sensitive insights and scheduling

### Zoom/Teams PiP Support
- Detects if Zoom or Microsoft Teams is installed
- Provides guidance for enabling Picture-in-Picture mode
- Requires Android 8.0+ for PiP functionality

## Enhanced Overlay UI

The floating overlay now provides:
- **Real-time status indicators** - Visual feedback for listening/processing states
- **Response preview** - Shows truncated version of AI responses
- **Pause indicator** - Clear visual indication when voice recognition is paused
- **Command feedback** - Displays confirmation of executed voice commands

## Technical Implementation

### New Components
- **VoiceCommandProcessor** - Handles parsing and execution of voice commands
- **IntegrationManager** - Manages external app integrations and sharing
- **Enhanced OverlayService** - Provides visual feedback via broadcast receivers
- **Extended MainViewModel** - Supports manual voice command triggers

### Broadcasting System
- Uses Android broadcast intents for service communication
- Real-time updates between WakeWordService and OverlayService
- Handles pause/resume states and response display

### File Management
- Auto-saves content to external files directory
- Creates timestamped files for organization
- Supports multiple export formats (text, markdown)

## Usage Examples

### Basic Voice Commands
1. Say wake word to activate
2. Speak command: "show insights"
3. View recent insights in overlay
4. Say "copy" to copy latest to clipboard

### Integration Workflow
1. Generate AI insight through normal interaction
2. Say "share slack" to send to Slack
3. Or say "save notion" to create Notion-compatible file
4. Content automatically formatted and shared/saved

### Pause and Resume
1. Say "pause" to stop voice recognition
2. Overlay shows "Paused" status with red indicator
3. Tap overlay or use app to resume listening
4. System returns to "Ready" state

## Privacy and Security

- No changes to existing privacy model
- File exports are local-only until manually shared
- External app integrations use standard Android sharing
- No additional cloud storage or external API calls
- Voice commands processed using existing AI pipeline