package com.lion.database_project.util

import android.telecom.Call.Details
import com.lion.database_project.R

enum class ClothCategory(var idx:Int, var category: String, var details: Int) {
    // 아우터
    OUTER_CATEGORY(1, "아우터", R.array.outerCategory),
    // 상의
    TOP_CATEGORY(2,"상의", R.array.topCategory),
    // 하의
    PANTS_CATEGORY(3,"하의", R.array.pantsCategory),
    // 신발
    SHOES_CATEGORY(4, "신발", R.array.shoesCategory),
    // 잡화
    ACCESSORIES_CATEGORY(2,"잡화", R.array.accessoriesCategory)
}