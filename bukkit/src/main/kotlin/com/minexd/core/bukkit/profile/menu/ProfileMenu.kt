package com.minexd.core.bukkit.profile.menu

import com.minexd.core.profile.Profile
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.GlassButton
import org.bukkit.entity.Player

class ProfileMenu(val profile: Profile) : Menu() {

    override fun getButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            ProfileLayout.render(player, buttons)

            for (i in 9 until 18) {
                buttons[i] = GlassButton(1)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>): Int {
        return 45
    }

}