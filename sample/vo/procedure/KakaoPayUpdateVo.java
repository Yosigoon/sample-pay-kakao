package com.dalbit.pay.vo.procedure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class KakaoPayUpdateVo {
    private String orderId;
    private String payOkDate;
    private String payOkTime;
    private String billId;
    private String autoBillKey="";
    private String autoYn="n";
    private int dalCnt;
    private String firstPayYn;
    private String aid;
}
