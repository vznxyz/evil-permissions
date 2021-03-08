package com.minexd.core.bukkit.rank.menu

import mkremins.fanciful.FancyMessage
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.GlassButton
import net.evilblock.cubed.menu.buttons.MenuButton
import net.evilblock.cubed.menu.menus.SelectColorMenu
import net.evilblock.cubed.util.math.Numbers
import net.evilblock.cubed.util.text.TextSplitter
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ConversationUtil
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import com.minexd.core.CoreXD
import com.minexd.core.bukkit.rank.event.RankUpdateEvent
import com.minexd.core.rank.Rank
import com.minexd.core.bukkit.rank.menu.prompt.AddGroupConversation
import com.minexd.core.bukkit.util.ColorMap
import net.evilblock.cubed.util.bukkit.ColorUtil
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
import net.evilblock.cubed.util.bukkit.prompt.NumberPrompt
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class EditRankMenu(val group: String, val rank: Rank) : Menu("Edit Rank") {

    init {
        updateAfterClick = true
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            buttons[0] = MenuButton()
                    .icon(Material.HOPPER)
                    .name("${ChatColor.AQUA}${ChatColor.BOLD}Edit Groups")
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "Change the groups that this rank belongs to."))
                        desc.add("")
                        desc.add(Button.styleAction(ChatColor.GREEN, "LEFT-CLICK", "to add a group"))
                        desc.add(Button.styleAction(ChatColor.RED, "LEFT-CLICK", "to remove a group"))
                    })
                    .action { _, clickType ->
                        if (clickType.isLeftClick) {
                            ConversationUtil.startConversation(player, AddGroupConversation(this, rank))
                        } else {
                            player.closeInventory()

                            val groupsMessage = FancyMessage("")

                            rank.groups.forEachIndexed { index, group ->
                                groupsMessage
                                        .then(group).color(ChatColor.BLUE).style(ChatColor.BOLD)
                                        .then("[").color(ChatColor.GRAY)
                                        .then("✗").color(ChatColor.RED).style(ChatColor.BOLD)
                                        .formattedTooltip(FancyMessage("${ChatColor.YELLOW}Click to remove this group."))
                                        .command("/rank editor rm_group ${rank.id} $group")
                                        .then("]").color(ChatColor.GRAY)

                                if (index != rank.groups.size - 1) {
                                    groupsMessage.then(", ").color(ChatColor.WHITE)
                                }
                            }

                            player.sendMessage("")

                            FancyMessage("${ChatColor.YELLOW}Groups of ${rank.displayName}")
                                    .style(ChatColor.BOLD)
                                    .formattedTooltip(FancyMessage("${ChatColor.YELLOW}You are viewing a list of ${rank.displayName}'s groups."))
                                    .send(player)

                            groupsMessage.send(player)

                            player.sendMessage("")
                        }
                    }

            buttons[1] = MenuButton()
                    .icon(Material.NAME_TAG)
                    .name("${ChatColor.AQUA}${ChatColor.BOLD}Edit Display Name")
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "Change the display name, which is rendered to chat and menu text."))
                        desc.add("")
                        desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit display name"))
                    })
                    .action(ClickType.LEFT) {
                        InputPrompt()
                                .withText("${ChatColor.GREEN}Please input a new display name.")
                                .acceptInput { input ->
                                    val newDisplayName = ChatColor.translateAlternateColorCodes('&', input)

                                    updateAttribute(player, rank, mapOf("displayName" to newDisplayName)) {
                                        rank.displayName = newDisplayName
                                    }
                                }
                                .start(player)
                    }

            buttons[2] = MenuButton()
                    .icon(ItemStack(Material.WOOL, 1, (ColorMap.woolMap[ChatColor.getByChar(rank.getDisplayColorChar())] ?: 15).toShort()))
                    .name("${ChatColor.AQUA}${ChatColor.BOLD}Edit Color")
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "Change the color that represents this rank. The color should match the rank's prefix."))
                        desc.add("")
                        desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit color"))
                    })
                    .action(ClickType.LEFT) {
                        SelectColorMenu { color ->
                            val newColor = ColorUtil.toChatColor(color).toString()

                            updateAttribute(player, rank, mapOf("gameColor" to newColor)) {
                                rank.gameColor = newColor
                            }
                        }.openMenu(player)
                    }

            buttons[3] = MenuButton()
                    .icon(ItemStack(Material.WOOL, 1, (ColorMap.woolMap[ChatColor.getByChar(rank.getDisplayColorChar())] ?: 15).toShort()))
                    .name("${ChatColor.AQUA}${ChatColor.BOLD}Edit Site Color")
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "Change the color that represents this rank on the website. e.g. #ffffff"))
                        desc.add("")
                        desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit site color"))
                    })
                    .action(ClickType.LEFT) {
                        EzPrompt.Builder()
                                .promptText("${ChatColor.GREEN}Please input a hex color value. ${ChatColor.GRAY}(e.g. #ffffff)")
                                .acceptInput { color ->
                                    if (!color.matches("#?(?:[0-9a-fA-F]{3}){1,2}".toRegex())) {
                                        player.sendMessage("${ChatColor.RED}Invalid color provided! Format: #ffffff")
                                        return@acceptInput
                                    }

                                    val siteColor = color.replace("#", "")

                                    updateAttribute(player, rank, mapOf("siteColor" to siteColor)) {
                                        rank.siteColor = siteColor
                                    }
                                }
                                .build()
                                .start(player)
                    }

            buttons[4] = MenuButton()
                    .texturedIcon(Constants.WOOD_NUMBER_TEXTURE)
                    .name { "${ChatColor.AQUA}${ChatColor.BOLD}Edit Order ${ChatColor.GRAY}(${Numbers.format(rank.displayOrder)})" }
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "Change the order in how ranks are sorted and prioritized."))
                        desc.add("")
                        desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit order"))
                    })
                    .action(ClickType.LEFT) {
                        NumberPrompt()
                                .withText("${ChatColor.GREEN}Please input the new order.")
                                .acceptInput { number ->
                                    val num = number.toInt()
                                    if (num < 1 || num > 999) {
                                        player.sendMessage("${ChatColor.RED}Invalid range! (1-999)")
                                        return@acceptInput
                                    }

                                    updateAttribute(player, rank, mapOf("displayOrder" to num)) {
                                        rank.displayOrder = num
                                    }
                                }
                                .start(player)
                    }

            buttons[5] = MenuButton()
                    .icon(Material.SIGN)
                    .name("${ChatColor.AQUA}${ChatColor.BOLD}Edit Prefix")
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "Change the prefix that is prepended to a player's nametag in chat messages."))
                        desc.add("")
                        desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit prefix"))
                    })
                    .action(ClickType.LEFT) {
                        InputPrompt()
                                .withText("${ChatColor.GREEN}Please input a new prefix.")
                                .acceptInput { input ->
                                    val newPrefix = ChatColor.translateAlternateColorCodes('&', input)

                                    updateAttribute(player, rank, mapOf("prefix" to newPrefix)) {
                                        rank.prefix = newPrefix
                                    }
                                }
                                .start(player)
                    }

            buttons[6] = MenuButton()
                    .icon(Material.SIGN)
                    .name("${ChatColor.AQUA}${ChatColor.BOLD}Edit Tablist Prefix")
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "Change the prefix that is prepended to a player's nametag in the tablist."))
                        desc.add("")
                        desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit tablist prefix"))
                    })
                    .action(ClickType.LEFT) {
                        InputPrompt()
                                .withText("${ChatColor.GREEN}Please input a new tablist prefix.")
                                .withLimit(16)
                                .acceptInput { input ->
                                    val newPrefix = ChatColor.translateAlternateColorCodes('&', input)

                                    updateAttribute(player, rank, mapOf("playerListPrefix" to newPrefix)) {
                                        rank.playerListPrefix = newPrefix
                                    }
                                }
                                .start(player)
                    }

            buttons[7] = MenuButton()
                    .icon(Material.ENCHANTMENT_TABLE)
                    .name("${ChatColor.AQUA}${ChatColor.BOLD}Edit Permissions")
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "The permissions that are assigned to players who are granted this rank."))
                        desc.add("")
                        desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to add a permission"))
                        desc.add(styleAction(ChatColor.RED, "RIGHT-CLICK", "to remove a permission"))
                        desc.add(styleAction(ChatColor.AQUA, "SHIFT RIGHT-CLICK", "to view permissions"))
                    })
                    .action { _, clickType ->
                        if (clickType.isLeftClick) {
                            InputPrompt()
                                    .withText("${ChatColor.GREEN}Please input the permission you want to add.")
                                    .withLimit(100)
                                    .acceptInput { input ->
                                        if (rank.permissions.contains(input)) {
                                            player.sendMessage("${ChatColor.RED}Permission node ${ChatColor.YELLOW}${input} ${ChatColor.RED}is already assigned to ${ChatColor.WHITE}${ChatColor.BOLD}${rank.displayName}!")
                                            return@acceptInput
                                        }

                                        Tasks.async {
                                            try {
                                                val response = CoreXD.instance.ranksService.update(rank.id, mapOf("addPermission" to input)).execute()
                                                if (response.isSuccessful) {
                                                    rank.permissions.add(input)
                                                    RankUpdateEvent(rank).call()

                                                    player.sendMessage("${ChatColor.GREEN}Successfully added permission ${ChatColor.WHITE}${input} ${ChatColor.GREEN}to ${rank.getColoredDisplayName()}${ChatColor.GREEN}!")
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                player.sendMessage("${ChatColor.RED}")
                                            }
                                        }

                                        openMenu(player)
                                    }
                                    .start(player)
                        } else {
                            player.closeInventory()

                            val permissionsMessages = arrayListOf<FancyMessage>()
                            var currentBaseMessage = FancyMessage("")

                            rank.permissions.sorted().forEachIndexed { index, permission ->
                                currentBaseMessage
                                        .then(permission).color(ChatColor.WHITE)
                                        .then("[").color(ChatColor.GRAY)
                                        .then("✗").color(ChatColor.RED).style(ChatColor.BOLD)
                                        .formattedTooltip(FancyMessage("${ChatColor.YELLOW}Click to remove this permission node."))
                                        .command("/rank editor rm_perm ${rank.id} $permission")
                                        .then("]").color(ChatColor.GRAY)

                                if (index != rank.permissions.size - 1) {
                                    currentBaseMessage.then(", ").color(ChatColor.WHITE)
                                }

                                if (currentBaseMessage.toJSONString().length >= 30000) {
                                    permissionsMessages.add(currentBaseMessage)
                                    currentBaseMessage = FancyMessage("")
                                }
                            }

                            permissionsMessages.add(currentBaseMessage)

                            player.sendMessage("")
                            player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}Permission nodes of ${rank.displayName}")

                            for (permissionsMessage in permissionsMessages) {
                                permissionsMessage.send(player)
                            }

                            player.sendMessage("")
                        }
                    }

            buttons[8] = MenuButton()
                    .icon(Material.NETHER_STAR)
                    .name("${ChatColor.AQUA}${ChatColor.BOLD}Edit Inheritance")
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "Structure the rank hierarchy by making ranks inherit other ranks."))
                        desc.add("")
                        desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to edit inheritance"))
                    })
                    .action(ClickType.LEFT) {
                        EditRankInheritanceMenu(group, rank).openMenu(player)
                    }

            buttons[9] = MenuButton()
                    .icon {
                        val data = if (rank.default) { 10 } else { 8 }.toShort()
                        ItemStack(Material.INK_SACK, 1, data)
                    }
                    .name {
                        buildString {
                            append("${ChatColor.AQUA}${ChatColor.BOLD}Toggle Default Rank")
                            append(" ")

                            if (rank.default) {
                                append("${ChatColor.GRAY}(${ChatColor.GREEN}enabled${ChatColor.GRAY})")
                            } else {
                                append("${ChatColor.GRAY}(${ChatColor.RED}disabled${ChatColor.GRAY})")
                            }
                        }
                    }
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "If enabled, this rank will be the default rank applied to all players."))
                        desc.add("")

                        if (rank.default) {
                            desc.add(styleAction(ChatColor.RED, "LEFT-CLICK", "to disable default rank"))
                        } else {
                            desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to enable default rank"))
                        }
                    })
                    .action(ClickType.LEFT) {
                        updateAttribute(player, rank, mapOf("default" to !rank.default)) {
                            rank.hidden = !rank.hidden
                        }
                    }

            buttons[10] = MenuButton()
                    .icon {
                        val data = if (rank.hidden) { 10 } else { 8 }.toShort()
                        ItemStack(Material.INK_SACK, 1, data)
                    }
                    .name {
                        buildString {
                            append("${ChatColor.AQUA}${ChatColor.BOLD}Toggle Hidden Rank")
                            append(" ")

                            if (rank.hidden) {
                                append("${ChatColor.GRAY}(${ChatColor.GREEN}enabled${ChatColor.GRAY})")
                            } else {
                                append("${ChatColor.GRAY}(${ChatColor.RED}disabled${ChatColor.GRAY})")
                            }
                        }
                    }
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "If hidden, this rank will not be displayed in the /list command."))
                        desc.add("")

                        if (rank.hidden) {
                            desc.add(styleAction(ChatColor.RED, "LEFT-CLICK", "to disable hidden rank"))
                        } else {
                            desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to enable hidden rank"))
                        }
                    })
                    .action(ClickType.LEFT) {
                        updateAttribute(player, rank, mapOf("hidden" to !rank.hidden)) {
                            rank.hidden = !rank.hidden
                        }
                    }

            buttons[11] = MenuButton()
                    .icon {
                        val data = if (rank.staff) { 10 } else { 8 }.toShort()
                        ItemStack(Material.INK_SACK, 1, data)
                    }
                    .name {
                        buildString {
                            append("${ChatColor.AQUA}${ChatColor.BOLD}Toggle Staff Rank")
                            append(" ")

                            if (rank.staff) {
                                append("${ChatColor.GRAY}(${ChatColor.GREEN}enabled${ChatColor.GRAY})")
                            } else {
                                append("${ChatColor.GRAY}(${ChatColor.RED}disabled${ChatColor.GRAY})")
                            }
                        }
                    }
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "If enabled, this rank will be considered a rank of staff privilege."))
                        desc.add("")

                        if (rank.staff) {
                            desc.add(styleAction(ChatColor.RED, "LEFT-CLICK", "to disable staff rank"))
                        } else {
                            desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to enable staff rank"))
                        }
                    })
                    .action(ClickType.LEFT) {
                        updateAttribute(player, rank, mapOf("staff" to !rank.staff)) {
                            rank.staff = !rank.staff
                        }
                    }

            for (i in 0 until 18) {
                if (!buttons.containsKey(i)) {
                    buttons[i] = GlassButton(7)
                }
            }
        }
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            Tasks.delayed(1L) {
                RankEditorMenu(group).openMenu(player)
            }
        }
    }

    private fun updateAttribute(player: Player, rank: Rank, data: Map<String, Any?>, success: () -> Unit) {
        player.closeInventory()

        Tasks.async {
            try {
                val response = CoreXD.instance.ranksService.update(rank.id, data).execute()
                if (response.isSuccessful) {
                    success.invoke()
                    RankUpdateEvent(rank).call()

                    Tasks.sync {
                        openMenu(player)
                    }
                } else {
                    player.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                player.sendMessage("${ChatColor.RED}Failed to update rank!")
            }
        }
    }

}