package com.example.hustchat.utils

object ImageUtils {    // Function to generate an avatar URL based on a name
    fun getAvatarUrl(name: String, existingUrl: String? = null): String {
        // 1. If the user already has a picture (e.g., from Storage), use it.
        if (!existingUrl.isNullOrEmpty()) {
            return existingUrl
        }

        // 2. If not, automatically generate an image from the name.
        // Using the ui-avatars.com service (Free)
        // background=random: Random background color
        // color=fff: White text color
        // size=128: Image size
        // bold=true: Makes the font bold
        val cleanName = name.replace(" ", "+") // Replace spaces with '+' for the URL
        return "https://ui-avatars.com/api/?name=$cleanName&background=random&color=fff&size=128&bold=true"
    }
}
