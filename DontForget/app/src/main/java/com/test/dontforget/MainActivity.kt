package com.test.dontforget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.transition.MaterialSharedAxis
import com.test.dontforget.DAO.Friend
import com.test.dontforget.DAO.UserClass
import com.test.dontforget.Repository.CategoryRepository
import com.test.dontforget.Repository.UserRepository
import com.test.dontforget.UI.CategoryAddPersonalFragment.CategoryAddPersonalFragment
import com.test.dontforget.UI.CategoryAddPublicFragment.CategoryAddPublicFragment
import com.test.dontforget.UI.CategoryOptionPersonalFragment.CategoryOptionPersonalFragment
import com.test.dontforget.UI.CategoryOptionPublicFragment.CategoryOptionPublicFragment
import com.test.dontforget.UI.CategoryOptionPublicOwnerFragment.CategoryOptionPublicOwnerFragment
import com.test.dontforget.UI.FriendsDetailFragment.FriendsDetailFragment
import com.test.dontforget.UI.JoinFragment.JoinFragment
import com.test.dontforget.UI.LoginFindPwFragment.LoginFindPwFragment
import com.test.dontforget.UI.LoginFragment.LoginFragment
import com.test.dontforget.UI.MainFragment.MainFragment
import com.test.dontforget.UI.MyPageModifyFragment.MyPageModifyFragment
import com.test.dontforget.UI.MyPageThemeFragment.MyPageThemeFragment
import com.test.dontforget.UI.TodoAddFragment.TodoAddFragment
import com.test.dontforget.UI.TodoDetailPersonalFragment.TodoDetailPersonalFragment
import com.test.dontforget.UI.TodoDetailPublicFragment.TodoDetailPublicFragment
import com.test.dontforget.UI.TodoDetailPublicOwnerFragment.TodoDetailPublicOwnerFragment
import com.test.dontforget.UI.MyPageWithDrawFragment.MyPageWithDrawFragment
import com.test.dontforget.Util.ThemeUtil
import com.test.dontforget.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    lateinit var activityMainBinding: ActivityMainBinding
    var newFragment:Fragment? = null
    var oldFragment:Fragment? = null

    // 확인할 권한 목록
    val permissionList = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    internal lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 123

    var nowLatitude = 0.0
    var nowLongitude = 0.0
    

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val themeName = sharedPreferences.getString("theme", ThemeUtil.LIGHT_MODE)
        if (themeName != null) {
            ThemeUtil.applyTheme(themeName)
            MyApplication.selectedTheme = themeName
        }
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        requestPermissions(permissionList,0)

        // 테마 설정 적용

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val loginedUser = sharedPreferences.getString("isLoggedUser",null)
        if (isLoggedIn) {
            if (loginedUser != null) {
                UserRepository.getUserInfoById(loginedUser){
                    // 가져온 데이터가 없을때
                    if (it.isSuccessful) {
                        if(it.result.exists()){

                            for(c1 in it.result.children){
                                var newFriendList = mutableListOf<Friend>()
                                var newHashMap = c1.child("userFriendList").value as ArrayList<HashMap<String,Any>>
                                for( i in newHashMap){
                                    var idx = i["friendIdx"] as Long
                                    var name = i["friendName"] as String
                                    var email = i["friendEmail"] as String

                                    var friend = Friend(idx, name, email)
                                    newFriendList.add(friend)
                                }
                                var userInfo = UserClass(
                                    c1.child("userIdx").value as Long,
                                    c1.child("userName").value as String,
                                    c1.child("userEmail").value as String,
                                    c1.child("userImage").value as String,
                                    c1.child("userIntroduce").value as String,
                                    c1.child("userId").value as String,
                                    newFriendList as ArrayList<Friend>
                                )
                                MyApplication.loginedUserInfo = userInfo

                                replaceFragment(MAIN_FRAGMENT,false,null)

                                // 위젯으로 실행시 글쓰기로 이동
//                                if(MyApplication.isStartedWithWidget == true){
////                                    replaceFragment(MAIN_FRAGMENT,false,null)
//                                    MyApplication.isStartedWithWidget = false
//                                    replaceFragment(TODO_ADD_FRAGMENT,true,null)
//                                }else{
//                                    MyApplication.isStartedWithWidget = false
//                                    replaceFragment(MAIN_FRAGMENT,false,null)
//                                }
                            }
                        }
                    }
                }
            }
        } else {
            replaceFragment(LOGIN_FRAGMENT,true,null)
        }

    }
    companion object{
        // 화면 분기
        val CATEGORY_ADD_PERSONAL_FRAGMENT = "CategoryAddPersonalFragment"
        val CATEGORY_ADD_PUBLIC_FRAGMENT = "CategoryAddPublicFragment"
        val CATEGORY_OPTION_PERSONAL_FRAGMENT = "CategoryOptionPersonalFragment"
        val CATEGORY_OPTION_PUBLIC_FRAGMENT = "CategoryOptionPublicFragment"
        val CATEGORY_OPTION_PUBLIC_OWNER_FRAGMENT = "CategoryOptionPublicOwnerFragment"
        val FRIENDS_DETAIL_FRAGMENT = "FriendsDetailFragment"
        val JOIN_FRAGMENT = "JoinFragment"
        val LOGIN_FIND_PW_FRAGMENT = "LoginFindPwFragment"
        val LOGIN_FRAGMENT = "LoginFragment"
        val MAIN_FRAGMENT = "MainFragment"
        val MY_PAGE_MODIFY_FRAGMENT = "MyPageModifyFragment"
        val MY_PAGE_THEME_FRAGMENT = "MyPageThemeFragment"
        val MY_PAGE_WITH_DRAW_FRAGMENT = "MyPageWithDrawFragment"
        val TODO_ADD_FRAGMENT = "TodoAddFragment"
        val TODO_DETAIL_PERSONAL_FRAGMENT = "TodoDetailPersonalFragment"
        val TODO_DETAIL_PUBLIC_FRAGMENT = "TodoDetailPublicFragment"
        val TODO_DETAIL_PUBLIC_OWNER_FRAGMENT = "TodoDetailPublicOwnerFragment"
    }
    fun replaceFragment(name:String, addToBackStack:Boolean, bundle:Bundle?){

        SystemClock.sleep(200)

        // Fragment 교체 상태로 설정한다.
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // newFragment 에 Fragment가 들어있으면 oldFragment에 넣어준다.
        if(newFragment != null){
            oldFragment = newFragment
        }

        // 새로운 Fragment를 담을 변수
        newFragment = when(name){
            CATEGORY_ADD_PERSONAL_FRAGMENT -> CategoryAddPersonalFragment()
            CATEGORY_ADD_PUBLIC_FRAGMENT -> CategoryAddPublicFragment()
            CATEGORY_OPTION_PERSONAL_FRAGMENT -> CategoryOptionPersonalFragment()
            CATEGORY_OPTION_PUBLIC_FRAGMENT -> CategoryOptionPublicFragment()
            CATEGORY_OPTION_PUBLIC_OWNER_FRAGMENT -> CategoryOptionPublicOwnerFragment()
            FRIENDS_DETAIL_FRAGMENT -> FriendsDetailFragment()
            JOIN_FRAGMENT -> JoinFragment()
            LOGIN_FIND_PW_FRAGMENT -> LoginFindPwFragment()
            LOGIN_FRAGMENT -> LoginFragment()
            MAIN_FRAGMENT -> MainFragment()
            MY_PAGE_MODIFY_FRAGMENT -> MyPageModifyFragment()
            MY_PAGE_THEME_FRAGMENT -> MyPageThemeFragment()
            MY_PAGE_WITH_DRAW_FRAGMENT -> MyPageWithDrawFragment()
            TODO_ADD_FRAGMENT -> TodoAddFragment()
            TODO_DETAIL_PERSONAL_FRAGMENT -> TodoDetailPersonalFragment()
            TODO_DETAIL_PUBLIC_FRAGMENT -> TodoDetailPublicFragment()
            TODO_DETAIL_PUBLIC_OWNER_FRAGMENT -> TodoDetailPublicOwnerFragment()
            else -> Fragment()
        }

        newFragment?.arguments = bundle

        if(newFragment != null) {

            // 애니메이션 설정

            if(oldFragment != null){
                oldFragment?.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                oldFragment?.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
                oldFragment?.enterTransition = null
                oldFragment?.returnTransition = null
            }

            newFragment?.exitTransition = null
            newFragment?.reenterTransition = null
            newFragment?.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
            newFragment?.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

            // Fragment를 교채한다.
            fragmentTransaction.replace(R.id.container_main, newFragment!!)

            if (addToBackStack == true) {
                // Fragment를 Backstack에 넣어 이전으로 돌아가는 기능이 동작할 수 있도록 한다.
                fragmentTransaction.addToBackStack(name)
            }

            // 교체 명령이 동작하도록 한다.
            fragmentTransaction.commit()
        }
    }

    // Fragment를 BackStack에서 제거한다.
    fun removeFragment(name:String){
        supportFragmentManager.popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    // 입력 요소에 포커스를 주는 메서드
    fun showSoftInput(view: View){
        view.requestFocus()

        val inputMethodManger = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        thread {
            SystemClock.sleep(200)
            inputMethodManger.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
    // 키보드 내리는 메서드
    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    // 친구목록 최신화
    fun updateLoginedUserInfo() {
        CategoryRepository.getUserInfoByIdx(MyApplication.loginedUserInfo.userIdx) {
            var newFriendList = ArrayList<Friend>()
            for (u1 in it.result.children) {
                var friendListHashMap = u1.child("userFriendList").value as ArrayList<HashMap<String, Any>>

                for (f in friendListHashMap) {
                    var idx = f["friendIdx"] as Long
                    var name = f["friendName"] as String
                    var email = f["friendEmail"] as String

                    val friend = Friend(idx, name, email)
                    newFriendList.add(friend)
                }
            }
            MyApplication.loginedUserInfo.userFriendList = newFriendList
        }
    }

    fun startLocationUpdates() {

        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    // 시스템으로 부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation!!)
        }
    }

    // 시스템으로 부터 받은 위치정보를 화면에 갱신해주는 메소드
    fun onLocationChanged(location: Location) {
        mLastLocation = location
        nowLatitude = mLastLocation.latitude // 갱신 된 위도
        nowLongitude = mLastLocation.longitude // 갱신 된 경도

        Log.d("techit", "$nowLatitude, $nowLongitude")
    }


    // 위치 권한이 있는지 확인하는 메서드
    private fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 위치 권한에 추가 런타임 권한이 필요
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }

    // 사용자에게 권한 요청 후 결과에 대한 처리 로직
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Log.d("techit", "onRequestPermissionsResult() _ 권한 허용 거부")
                Toast.makeText(this, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}