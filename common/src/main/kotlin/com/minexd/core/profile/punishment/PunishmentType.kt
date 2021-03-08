package com.minexd.core.profile.punishment

enum class PunishmentType constructor(
        val action: String,
        val color: String,
        val temporal: Boolean,
        val kick: Boolean,
        vararg kickMessages: String
) {

    BLACKLIST("blacklisted", "§4", false, true, "&cYou've been blacklisted from MineJunkie."),
    BAN("banned", "§c", true, true, "&cYou've been suspended from MineJunkie."),
    MUTE("muted", "§e", true, false),
    WARN("warned", "§a", false, false);

    val kickMessages: List<String> = listOf(*kickMessages)

    fun getViewPermission(): String {
        return "punishments.${name.toLowerCase()}.view"
    }

    fun getDeletePermission(): String {
        return "punishments.${name.toLowerCase()}.delete"
    }

}
