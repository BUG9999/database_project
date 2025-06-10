# 쇼핑몰 관리 앱 실습 1조 결과물 - 의류 판매 관리자 앱

## 팀원 구성 및 역할
- 정지은 - InputFragment, ModifyFragment UI 및 기능 구현
- 최영석 - SearchFragment, ShowFragment UI 및 기능 구현
- 김재민 - HomeFragment, LoginFragment, SettingPasswordFragment UI 및 기능 구현

  ## 프로젝트 기간
  2024.11.20 ~ 2024.12.04(2주)

  ## 사용 기술
  - Fragment
  - Room DataBase
  - Coroutines
  - ViewBinding
  - Kotlin Extension Functions
  - Git
  - SharedPreferences
 
    ## 화면에 따른 기능

    ### HomeFragment
    ::주의:: 카테고리가 비어있을경우 세부 카테고리가 안뜹니다!
    - 상위 카테고리 누를 시에 상위 카테고리를 기반으로 옷 설정
      
    - 하위 카테고리의 경우 ```상위 카테고리 물품이 없을 경우 하위 카테고리가 "전체" 만 나타남```
    - 하위 카테고리에서 전체 버튼과 그외의 버튼이 토글로 구성됨 (예 전체가 true -> 그 외가 false , 그외가 true -> 전체는 false)
    - 하위 카테고리에서 전체 버튼은 그 외의 버튼이 눌리기 전까지는 true로 고정됨
      
    - 다른 Fragment 이동시 하위 카테고리는 전체로 고정되고 상위를 기반으로 다시 옷 목록 설정
    ### InputFragment
    - 상위 카테고리를 선택해야 하위 카테고리를 선택할 수 있도록 함
    - 필수로 입력해야 하는 정보를 입력하지 않을 시 유효성 검사를 통해 에러메세지를 띄우게 함
    - 상품의 정보를 입력하고, 데이터베이스에 저장
 
    ### LoginFragment
    - 관리자 로그인 비밀번호입력

    ### ModifyFragment
    - 상품의 데이터 정보를 가져와서 수정할 수 있게 함

    ### SearchFragment
    - 데이터에 있는 상품들을 검색할수 있게 구성
 
    ### SettingPasswordFragment
    - 관리자 로그인 비밀번호 등록
 
    ### ShowFragment
    - 메인화면에서 RecyclerView의 항목 클릭시 그 항목의 세부 정보를 표시

    
  


