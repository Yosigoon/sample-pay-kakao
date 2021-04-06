package com.dalbit.pay.vo.procedure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;

@Getter @Setter @ToString
public class PayInfoVo {

    private String orderId;
    private String memNo;
    private String memSex;
    private String memAge;
    private String memSlct;
    private String payWay;
    private String payDtComein;
    private String payYn;
    private String payOkDate;
    private String payOkTime;
    private String paySlct;
    private int payAmt;
    private int itemAmt;
    private String payCode;
    private String payInfo="";
    private String payIp;
    private String loginMedia;
    private String appVer;
    private String firstPayYn;
    private String billId;
    private String interest;
    private String cardNo;
    private String cardNm;
    private String cardCode;
    private String apprNo;
    private String autoBillKey="";
    private String phoneNo="";
    private String autoYn="n";
    private String ezKey;
    private String commId;
    private String accountNo="";
    private String bankCode;
    private String rcptDt;
    private String rcptNm="";
    private String serviceId="";
    private int os;
    private String pageCode;
    private String itemCode;
    private int dalCnt;
    private String receiptCode;
    private String receiptPhone;
    private String receiptSocial;
    private String receiptBiz;
    private String receiptStateCode;
    private String payletterToken;
    private String packetState;
}
