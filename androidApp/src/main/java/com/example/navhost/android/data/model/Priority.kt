package com.example.navhost.android.data.model

/**
 *  优先级：高、中、低
 */
enum class Priority(val displayText: String) {
    HIGH("高"),
    MEDIUM("中"),
    LOW("低");
    override fun toString(): String {
        return displayText
    }
}