package com.lion.database_project.repository

import android.content.Context
import com.lion.database_project.database.ShopDatabase
import com.lion.database_project.model.ShopModel
import com.lion.database_project.vo.ShopVo

class ShopRepository {
    companion object {
        // 옷 정보를 저장하는 메서드
        fun insertClothData(context: Context, shopModel: ShopModel) {
            // 데이터를 Vo 객체에 담는다.
            val shopVO = ShopVo(
                clothName = shopModel.clothName,
                clothPrice = shopModel.clothPrice,
                clothDiscountRate = shopModel.clothDiscountRate,
                clothDescription = shopModel.clothDescription,
                clothDefaultCategory = shopModel.clothDefaultCategory,
                clothDetailCategory = shopModel.clothDetailCategory,
                clothImage = shopModel.clothImage
            )
            // 저장한다.
            val shopDatabase = ShopDatabase.getInstance(context)
            shopDatabase?.shopDAO()?.insertClothData(shopVO)
        }

        // 옷 정보 전체를 가져오는 메서드
        fun selectClotheDataAll(context: Context): MutableList<ShopModel> {
            // 데이터를 가져온다.
            val shopDatabase = ShopDatabase.getInstance(context)
            val clothList = shopDatabase?.shopDAO()?.selectClothDataAll()

            // 옷 데이터를 담을 리스트
            val tempList = mutableListOf<ShopModel>()

            // 옷의 수만큼 반복
            clothList?.forEach {
                val shopModel = ShopModel(
                    it.shopIdx,
                    it.clothName,
                    it.clothPrice,
                    it.clothDiscountRate,
                    it.clothDescription,
                    it.clothDefaultCategory,
                    it.clothDetailCategory,
                    it.clothImage
                )
                // 리스트에 담는다.
                tempList.add(shopModel)
            }
            return tempList
        }

        // 옷 정보 한개를 가져오는 메서드
        fun selectClothDataByShopIdx(context: Context, shopIdx: Int): ShopModel {
            val shopDatabase = ShopDatabase.getInstance(context)
            // 옷 정보 데이터를 가져온다.
            val shopVo = shopDatabase?.shopDAO()?.selectClothDataByClothIdx(shopIdx)
            // Model 객체에 담는다.
            val shopModel = ShopModel(
                shopVo?.shopIdx!!,
                shopVo.clothName,
                shopVo.clothPrice,
                shopVo.clothDiscountRate,
                shopVo.clothDescription,
                shopVo.clothDefaultCategory,
                shopVo.clothDetailCategory,
                shopVo.clothImage
            )
            return shopModel
        }

        // 옷 정보를 삭제하는 메서드
        fun deleteClothDataByShopIdx(context: Context, shopIdx: Int) {
            // 삭제한다.
            val shopDatabase = ShopDatabase.getInstance(context)
            val shopVo = ShopVo(shopIdx = shopIdx)
            shopDatabase?.shopDAO()?.deleteClothInfo(shopVo)
        }

        // 옷 정보를 수정하는 메서드
        fun updateClothDataByShopIdx(context: Context, shopModel: ShopModel) {
            // VO에 데이터를 담는다.
            val shopVo = ShopVo(
                shopModel.shopIdx,
                shopModel.clothName,
                shopModel.clothPrice,
                shopModel.clothDiscountRate,
                shopModel.clothDescription,
                shopModel.clothDefaultCategory,
                shopModel.clothDetailCategory,
                shopModel.clothImage
            )

            // 수정하는 메서드를 호출한다.
            val shopDatabase = ShopDatabase.getInstance(context)
            shopDatabase?.shopDAO()?.updateClothData(shopVo)

        }

        // 옷 정보 검색
        fun selectShopDataAllByClothName(
            context: Context,
            clothName: String
        ): MutableList<ShopModel> {
            // 데이터를 가져온다.
            val shopDatabase = ShopDatabase.getInstance(context)
            val shopList =
                shopDatabase?.shopDAO()?.selectShopDataAllByClothName(clothName)

            // 학생 데이터를 담을 리스트
            val tempList = mutableListOf<ShopModel>()

            // 학생의 수 만큼 반복한다.
            shopList?.forEach {
                val shopModel = ShopModel(
                    it.shopIdx,
                    it.clothName,
                    it.clothPrice,
                    it.clothDiscountRate,
                    it.clothDescription,
                    it.clothDefaultCategory,
                    it.clothDetailCategory,
                    it.clothImage
                )
                // 리스트에 담는다.
                tempList.add(shopModel)
            }
            return tempList
        }
    }
}