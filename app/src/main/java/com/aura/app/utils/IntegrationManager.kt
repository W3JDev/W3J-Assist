package com.aura.app.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class IntegrationManager(private val context: Context) {
    
    companion object {
        private const val NOTION_PACKAGE = "notion.id"
        private const val OBSIDIAN_PACKAGE = "md.obsidian"
        private const val SLACK_PACKAGE = "com.Slack"
        private const val DISCORD_PACKAGE = "com.discord"
        private const val TEAMS_PACKAGE = "com.microsoft.teams"
        private const val ZOOM_PACKAGE = "us.zoom.videomeetings"
    }
    
    /**
     * Share content with Slack
     */
    fun shareToSlack(content: String, title: String = "Aura Insight"): Boolean {
        return try {
            if (isAppInstalled(SLACK_PACKAGE)) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    `package` = SLACK_PACKAGE
                    putExtra(Intent.EXTRA_TEXT, content)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } else {
                shareViaGenericShare(content, title, "Slack not installed")
                false
            }
        } catch (e: Exception) {
            shareViaGenericShare(content, title, "Error sharing to Slack")
            false
        }
    }
    
    /**
     * Share content with Discord
     */
    fun shareToDiscord(content: String, title: String = "Aura Insight"): Boolean {
        return try {
            if (isAppInstalled(DISCORD_PACKAGE)) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    `package` = DISCORD_PACKAGE
                    putExtra(Intent.EXTRA_TEXT, content)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } else {
                shareViaGenericShare(content, title, "Discord not installed")
                false
            }
        } catch (e: Exception) {
            shareViaGenericShare(content, title, "Error sharing to Discord")
            false
        }
    }
    
    /**
     * Auto-save to Notion-compatible format
     */
    suspend fun saveToNotion(content: String, title: String = "Aura Note"): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isAppInstalled(NOTION_PACKAGE)) {
                // Create a text file that can be imported to Notion
                val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
                val fileName = "aura_note_$timestamp.txt"
                val file = File(context.getExternalFilesDir(null), fileName)
                
                FileWriter(file).use { writer ->
                    writer.write("# $title\n\n")
                    writer.write("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")
                    writer.write(content)
                }
                
                // Open with Notion if possible
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(file), "text/plain")
                    `package` = NOTION_PACKAGE
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // If Notion can't handle it, just save the file
                }
                
                true
            } else {
                saveToLocalFile(content, title)
                false
            }
        } catch (e: Exception) {
            saveToLocalFile(content, title)
            false
        }
    }
    
    /**
     * Auto-save to Obsidian-compatible format
     */
    suspend fun saveToObsidian(content: String, title: String = "Aura Note"): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // Create markdown file compatible with Obsidian
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "aura_note_$timestamp.md"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileWriter(file).use { writer ->
                writer.write("# $title\n\n")
                writer.write("**Generated:** ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")
                writer.write("**Tags:** #aura #ai-insight\n\n")
                writer.write("---\n\n")
                writer.write(content)
            }
            
            if (isAppInstalled(OBSIDIAN_PACKAGE)) {
                // Try to open with Obsidian
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(file), "text/markdown")
                    `package` = OBSIDIAN_PACKAGE
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // File is saved, even if Obsidian couldn't open it
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get calendar context (placeholder for calendar integration)
     */
    fun getCalendarContext(): String {
        return try {
            val calendar = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
            
            "Current time: ${timeFormat.format(calendar.time)}\n" +
            "Date: ${dateFormat.format(calendar.time)}\n" +
            "Context: General productivity session"
        } catch (e: Exception) {
            "Calendar context unavailable"
        }
    }
    
    /**
     * Check if Zoom/Teams supports PiP mode
     */
    fun supportsPiPMode(appPackage: String): Boolean {
        return when (appPackage) {
            ZOOM_PACKAGE, TEAMS_PACKAGE -> {
                isAppInstalled(appPackage) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
            }
            else -> false
        }
    }
    
    /**
     * Trigger PiP mode for supported video apps
     */
    fun triggerPiPMode(): String {
        return when {
            supportsPiPMode(ZOOM_PACKAGE) -> {
                try {
                    // Send intent to suggest PiP mode (this is app-specific)
                    val intent = context.packageManager.getLaunchIntentForPackage(ZOOM_PACKAGE)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent?.let { context.startActivity(it) }
                    "Zoom PiP mode available - minimize the app to enable Picture-in-Picture"
                } catch (e: Exception) {
                    "Unable to access Zoom PiP mode"
                }
            }
            supportsPiPMode(TEAMS_PACKAGE) -> {
                try {
                    val intent = context.packageManager.getLaunchIntentForPackage(TEAMS_PACKAGE)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent?.let { context.startActivity(it) }
                    "Teams PiP mode available - minimize the app to enable Picture-in-Picture"
                } catch (e: Exception) {
                    "Unable to access Teams PiP mode"
                }
            }
            else -> "No PiP-compatible video apps detected"
        }
    }
    
    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    private fun shareViaGenericShare(content: String, title: String, fallbackMessage: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "$fallbackMessage\n\n$content")
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Silently fail - at least the content was processed
        }
    }
    
    private suspend fun saveToLocalFile(content: String, title: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "aura_backup_$timestamp.txt"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileWriter(file).use { writer ->
                writer.write("$title\n\n")
                writer.write("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")
                writer.write(content)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}