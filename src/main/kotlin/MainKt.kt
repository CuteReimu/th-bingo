package org.tfcc.bingo

import org.tfcc.bingo.network.Network

open class MainKt {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Network.onInit()
        }
    }
}