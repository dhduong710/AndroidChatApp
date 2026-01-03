package com.example.hustchat.model

sealed class ChatItem {
    data class MessageItem(val message: Message) : ChatItem()
    data class DateHeader(val timestamp: Long) : ChatItem()

    val id: String get() = when(this) {
        is MessageItem -> message.id
        is DateHeader -> "date_$timestamp"
    }
}