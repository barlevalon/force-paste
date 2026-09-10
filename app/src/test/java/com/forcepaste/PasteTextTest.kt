package com.forcepaste

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class PasteTextTest {
    @Test fun commitsCompleteCodePoints() = runBlocking {
        val commits = mutableListOf<String>()
        assertTrue(pasteText("A😀𐐀e\u0301\nB") { commits.add(it) })
        assertEquals(listOf("A", "😀", "𐐀", "e", "\u0301", "\n", "B"), commits)
    }

    @Test fun cancellationStopsAtACompleteCodePoint() = runBlocking {
        val commits = mutableListOf<String>()
        val firstCommit = CompletableDeferred<Unit>()
        val job = launch {
            pasteText("😀remaining") {
                commits.add(it)
                firstCommit.complete(Unit)
                true
            }
        }
        firstCommit.await()
        job.cancelAndJoin()
        assertEquals(listOf("😀"), commits)
    }

    @Test fun alreadyCancelledWorkNeverCommits() = runBlocking {
        var committed = false
        val job = launch {
            currentCoroutineContext().cancel()
            pasteText("text") { committed = true; true }
        }
        job.join()
        assertFalse(committed)
    }

    @Test fun failedCommitStopsWithoutRetry() = runBlocking {
        for (failAt in listOf(1, 2)) {
            val commits = mutableListOf<String>()
            assertFalse(pasteText("A😀B") {
                commits.add(it)
                commits.size != failAt
            })
            assertEquals(listOf("A", "😀").take(failAt), commits)
        }
    }

    @Test fun thrownCommitStopsWithoutRetry() = runBlocking {
        var attempts = 0
        try {
            pasteText("AB") { attempts++; throw IllegalStateException("editor closed") }
            fail("Expected editor failure")
        } catch (_: IllegalStateException) {
            assertEquals(1, attempts)
        }
    }
}
