package com.test.dontforget.DAO

data class AlertClass(
    var alertIdx : Long,
    var alertContent : String,
    var alertReceiverIdx : Long,
    val alertType : Long,
    val alertName : String
)