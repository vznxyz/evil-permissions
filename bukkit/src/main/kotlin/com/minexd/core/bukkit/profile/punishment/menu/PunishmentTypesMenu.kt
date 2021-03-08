package com.minexd.core.bukkit.profile.punishment.menu

import com.minexd.core.profile.Profile
import com.minexd.core.profile.punishment.PunishmentType
import com.minexd.core.util.Permissions
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.text.TextUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class PunishmentTypesMenu(private val profile: Profile) : Menu() {

    override fun getTitle(player: Player): String {
        return "Punishments - ${profile.getUsername()}"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        if (player.hasPermission(Permissions.BLACKLIST_VIEW)) {
            buttons[10] = PunishmentTypeButton(PunishmentType.BLACKLIST)
            buttons[12] = PunishmentTypeButton(PunishmentType.BAN)
            buttons[14] = PunishmentTypeButton(PunishmentType.MUTE)
            buttons[16] = PunishmentTypeButton(PunishmentType.WARN)
        } else {
            buttons[11] = PunishmentTypeButton(PunishmentType.BAN)
            buttons[13] = PunishmentTypeButton(PunishmentType.MUTE)
            buttons[15] = PunishmentTypeButton(PunishmentType.WARN)
        }

        return buttons
    }

    override fun size(buttons: Map<Int, Button>): Int {
        return 27
    }

    private inner class PunishmentTypeButton(private val punishmentType: PunishmentType) : Button() {
        override fun getName(player: Player): String {
            return "${punishmentType.color}${punishmentType.name.toLowerCase().capitalize()}s"
        }

        override fun getDescription(player: Player): List<String> {
            val matchingPunishmentsCount = profile.punishments.filter { it.punishmentType == punishmentType }.size
            val pluralized = TextUtil.pluralize(matchingPunishmentsCount, "punishment", "punishments")
            return listOf("${ChatColor.GRAY}There are $matchingPunishmentsCount $pluralized on record.")
        }

        override fun getMaterial(player: Player): Material {
            return Material.WOOL
        }

        override fun getDamageValue(player: Player): Byte {
            return when (punishmentType) {
                PunishmentType.BLACKLIST -> 14
                PunishmentType.BAN -> 1
                PunishmentType.MUTE -> 4
                PunishmentType.WARN -> 5
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            PunishmentsMenu(profile, punishmentType).openMenu(player)
        }

    }

}