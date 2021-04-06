package com.dalbit.pay.vo.kakao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class KakaoPayReadyVo {
    //response
    private String tid;                     //결제 고유 번호, 20자
    private String next_redirect_pc_url;
    private String next_redirect_mobile_url;
    private String next_redirect_app_url;
    private String android_app_scheme;
    private String ios_app_scheme;
    private String created_at;                //결제 준비 요청 시간
}
