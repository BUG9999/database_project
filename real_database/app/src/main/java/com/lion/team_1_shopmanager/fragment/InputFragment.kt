package com.lion.database_project.fragment

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import com.lion.database_project.MainActivity
import com.lion.database_project.R
import com.lion.database_project.databinding.FragmentInputBinding
import com.lion.database_project.model.ShopModel
import com.lion.database_project.repository.ShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class InputFragment : Fragment() {

    private lateinit var fragmentInputBinding: FragmentInputBinding
    private lateinit var mainActivity: MainActivity

    lateinit var albumLauncher: ActivityResultLauncher<Intent>
    var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentInputBinding = FragmentInputBinding.inflate(layoutInflater,container,false)
        mainActivity = activity as MainActivity

        // 툴바 세팅
        settingToolbar()
        // 카테고리 입력 설정
        setupClothCategoryDropdown()
        // 앨범 런처
        createAlbumLauncher()
        // 사진 수정 버튼
        setupImageUploadButton()
        return fragmentInputBinding.root
    }


    // 툴바 설정
    fun settingToolbar(){
        fragmentInputBinding.apply {
            // 타이틀
            fragmentInputToolbar.title = "상품 정보 등록"

            // 뒤로가기(홈 프래그먼트)
            fragmentInputToolbar.setNavigationIcon(R.drawable.menu_24px)
            fragmentInputToolbar.setNavigationOnClickListener {
                mainActivity.activityMainBinding.drawerLayoutMain.open()
            }

            // 입력 완료
            fragmentInputToolbar.inflateMenu(R.menu.input_toolbar_menu)
            fragmentInputToolbar.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.input_done -> {
                        val isValid = checkedInputFields()
                        if (isValid){
                            // 데이터 저장
                            saveClothesInfoToDatabase()
                        }
                    }
                }
                true
            }
        }
    }

    // 이미지 업로드 버튼 설정
    private fun setupImageUploadButton() {
        fragmentInputBinding.apply {
            settingReplaceImage()
        }
    }

    // 이미지 변경
    fun settingReplaceImage() {
        fragmentInputBinding.buttonUploadPhoto.setOnClickListener {
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
                        fragmentInputBinding.imageViewInput.setImageBitmap(bitmap)
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
                                fragmentInputBinding.imageViewInput.setImageBitmap(bitmap)
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

    // 정보 등록 완료 처리
    fun saveClothesInfoToDatabase(){
        fragmentInputBinding.apply {
            val clothesName = textFieldInputName.editText?.text?.toString()!!
            val clothesPrice = textFieldInputPrice.editText?.text?.toString()!!.toInt()
            val clothesDiscountRate = textFieldInputDiscountRate.editText?.text?.toString()?.toIntOrNull() ?: 0
            val clothesDescription = textFieldInputDescription.editText?.text?.toString()!!
            val clothesCategory = textFieldClothCategory.editText?.text?.toString()!!
            val clothesSubCategory = textFieldSubCategory.editText?.text?.toString()!!
            val clothesImage = selectedImageUri?.let { uri ->
                copyImageToInternalStorage(uri, mainActivity)?.toString() ?: ""
                // 선택된 이미지가 없으면 빈 문자열 처리
            } ?: ""

            val shopModel = ShopModel(0, clothesName, clothesPrice, clothesDiscountRate, clothesDescription, clothesCategory, clothesSubCategory, clothesImage)

            CoroutineScope(Dispatchers.Main).launch {
                val work1 = async(Dispatchers.IO){
                    ShopRepository.insertClothData(mainActivity, shopModel)
                }
                work1.join()
                // 이전 화면으로 돌아간다
                mainActivity.replaceFragment(MainActivity.FragmentName.HOME_FRAGMENT,false,false,null)
            }
        }
    }

    // 입력 필드 유효성 검사
    fun checkedInputFields(): Boolean {
        var isValid = true

        fragmentInputBinding.apply {
            // 상품명 확인
            val name = textFieldInputName.editText?.text.toString()
            if (name.isEmpty()) {
                textFieldInputName.error = "상품명을 입력해주세요"
                isValid = false
            } else {
                textFieldInputName.error = null
            }

            // 상품 가격 확인
            val price = textFieldInputPrice.editText?.text.toString()
            if (price.isEmpty()) {
                textFieldInputPrice.error = "상품 가격을 입력해주세요"
                isValid = false
            } else {
                textFieldInputPrice.error = null
            }

            // 상품 설명 확인
            val description = textFieldInputDescription.editText?.text.toString()
            if (description.isEmpty()) {
                textFieldInputDescription.error = "상품 설명을 입력해주세요"
                isValid = false
            } else {
                textFieldInputDescription.error = null
            }

            // 상위 카테고리 확인
            val clothCategory = textFieldClothCategory.editText?.text.toString()
            if (clothCategory.isEmpty()) {
                textFieldClothCategory.error = "상위 카테고리를 선택해주세요"
                isValid = false
            } else {
                textFieldClothCategory.error = null
            }

            // 하위 카테고리 확인
            if (textFieldSubCategory.visibility == View.VISIBLE) {
                val subCategory = textFieldSubCategory.editText?.text.toString()
                if (subCategory.isEmpty()) {
                    textFieldSubCategory.error = "하위 카테고리를 선택해주세요"
                    isValid = false
                } else {
                    textFieldSubCategory.error = null
                }
            }

            // 이미지 등록 확인
            if (selectedImageUri == null) {
                errorImageText.visibility = View.VISIBLE
                errorImageText.text = "상품 이미지를 등록해주세요"
                isValid = false
            } else {
                errorImageText.visibility = View.GONE
            }
        }

        return isValid
    }

    // 상위 카테고리 설정
    private fun setupClothCategoryDropdown() {
        val clothCategories = resources.getStringArray(R.array.clothCategory)
        val clothCategoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, clothCategories)
        val clothCategoryDropdown = fragmentInputBinding.textFieldClothCategory.editText as? AutoCompleteTextView
        clothCategoryDropdown?.setAdapter(clothCategoryAdapter)

        // 하위 카테고리 숨기기 초기 설정
        fragmentInputBinding.textFieldSubCategory.visibility = View.GONE

        // 상위 카테고리 선택 리스너 설정
        clothCategoryDropdown?.setOnItemClickListener { _, _, position, _ ->
            updateSubCategoryDropdown(position)
        }
    }

    // 하위 카테고리 설정
    private fun updateSubCategoryDropdown(categoryPosition: Int) {

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
        val subCategoryDropdown = fragmentInputBinding.textFieldSubCategory.editText as? AutoCompleteTextView
        subCategoryDropdown?.setAdapter(subCategoryAdapter)
        // 새로운 상위 카테고리가 선택되면 하위 카테고리 필드 초기화
        subCategoryDropdown?.text?.clear()

        // 하위 카테고리 필드 보이기 설정 (상위 카테고리가 선택되었을 때만)
        if (subCategories.isNotEmpty()) {
            fragmentInputBinding.textFieldSubCategory.visibility = View.VISIBLE
        }
    }
}