package com.lion.database_project.vo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ShopTable")
data class ShopVo(
    @PrimaryKey(autoGenerate = true)
    // 옷 번호
    val shopIdx: Int = 0,
    // 옷 이름
    val clothName: String = "",
    // 옷 가격
    val clothPrice: Int = 0,
    // 옷 할인율
    val clothDiscountRate : Int = 0,
    // 옷 설명
    val clothDescription: String = "",
    // 기본 카테고리
    val clothDefaultCategory: String = "",
    // 세부 카테고리
    val clothDetailCategory: String = "",
    // 옷 이미지
    val clothImage: String = ""
)