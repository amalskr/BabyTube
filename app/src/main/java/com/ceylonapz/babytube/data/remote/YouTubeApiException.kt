package com.ceylonapz.babytube.data.remote

class YouTubeApiException(
    message: String,
    /** YouTube's machine-readable error reason, e.g. "quotaExceeded" or "invalidPageToken". */
    val reason: String? = null,
) : Exception(message)
