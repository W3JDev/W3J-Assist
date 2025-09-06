package com.aura.app.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class GeminiClient {
    
    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"
        // Note: In production, this should be stored securely
        private const val API_KEY = "YOUR_GEMINI_API_KEY" // Replace with actual API key
    }
    
    private val client = OkHttpClient.Builder()
        .build()
    
    suspend fun processText(inputText: String): String = withContext(Dispatchers.IO) {
        try {
            val requestBody = createRequestBody(inputText)
            val request = Request.Builder()
                .url("$BASE_URL?key=$API_KEY")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                parseGeminiResponse(responseBody ?: "")
            } else {
                "Error: Unable to process request"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    private fun createRequestBody(inputText: String): RequestBody {
        val json = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Please provide a helpful and concise response to: $inputText")
                        })
                    })
                })
            })
        }
        
        return json.toString().toRequestBody("application/json".toMediaType())
    }
    
    private fun parseGeminiResponse(responseBody: String): String {
        return try {
            val json = JSONObject(responseBody)
            val candidates = json.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    val firstPart = parts.getJSONObject(0)
                    firstPart.getString("text")
                } else {
                    "No response generated"
                }
            } else {
                "No response generated"
            }
        } catch (e: Exception) {
            "Error parsing response: ${e.message}"
        }
    }
}