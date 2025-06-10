package com.lion.database_project.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lion.database_project.vo.ShopVo

@Dao
interface ShopDao {

    // 옷 정보 저장
    @Insert
    fun insertClothData(shopVo: ShopVo)

    // 옷 전체 목록을 가져온다.
    // order by 컬럼명 : 컬럼명을 기준으로 행을 오름 차순 정렬한다.
    // order by 컬럼명 desc : 컬럼명을 기준으로 행을 내림 차순 정렬한다.
    @Query("""
        select * from ShopTable
        order by shopIdx desc """)
    fun selectClothDataAll() : List<ShopVo>

    // 옷 정보에 있는 Idx를 통해 정보를 가져온다.
    @Query("""
        select * from ShopTable
        where shopIdx = :shopIdx""")
    fun selectClothDataByClothIdx(shopIdx:Int):ShopVo

    // 옷 정보  삭제
    @Delete
    fun deleteClothInfo(shopVo: ShopVo)

    // 옷 정보 수정
    @Update
    fun updateClothData(shopVo: ShopVo)

    // 옷 정보 검색
    @Query("""
     SELECT * FROM ShopTable WHERE clothName LIKE '%' || :clothName || '%'
    """)
    fun selectShopDataAllByClothName(clothName:String):List<ShopVo>
}