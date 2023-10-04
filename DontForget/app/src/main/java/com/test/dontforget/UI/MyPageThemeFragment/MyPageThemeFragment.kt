package com.test.dontforget.UI.MyPageThemeFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.test.dontforget.MainActivity
import com.test.dontforget.MyApplication
import com.test.dontforget.R
import com.test.dontforget.Util.ThemeUtil.DARK_MODE
import com.test.dontforget.Util.ThemeUtil.LIGHT_MODE
import com.test.dontforget.Util.ThemeUtil.applyTheme
import com.test.dontforget.databinding.FragmentMyPageThemeBinding

class MyPageThemeFragment : Fragment() {
    lateinit var fragmentMyPageThemeBinding: FragmentMyPageThemeBinding
    lateinit var mainActivity: MainActivity
    lateinit var myApplication: MyApplication
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMyPageThemeBinding = FragmentMyPageThemeBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        myApplication = MyApplication()
        var whatsTheme = ""
        fragmentMyPageThemeBinding.run {
            toolbarMyPageTheme.run {
                setTitle(getString(R.string.myPage_theme))
                setNavigationIcon(R.drawable.ic_arrow_back_24px)
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.MY_PAGE_THEME_FRAGMENT)
                }
            }
            val sharedPreferences =  requireActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
            val themeName = sharedPreferences.getString("theme", LIGHT_MODE)
            when(themeName){
                LIGHT_MODE ->{
                    radioGroupMyPageTheme.check(R.id.radioButton_myPageTheme_white)
                }
                DARK_MODE->{
                    radioGroupMyPageTheme.check(R.id.radioButton_myPageTheme_dark)
                }
            }

            radioGroupMyPageTheme.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.radioButton_myPageTheme_white -> {
                        whatsTheme = LIGHT_MODE
                    }
                    R.id.radioButton_myPageTheme_dark -> {
                        whatsTheme = DARK_MODE
                    }
                }
            }
            buttonMyPageThemeComplete.setOnClickListener {
                val sharedPreferences =  requireActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("theme",whatsTheme)
                editor.apply()
                // MyApplication에서 selectedTheme 업데이트
                MyApplication.selectedTheme = whatsTheme
                // 테마 적용
                applyTheme(whatsTheme)

            }
        }
        return fragmentMyPageThemeBinding.root
    }
}