package com.test.dontforget.UI.JoinFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.test.dontforget.MainActivity
import com.test.dontforget.R
import com.test.dontforget.databinding.FragmentJoinBinding
import com.test.dontforget.databinding.FragmentJoinTermBinding

class JoinTermFragment : Fragment(){
    lateinit var fragmentJoinTermBinding: FragmentJoinTermBinding
    lateinit var mainActivity : MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentJoinTermBinding = FragmentJoinTermBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        fragmentJoinTermBinding.run {
            materialToolbar.run {
                title = "이용약관"
                setNavigationIcon(R.drawable.ic_arrow_back_24px)
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.JOIN_TERM_FRAGMENT)
                }
            }
        }
        return fragmentJoinTermBinding.root
    }
}