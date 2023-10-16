
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.dontforget.MainActivity
import com.test.dontforget.MyApplication
import com.test.dontforget.R
import com.test.dontforget.Repository.UserRepository
import com.test.dontforget.UI.MainMyPageFragment.MainMyPageViewModel
import com.test.dontforget.Util.LoadingDialog
import com.test.dontforget.databinding.DialogNormalBinding
import com.test.dontforget.databinding.FragmentMainMyPageBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainMyPageFragment : Fragment() {
    lateinit var fragmentMainMyPageBinding: FragmentMainMyPageBinding
    lateinit var mainActivity: MainActivity
    lateinit var mainMyPageViewModel: MainMyPageViewModel
    lateinit var firebaseAuth : FirebaseAuth
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMainMyPageBinding = FragmentMainMyPageBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        firebaseAuth = FirebaseAuth.getInstance()
        fragmentMainMyPageBinding.run {

            toolbarMainMyPage.run {
                title = getString(R.string.myPage)
            }
            loadSampleData()
            mainMyPageViewModel = ViewModelProvider(mainActivity)[MainMyPageViewModel::class.java]
            mainMyPageViewModel.run {
                userName.observe(mainActivity) {
                    fragmentMainMyPageBinding.textViewMainMyPageName.setText(it.toString())
                }
                userImage.observe(mainActivity){
                    GlobalScope.launch {
                        UserRepository.getProfile(it.toString()){
                            if(it.isSuccessful){
                                val fileUri = it.result
                                Glide.with(mainActivity).load(fileUri).diskCacheStrategy(
                                    DiskCacheStrategy.ALL).into(imageViewMyPageProfile)
                            }else{
                                fragmentMainMyPageBinding.imageViewMyPageProfile.setImageResource(R.drawable.ic_person_24px)
                            }
                        }

                    }

                }
                userEmail.observe(mainActivity) {
                    fragmentMainMyPageBinding.textViewMainMyPageEmail.setText(it.toString())
                }
                userIntoduce.observe(mainActivity) {
                    fragmentMainMyPageBinding.textViewMainMyPageIntroduce.setText(it.toString())
                }

            }
            mainMyPageViewModel.getUserInfo(MyApplication.loginedUserInfo)
            cardViewMainMyPageModify.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.MY_PAGE_MODIFY_FRAGMENT, true, null)
            }
            cardViewMainMyPageTheme.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.MY_PAGE_THEME_FRAGMENT, true, null)
            }
            cardViewMainMyPageLogout.setOnClickListener {
                val dialogNormalBinding = DialogNormalBinding.inflate(layoutInflater)
                val builder = MaterialAlertDialogBuilder(mainActivity)

                dialogNormalBinding.textViewDialogNormalTitle.text = "로그아웃"
                dialogNormalBinding.textViewDialogNormalContent.text = "로그아웃 하시겠습니까?"

                builder.setView(dialogNormalBinding.root)

                builder.setPositiveButton("로그아웃") { dialog, which ->
                    // 자동 로그인 해제
                    val sharedPreferences = requireActivity().getSharedPreferences(
                        "MyAppPreferences",
                        Context.MODE_PRIVATE
                    )
                    val editor = sharedPreferences.edit()
                    editor.remove("isLoggedIn")
                    editor.remove("isLoggedUser")
                    editor.apply()
                    val googleSignInClient = GoogleSignIn.getClient(
                        requireActivity(),
                        GoogleSignInOptions.DEFAULT_SIGN_IN
                    )
                    googleSignInClient.signOut()
                    firebaseAuth.signOut()
                    Glide.with(requireActivity()).clear(imageViewMyPageProfile)
                    MyApplication.isLogined = false
                    MyApplication.loginedUserProfile = ""
                    mainActivity.removeFragment(MainActivity.MAIN_FRAGMENT)
                    mainActivity.replaceFragment(MainActivity.LOGIN_FRAGMENT, false, null)

                }
                builder.setNegativeButton("취소", null)
                builder.show()

            }
            cardViewMainMyPageWithDraw.setOnClickListener {
                val currentUser = firebaseAuth.currentUser
                val bundle = Bundle()
                // 사용자가 로그인한 제공업체(Provider) 목록 가져오기
                val providers = currentUser?.providerData
                // providers 목록을 반복하여 사용자가 연결된 제공업체 확인
                for (profile in providers!!) {
                    val providerId = profile.providerId
                    if (providerId == GoogleAuthProvider.PROVIDER_ID) {
                        bundle.putInt("UserType", MyApplication.GOOGLE_LOGIN)
                    } else if (providerId == EmailAuthProvider.PROVIDER_ID) {
                        bundle.putInt("UserType", MyApplication.EMAIL_LOGIN)
                    }
                }
                mainActivity.replaceFragment(MainActivity.MY_PAGE_WITH_DRAW_FRAGMENT,true,bundle)
            }

        }

        return fragmentMainMyPageBinding.root
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
            fragmentMainMyPageBinding.shimmerLayoutMainMyPage.startShimmer()
            fragmentMainMyPageBinding.shimmerLayoutMainMyPage.visibility = View.VISIBLE
            fragmentMainMyPageBinding.imageViewMyPageProfile.visibility = View.GONE
        }else{
            fragmentMainMyPageBinding.shimmerLayoutMainMyPage.stopShimmer()
            fragmentMainMyPageBinding.shimmerLayoutMainMyPage.visibility = View.GONE
            fragmentMainMyPageBinding.imageViewMyPageProfile.visibility = View.VISIBLE

        }
    }
}