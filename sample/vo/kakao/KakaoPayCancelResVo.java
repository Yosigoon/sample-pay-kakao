package com.dalbit.pay.vo.kakao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class KakaoPayCancelResVo {
    private String aid, tid, cid, status, partner_order_id, partner_user_id, payment_method_type, item_name;
    private Integer quantity;
    private AmountVo amount;
    private AmountVo canceled_amount;
    private AmountVo cancel_available_amount;
    private String created_at;
    private String approved_at;
    private String canceled_at;
}
