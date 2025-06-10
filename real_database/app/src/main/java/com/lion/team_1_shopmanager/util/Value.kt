package com.lion.database_project.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.fragment.app.Fragment

/**
 * class Value{
 *
 *     companion object{
 *
 *     }
 * }
 */
object Value {
    val DETAIL_CATEGORY_MAP = ClothCategory.entries.fold(mutableMapOf<String, Int>()){ map, clothCategory->
        clothCategory.apply { map[category] = details }
        map
    }

    // 할인율 계산
    fun calculatePrice(originalPrice: Int, discountRate: Int): Int {
        return originalPrice - ((originalPrice/100) * discountRate)
    }

    // 키보드를 내리는 메서드
    fun Context.hideSoftInput(view: View) {
        // 포커스가 있는 뷰가 있다면
        // 입력을 관리하는 매니저
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        // 키보드를 내린다.
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        // 포커스를 해제한다.
        view.clearFocus()
    }

    // 마지막 입력 버튼 메서드
    fun Fragment.setLastComplete(text: EditText, button: Button){
        text.setOnEditorActionListener { v, actionId, event ->
            this.requireContext().hideSoftInput(text)
            button.callOnClick()
            false
        }
    }
}

