package com.minexd.core.bukkit.rank.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.MenuButton
import net.evilblock.cubed.util.text.TextSplitter
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import com.minexd.core.CoreXD
import com.minexd.core.rank.RankHandler
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

class RankGroupEditorMenu : Menu() {

    init {
        updateAfterClick = true
    }

    override fun getTitle(player: Player): String {
        return "Rank Groups"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            buttons[0] = MenuButton()
                    .texturedIcon(Constants.GREEN_PLUS_TEXTURE)
                    .name("${ChatColor.AQUA}${ChatColor.BOLD}Create New Rank")
                    .lore(arrayListOf<String>().also { desc ->
                        desc.add("")
                        desc.addAll(TextSplitter.split(text = "Create a new rank by completing the setup procedure."))
                        desc.add("")
                        desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to create new rank"))
                    })
                    .action(ClickType.LEFT) {
                        InputPrompt()
                                .withText(EzPrompt.IDENTIFIER_PROMPT)
                                .withLimit(32)
                                .acceptInput { input ->
                                    Tasks.async {
                                        try {
                                            val response = CoreXD.instance.ranksService.create(input).execute()
                                            if (response.isSuccessful) {
                                                val rank = response.body()!!
                                                RankHandler.cache(rank)

                                                Tasks.sync {
                                                    EditRankMenu("GLOBAL", rank).openMenu(player)
                                                }
                                            } else {
                                                player.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            player.sendMessage("${ChatColor.RED}Failed to send request to API!")
                                        }
                                    }
                                }
                                .start(player)
                    }

            for (group in CoreXD.instance.plugin.getActiveGroups()) {
                buttons[buttons.size] = RankGroupButton(group)
            }
        }
    }

    private class RankGroupButton(private val group: String) : Button() {
        override fun getName(player: Player): String {
            val active = if (CoreXD.instance.plugin.getActiveGroups().contains(group)) {
                "${ChatColor.GREEN}${ChatColor.BOLD}(Active)"
            } else {
                "${ChatColor.RED}${ChatColor.RED}(Inactive)"
            }

            return "${ChatColor.BLUE}${ChatColor.BOLD}$group $active"
        }

        override fun getMaterial(player: Player): Material {
            return Material.HOPPER
        }

        override fun applyMetadata(player: Player, itemMeta: ItemMeta): ItemMeta? {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true) // b = ignoreLevelRestrictions (same as addUnsafeEnchantment)
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            return itemMeta
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            RankEditorMenu(group).openMenu(player)
        }
    }

}