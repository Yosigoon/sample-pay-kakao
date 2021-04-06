package com.dalbit.pay.vo.kakao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class AmountVo {
    private Integer total, tax_free, vat, point, discount;
}
