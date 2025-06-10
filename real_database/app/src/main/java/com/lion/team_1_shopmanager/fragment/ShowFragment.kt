package com.lion.database_project.fragment

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lion.database_project.MainActivity
import com.lion.database_project.MainActivity.FragmentName
import com.lion.database_project.R
import com.lion.database_project.databinding.FragmentShowBinding
import com.lion.database_project.repository.ShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class ShowFragment : Fragment() {
    private lateinit var fragmentShowBinding: FragmentShowBinding
    private lateinit var mainActivity: MainActivity

    var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentShowBinding = FragmentShowBinding.inflate(layoutInflater, container, false)
        // 초기화
        initialize()
        // Toolbar
        setUpToolbar()
        // SetText
        setUpClothInfoText()
        return fragmentShowBinding.root
    }

    // 초기화
    private fun initialize() {
        mainActivity = activity as MainActivity
    }

    // 툴바 설정
    private fun setUpToolbar() {
        fragmentShowBinding.apply {
            toolbarShowCloth.title = "옷 정보 보기"
            toolbarShowCloth.setNavigationIcon(R.drawable.arrow_back_24px)
            toolbarShowCloth.setNavigationOnClickListener {
                mainActivity.removeFragment(FragmentName.SHOW_FRAGMENT)
            }
            toolbarShowCloth.inflateMenu(R.menu.toolbar_show_cloth_info)
            toolbarShowCloth.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.show_cloth_menu_modify -> {
                        val shopIdx = arguments?.getInt("shopIdx")
                        val dataBundle = Bundle()
                        dataBundle.putInt("shopIdx", shopIdx!!)
                        mainActivity.replaceFragment(
                            FragmentName.MODIFY_FRAGMENT,
                            true,
                            true,
                            dataBundle
                        )
                    }

                    R.id.show_cloth_menu_remove -> {
                        val builder = MaterialAlertDialogBuilder(mainActivity)
                        builder.setTitle("상품 데이터 삭제")
                        builder.setMessage("상품 데이터 제거시 복구가 불가능합니다")
                        builder.setNegativeButton("취소", null)
                        builder.setPositiveButton("삭제") { dialogInterface: DialogInterface, i: Int ->
                            deleteData()
                        }
                        builder.show()
                    }
                }
                true
            }
        }
    }

    // 데이터 삭제
    private fun deleteData() {
        CoroutineScope(Dispatchers.Main).launch {
            val shopIdx = arguments?.getInt("shopIdx")
            val work1 = async(Dispatchers.IO) {
                ShopRepository.deleteClothDataByShopIdx(mainActivity, shopIdx!!)
            }
            work1.join()
            mainActivity.removeFragment(FragmentName.SHOW_FRAGMENT)

        }
    }

    // 텍스트 세팅
    private fun setUpClothInfoText() {
        fragmentShowBinding.apply {
            if (arguments != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val work1 = async(Dispatchers.IO) {
                        val shopIdx = arguments?.getInt("shopIdx")
                        ShopRepository.selectClothDataByShopIdx(mainActivity, shopIdx!!)
                    }
                    val shopModel = work1.await()
                    fragmentShowBinding.apply {
                        textFieldShowClothCategory.editText?.setText(shopModel.clothDefaultCategory)
                        textFieldShowSubCategory.editText?.setText(shopModel.clothDetailCategory)
                        textFieldShowClothName.editText?.setText(shopModel.clothName)
                        textFieldShowClothPrice.editText?.setText("${shopModel.clothPrice}원")
                        textFieldShowDiscountRate.editText?.setText("${shopModel.clothDiscountRate}%")
                        textFieldShowClothDescription.editText?.setText(shopModel.clothDescription)

                        // 내부 저장소 URI 설정
                        selectedImageUri = shopModel.clothImage.toUri()
                        imageViewPrice.setImageURI(Uri.parse(selectedImageUri.toString()))
                    }
                }
            }
        }
    }
}

