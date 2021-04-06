package com.dalbit.pay.vo.procedure;

import com.dalbit.pay.vo.kakao.FailVo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class KakaoPayApproveVo extends FailVo {
    private String partner_order_id;
    private String partner_user_id;
    private String pg_token;
    private String total_amount;

    private String tid;
    private int dalCnt;
    private int itemAmt;
    private int os;
    private String isHybrid;
    private String gubun;
    private String pageCode;
    private String itemNo;

}
