package org.tfcc.bingo

import org.junit.Test

class SpellTest {
    @Test
    fun spellTest() {
        SpellFactory.randSpells(
            arrayOf("6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18"),
            arrayOf("PH", "EX", "L"),
            10
        )
    }
}