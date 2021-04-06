package com.dalbit.pay.vo.procedure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class PurchaseDalVo {

    private String mem_no;
    private String os;
    private String itemCode;
    private String itemPrice;
    private int itemCnt;
    private String order_id;
    //private int gainDal;
}
