package com.liferemaining.app.domain

import com.liferemaining.app.util.ActionPrompts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptSeedTest {

    @Test
    fun `prompt list is non trivial and unique`() {
        assertTrue(ActionPrompts.all.size >= 10)
        assertEquals(ActionPrompts.all.size, ActionPrompts.all.distinct().size)
    }

    @Test
    fun `prompt for a day is stable`() {
        val a = ActionPrompts.promptFor(120, 0)
        val b = ActionPrompts.promptFor(120, 0)
        assertEquals(a, b)
    }

    @Test
    fun `shuffle changes the prompt`() {
        assertNotEquals(
            ActionPrompts.promptFor(120, 0),
            ActionPrompts.promptFor(120, 1)
        )
    }

    @Test
    fun `negative seed stays in range`() {
        val prompt = ActionPrompts.promptAt(-12345)
        assertTrue(ActionPrompts.all.contains(prompt))
    }
}
