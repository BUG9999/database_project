package com.lion.database_project.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lion.database_project.MainActivity
import com.lion.database_project.R
import com.lion.database_project.adapter.SearchListAdapter
import com.lion.database_project.databinding.FragmentSearchBinding
import com.lion.database_project.model.ShopModel
import com.lion.database_project.repository.ShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private lateinit var fragmentSearchBinding: FragmentSearchBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var searchListAdapter: SearchListAdapter

    var clothList = mutableListOf<ShopModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSearchBinding = FragmentSearchBinding.inflate(layoutInflater, container, false)

        // 초기화
        initialize()

        // TextField
        settingTextField()

        // RecyclerView
        settingRecyclerView()

        // Toolbar
        settingToolbar()

        return fragmentSearchBinding.root
    }

    // 초기화
    private fun initialize() {
        mainActivity = activity as MainActivity
        searchListAdapter = SearchListAdapter(mainActivity)
    }

    // Set Toolbar
    fun settingToolbar() {
        fragmentSearchBinding.apply {
            toolbarSearchCloth.setNavigationIcon(R.drawable.menu_24px)
            toolbarSearchCloth.setNavigationOnClickListener {
                mainActivity.activityMainBinding.drawerLayoutMain.open()
            }
            toolbarSearchCloth.title = "옷 검색"
        }
    }

    // Set RecyclerView
    fun settingRecyclerView() {
        fragmentSearchBinding.apply {
            recyclerViewSearchCloth.adapter = searchListAdapter
            recyclerViewSearchCloth.layoutManager = GridLayoutManager(mainActivity, 3)
        }
    }

    // Set TextField
    fun settingTextField() {
        fragmentSearchBinding.apply {
            // 검색창에 포커스를 준다.
            mainActivity.showSoftInput(textFieldSearchClothName.editText!!)

            // 키보드 입력할 때마다 데이터 검색 및 갱신
            textFieldSearchClothName.editText?.addTextChangedListener { text ->
                CoroutineScope(Dispatchers.Main).launch {
                    val keyword = text?.toString()?.trim() ?: ""

                    if (text?.isEmpty() == true) {
                        // 리스트 초기화
                        clothList.clear()
                        textViewWrongSearch.visibility = View.VISIBLE
                        searchListAdapter.refreshList(clothList)
                    } else {
                        // 데이터 검색
                        val work1 = async(Dispatchers.IO) {
                            ShopRepository.selectShopDataAllByClothName(mainActivity, keyword)
                        }

                        clothList = work1.await()
                        if (clothList.isEmpty()) {
                            textViewWrongSearch.visibility = View.VISIBLE
                        } else {
                            textViewWrongSearch.visibility = View.GONE
                        }
                        searchListAdapter.refreshList(clothList)
                    }
                }
            }
        }
    }
}