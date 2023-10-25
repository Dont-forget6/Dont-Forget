package com.test.dontforget.UI.MyPageModifyFragment

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.test.dontforget.DAO.AlertClass
import com.test.dontforget.DAO.CategoryClass
import com.test.dontforget.DAO.Friend
import com.test.dontforget.DAO.JoinFriend
import com.test.dontforget.DAO.TodoClass
import com.test.dontforget.DAO.UserClass
import com.test.dontforget.MainActivity
import com.test.dontforget.MyApplication
import com.test.dontforget.R
import com.test.dontforget.Repository.AlertRepository
import com.test.dontforget.Repository.CategoryRepository
import com.test.dontforget.Repository.JoinFriendRepository
import com.test.dontforget.Repository.TodoRepository
import com.test.dontforget.Repository.UserRepository
import com.test.dontforget.UI.MainMyPageFragment.MyPageModifyViewModel
import com.test.dontforget.databinding.DialogMypageProfileBinding
import com.test.dontforget.databinding.FragmentMyPageModifyBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyPageModifyFragment : Fragment() {
    lateinit var fragmentMyPageModifyBinding: FragmentMyPageModifyBinding
    lateinit var mainActivity: MainActivity
    lateinit var albumLauncher: ActivityResultLauncher<Intent>
    lateinit var myPageModifyViewModel : MyPageModifyViewModel
    // 업로드할 이미지의 Uri
    var uploadUri: Uri? = null
    lateinit var user : UserClass
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMyPageModifyBinding = FragmentMyPageModifyBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        // 앨범 설정
        albumLauncher = albumSetting(fragmentMyPageModifyBinding.imageViewMyPageModifyProfile)
        fragmentMyPageModifyBinding.run {
            user = MyApplication.loginedUserInfo
            toolbarMyPageModify.run {
                title = getString(R.string.myPage_modify)
                setNavigationIcon(R.drawable.ic_arrow_back_24px)
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.MY_PAGE_MODIFY_FRAGMENT)
                }
            }
            loadSampleData()
            myPageModifyViewModel = ViewModelProvider(mainActivity)[MyPageModifyViewModel::class.java]
            myPageModifyViewModel.run {
                userName.observe(mainActivity){
                    user.userName = it
                    fragmentMyPageModifyBinding.textInputEditTextMyPageModifyName.setText(it.toString())
                }
                userIntoduce.observe(mainActivity){
                    user.userIntroduce = it
                    fragmentMyPageModifyBinding.textInputEditTextMyPageModifyIntroduce.setText(it.toString())
                }
                userImage.observe(mainActivity){
                    UserRepository.getProfile(it) {
                        if (it.isSuccessful) {
                            val fileUri = it.result
                            Glide.with(mainActivity).load(fileUri).into(fragmentMyPageModifyBinding.imageViewMyPageModifyProfile)
                        } else {
                            fragmentMyPageModifyBinding.imageViewMyPageModifyProfile.setImageResource(R.drawable.ic_person_24px)
                        }
                    }
                }
            }
            myPageModifyViewModel.getUserInfo(MyApplication.loginedUserInfo)
            // 사진 변경 클릭
            buttonMyPageModifyModifyPhoto.setOnClickListener {
                val dialogMypageProfileBinding = DialogMypageProfileBinding.inflate(layoutInflater)
                val builder = MaterialAlertDialogBuilder(mainActivity)
                val dialog = builder.setView(dialogMypageProfileBinding.root).create()
                dialogMypageProfileBinding.buttonMyPageDefault.setOnClickListener {
                    uploadUri = "None".toUri()
                    fragmentMyPageModifyBinding.imageViewMyPageModifyProfile.setImageResource(R.drawable.ic_person_24px)
                    dialog.dismiss()
                }
                dialogMypageProfileBinding.buttonMyPageAlbum.setOnClickListener {
                    // 앨범에서 사진을 선택할 수 있는 Activity를 실행한다.
                    val newIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    newIntent.setType("image/*")
                    val mimeType = arrayOf("image/*")
                    newIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
                    albumLauncher.launch(newIntent)
                    dialog.dismiss()
                }
                dialog.show()
            }
            buttonMyPageModifyModifyComplete.setOnClickListener {
                val newIntroduce =
                    fragmentMyPageModifyBinding.textInputLayoutMyPageModifyIntroduce.editText?.text.toString()
                val newName = fragmentMyPageModifyBinding.textInputLayoutMyPageModifyName.editText?.text.toString()
                val newImage = when(uploadUri){
                    null -> user.userImage
                    "None".toUri() -> "None"
                    else -> "image/img_${System.currentTimeMillis()}.jpg"
                }
                Log.e("으악",newImage)
                // 이미지가 변경되지 않으면 업로드하지 않고 이전 이미지를 사용
                if (newImage != user.userImage) {
                    UserRepository.setUploadProfile(newImage, uploadUri!!) {}
                }

                if ( newName.isNotEmpty()||newIntroduce.isNotEmpty()) {
                    UserRepository.getUserInfoByIdx(MyApplication.loginedUserInfo.userIdx) {
                        for (c1 in it.result.children) {
                            var userIdx = c1.child("userIdx").value as Long
                            var userEmail = c1.child("userEmail").value as String
                            var userId = c1.child("userId").value as String
                            var userFriendList = mutableListOf<Friend>()
                            var userName = c1.child("userName").value as String
                            var userFriendListHashMap =
                                c1.child("userFriendList").value as ArrayList<HashMap<String, Any>>

                            for (i in userFriendListHashMap) {
                                var idx = i["friendIdx"] as Long
                                var name = i["friendName"] as String
                                var email = i["friendEmail"] as String
                                var friend = Friend(idx, name, email)
                                userFriendList.add(friend)
                            }

                            var modifyUserClass = UserClass(
                                userIdx,
                                newName,
                                userEmail,
                                newImage,
                                newIntroduce,
                                userId,
                                userFriendList as ArrayList<Friend>
                            )

                            // 수정된 친구정보 반영
                            CoroutineScope(Dispatchers.Main).launch {
                                modifyName(userName,newName)
                            }
                            UserRepository.modifyUserInfo(modifyUserClass) {}
                            MyApplication.loginedUserInfo = modifyUserClass
                        }

                        GlobalScope.launch {
                            delay(1050)
                            Snackbar.make(
                                fragmentMyPageModifyBinding.root,
                                "수정되었습니다.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            mainActivity.removeFragment(MainActivity.MY_PAGE_MODIFY_FRAGMENT)
                        }
                    }
                } else {
                    Snackbar.make(
                        fragmentMyPageModifyBinding.root,
                        "빈 칸을 채워주세요.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }


        }

        return fragmentMyPageModifyBinding.root
    }

    private fun loadSampleData(){
        lifecycleScope.launch {
            showSampleData(isLoading = true)
            delay(1500)
            showSampleData(isLoading = false)
        }
    }
    private fun showSampleData(isLoading:Boolean){
        if(isLoading){
            fragmentMyPageModifyBinding.shimmerLayoutMainMyPageModify.startShimmer()
            fragmentMyPageModifyBinding.shimmerLayoutMainMyPageModify.visibility = View.VISIBLE
            fragmentMyPageModifyBinding.imageViewMyPageModifyProfile.visibility = View.GONE
        }else{
            fragmentMyPageModifyBinding.shimmerLayoutMainMyPageModify.stopShimmer()
            fragmentMyPageModifyBinding.shimmerLayoutMainMyPageModify.visibility = View.GONE
            fragmentMyPageModifyBinding.imageViewMyPageModifyProfile.visibility = View.VISIBLE

        }
    }

    // 앨범 설정
    fun albumSetting(previewImageView: ImageView): ActivityResultLauncher<Intent> {
        val albumContract = ActivityResultContracts.StartActivityForResult()
        val albumLauncher = registerForActivityResult(albumContract) {
            if (it?.resultCode == AppCompatActivity.RESULT_OK) {
                // 선택한 이미지에 접근할 수 있는 Uri 객체를 추출한다.
                if (it.data?.data != null) {
                    uploadUri = it.data?.data

                    // 안드로이드 10 (Q) 이상이라면...
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // 이미지를 생성할 수 있는 디코더를 생성한다.
                        val source = ImageDecoder.createSource(mainActivity.contentResolver, uploadUri!!)
                        // Bitmap객체를 생성한다.
                        val bitmap = ImageDecoder.decodeBitmap(source)

                        previewImageView.setImageBitmap(bitmap)
                    } else {
                        // 컨텐츠 프로바이더를 통해 이미지 데이터 정보를 가져온다.
                        val cursor = mainActivity.contentResolver.query(uploadUri!!, null, null, null, null)
                        if (cursor != null) {
                            cursor.moveToNext()

                            // 이미지의 경로를 가져온다.
                            val idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            val source = cursor.getString(idx)

                            // 이미지를 생성하여 보여준다.
                            val bitmap = BitmapFactory.decodeFile(source)
                            previewImageView.setImageBitmap(bitmap)
                        }
                    }
                }
            }
        }

        return albumLauncher
    }

    suspend fun modifyName(beforeName:String,afterName:String){
        delay(1000L)
        lateinit var modifyCategory:CategoryClass
        // Category Owner 변경

        CategoryRepository.getAllCategory {
            for (c1 in it.result.children) {
                val categoryIdx = c1.child("categoryIdx").value as Long
                val categoryName = c1.child("categoryName").value as String
                val categoryColor = c1.child("categoryColor").value as Long
                val categoryFontColor = c1.child("categoryFontColor").value as Long
                val categoryJoinUserIdxList =
                    c1.child("categoryJoinUserIdxList").value as ArrayList<Long>?
                val categoryJoinUserNameList =
                    c1.child("categoryJoinUserNameList").value as ArrayList<String>?
                val categoryIsPublic = c1.child("categoryIsPublic").value as Long
                val categoryOwnerIdx = c1.child("categoryOwnerIdx").value as Long
                val categoryOwnerName = c1.child("categoryOwnerName").value as String
                val newCategoryJoinUserNameList = ArrayList<String>()
                // 카테고리 namelist안에 이름 같은게 잇을경우 변경
                if (categoryJoinUserNameList != null) {
                    for( (index,value) in categoryJoinUserNameList.withIndex()){
                        if(value == beforeName){
                           newCategoryJoinUserNameList.add(index,afterName)
                        }else{
                           newCategoryJoinUserNameList.add(index,value)
                        }
                    }
                }
                if(categoryOwnerName == beforeName) {
                    modifyCategory = CategoryClass(
                        categoryIdx,
                        categoryName,
                        categoryColor,
                        categoryFontColor,
                        categoryJoinUserIdxList,
                        newCategoryJoinUserNameList,
                        categoryIsPublic,
                        categoryOwnerIdx,
                        afterName
                    )
                }else{
                    modifyCategory = CategoryClass(
                        categoryIdx,
                        categoryName,
                        categoryColor,
                        categoryFontColor,
                        categoryJoinUserIdxList,
                        newCategoryJoinUserNameList,
                        categoryIsPublic,
                        categoryOwnerIdx,
                        categoryOwnerName
                    )
                }
                CategoryRepository.modifyCategory(modifyCategory){}
            }

        }
        // 할일 Owner 변경
        TodoRepository.getAllTodo {
            for(c1 in it.result.children){
                var todoIdx = c1.child("todoIdx").value as Long
                var todoContent = c1.child("todoContent").value as String
                var todoIsChecked = c1.child("todoIsChecked").value as Long
                var todoCategoryIdx = c1.child("todoCategoryIdx").value as Long
                var todoCategoryName = c1.child("todoCategoryName").value as String
                var todoFontColor = c1.child("todoFontColor").value as Long
                var todoBackgroundColor = c1.child("todoBackgroundColor").value as Long
                var todoDate = c1.child("todoDate").value as String
                var todoAlertTime = c1.child("todoAlertTime").value as String
                var todoLocationName = c1.child("todoLocationName").value as String
                var todoLocationLatitude = c1.child("todoLocationLatitude").value as String
                var todoLocationLongitude = c1.child("todoLocationLongitude").value as String
                var todoMemo = c1.child("todoMemo").value as String
                var todoOwnerIdx = c1.child("todoOwnerIdx").value as Long
                var todoOwnerName = c1.child("todoOwnerName").value as String
                if(todoOwnerName == beforeName){
                    val todo = TodoClass(
                        todoIdx,
                        todoContent,
                        todoIsChecked,
                        todoCategoryIdx,
                        todoCategoryName,
                        todoFontColor,
                        todoBackgroundColor,
                        todoDate,
                        todoAlertTime,
                        todoLocationName,
                        todoLocationLatitude,
                        todoLocationLongitude,
                        todoMemo,
                        todoOwnerIdx,
                        afterName
                    )
                    Log.e("테스트",todo.toString())
                    TodoRepository.modifyTodo(todo){}
                }
            }
        }

        // joinFriendInfo joinFriendSenderName변경
        // joinFriendInfo
        JoinFriendRepository.getAllJoinFriend {
            for(c1 in it.result.children){
                var joinFriendIdx = c1.child("joinFriendIdx").value as Long
                var joinFriendReceiverEmail = c1.child("joinFriendReceiverEmail").value as String
                var joinFriendSenderIdx = c1.child("joinFriendSenderIdx").value as Long
                var joinFriendSenderName = c1.child("joinFriendSenderName").value as String
                if(joinFriendSenderName == beforeName){
                    val joinFriend = JoinFriend(
                        joinFriendIdx,
                        joinFriendSenderIdx,
                        afterName,
                        joinFriendReceiverEmail
                    )
                    JoinFriendRepository.modifyJoinFriend(joinFriend)
                }
            }
        }

        // 유저 안에 friendList 변경
        var myFriendList = MyApplication.loginedUserInfo.userFriendList

        // 사용자의 친구들의 계정을 모두 불러와서 안에들어있는 내정보 삭제
        for(friend in myFriendList){
            JoinFriendRepository.getUserInfoByIdx(friend.friendIdx){
                for (c1 in it.result.children) {
                    var userIdx = c1.child("userIdx").value as Long
                    var userName = c1.child("userName").value as String
                    var userEmail = c1.child("userEmail").value as String
                    var userImage = c1.child("userImage").value as String
                    var userIntroduce = c1.child("userIntroduce").value as String
                    var userId = c1.child("userId").value as String
                    var userFriendList = mutableListOf<Friend>()

                    var userFriendListHashMap =
                        c1.child("userFriendList").value as ArrayList<HashMap<String, Any>>

                    for (i in userFriendListHashMap) {
                        var idx = i["friendIdx"] as Long
                        var name = i["friendName"] as String
                        var email = i["friendEmail"] as String

                        var friend = Friend(idx, name, email)
                        userFriendList.add(friend)
                    }
                    for( v in userFriendList){
                        if(v.friendName == beforeName){
                            v.friendName = afterName
                        }
                    }

                    var friendUserClass = UserClass(
                        userIdx,
                        userName,
                        userEmail,
                        userImage,
                        userIntroduce,
                        userId,
                        userFriendList as ArrayList<Friend>
                    )

                    // 수정된 친구정보 반영
                    UserRepository.modifyUserInfo(friendUserClass){}
                }
            }
        }
        AlertRepository.getAlertAll {
            for(c1 in it.result.children){
                val alertIdx = c1.child("alertIdx").value as Long
                val alertContent = c1.child("alertContent").value as String
                var alertName = c1.child("alertName").value as String
                val alertReceiverIdx = c1.child("alertReceiverIdx").value as Long
                val alertType = c1.child("alertType").value as Long
                if( alertName == beforeName){
                    alertName = afterName
                }
                val alertInfo = AlertClass(alertIdx,alertContent, alertReceiverIdx, alertType, alertName)
                AlertRepository.modifyAlertUserName(alertInfo){}
            }
        }
    }

}