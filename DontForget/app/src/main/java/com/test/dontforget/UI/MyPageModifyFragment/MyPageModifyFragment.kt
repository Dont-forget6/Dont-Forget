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
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.test.dontforget.DAO.Friend
import com.test.dontforget.DAO.UserClass
import com.test.dontforget.MainActivity
import com.test.dontforget.MyApplication
import com.test.dontforget.R
import com.test.dontforget.Repository.UserRepository
import com.test.dontforget.UI.MainMyPageFragment.MyPageModifyViewModel
import com.test.dontforget.databinding.DialogMypageProfileBinding
import com.test.dontforget.databinding.FragmentMyPageModifyBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

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

            myPageModifyViewModel = ViewModelProvider(mainActivity)[MyPageModifyViewModel::class.java]
            myPageModifyViewModel.run {
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

                val newImage = when(uploadUri){
                    null -> user.userImage
                    "None".toUri() -> "None"
                    else -> "image/img_${System.currentTimeMillis()}.jpg"
                }

                // 이미지가 변경되지 않으면 업로드하지 않고 이전 이미지를 사용
                if (newImage != user.userImage) {
                    UserRepository.setUploadProfile(newImage, uploadUri!!) {}
                }

                if (newIntroduce.isNotEmpty()) {
                    UserRepository.getUserInfoByIdx(MyApplication.loginedUserInfo.userIdx) {
                        for (c1 in it.result.children) {
                            var userIdx = c1.child("userIdx").value as Long
                            var userName = c1.child("userName").value as String
                            var userEmail = c1.child("userEmail").value as String
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

                            var friendUserClass = UserClass(
                                userIdx,
                                userName,
                                userEmail,
                                newImage,
                                newIntroduce,
                                userId,
                                userFriendList as ArrayList<Friend>
                            )

                            // 수정된 친구정보 반영
                            UserRepository.modifyUserInfo(friendUserClass) {}
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

}