package com.lion.database_project.model

data class ShopModel(
    // 옷 번호
    val shopIdx: Int,
    // 옷 이름
    val clothName: String,
    // 옷 가격
    val clothPrice: Int,
    // 옷 할인율
    val clothDiscountRate : Int,
    // 옷 설명
    val clothDescription: String,
    // 기본 카테고리
    val clothDefaultCategory: String,
    // 세부 카테고리
    val clothDetailCategory: String,
    // 옷 이미지
    val clothImage: String
)