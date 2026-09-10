package com.forcepaste

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive

// A false result means the editor reported failure; never retry a partial paste.
internal suspend fun pasteText(text: String, commit: (String) -> Boolean): Boolean {
    var offset = 0
    while (offset < text.length) {
        currentCoroutineContext().ensureActive()
        val end = offset + Character.charCount(text.codePointAt(offset))
        if (!commit(text.substring(offset, end))) return false
        offset = end
        if (offset < text.length) delay(5)
    }
    return true
}
