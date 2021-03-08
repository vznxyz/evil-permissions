package com.minexd.core.bukkit.hook

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import com.minexd.core.bukkit.BukkitPlugin
import com.minexd.core.rank.RankHandler
import com.minexd.core.profile.ProfileHandler
import org.bukkit.entity.Player

/**
 * This class will be registered through the register-method in the
 * plugins onEnable-method.
 */
class CorePlaceholderExpansion : PlaceholderExpansion() {

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    override fun persist(): Boolean {
        return true
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return `true`
     *
     * @return Always true since it's an internal class.
     */
    override fun canRegister(): Boolean {
        return true
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br></br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    override fun getAuthor(): String {
        return BukkitPlugin.instance.description.authors.toString()
    }

    /**
     * The placeholder identifier should go here.
     * <br></br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br></br>This must be unique and can not contain % or _
     *
     * @return The identifier in `%<identifier>_<value>%` as String.
     */
    override fun getIdentifier(): String {
        return "core"
    }

    /**
     * This is the version of the expansion.
     * <br></br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    override fun getVersion(): String {
        return BukkitPlugin.instance.description.version
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br></br>We specify the value identifier in this method.
     * <br></br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param player A [Player].
     * @param identifier A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        if (player == null) {
            return ""
        }

        val profile = ProfileHandler.getProfile(player.uniqueId)

        // %core_rank_name%
        if (identifier == "rank_name") {
            return profile.getBestDisplayRank().displayName ?: RankHandler.getDefaultRank().displayName
        }

        // %core_rank_name_colored%
        if (identifier == "rank_name_colored") {
            return profile.getBestDisplayRank().getColoredDisplayName() ?: RankHandler.getDefaultRank().getColoredDisplayName()
        }

        // %core_rank_prefix%
        if (identifier == "rank_prefix") {
            return profile.getBestDisplayRank().getChatPrefix() ?: RankHandler.getDefaultRank().getChatPrefix()
        }

        return null
    }

}