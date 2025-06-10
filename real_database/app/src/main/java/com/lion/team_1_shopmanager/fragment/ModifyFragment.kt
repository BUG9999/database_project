package com.lion.database_project.fragment

import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lion.database_project.MainActivity
import com.lion.database_project.R
import com.lion.database_project.databinding.FragmentModifyBinding
import com.lion.database_project.model.ShopModel
import com.lion.database_project.repository.ShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class ModifyFragment : Fragment() {

    private lateinit var fragmentModifyBinding: FragmentModifyBinding
    private lateinit var mainActivity: MainActivity

    lateinit var albumLauncher: ActivityResultLauncher<Intent>
    var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentModifyBinding = FragmentModifyBinding.inflate(layoutInflater,container,false)
        mainActivity = activity as MainActivity

        // 툴바 호출
        settingToolbar()
        // TextField 세팅
        settingTextField()
        // 카테고리 드롭 다운
        setupClothCategoryDropdown()

        createAlbumLauncher()
        setupImageUploadButton()

        return fragmentModifyBinding.root
    }

    // 툴바 설정
    fun settingToolbar(){
        fragmentModifyBinding.apply {
            // 타이틀
            fragmentModifyToolbar.title = "상품 정보 수정"

            // 뒤로가기
            fragmentModifyToolbar.setNavigationIcon(R.drawable.arrow_back_24px)
            fragmentModifyToolbar.setNavigationOnClickListener {
                mainActivity.removeFragment(MainActivity.FragmentName.MODIFY_FRAGMENT)
            }

            // 입력 완료
            fragmentModifyToolbar.inflateMenu(R.menu.modify_toolbar_menu)
            fragmentModifyToolbar.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.modify_done -> {
                        val isValid = checkedInputFields()
                        if (isValid){
                            val builder = MaterialAlertDialogBuilder(mainActivity)
                            builder.setTitle("상품 정보 수정")
                            builder.setMessage("상품 정보 수정시 복구가 불가능합니다")
                            builder.setNegativeButton("취소", null)
                            builder.setPositiveButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                                // 수정 메서드를 호출한다.
                                processingModifyClothData()
                            }
                            builder.show()
                        }
                    }
                }
                true
            }
        }
    }

    // 이미지 업로드 버튼 설정
    private fun setupImageUploadButton() {
        fragmentModifyBinding.apply {
            settingReplaceImage()
        }
    }

    // 이미지 변경
    fun settingReplaceImage() {
        fragmentModifyBinding.buttonModifyPhoto.setOnClickListener {
            val albumIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    // 이미지 타입을 설정한다.
                    type = "image/*"
                    // 선택할 파일의 타입을 지정(안드로이드 OS가 사전 작업을 할 수 있도록)
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
                }
            // 액티비티 실행
            albumLauncher.launch(albumIntent)
        }
    }

    // 런처를 생성하는 메서드
    fun createAlbumLauncher() {
        albumLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK && it.data != null) {
                it.data?.data?.let { uri ->
                    // 선택된 이미지 Uri를 저장
                    selectedImageUri = uri
                    // android 10 버전 이상이라면
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val source = ImageDecoder.createSource(mainActivity.contentResolver, uri)
                        val bitmap: Bitmap = ImageDecoder.decodeBitmap(source)
                        fragmentModifyBinding.imageViewModify.setImageBitmap(bitmap)
                    } else {
                        // ContentProvider를 통해 사진 데이터를 가져온다.
                        val cursor = mainActivity.contentResolver.query(uri, null, null, null, null)
                        cursor?.use {
                            if (it.moveToFirst()) {
                                // 이미지의 경로를 가져온다.
                                val idx = it.getColumnIndex(MediaStore.Images.Media.DATA)
                                val path = it.getString(idx)
                                // 이미지 경로를 Uri로 변환
                                selectedImageUri = Uri.parse(path)
                                // 이미지를 생성한다.
                                val bitmap = BitmapFactory.decodeFile(path)
                                fragmentModifyBinding.imageViewModify.setImageBitmap(bitmap)
                            }
                        }
                    }
                }
            }
        }
    }

    // 외부 저장소의 이미지를 내부 저장소로 복사하는 메서드
    fun copyImageToInternalStorage(uri: Uri, context: Context): Uri? {
        try {
            // 외부 저장소의 파일에 접근
            val contentResolver: ContentResolver = context.contentResolver
            // 주어진 URI에 해당하는 이미지 파일을 입력 스트림으로 열고 URI를 통해 파일을 열 수 없으면 null을 반환합니다.
            val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return null
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            // 내부 저장소에 이미지를 저장할 파일을 생성합니다.
            // 파일명은 "copied_image_" 뒤에 현재 시간을 붙여 고유하게 생성합니다.
            val file = File(context.filesDir, "copied_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            // 비트맵을 JPEG 형식으로 압축하여 출력 스트림에 저장
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            // 저장된 파일의 URI를 반환합니다. 이를 통해 내부 저장소에 저장된 파일을 참조할 수 있습니다.
            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    // 입력 요소 구성
    fun settingTextField(){
        fragmentModifyBinding.apply {
            val shopIdx = arguments?.getInt("shopIdx")!!
            CoroutineScope(Dispatchers.Main).launch {
                val work1 = async(Dispatchers.IO) {
                    ShopRepository.selectClothDataByShopIdx(mainActivity, shopIdx)
                }
                val shopModel = work1.await()

                textFieldModifyName.editText?.setText(shopModel.clothName)
                textFieldModifyPrice.editText?.setText(shopModel.clothPrice.toString())
                textFieldModifyDiscountRate.editText?.setText(shopModel.clothDiscountRate.toString())
                textFieldModifyDescription.editText?.setText(shopModel.clothDescription)
                // 카테고리 세팅
                setupClothCategoryDropdown(shopModel.clothDefaultCategory, shopModel.clothDetailCategory)
                // 이미지 설정
                if (shopModel.clothImage.isNotEmpty()) {
                    val imageUri = Uri.parse(shopModel.clothImage)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val source = ImageDecoder.createSource(mainActivity.contentResolver, imageUri)
                        val bitmap: Bitmap = ImageDecoder.decodeBitmap(source)
                        fragmentModifyBinding.imageViewModify.setImageBitmap(bitmap)
                    } else {
                        val bitmap = BitmapFactory.decodeFile(imageUri.path)
                        fragmentModifyBinding.imageViewModify.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    // 수정
    fun processingModifyClothData() {
        fragmentModifyBinding.apply {
            val shopIdx = arguments?.getInt("shopIdx")!!

            CoroutineScope(Dispatchers.Main).launch {
                val deferred = async(Dispatchers.IO) {
                    ShopRepository.selectClothDataByShopIdx(mainActivity, shopIdx)
                }
                val existingShopModel = deferred.await()

                // 옷 이름
                val clothName = textFieldModifyName.editText?.text.toString()
                // 옷 가격
                val clothPrice = textFieldModifyPrice.editText?.text.toString().toInt()
                // 옷 할인율
                val clothDiscountRate = textFieldModifyDiscountRate.editText?.text?.toString()?.toIntOrNull() ?: 0
                // 옷 설명
                val clothDescription = textFieldModifyDescription.editText?.text.toString()
                // 기본 카테고리
                val clothDefaultCategory = textFieldModifyClothCategory.editText?.text.toString()
                // 세부 카테고리
                val clothDetailCategory = textFieldModifySubCategory.editText?.text.toString()

                // 옷 이미지: 이미지가 이미 선택된 경우 그 이미지를 사용, 그렇지 않으면 기존 이미지 사용
                val clothImage = if (selectedImageUri != null && selectedImageUri.toString() != existingShopModel?.clothImage) {
                    // 새로운 이미지를 선택한 경우 내부 저장소로 복사 후 URI 반환
                    copyImageToInternalStorage(selectedImageUri!!, mainActivity)?.toString() ?: ""
                } else {
                    // 새로운 이미지를 선택하지 않은 경우 기존 이미지 유지
                    existingShopModel?.clothImage.orEmpty()
                }

                val shopModel = ShopModel(
                    shopIdx,
                    clothName,
                    clothPrice,
                    clothDiscountRate,
                    clothDescription,
                    clothDefaultCategory,
                    clothDetailCategory,
                    clothImage
                )

                launch(Dispatchers.IO) {
                    ShopRepository.updateClothDataByShopIdx(mainActivity, shopModel)
                }

                mainActivity.removeFragment(MainActivity.FragmentName.MODIFY_FRAGMENT)
            }
        }
    }

    // 입력 필드 유효성 검사
    fun checkedInputFields(): Boolean {
        var isValid = true

        fragmentModifyBinding.apply {
            // 상품명 확인
            val name = textFieldModifyName.editText?.text.toString()
            if (name.isEmpty()) {
                textFieldModifyName.error = "상품명을 입력해주세요"
                isValid = false
            } else {
                textFieldModifyName.error = null
            }

            // 상품 가격 확인
            val price = textFieldModifyPrice.editText?.text.toString()
            if (price.isEmpty()) {
                textFieldModifyPrice.error = "상품 가격을 입력해주세요"
                isValid = false
            } else {
                textFieldModifyPrice.error = null
            }

            // 상품 설명 확인
            val description = textFieldModifyDescription.editText?.text.toString()
            if (description.isEmpty()) {
                textFieldModifyDescription.error = "상품 설명을 입력해주세요"
                isValid = false
            } else {
                textFieldModifyDescription.error = null
            }

            // 상위 카테고리 확인
            val clothCategory = textFieldModifyClothCategory.editText?.text.toString()
            if (clothCategory.isEmpty()) {
                textFieldModifyClothCategory.error = "상위 카테고리를 선택해주세요"
                isValid = false
            } else {
                textFieldModifyClothCategory.error = null
            }

            // 하위 카테고리 확인
            if (textFieldModifySubCategory.visibility == View.VISIBLE) {
                val subCategory = textFieldModifySubCategory.editText?.text.toString()
                if (subCategory.isEmpty()) {
                    textFieldModifySubCategory.error = "하위 카테고리를 선택해주세요"
                    isValid = false
                } else {
                    textFieldModifySubCategory.error = null
                }
            }
        }
        return isValid
    }


    // 상위 카테고리 설정
    private fun setupClothCategoryDropdown(selectedCategory: String? = null, selectedSubCategory: String? = null) {
        val clothCategories = resources.getStringArray(R.array.clothCategory)
        val clothCategoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, clothCategories)
        val clothCategoryDropdown = fragmentModifyBinding.textFieldModifyClothCategory.editText as? AutoCompleteTextView
        clothCategoryDropdown?.setAdapter(clothCategoryAdapter)

        // 선택된 상위 카테고리 초기 설정
        selectedCategory?.let {
            clothCategoryDropdown?.setText(it, false)
            val position = clothCategories.indexOf(it)
            if (position >= 0) {
                updateSubCategoryDropdown(position, selectedSubCategory)
            }
        }
        // 상위 카테고리 선택 리스너 설정
        clothCategoryDropdown?.setOnItemClickListener { _, _, position, _ ->
            updateSubCategoryDropdown(position)
        }
    }

    // 하위 카테고리 설정
    private fun updateSubCategoryDropdown(categoryPosition: Int, selectedSubCategory: String? = null) {
        val subCategories = when (categoryPosition) {
            // 아우터
            0 -> resources.getStringArray(R.array.outerCategory)
            // 상의
            1 -> resources.getStringArray(R.array.topCategory)
            // 바지
            2 -> resources.getStringArray(R.array.pantsCategory)
            // 신발
            3 -> resources.getStringArray(R.array.shoesCategory)
            // 잡화
            4 -> resources.getStringArray(R.array.accessoriesCategory)
            else -> emptyArray()
        }

        // 하위 카테고리 어댑터 설정
        val subCategoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subCategories)
        val subCategoryDropdown = fragmentModifyBinding.textFieldModifySubCategory.editText as? AutoCompleteTextView
        subCategoryDropdown?.setAdapter(subCategoryAdapter)

        // 선택된 하위 카테고리 초기 설정
        selectedSubCategory?.let {
            if (subCategories.contains(it)) {
                subCategoryDropdown?.setText(it, false)
            }
        }

        // 새로운 상위 카테고리가 선택되면 하위 카테고리 필드 초기화
        if (selectedSubCategory == null) {
            subCategoryDropdown?.text?.clear()
        }
    }
}