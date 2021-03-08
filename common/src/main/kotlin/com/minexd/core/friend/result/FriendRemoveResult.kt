package com.minexd.core.friend.result

enum class FriendRemoveResult {

    FRIEND_REMOVED,
    REQUEST_CANCELLED,
    REQUEST_REJECTED;

    data class Response(val result: FriendRemoveResult)

}