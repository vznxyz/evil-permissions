package com.minexd.core.bukkit.profile.punishment.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.bukkit.Tasks
import com.minexd.core.bukkit.profile.punishment.menu.button.PunishmentButton
import com.minexd.core.profile.Profile
import com.minexd.core.profile.punishment.PunishmentType
import org.bukkit.entity.Player

class PunishmentsMenu(private val profile: Profile, private val punishmentType: PunishmentType) : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player): String {
        return "${punishmentType.name.toLowerCase().capitalize()}s"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        val punishments = profile.punishments.filter { it.punishmentType == punishmentType }.sortedBy { -it.issuedAt }
        for (punishment in punishments) {
            buttons[buttons.size] = PunishmentButton(this, profile, punishment)
        }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            Tasks.delayed(1L) {
                PunishmentTypesMenu(profile).openMenu(player)
            }
        }
    }

}