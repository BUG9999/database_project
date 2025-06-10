package com.lion.database_project

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialSharedAxis
import com.lion.a07_studentmanager.fragment.LoginFragment
import com.lion.a07_studentmanager.fragment.SettingPasswordFragment
import com.lion.database_project.databinding.ActivityMainBinding
import com.lion.database_project.databinding.HeaderMainBinding
import com.lion.database_project.fragment.HomeFragment
import com.lion.database_project.fragment.InputFragment
import com.lion.database_project.fragment.ModifyFragment
import com.lion.database_project.fragment.SearchFragment
import com.lion.database_project.fragment.ShowFragment
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
     lateinit var activityMainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 초기화면 설정
        // Preferences 객체를 가져온다.
        val managerPef = getSharedPreferences("manager", MODE_PRIVATE)
        // 저장되어 있는 비밀번호를 가져온다.
        val managerPassword = managerPef.getString("password", null)
        // 저장되어 있는 비밀번호가 없다면..
        if(managerPassword == null){
            replaceFragment(FragmentName.SETTING_PASSWORD_FRAGMENT, false, false, null)
        } else {
            replaceFragment(FragmentName.LOGIN_FRAGMENT, false, false, null)
        }

        // 네비게이션 뷰 구성
        settingNavigationView()

        activityMainBinding.drawerLayoutMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

    }

    // 네비게이션 뷰 구성
    fun settingNavigationView() {
        activityMainBinding.apply {
            // 헤더
            val headerMainBinding = HeaderMainBinding.inflate(layoutInflater)
            navigationViewMain.addHeaderView(headerMainBinding.root)

            navigationViewMain.setCheckedItem(R.id.navigation_menu_item_home)

            // 메뉴
            navigationViewMain.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.navigation_menu_item_home -> {
                        replaceFragment(FragmentName.HOME_FRAGMENT,false,false,null)
                    }

                    R.id.navigation_menu_item_input -> {
                        replaceFragment(FragmentName.INPUT_FRAGMENT,false,false,null)
                    }

                    R.id.navigation_menu_item_search -> {
                        replaceFragment(FragmentName.SEARCH_FRAGMENT,false,false,null)
                    }
                }
                drawerLayoutMain.close()
                true
            }
        }
    }

    // 프래그먼트를 교체하는 함수
    fun replaceFragment(
        fragmentName: FragmentName,
        isAddToBackStack: Boolean,
        animate: Boolean,
        dataBundle: Bundle?
    ) {
        // 프래그먼트 객체
        val newFragment = when (fragmentName) {
            // 홈 화면
            FragmentName.HOME_FRAGMENT -> HomeFragment()
            // 검색 화면
            FragmentName.SEARCH_FRAGMENT -> SearchFragment()
            // 옷 정보 보여주는 화면
            FragmentName.SHOW_FRAGMENT -> ShowFragment()
            // 옷 정보 등록 화면
            FragmentName.INPUT_FRAGMENT -> InputFragment()
            // 옷 정보 수정 화면
            FragmentName.MODIFY_FRAGMENT -> ModifyFragment()

            FragmentName.LOGIN_FRAGMENT -> LoginFragment()

            FragmentName.SETTING_PASSWORD_FRAGMENT -> SettingPasswordFragment()
        }

        // bundle 객체가 null이 아니라면
        if (dataBundle != null) {
            newFragment.arguments = dataBundle
        }

        // 프래그먼트 교체
        supportFragmentManager.commit {

            if (animate) {
                newFragment.exitTransition =
                    MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
                newFragment.reenterTransition =
                    MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
                newFragment.enterTransition =
                    MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
                newFragment.returnTransition =
                    MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
            }

            replace(R.id.fragmentContainerViewMain, newFragment)
            if (isAddToBackStack) {
                addToBackStack(fragmentName.str)
            }
        }
    }

    // 프래그먼트를 BackStack에서 제거하는 메서드
    fun removeFragment(fragmentName: FragmentName) {
        supportFragmentManager.popBackStack(
            fragmentName.str,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

    }

    // 키보드 올리는 메서드
    fun showSoftInput(view: View) {
        // 입력을 관리하는 매니저
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        // 포커스를 준다.
        view.requestFocus()

//        thread {
//            SystemClock.sleep(1000)
//            // 키보드를 올린다.
//            inputManager.showSoftInput(view, 0)
//        }
        view.post {
            inputManager.showSoftInput(view, 0)
        }
    }

    // 키보드를 내리는 메서드
    fun hideSoftInput() {
        // 포커스가 있는 뷰가 있다면
        if (currentFocus != null) {
            // 입력을 관리하는 매니저
            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            // 키보드를 내린다.
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            // 포커스를 해제한다.
            currentFocus?.clearFocus()
        }
    }

    // 프래그먼트들을 나타내는 값들
    enum class FragmentName(var number: Int, var str: String) {
        // 메인 화면
        HOME_FRAGMENT(1, "HomeFragment"),

        // 검색 화면
        SEARCH_FRAGMENT(2, "SearchFragment"),

        // 상품 정보
        SHOW_FRAGMENT(3, "ShowFragment"),

        // 옷 정보 등록 화면
        INPUT_FRAGMENT(4, "InputFragment"),

        // 옷 정보 수정 화면
        MODIFY_FRAGMENT(5, "ModifyFragment"),

        // 로그인 화면
        LOGIN_FRAGMENT(1, "LoginFragment"),

        // 관리자 비밀번호 설정화면
        SETTING_PASSWORD_FRAGMENT(2, "SettingPasswordFragment")

    }
}