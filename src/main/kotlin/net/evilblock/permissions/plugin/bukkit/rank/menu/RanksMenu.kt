package net.evilblock.permissions.plugin.bukkit.rank.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.BackButton
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PageButton
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.TextSplitter
import net.evilblock.cubed.util.bukkit.enchantment.GlowEnchantment
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.plugin.bukkit.rank.menu.bulk.BulkActionMenu
import net.evilblock.permissions.plugin.bukkit.util.ColorMap
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.lang.Exception

class RanksMenu(val group: String) : PaginatedMenu() {

    init {
        updateAfterClick = true
    }

    var bulkSelectionEnabled: Boolean = false
    var bulkSelection: HashSet<Rank> = hashSetOf()

    override fun getPrePaginatedTitle(player: Player): String {
        return "Rank Editor - $group"
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 36
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        // buttons positioned on left
        if (bulkSelectionEnabled) {
            buttons[0] = DisableBulkSelectionButton(this)
            buttons[1] = RedirectBulkActionMenuButton(this)
        } else {
            buttons[0] = EnableBulkSelectionButton(this)
        }

        // pagination buttons
        buttons[3] = PageButton(-1, this)
        buttons[5] = PageButton(1, this)

        // back button positioned on right
        buttons[8] = BackButton {
            RankGroupsMenu().openMenu(player)
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

        // populate buttons from ranks
        val sortedRanks = EvilPermissions.instance.rankHandler.getRanksByGroup(group).sortedBy { it.displayOrder }
        for (rank in sortedRanks) {
            buttons[buttons.size] = RankButton(this, rank)
        }

        return buttons
    }

    private class EnableBulkSelectionButton(private val parent: RanksMenu) : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.GREEN}${ChatColor.BOLD}Enable Bulk Selection"
        }

        override fun getMaterial(player: Player): Material {
            return Material.WOOL
        }

        override fun getDamageValue(player: Player): Byte {
            return 13
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            parent.bulkSelectionEnabled = true
        }
    }

    private class DisableBulkSelectionButton(private val parent: RanksMenu) : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.RED}${ChatColor.BOLD}Disable Bulk Selection"
        }

        override fun getMaterial(player: Player): Material {
            return Material.WOOL
        }

        override fun getDamageValue(player: Player): Byte {
            return 14
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            parent.bulkSelectionEnabled = false
            parent.bulkSelection.clear()
        }
    }

    private class RedirectBulkActionMenuButton(private val parent: RanksMenu) : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.YELLOW}${ChatColor.BOLD}Perform Bulk Action"
        }

        override fun getMaterial(player: Player): Material {
            return Material.SHEARS
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            BulkActionMenu(parent).openMenu(player)
        }
    }

    private class RankButton(private val parent: RanksMenu, private val rank: Rank) : Button() {

        override fun getButtonItem(player: Player): ItemStack {
            val itemStack = super.getButtonItem(player)

            val isBulkSelected = parent.bulkSelectionEnabled && parent.bulkSelection.contains(rank)
            val isDarkRed = rank.gameColor.replace("&", "") == "4"

            if (isBulkSelected || isDarkRed) {
                GlowEnchantment.addGlow(itemStack)
            }

            return itemStack
        }

        override fun getName(player: Player): String {
            val isBulkSelected = parent.bulkSelectionEnabled && parent.bulkSelection.contains(rank)
            if (isBulkSelected) {
                return rank.getColoredDisplayName() + " ${ChatColor.AQUA}${ChatColor.BOLD}(*)"
            }

            return rank.getColoredDisplayName()
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()
            description.add("${ChatColor.YELLOW}${ChatColor.BOLD}Metadata")
            description.add("${ChatColor.GRAY}Name: ${ChatColor.RESET}${rank.id}")
            description.add("${ChatColor.GRAY}Display name: ${ChatColor.RESET}${rank.displayName}")
            description.add("${ChatColor.GRAY}Display order: ${ChatColor.RESET}${rank.displayOrder}")
            description.add("${ChatColor.GRAY}Prefix: ${ChatColor.RESET}${rank.prefix}Example")
            description.add("${ChatColor.GRAY}Player List Prefix: ${ChatColor.RESET}${rank.playerListPrefix}Example")
            description.add("${ChatColor.GRAY}Game Color: ${ChatColor.RESET}${rank.gameColor}${rank.gameColor}")
            description.add("${ChatColor.GRAY}Default: ${ChatColor.RESET}${rank.default}")
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
                (ColorMap.woolMap[ChatColor.getByChar(rank.gameColor.replace("&", ""))]?: 15).toByte()
            } catch (e: Exception) {
                return 15.toByte()
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            // bulk selection handling
            if (parent.bulkSelectionEnabled) {
                if (clickType.isLeftClick) {
                    parent.bulkSelection.add(rank)
                } else if (clickType.isRightClick) {
                    parent.bulkSelection.remove(rank)
                }

                return
            }

            // shift click = delete
            // left click = edit attributes
            // right click = detailed information
            if (clickType.isShiftClick) {
                ConfirmMenu("Delete rank?") { delete ->
                    if (delete) {
                        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
                            EvilPermissions.instance.database.deleteRank(rank)
                        }
                    }

                    player.closeInventory()

                    BukkitPlugin.instance.server.scheduler.runTaskLater(BukkitPlugin.instance, { parent.openMenu(player) }, 2L)
                }.openMenu(player)
            } else {
                if (clickType.isLeftClick) {
                    EditAttributesMenu(parent, rank).openMenu(player)
                } else if (clickType.isRightClick) {
                    player.closeInventory()

                    val messages = arrayListOf<String>()
                    messages.add("${ChatColor.YELLOW}${ChatColor.BOLD}Detailed information of ${rank.displayName}")
                    messages.add("${ChatColor.GRAY}Name: ${ChatColor.RESET}${rank.id}")
                    messages.add("${ChatColor.GRAY}Display name: ${ChatColor.RESET}${rank.displayName}")
                    messages.add("${ChatColor.GRAY}Display order: ${ChatColor.RESET}${rank.displayOrder}")
                    messages.add("${ChatColor.GRAY}Prefix: ${ChatColor.RESET}${ChatColor.translateAlternateColorCodes('&', rank.prefix)}Example")
                    messages.add("${ChatColor.GRAY}Player List Prefix: ${ChatColor.RESET}${ChatColor.translateAlternateColorCodes('&', rank.playerListPrefix)}Example")
                    messages.add("${ChatColor.GRAY}Game Color: ${ChatColor.RESET}${ChatColor.translateAlternateColorCodes('&', rank.gameColor)}${rank.gameColor}")
                    messages.add("${ChatColor.GRAY}Default: ${ChatColor.RESET}${rank.default}")
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
                        val permissionsConcat = TextSplitter.split(1000, "${ChatColor.YELLOW}${ChatColor.BOLD}Permissions: ${ChatColor.RESET}" + rank.getCompoundedPermissions().sorted().joinToString(), "", " ")
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