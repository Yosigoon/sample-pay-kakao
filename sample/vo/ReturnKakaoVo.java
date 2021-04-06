package com.dalbit.pay.vo;

import com.dalbit.pay.vo.kakao.KakaoPayApprovalVo;
import com.dalbit.pay.vo.procedure.KakaoPayApproveVo;
import com.dalbit.util.DalbitUtil;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReturnKakaoVo {

    public ReturnKakaoVo(){}
    public ReturnKakaoVo(KakaoPayApprovalVo kakaoPayApprovalVo, KakaoPayApproveVo target){
        setOrder_no(kakaoPayApprovalVo.getPartner_order_id());
        setProduct_name(kakaoPayApprovalVo.getItem_name());
        setAmount(String.valueOf(kakaoPayApprovalVo.getAmount().getTotal()));
        setTransaction_date(kakaoPayApprovalVo.getApproved_at().replace("T", " "));
        setOs(String.valueOf(target.getOs()));
        setIsHybrid(target.getIsHybrid());
        setGubun(target.getGubun());
        setItemCnt(target.getItemAmt());
        setDalCnt(target.getDalCnt());
        setPageCode(target.getPageCode());
    }

    private String order_no;
    private String product_name;
    private String amount;
    //private String pay_info="";
    private String transaction_date;
    private String os;
    private String isHybrid;
    private String pageCode;
    private String gubun;
    private int itemCnt;
    private int dalCnt;
}
