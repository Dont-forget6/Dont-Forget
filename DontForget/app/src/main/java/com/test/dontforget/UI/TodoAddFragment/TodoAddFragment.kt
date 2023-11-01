package com.test.dontforget.UI.TodoAddFragment

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import com.test.dontforget.AlarmFunctions
import com.test.dontforget.DAO.AlertClass
import com.test.dontforget.DAO.TodoClass
import com.test.dontforget.MainActivity
import com.test.dontforget.MyApplication
import com.test.dontforget.R
import com.test.dontforget.Repository.AlertRepository
import com.test.dontforget.Repository.CategoryRepository
import com.test.dontforget.Repository.TodoRepository
import com.test.dontforget.databinding.DialogNormalBinding
import com.test.dontforget.databinding.FragmentTodoAddBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class TodoAddFragment : Fragment() {

    lateinit var todoAddActivity: TodoAddActivity
    lateinit var todoAddBinding: FragmentTodoAddBinding
    lateinit var viewModel: TodoAddFragmentViewModel

    lateinit var alarmFunctions: AlarmFunctions

    //이름,위도,경도 결과 받아옴
    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

            if(it.resultCode == Activity.RESULT_OK){
                val intent = it.data
                if(intent!=null){
                    val place = Autocomplete.getPlaceFromIntent(intent)

                    var placeName = place.name
                    // ㅎㅎ포차

                    var placeDetail = place.address
                    // 대한민국 서울특별시 중랑구 상봉동 번지 1층 88-48

                    // Send 위치 데이터
                    var temp = placeDetail + "@" + placeName

                    // Show 위치 데이터
                    var temp2 = placeDetail + " , "+ placeName

                    //Send 위치 데이터 저장
                    MyApplication.locationName = temp

                    //Show 위치 데이터 저장
                    MyApplication.locationStoredName = temp2

                    viewModel.locate.value = MyApplication.locationStoredName

                    //장소 위도
                    var latitude = place.latLng.latitude
                    MyApplication.locationLatitude = latitude.toString()

                    //장소 경도
                    var longitude = place.latLng.longitude
                    MyApplication.locationLongitude = longitude.toString()

                }else if(it.resultCode == Activity.RESULT_CANCELED){
                }
            }
        }


    var date:String =""
    var myDate :String = ""
    var time:String = ""
    var name :String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        todoAddActivity = activity as TodoAddActivity
        todoAddBinding = FragmentTodoAddBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(todoAddActivity).get(TodoAddFragmentViewModel::class.java)


        viewModel.run {
            viewModel.name.observe(todoAddActivity){
                todoAddBinding.textViewTodoAddCategory.text = String.format("%s",it)
            }
            viewModel.categoryColor.observe(todoAddActivity){
                todoAddBinding.run {
                    cardviewTodoAddCategory.setCardBackgroundColor(it.toInt())
                    textInputLayoutTodoAdd.boxStrokeColor = it.toInt()
                    textInputLayoutTodoAdd.hintTextColor = ColorStateList.valueOf(it.toInt())
                    textInputLayoutTodoAddMemo.boxStrokeColor = it.toInt()
                    textInputLayoutTodoAddMemo.hintTextColor = ColorStateList.valueOf(it.toInt())
                }
            }
            viewModel.fontColor.observe(todoAddActivity){
                todoAddBinding.textViewTodoAddCategory.setTextColor(it.toInt())
            }
            viewModel.date.observe(todoAddActivity){
                todoAddBinding.textViewTodoAddDate.setText(it)
            }
            viewModel.time.observe(todoAddActivity){
                todoAddBinding.textViewTodoAddAlert.setText(it)
            }
            viewModel.locate.observe(todoAddActivity){
                todoAddBinding.textViewTodoAddLocation.setText(it)
            }
        }


        todoAddBinding.run {
            //툴바
            toolbarTodoAdd.run {
                setNavigationIcon(R.drawable.ic_arrow_back_24px)
                setNavigationOnClickListener {
                    viewModel.resetList()

                    val mainIntent = Intent(todoAddActivity, MainActivity::class.java)
                    startActivity(mainIntent)
                    todoAddActivity.finish()
                }
                setTitle("할일 추가")
            }
            //카데고리
            linearlayoutTodoAddCategory.run {

                setOnClickListener {
                    //번들로 기존 카데고리 데이터 넘겨줌
                    var bundle = Bundle()
                    bundle.putString("category","${textViewTodoAddCategory.text}")

                    var bottomDialog = TodoAddBottomDialog()
                    bottomDialog.arguments = bundle
                    bottomDialog.show(todoAddActivity.supportFragmentManager,"카테고리")
                }

            }
            //날짜
            linearlayoutTodoAddDate.run {

                setOnClickListener {
                    val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build()
                    materialDatePicker.addOnPositiveButtonClickListener {

                        //보여주는 DateFormat
                        val dateformatter = SimpleDateFormat("yyyy년 MM월 dd일")
                        val dates = dateformatter.format(Date(it))
                        myDate = dates

                        //보내는 DateFormat
                        val sendDateFormats = SimpleDateFormat("yyyy-MM-dd")
                        val dateOne = sendDateFormats.format(Date(it))
                        date = dateOne

                        textViewTodoAddDate.setText(dates)

                        viewModel.date.value = dates
                    }

                    materialDatePicker.show(todoAddActivity.supportFragmentManager,"Date")

                }
            }
            //시간
            linearlayoutTodoAddAlert.run {
                setOnClickListener {
                    var today = Calendar.getInstance()
                    var currentHour = today.get(Calendar.HOUR_OF_DAY)
                    var currentMinute = today.get(Calendar.MINUTE)
                    var materialTimePicker = MaterialTimePicker.Builder()
                    materialTimePicker
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setTitleText("Select Time")
                        .setHour(currentHour)
                        .setMinute(currentMinute)
                        .setInputMode(INPUT_MODE_KEYBOARD)
                        .build().apply {
                            addOnPositiveButtonClickListener {

                                //시간
                                time = "${hour}:${minute}"

                                //시 숫자가 10보다 작을떄
                                if("${hour}".toInt()<10){
                                    time = "0${hour}:${minute}"
                                }

                                //분 숫자가 10보다 작을떄
                                if("${minute}".toInt()<10){
                                    time = "${hour}:0${minute}"
                                }

                                //시,분 숫자가 10보다 작을 떄
                                if("${hour}".toInt()<10 && "${minute}".toInt()<10){
                                    time = "0${hour}:0${minute}"
                                }

                                var newhour = "${hour}".toInt()

                                if(newhour in 1..11){
                                    textViewTodoAddAlert.text= " 오전 ${hour}시 ${minute}분"
                                    viewModel.time.value = textViewTodoAddAlert.text.toString()
                                }

                                if(newhour in 13..23){
                                    var hours = "${hour}".toInt()-12
                                    textViewTodoAddAlert.text= " 오후 ${hours}시 ${minute}분"
                                    viewModel.time.value = textViewTodoAddAlert.text.toString()

                                }

                                if(newhour == 12){
                                    textViewTodoAddAlert.text= " 오후 ${hour}시 ${minute}분"
                                    viewModel.time.value = textViewTodoAddAlert.text.toString()
                                }

                                if(newhour == 0){
                                    var hours = newhour+12
                                    textViewTodoAddAlert.text= " 오전 ${hours}시 ${minute}분"
                                    viewModel.time.value = textViewTodoAddAlert.text.toString()
                                }
                            }
                        }
                        .show(todoAddActivity.supportFragmentManager,"Time")

                }
            }
            //위치
            linearlayoutTodoAddLocation.run {

                setOnClickListener {
                    //구글맵 키 받아옴
                    val key = com.test.dontforget.BuildConfig.googlemapkey

                    // plac api 초기화
                    Places.initialize(context,key)
                    val placesClient = Places.createClient(todoAddActivity)

                    val field = listOf(
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.TYPES,
                        Place.Field.ADDRESS)
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,field)
                        .setHint("주소를 입력해주세요")
                        .build(todoAddActivity)
                    startAutocomplete.launch(intent)
                }

            }

            buttonTodoAddComplete.run {

                setOnClickListener {
                    //유효성 검사
                    if(editTextTodoAdd.text!!.isEmpty()){
                        var dialogNormalBinding = DialogNormalBinding.inflate(layoutInflater)
                        val builder = MaterialAlertDialogBuilder(todoAddActivity)

                        dialogNormalBinding.textViewDialogNormalTitle.text = "경고"
                        dialogNormalBinding.textViewDialogNormalContent.text = "할일을 입력해주세요."

                        builder.setView(dialogNormalBinding.root)
                        builder.setNegativeButton("취소"){ dialogInterface: DialogInterface, i: Int ->

                        }
                        builder.setPositiveButton("확인"){dialogInterface: DialogInterface, i: Int ->
                            todoAddActivity.showSoftInput(editTextTodoAdd)
                        }
                        builder.show()
                        return@setOnClickListener
                    }
                    if(textViewTodoAddCategory.text.toString() == "카테고리 없음"){
                        var dialogNormalBinding = DialogNormalBinding.inflate(layoutInflater)
                        val builder = MaterialAlertDialogBuilder(todoAddActivity)

                        dialogNormalBinding.textViewDialogNormalTitle.text = "경고"
                        dialogNormalBinding.textViewDialogNormalContent.text = "카데고리를 선택해주세요."

                        builder.setView(dialogNormalBinding.root)
                        builder.setNegativeButton("취소"){ dialogInterface: DialogInterface, i: Int ->

                        }
                        builder.setPositiveButton("확인"){dialogInterface: DialogInterface, i: Int ->

                        }
                        builder.show()
                        return@setOnClickListener
                    }
                    if(textViewTodoAddDate.text.toString() == "날짜 없음"){
                        var dialogNormalBinding = DialogNormalBinding.inflate(layoutInflater)
                        val builder = MaterialAlertDialogBuilder(todoAddActivity)

                        dialogNormalBinding.textViewDialogNormalTitle.text = "경고"
                        dialogNormalBinding.textViewDialogNormalContent.text = "날짜를 선택해주세요."

                        builder.setView(dialogNormalBinding.root)
                        builder.setNegativeButton("취소"){ dialogInterface: DialogInterface, i: Int ->

                        }
                        builder.setPositiveButton("확인"){dialogInterface: DialogInterface, i: Int ->

                        }
                        builder.show()
                        return@setOnClickListener
                    }


                    var newDate= date
                    var newTime = time

                    TodoRepository.getTodoIdx {
                        var idx = it.result.value as Long
                        idx++

                        var content = editTextTodoAdd.text.toString()
                        var useridx = MyApplication.loginedUserInfo.userIdx
                        var name = MyApplication.categoryname
                        var backgroundColor = MyApplication.categoryColor
                        var fontColor = MyApplication.categoryFontColor
                        var dates = newDate
                        var memo = editTextTodoAddMemo.text.toString()

                        //알림, 장소 이름,위도,경도 없을시
                        var time = newTime
                        if(time==""){
                            time = "알림 없음"
                        }

                        var locationName = ""
                        var locationLongtitude = ""
                        var locationLatitude = ""

                        if(textViewTodoAddLocation.text == "위치 없음"){
                            locationName = "위치 없음"
                            locationLongtitude = "None"
                            locationLatitude = "None"
                        }else{
                            locationName = MyApplication.locationName
                            locationLongtitude = MyApplication.locationLongitude
                            locationLatitude = MyApplication.locationLatitude
                        }

                        if(locationLatitude !="None" && locationLongtitude != "None"){
                            val location = Location("my_provider")
                            location.latitude = MyApplication.locationLatitude.toDouble() // 위도
                            location.longitude = MyApplication.locationLongitude.toDouble() // 경도

                        }

                        if(time == "알림 없음") {

                        } else {
                            val now = Calendar.getInstance()
                            var alarmTime = "${dates} $time:00" // 알람이 울리는 시간
                            var alarmDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(alarmTime)
                            var calculateDate = (alarmDate.time - now.time.time)

                            //                val random = (1..100000) // 1~100000 범위에서 알람코드 랜덤으로 생성
                            var alarmCode = idx.toInt()
                            //                deleteAlarm(alarmCode)
                            if (calculateDate < 0) {
                            } else {
                                setAlarm(alarmCode, content, alarmTime)
                            }
                        }

                        CategoryRepository.getAllCategory {
                            for (c1 in it.result.children){
                                val categoryJoinUserIdxList =
                                    c1.child("categoryJoinUserIdxList").value as ArrayList<Long>?
                                var names = c1.child("categoryName").value.toString()
                                if(useridx !in categoryJoinUserIdxList!!){
                                    continue
                                }
                                if(names == name) {
                                    var catgoryIdx = c1.child("categoryIdx").value as Long
                                    var owneridx = MyApplication.loginedUserInfo.userIdx
                                    var ownerName = MyApplication.loginedUserInfo.userName
                                    var categoryIsPublic = c1.child("categoryIsPublic").value as Long
                                    var newPublicdata = categoryIsPublic.toInt()

                                    //개인 카테고리 추가시
                                    if(newPublicdata == 0){
                                        var newclass = TodoClass(idx, content, 0, catgoryIdx, name, fontColor.toLong(), backgroundColor.toLong(), dates,
                                            time, locationName, locationLatitude, locationLongtitude, memo, owneridx, ownerName)
                                        TodoRepository.setTodoAddInfo(newclass){
                                            TodoRepository.setTodoIdx(idx){
                                                Toast.makeText(todoAddActivity, "저장되었습니다", Toast.LENGTH_SHORT).show()

                                                val mainIntent = Intent(todoAddActivity, MainActivity::class.java)
                                                startActivity(mainIntent)
                                                todoAddActivity.finish()
                                                viewModel.resetList()
                                            }
                                        }
                                    }

                                    //공용 카데고리 추가시
                                    if(newPublicdata == 1){
                                        var newclass = TodoClass(idx, content, 0, catgoryIdx, name, fontColor.toLong(), backgroundColor.toLong(), dates,
                                            time, locationName, locationLatitude, locationLongtitude, memo, owneridx, ownerName)
                                        TodoRepository.setTodoAddInfo(newclass) {
                                            TodoRepository.setTodoIdx(idx) {
                                                var alertName = "${names}에 ${myDate} "
                                                var text = "새 할일이 추가되었습니다"
                                                AlertRepository.getAlertIdx {
                                                    var idx = it.result.value as Long
                                                    idx++
                                                    var newclass2 = AlertClass(idx, text, useridx, 2,alertName)
                                                    AlertRepository.addAlertInfo(newclass2) {
                                                        AlertRepository.setAlertIdx(idx) {
                                                            Toast.makeText(todoAddActivity, "저장되었습니다", Toast.LENGTH_SHORT).show()

                                                            val mainIntent = Intent(todoAddActivity, MainActivity::class.java)
                                                            startActivity(mainIntent)
                                                            todoAddActivity.finish()
                                                            viewModel.resetList()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return  todoAddBinding.root
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetList()
    }

    private fun setAlarm(alarmCode : Int, content : String, time : String){
        alarmFunctions = AlarmFunctions(todoAddActivity)
        alarmFunctions.callAlarm(time, alarmCode, content)
    }
}
