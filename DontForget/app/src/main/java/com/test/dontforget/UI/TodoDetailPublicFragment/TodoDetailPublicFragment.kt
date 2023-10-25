package com.test.dontforget.UI.TodoDetailPublicFragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.test.dontforget.MainActivity
import com.test.dontforget.R
import com.test.dontforget.UI.TodoDetailPersonalFragment.TodoDetailViewModel
import com.test.dontforget.databinding.DialogNormalBinding
import com.test.dontforget.databinding.FragmentTodoDetailPublicBinding

class TodoDetailPublicFragment : Fragment() {

    lateinit var fragmentTodoDetailPublicBinding: FragmentTodoDetailPublicBinding
    lateinit var mainActivity: MainActivity

    lateinit var todoDetailViewModel: TodoDetailViewModel

    var todoIdx = 0L

    var time = ""
    var latitude = ""
    var longitude = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentTodoDetailPublicBinding = FragmentTodoDetailPublicBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        mainActivity.mLocationRequest =  LocationRequest.create().apply {

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        }

        todoIdx = arguments?.getLong("todoIdx",0)!!

        todoDetailViewModel = ViewModelProvider(mainActivity)[TodoDetailViewModel::class.java]
        todoDetailViewModel.run {

            todoContent.observe(mainActivity) {
                fragmentTodoDetailPublicBinding.textInputEditTextTodoDetailPublic.setText(it.toString())
            }
            todoCategoryName.observe(mainActivity) {
                fragmentTodoDetailPublicBinding.buttonTodoDetailPublicCategory.text = it.toString()
            }
            todoDate.observe(mainActivity) {
                fragmentTodoDetailPublicBinding.textViewTodoDetailPublicDate.text = it.toString()
            }
            todoAlertTime.observe(mainActivity) {
                time = it.toString()
                if(time == "알림 없음") {
                    fragmentTodoDetailPublicBinding.textViewTodoDetailPublicAlert.text = "알림 없음"
                } else {
                    var alertTime = it.toString().split(":")
                    var newhour = "${alertTime.get(0)}".toInt()

                    fragmentTodoDetailPublicBinding.run {
                        if(newhour in 1..11){
                            textViewTodoDetailPublicAlert.text= "오전 ${alertTime.get(0)}시 ${alertTime.get(1)}분"
                        }

                        if(newhour in 13..23){
                            var hours = "${alertTime.get(0)}".toInt()-12
                            textViewTodoDetailPublicAlert.text= "오후 ${hours}시 ${alertTime.get(1)}분"
                        }

                        if(newhour == 12){
                            textViewTodoDetailPublicAlert.text= "오후 ${alertTime.get(0)}시 ${alertTime.get(1)}분"
                        }

                        if(newhour == 0){
                            var hours = newhour+12
                            textViewTodoDetailPublicAlert.text= "오전 ${hours}시 ${alertTime.get(1)}분"
                        }
                    }
                }
            }
            todoLocationName.observe(mainActivity) {
                if(it.toString() == "위치 없음") {
                    fragmentTodoDetailPublicBinding.textViewTodoDetailPublicLocation.text = it.toString()
                } else {
                    fragmentTodoDetailPublicBinding.textViewTodoDetailPublicLocation.text =
                        it.toString().split("@").get(0)
                }
            }
            todoLocationLatitude.observe(mainActivity) {
                latitude = it.toString()
            }
            todoLocationLongitude.observe(mainActivity) {
                longitude = it.toString()
            }
            todoOwnerName.observe(mainActivity) {
                fragmentTodoDetailPublicBinding.textViewTodoDetailPublicMadeby.text = it.toString()
            }
            todoFontColor.observe(mainActivity) {
                fragmentTodoDetailPublicBinding.buttonTodoDetailPublicCategory.setTextColor(it.toInt())
            }
            todoBackgroundColor.observe(mainActivity) {
                fragmentTodoDetailPublicBinding.run {
                    buttonTodoDetailPublicCategory.setBackgroundColor(it.toInt())
                    textInputLayoutTodoDetailPublic.boxStrokeColor = it.toInt()
                    textInputLayoutTodoDetailPublic.hintTextColor = ColorStateList.valueOf(it.toInt())
                }
            }
            todoMemo.observe(mainActivity) {
                fragmentTodoDetailPublicBinding.textViewTodoDetailPublicMemo.text = it.toString()
            }
        }
        todoDetailViewModel.getTodoInfo(todoIdx)

        fragmentTodoDetailPublicBinding.run {

            textInputEditTextTodoDetailPublic.isEnabled = false

            toolbarTodoDetailPublic.run{
                title = "할일 상세"

                // back 버튼 설정
                setNavigationIcon(R.drawable.ic_arrow_back_24px)

                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.TODO_DETAIL_PUBLIC_FRAGMENT)
                }
            }

            buttonTodoDetailPublicRoute.setOnClickListener {

                if(textViewTodoDetailPublicLocation.text == "위치 없음") {
                    var dialogNormalBinding = DialogNormalBinding.inflate(layoutInflater)
                    val builder = MaterialAlertDialogBuilder(mainActivity)

                    dialogNormalBinding.textViewDialogNormalTitle.text = "위치 설정"
                    dialogNormalBinding.textViewDialogNormalContent.text = "설정되어 있는 위치가 없습니다. \n위치를 설정해주세요."

                    builder.setView(dialogNormalBinding.root)
                    builder.setPositiveButton("확인",null)
                    builder.show()

                    return@setOnClickListener
                }

                val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION // 또는 ACCESS_COARSE_LOCATION
                val requestCode = 123 // 요청 코드 (임의의 숫자)

                if (ContextCompat.checkSelfPermission(requireContext(), locationPermission) == PackageManager.PERMISSION_GRANTED) {
                    // 이미 위치 권한이 허용되어 있음
                    // 권한이 필요한 기능 수행
                    mainActivity.startLocationUpdates()

                    val handler = Handler()
                    handler.postDelayed({
                        val url2 ="kakaomap://route?sp=${mainActivity.nowLatitude},${mainActivity.nowLongitude}&ep=$latitude,$longitude&by=CAR"
                        Log.d("techit", "url : $url2")
                        var intent =  Intent(Intent.ACTION_VIEW, Uri.parse(url2))
                        intent.addCategory(Intent.CATEGORY_BROWSABLE)
                        var list = mainActivity.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

                        //카카오맵 어플리케이션이 사용자 핸드폰에 깔려있으면 바로 앱으로 연동
                        //그렇지 않다면 다운로드 페이지로 연결

                        if (list== null || list.isEmpty()){
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.daum.android.map")))
                        }else{
                            startActivity(intent)
                        }
                    }, 1000) // 2000ms (2초) 후에 RecyclerView 생성
                } else {
                    // 권한을 요청
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(locationPermission), requestCode)

                    val snackbar: Snackbar = Snackbar.make(fragmentTodoDetailPublicBinding.root, "위치 권한 허용 후 다시 길찾기를 실행해주세요.", Snackbar.LENGTH_INDEFINITE)

                    // 액션 추가
                    snackbar.setAction("확인") {}

                    snackbar.show()
                }
            }
        }
        return fragmentTodoDetailPublicBinding.root
    }
}