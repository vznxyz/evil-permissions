package net.evilblock.permissions.bukkit.rank.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.BackButton
import net.evilblock.cubed.menu.pagination.PageButton
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.bukkit.enchantment.GlowEnchantment
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.rank.event.RankUpdateEvent
import net.evilblock.permissions.bukkit.util.ColorMap
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.rank.RankHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class SelectInheritanceMenu(private val parent: EditAttributesMenu, private val rank: Rank) : PaginatedMenu() {

    init {
        updateAfterClick = true
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Select Inherited Ranks"
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 36
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        // pagination buttons
        buttons[3] = PageButton(-1, this)
        buttons[5] = PageButton(1, this)

        // buttons positioned on right
        buttons[8] = BackButton {
            parent.openMenu(player)
        }

        // toolbar placeholders
        for (i in 0..8) {
            if (!buttons.containsKey(i)) {
                buttons[i] = Button.placeholder(Material.STAINED_GLASS_PANE, 8, " ")
            }
        }

        return buttons
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        val sortedRanks = RankHandler.getRanks().sortedBy { it.displayOrder }
        for (rank in sortedRanks) {
            if (rank == this.rank) {
                continue
            }

            buttons[buttons.size] = RankInheritButton(this, rank)
        }

        return buttons
    }

    class OpenButton(private val parent: EditAttributesMenu, private val rank: Rank) : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}Inheritance"
        }

        override fun getDescription(player: Player): List<String> {
            return listOf(
                "",
                "${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK to edit inheritance"
            )
        }

        override fun getMaterial(player: Player): Material {
            return Material.NETHER_STAR
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            SelectInheritanceMenu(parent, rank).openMenu(player)
        }
    }

    private class RankInheritButton(private val parent: SelectInheritanceMenu, private val rank: Rank) : Button() {
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
            if (clickType.isLeftClick) {
                if (parent.rank.inheritedRanks.contains(rank)) {
                    player.sendMessage("${parent.rank.getColoredDisplayName()} ${ChatColor.RED}already inherits ${ChatColor.RESET}${rank.getColoredDisplayName()}${ChatColor.RED}.")
                    return
                }

                val dependencyLock = parent.rank.findDependencyLock(rank)
                if (dependencyLock != null) {
                    player.sendMessage("${parent.rank.getColoredDisplayName()} ${ChatColor.RED}can't inherit ${ChatColor.RESET}${rank.getColoredDisplayName()} ${ChatColor.RED}because of a dependency lock (${ChatColor.RESET}${dependencyLock.getColoredDisplayName()}${ChatColor.RED}).")
                    player.sendMessage("${ChatColor.RED}A dependency lock means that rank A can't inherit rank B because rank A indirectly inherits rank B or vice-versa, which would recursively try to collect inheritance resulting in a lock, or a never-ending execution stack.")
                    return
                }

                if (parent.rank.inheritedRanks.add(rank)) {
                    Bukkit.getPluginManager().callEvent(RankUpdateEvent(parent.rank))
                    player.sendMessage("${ChatColor.AQUA}Added ${ChatColor.RESET}${rank.getColoredDisplayName()} ${ChatColor.AQUA}to ${ChatColor.RESET}${parent.rank.getColoredDisplayName()}${ChatColor.AQUA}'s inherited ranks.")
                    EvilPermissions.instance.database.saveRank(parent.rank)
                }
            } else if (clickType.isRightClick) {
                if (parent.rank.inheritedRanks.remove(rank)) {
                    Bukkit.getPluginManager().callEvent(RankUpdateEvent(parent.rank))
                    player.sendMessage("${ChatColor.AQUA}Removed ${ChatColor.RESET}${rank.getColoredDisplayName()} ${ChatColor.AQUA}from ${ChatColor.RESET}${parent.rank.getColoredDisplayName()}${ChatColor.AQUA}'s inherited ranks.")
                }
            }
        }
    }

}