package com.minexd.core.bukkit.profile.grant.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import com.minexd.core.bukkit.profile.grant.menu.button.GrantButton
import com.minexd.core.profile.Profile
import com.minexd.core.profile.ProfileHandler
import com.minexd.core.profile.grant.Grant
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import kotlin.collections.HashMap

class GrantsMenu(private val target: Profile) : PaginatedMenu() {

    internal data class GrantResourceData(val issuer: Profile?, val remover: Profile?)

    private val resources = hashMapOf<Grant, GrantResourceData>()

    init {
        async = true
    }

    override fun asyncLoadResources(callback: (Boolean) -> Unit) {
        try {
            for (grant in target.grants) {
                var issuer: Profile? = null
                var remover: Profile? = null

                if (grant.issuedBy != null) {
                    issuer = try {
                        ProfileHandler.getOrFetchProfile(grant.issuedBy!!)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (grant.removed) {
                    remover = try {
                        ProfileHandler.getOrFetchProfile(grant.removedBy!!)
                    } catch (e: Exception) {
                        null
                    }
                }

                resources[grant] = GrantResourceData(issuer, remover)
            }

            callback.invoke(true)
        } catch (e: Exception) {
            callback.invoke(false)
        }
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Grants - ${target.getColoredUsername()}${ChatColor.RESET}"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            val grants = target.grants.sortedBy { -it.issuedAt }
            for (grant in grants) {
                buttons[buttons.size] = GrantButton(this, target, grant, resources[grant]?.issuer, resources[grant]?.remover)
            }
        }
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 36
    }

}