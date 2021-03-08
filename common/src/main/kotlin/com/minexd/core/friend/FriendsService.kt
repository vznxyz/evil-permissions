package com.minexd.core.friend
import com.minexd.core.friend.result.Friend
import com.minexd.core.friend.result.FriendRemoveResult
import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface FriendsService {

    @GET("friends/list")
    fun getFriendsList(@Query("player_id") id: UUID): Call<List<Friend>>

    @GET("friendships/list")
    fun getFriendships(@Query("player_id") id: UUID): Call<List<Friend>>

    @GET("friendships/incoming")
    fun getIncomingFriendships(@Query("player_id") id: UUID): Call<List<Friend>>

    @GET("friendships/outgoing")
    fun getOutgoingFriendships(@Query("player_id") id: UUID): Call<List<Friend>>

    @GET("friendships/show")
    fun showFriendship(@Query("player_id") id: UUID, @Query("friend_id") friendId: UUID): Call<Friendship>

    @JvmSuppressWildcards
    @POST("friendships/create")
    fun createFriendship(@Query("player_id") id: UUID, @Query("friend_id") friendId: UUID, @Query("only_accept") onlyAccept: Boolean = false): Call<Friendship>

    @JvmSuppressWildcards
    @POST("friendships/destroy")
    fun destroyFriendship(@Query("player_id") id: UUID, @Query("friend_id") friendId: UUID): Call<FriendRemoveResult.Response>

    @JvmSuppressWildcards
    @POST("friendships/update")
    fun updateFriendship(@Query("player_id") id: UUID, @Body data: Map<String, Any?>): Call<Void>

}