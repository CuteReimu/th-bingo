package org.tfcc.bingo

import java.io.Serializable

data class Spell(
    val game: String,
    val name: String,
    var rank: String,
    var star: Int,
    var desc: String,
    val id: Int
) : Serializable {
    fun same(spell: Spell): Boolean {
        return spell.game == game && spell.id == id
    }
}