package com.minexd.core.bukkit.rank.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.BackButton
import net.evilblock.cubed.menu.buttons.GlassButton
import net.evilblock.cubed.menu.pagination.PageButton
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.bukkit.enchantment.GlowEnchantment
import com.minexd.core.CoreXD
import com.minexd.core.bukkit.util.ColorMap
import com.minexd.core.rank.Rank
import com.minexd.core.rank.RankHandler
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class EditRankInheritanceMenu(private val group: String, private val rank: Rank) : PaginatedMenu() {

    init {
        updateAfterClick = true
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Edit Inheritance"
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 36
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            if (page == 1) {
                buttons[8] = BackButton {
                    EditRankMenu(group, rank).openMenu(player)
                }
            } else {
                buttons[0] = PageButton(-1, this)
            }

            buttons[8] = PageButton(1, this)

            for (i in 0..8) {
                if (!buttons.containsKey(i)) {
                    buttons[i] = GlassButton(7)
                }
            }
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            val sortedRanks = RankHandler.getRanks().sortedBy { it.displayOrder }
            for (rank in sortedRanks) {
                if (rank == this.rank) {
                    continue
                }

                buttons[buttons.size] = RankInheritButton(this, rank)
            }
        }
    }

    private class RankInheritButton(private val parent: EditRankInheritanceMenu, private val rank: Rank) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            val itemStack = super.getButtonItem(player)

            val isInherited = parent.rank.inheritedRanks.contains(rank)
            val isDarkRed = rank.getDisplayColorChar() == "4"

            if (isInherited || isDarkRed) {
                GlowEnchantment.addGlow(itemStack)
            }

            return itemStack
        }

        override fun getName(player: Player): String {
            val isInherited = parent.rank.inheritedRanks.contains(rank)
            if (isInherited) {
                return rank.getColoredDisplayName() + " ${ChatColor.AQUA}${ChatColor.BOLD}(*)"
            }

            return rank.getColoredDisplayName()
        }

        override fun getMaterial(player: Player): Material {
            return Material.WOOL
        }

        override fun getDamageValue(player: Player): Byte {
            return try {
                (ColorMap.woolMap[ChatColor.getByChar(rank.getDisplayColorChar())]?: 15).toByte()
            } catch (e: Exception) {
                return 15.toByte()
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            player.closeInventory()

            Tasks.async {
                try {
                    val response = CoreXD.instance.ranksService.update(parent.rank.id, mapOf("inheritRank" to rank.id)).execute()
                    if (response.isSuccessful) {
                        Tasks.sync {
                            parent.openMenu(player)
                        }
                    } else {
                        player.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}