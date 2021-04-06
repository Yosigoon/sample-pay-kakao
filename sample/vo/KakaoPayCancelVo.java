package com.dalbit.pay.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class KakaoPayCancelVo {
    private String orderId;
    private String memNo;
    private String tid;
    private Integer cancel_amount;
    private Integer cancel_tax_free_amount;
}
