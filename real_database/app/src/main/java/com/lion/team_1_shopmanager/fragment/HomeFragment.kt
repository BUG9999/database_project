package com.lion.database_project.fragment


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lion.database_project.MainActivity
import com.lion.database_project.MainActivity.FragmentName
import com.lion.database_project.R
import com.lion.database_project.adapter.ClothesListAdapter
import com.lion.database_project.adapter.OnClickConvertListener
import com.lion.database_project.databinding.FragmentHomeBinding
import com.lion.database_project.model.ShopModel
import com.lion.database_project.repository.ShopRepository
import com.lion.database_project.util.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale.filter
import kotlin.concurrent.thread

class HomeFragment : Fragment(),OnClickConvertListener{
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    private val adapter: ClothesListAdapter by lazy { ClothesListAdapter(this) }

    private var defaultCategory:String = "전체"

    private val detailCategoryList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater,container,false)
        mainActivity = activity as MainActivity
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("st","onViewCreated")
        //setRepository()
        loadClothes()
        setUpToolbar()
        setRecyclerView()
        clickListener()
        Log.d("st","detailAllChip : ${binding.detailAllChip.isChecked}")

    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            CoroutineScope(Dispatchers.Main).launch{
                // 초기 세팅 상태 일 때이니 true 를 준다.
                binding.detailAllChip.isChecked = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * RecyclerView에 어댑터 설정
     */
    private fun setRecyclerView() {
        binding.categoryClothesList.adapter = adapter
        binding.categoryClothesList.layoutManager = GridLayoutManager(requireContext(),3)
    }

    /**
     * HomeFragment 의 클릭 리스너 들
     */
    private fun clickListener(){
        // default 카테고리
        setCategoryListener()
        // 필터 버튼
        setFilterOnClick()
        // detailAllChip
        setDetailAllChipListener()
    }

    // 툴바 설정
    private fun setUpToolbar() {
        binding.apply {
            toolbarHome.title = "HoNight"
            toolbarHome.setNavigationIcon(R.drawable.menu_24px)
            toolbarHome.setNavigationOnClickListener {
                mainActivity.activityMainBinding.drawerLayoutMain.open()
            }
        }
    }

    /**
     * HomeFragment 의  default 카테고리 클릭 리스너
     */
    private fun setCategoryListener(){
        binding.apply {
            // default category 반응
            clothChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                // 상위 버튼 클릭시 세부 클릭 고정
                detailAllChip.isChecked = true
                defaultCategory =
                    group.run {  findViewById<Chip>(checkedChipId).text }.toString()
                Log.d("st"," - checkedChipId -")
                Log.d("st", defaultCategory)
                loadDefaultClothes(defaultCategory)
            }

            detailClothChipGroup.setOnCheckedStateChangeListener{ group, ids ->
                //
                Log.d("st"," - detailCloth - ")
                val result =
                group.checkedChipIds.fold(mutableListOf<String>()) { first, it ->
                    Log.d("st", "first : $first")
                    first += group.run { "${findViewById<Chip>(it).text}" }
                    first
                }

                Log.d("st","result : $result")

                loadDetailClothes(result)
            }
        }
    }

    /**
     * HomeFragment filter 버튼 클릭 리스너
     */
    private fun setFilterOnClick(){
        binding.apply {
            // 버튼 초기 텍스트
            //filterButtonStateConvert(detailView.isVisible)

            filterButton.setOnClickListener {
                detailView.run {
                    isVisible = !isVisible
                    // 필터 버튼 상태 변경
                    filterButtonStateConvert(isVisible)
                }
            }
        }
    }

    private fun setDetailAllChipListener(){
        binding.detailAllChip.apply {
            setOnClickListener {
                isChecked = true
                if (isChecked) {
                    // 세부 카테고리 체크 해제
                    binding.detailClothChipGroup.clearCheck()
                    Log.d("st", " - detailAllChip OnClick - ")
                    loadDefaultClothes(defaultCategory)
                }
            }
//            setOnCheckedChangeListener { it, isChecked ->
//                if (!isChecked) {
//                    // 자신의 체크 고정
//                    it.isChecked = true
//                    Log.d("st"," - detailAllChip Change - \n${it.isChecked}")
//                }
//            }
        }
    }

    /**
     * HomeFragment 의  default 카테고리와 관련된 detail 카테고리 세팅 매서드
     */
    private fun setDetailCategoryList(defaultCategory: String){
        binding.detailClothChipGroup.apply {

                // 상위 옷이 비어 있지 않을 경우 // 세부 선택이 없을 경우
                // detailClothChipGroup 초기화
                removeAllViews()
            if (defaultCategory != "전체") {
                // detailClothChipGroup 세팅
                val detailResId = Value.DETAIL_CATEGORY_MAP[defaultCategory]!!
                // detail 목록 추가
                resources.getStringArray(detailResId).forEach {
                    addDetailChip(it)
                }
                // 초기 세팅 상태 일 때이니 true 를 준다.
                binding.detailAllChip.isChecked = true
            }
        }
        binding.detailView.isVisible = true
        Log.d("st"," : setDetailCategoryList : ")
        Log.d("st","detailAllChip : ${binding.detailAllChip.isChecked}")
        Log.d("st","detailView : ${binding.detailView.isVisible}")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun ChipGroup.addDetailChip(detailName:String){
        addView(Chip(requireContext()).apply {
            text = detailName
            chipStrokeWidth = 0f
            elevation = 4f
            isCheckable = true
            detailColorConvert(this)
            setOnClickListener {
                // Log.d("st","${text}: $isChecked")
                if (isChecked && binding.detailAllChip.isChecked) {
                    binding.detailAllChip.isChecked = false
                }

            }
        })
    }

    /**
     * HomeFragment 의  filter 버튼의 텍스트 변화 매서드
     */
    private fun filterButtonStateConvert(selector:Boolean){
        binding.filterButton.apply {
            Log.d("st","filterButtonStateConvert")
            text = if (selector) "닫기"
                    else "필터"

            isChecked = selector
            Log.d("st","filterButton : $isChecked")
        }
    }

    private fun detailColorConvert(detailChip: Chip){
        detailChip.apply {
            setTextColor(resources.getColor(R.color.defaultLight,null))
            setChipBackgroundColorResource(R.color.DetailChipSelect)
        }
    }

    override fun onClickItem(shopIdx:Int){
        mainActivity.replaceFragment(FragmentName.SHOW_FRAGMENT,
            isAddToBackStack = true,
            animate = true,
            dataBundle = Bundle().apply {  putInt("shopIdx",shopIdx) }
        )
        Log.d("st","shopIdx: $shopIdx")
    }

    private fun loadClothes(){
        //기존 선택 사항으로 초기화
        Log.d("st"," -- loadClothes -- ")
        Log.d("st","defaultCategory : $defaultCategory")
        Log.d("st","detailCategoryList : $detailCategoryList")
        Log.d("st","${binding.detailAllChip.isChecked}")
        loadDetailClothes(detailCategoryList)
    }

    private fun loadDefaultClothes(setDefault: String){
        Log.d("st","  = loadDefaultClothes =  ")
        defaultCategory = setDefault // 상위 선택
        Log.d("st","defaultCategory : $defaultCategory")
        loadDetailClothes(mutableListOf()) // 빈값 전달 -> 상위 선택 으로만 분류됨
    }

    private fun loadDetailClothes(categoryList: MutableList<String>){
        CoroutineScope(Dispatchers.IO).launch {
            val totalClothes = ShopRepository.selectClotheDataAll(requireContext())
            detailCategoryList.clear()
            detailCategoryList += categoryList

            val clothes = if (defaultCategory != "전체"){
                 if (detailCategoryList.isNotEmpty()) {
                     selectFilter(detailCategoryList,totalClothes)
                 }else {
                     // 선택된 것이 없고  allDetailChip 이 true 일 때
                     totalClothes.filter { it.clothDefaultCategory == defaultCategory }
                 }
             }else totalClothes
            withContext(Dispatchers.Main){
                setSystemView(clothes.isEmpty())
                if (clothes.isNotEmpty()){
                    adapter.setShopItem(clothes)
                }

                // 세부 선택이 비어야 detail 생성
                if (detailCategoryList.isEmpty()) {
                    //옷이 비어 있는 지를 전달
                    setDetailChip(clothes.isEmpty())
                }
            }

        }
    }

    private fun setDetailChip(isEmpty:Boolean){
        Log.d("st","== setDetailChip ==")
        if (isEmpty){
            // 관련 default 가 비어있을 경우 전체
            setDetailCategoryList("${ binding.allChip.text }")
        }else{
            Log.d("st","clothes.isNotEmpty()")
            // detail category 세팅
            setDetailCategoryList(defaultCategory)
        }
        filterButtonStateConvert(binding.detailView.isVisible)
    }

    fun setSystemView(onSystem: Boolean){
        if (onSystem){
            binding.emptyListTextView.isVisible = true
            binding.categoryClothesList.isVisible = false
        }else{
            binding.emptyListTextView.isVisible = false
            binding.categoryClothesList.isVisible = true
        }
    }

    fun selectFilter(categoryList: MutableList<String>, totalClothes:MutableList<ShopModel>) =
        run {
        val selectClothes = mutableListOf<ShopModel>()
        categoryList.forEach { detail ->
            // 확인된 카테고리의 경우 제외하면서 진행
            // 카테고리 선택한 순서로 정렬
            totalClothes.removeAll {
                val detailCheck = it.clothDetailCategory == detail
                if (detailCheck)
                    selectClothes += it
                detailCheck
            }
        }
        selectClothes
    }

}