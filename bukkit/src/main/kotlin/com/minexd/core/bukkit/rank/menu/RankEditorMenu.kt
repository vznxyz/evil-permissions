package com.minexd.core.bukkit.rank.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.BackButton
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PageButton
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.text.TextSplitter
import net.evilblock.cubed.util.bukkit.enchantment.GlowEnchantment
import com.minexd.core.CoreXD
import com.minexd.core.rank.Rank
import com.minexd.core.bukkit.util.ColorMap
import com.minexd.core.rank.RankHandler
import net.evilblock.cubed.menu.buttons.GlassButton
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class RankEditorMenu(val group: String) : PaginatedMenu() {

    init {
        updateAfterClick = true
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Rank Editor - $group"
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            buttons[0] = if (page == 1) {
                BackButton {
                    RankGroupEditorMenu().openMenu(player)
                }
            } else {
                PageButton(-1, this)
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
            val sortedRanks = RankHandler.getRanksByGroup(group).sortedBy { it.displayOrder }
            for (rank in sortedRanks) {
                buttons[buttons.size] = RankButton(this, rank)
            }
        }
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 36
    }

    private inner class RankButton(private val parent: RankEditorMenu, private val rank: Rank) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            val itemStack = super.getButtonItem(player)

            if (rank.getDisplayColorChar() == "4") {
                GlowEnchantment.addGlow(itemStack)
            }

            return itemStack
        }

        override fun getName(player: Player): String {
            return rank.getColoredDisplayName()
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()
            description.add("${ChatColor.GRAY}Name: ${ChatColor.RESET}${rank.id}")
            description.add("${ChatColor.GRAY}Display name: ${ChatColor.RESET}${rank.displayName}")
            description.add("${ChatColor.GRAY}Display color: ${ChatColor.RESET}${rank.getColor()}&${rank.getDisplayColorChar()}")
            description.add("${ChatColor.GRAY}Display order: ${ChatColor.RESET}${rank.displayOrder}")
            description.add("${ChatColor.GRAY}Prefix: ${ChatColor.RESET}${ChatColor.translateAlternateColorCodes('&', rank.prefix)}Example")
            description.add("${ChatColor.GRAY}Player list prefix: ${ChatColor.RESET}${ChatColor.translateAlternateColorCodes('&', rank.playerListPrefix)}Example")
            description.add("${ChatColor.GRAY}Display name prefix: ${ChatColor.RESET}${ChatColor.translateAlternateColorCodes('&', rank.displayNamePrefix)}Example")
            description.add("${ChatColor.GRAY}Default: ${ChatColor.RESET}${rank.default}")
            description.add("${ChatColor.GRAY}Hidden: ${ChatColor.RESET}${rank.hidden}")
            description.add("")
            description.add("${ChatColor.YELLOW}${ChatColor.BOLD}Groups")

            for (group in rank.groups) {
                description.add(" ${ChatColor.BLUE}${ChatColor.BOLD}$group")
            }

            description.add("")
            description.add("${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK to edit attributes")
            description.add("${ChatColor.AQUA}${ChatColor.BOLD}RIGHT-CLICK to show detailed info")
            description.add("${ChatColor.RED}${ChatColor.BOLD}SHIFT-CLICK to delete rank")

            val bar = ChatColor.translateAlternateColorCodes('&', BAR)
            description.add(0, bar)
            description.add(bar)

            return description
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
            if (clickType.isShiftClick && clickType.isLeftClick) {
                ConfirmMenu("Delete rank?") { confirmed ->
                    if (confirmed) {
                        Tasks.async {
                            try {
                                val response = CoreXD.instance.ranksService.delete(rank.id).execute()
                                if (!response.isSuccessful) {
                                    player.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    parent.openMenu(player)
                }.openMenu(player)
            } else {
                if (clickType.isLeftClick) {
                    EditRankMenu(group, rank).openMenu(player)
                } else if (clickType.isRightClick) {
                    player.closeInventory()

                    val messages = arrayListOf<String>()
                    messages.add("${ChatColor.YELLOW}${ChatColor.BOLD}Detailed information of ${rank.displayName}")
                    messages.add("${ChatColor.GRAY}Name: ${ChatColor.RESET}${rank.id}")
                    messages.add("${ChatColor.GRAY}Display name: ${ChatColor.RESET}${rank.displayName}")
                    messages.add("${ChatColor.GRAY}Display color: ${ChatColor.RESET}${rank.getColor()}&${rank.getDisplayColorChar()}")
                    messages.add("${ChatColor.GRAY}Display order: ${ChatColor.RESET}${rank.displayOrder}")
                    messages.add("${ChatColor.GRAY}Prefix: ${ChatColor.RESET}${ChatColor.translateAlternateColorCodes('&', rank.prefix)}Example")
                    messages.add("${ChatColor.GRAY}Player list prefix: ${ChatColor.RESET}${ChatColor.translateAlternateColorCodes('&', rank.playerListPrefix)}Example")
                    messages.add("${ChatColor.GRAY}Display name prefix: ${ChatColor.RESET}${ChatColor.translateAlternateColorCodes('&', rank.displayNamePrefix)}Example")
                    messages.add("${ChatColor.GRAY}Default: ${ChatColor.RESET}${rank.default}")
                    messages.add("${ChatColor.GRAY}Hidden: ${ChatColor.RESET}${rank.hidden}")
                    messages.add("")

                    if (rank.groups.isEmpty()) {
                        messages.add("${ChatColor.YELLOW}${ChatColor.BOLD}Groups: ${ChatColor.RESET}None assigned")
                    } else {
                        val groupsConcat = rank.groups.joinToString { "${ChatColor.BLUE}${ChatColor.BOLD}$it${ChatColor.RESET}" }
                        messages.add("${ChatColor.YELLOW}${ChatColor.BOLD}Groups: " + groupsConcat)
                    }

                    messages.add("")

                    if (rank.inheritedRanks.isEmpty()) {
                        messages.add("${ChatColor.YELLOW}${ChatColor.BOLD}Inherits: ${ChatColor.RESET}None inherited")
                    } else {
                        val inheritedConcat = rank.inheritedRanks.joinToString() { rank -> rank.getColoredDisplayName() }
                        messages.add("${ChatColor.YELLOW}${ChatColor.BOLD}Inherits: ${ChatColor.RESET}" + inheritedConcat)
                    }

                    messages.add("")

                    if (rank.getCompoundedPermissions().isEmpty()) {
                        messages.add("${ChatColor.YELLOW}${ChatColor.BOLD}Permissions: ${ChatColor.RESET}None assigned")
                    } else {
                        val permissionsConcat = TextSplitter.split(length = 1000, text = "${ChatColor.YELLOW}${ChatColor.BOLD}Permissions: ${ChatColor.RESET}" + rank.getCompoundedPermissions().sorted().joinToString())
                        messages.addAll(permissionsConcat)
                    }

                    messages.add(0, "${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}----------------------------------------")
                    messages.add("${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}----------------------------------------")

                    for (message in messages) {
                        player.sendMessage(message)
                    }
                }
            }
        }

    }

}