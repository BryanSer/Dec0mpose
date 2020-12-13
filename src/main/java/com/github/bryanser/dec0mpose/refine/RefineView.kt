package com.github.bryanser.dec0mpose.refine

import com.github.bryanser.brapi.kview.KViewContext
import com.github.bryanser.brapi.kview.KViewHandler
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack



class RefineViewContext(p: Player) : KViewContext("§6精炼") {
    val display = mutableListOf<ItemStack>()
    fun update() {
        _update()
        KViewHandler.updateUI(player)
    }

    private fun _update() {

        display.clear()
        val item = inventory.getItem(SLOT) ?: return

    }
}

const val SLOT = 4
const val BOTTOM = 16
const val INFO = 44

val RefineView = KViewHandler.createKView("Refine View", 6, ::RefineViewContext) {

}

