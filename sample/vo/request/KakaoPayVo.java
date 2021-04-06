package com.dalbit.pay.vo.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter @Setter @ToString
public class KakaoPayVo {
    @NotBlank
    private String Prdtnm;
    @NotBlank
    private String Prdtprice;
    @NotBlank
    private String itemNo;
    @NotBlank
    private String pageCode;

    private int itemAmt;
}
