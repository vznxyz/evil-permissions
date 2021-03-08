package com.minexd.core.presence

data class PlayerPresence(
        var state: State,
        var server: String,
        var heartbeat: Long,
        var session: Long
) {

    var rich: String? = null

    constructor(map: Map<String, String>) : this(
            state = State.valueOf(map.getOrDefault("state", "OFFLINE")),
            server = map.getValue("server"),
            heartbeat = map.getValue("heartbeat").toLong(),
            session = map.getValue("session").toLong()
    ) {
        if (map.containsKey("rich")) {
            rich = map.getValue("rich")
        }
    }

    fun toMap(): Map<String, String> {
        return mapOf(
                "state" to state.name,
                "server" to server,
                "heartbeat" to heartbeat.toString(),
                "session" to session.toString()
        )
    }

    fun isOnline(): Boolean {
        return if (System.currentTimeMillis() - heartbeat >= 30_000) {
            false
        } else {
            state == State.ONLINE
        }
    }

    enum class State {
        ONLINE,
        OFFLINE
    }

}