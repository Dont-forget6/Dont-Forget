package com.test.dontforget.DAO

data class JoinFriend(
    var joinFriendIdx : Long,
    var joinFriendSenderIdx : Long,
    var joinFriendSenderName : String,
    var joinFriendReceiverEmail : String,
)