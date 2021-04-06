package com.dalbit.pay.vo.request;

import com.dalbit.pay.vo.StoreVo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter  @ToString
public class PayCheckVo {

    private StoreVo storeVo;
    private boolean isValid = true;
    private String msgCode;
    private int os;
    private String orderId;
    private String memNo;   //회원번호
    private String itemNo;  //아이템코드
    private int price;      //가격
    private int itemCnt;    //수량


}
