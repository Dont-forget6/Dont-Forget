package com.test.dontforget.UI.MainAlertFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.test.dontforget.DAO.AlertClass
import com.test.dontforget.Repository.AlertRepository

class MainAlertViewModel : ViewModel() {

    var alertIdx = MutableLiveData<Long>()
    var alertContent = MutableLiveData<String>()
    var alertReceiverIdx = MutableLiveData<Long>()
    var alertType = MutableLiveData<Long>()
    var alertList = MutableLiveData<MutableList<AlertClass>>()

    init {
        alertList.value = mutableListOf<AlertClass>()
    }

    fun getAlertInfo(todoIdx: Long) {

        AlertRepository.getAlertInfoByIdx(todoIdx) { it ->
            for (c1 in it.result.children) {
                alertIdx.value = c1.child("alertIdx").value as Long
                alertContent.value = c1.child("alertContent").value as String
                alertReceiverIdx.value = c1.child("alertReceiverIdx").value as Long
                alertType.value = c1.child("alertType").value as Long
            }
        }
    }

    fun getAlert(userIdx: Long) {

        val tempList = mutableListOf<AlertClass>()

        AlertRepository.getAlertInfoAll(userIdx) {
            for (c1 in it.result.children) {
                val alertIdx = c1.child("alertIdx").value as Long
                var alertContent = c1.child("alertContent").value as String
                val alertReceiverIdx = c1.child("alertReceiverIdx").value as Long
                val alertType = c1.child("alertType").value as Long
                val alertName = c1.child("alertName").value as String
                val a1 = AlertClass(alertIdx, alertContent, alertReceiverIdx, alertType,alertName)
                tempList.add(a1)
            }

            tempList.reverse()
            alertList.value = tempList
        }
    }
}