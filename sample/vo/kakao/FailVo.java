package com.dalbit.pay.vo.kakao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class FailVo {
    private int code;
    private String msg;
    private ExtrasVo extras;
}
