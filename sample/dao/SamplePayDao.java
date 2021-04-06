package com.dalbit.pay.dao;

import com.dalbit.pay.vo.procedure.KakaoPayApproveVo;
import com.dalbit.pay.vo.procedure.KakaoPayUpdateVo;
import com.dalbit.pay.vo.procedure.PayInfoVo;
import org.springframework.stereotype.Repository;

@Repository
public interface SamplePayDao {
    int payInfoInsert(PayInfoVo payInfoVo);
    KakaoPayApproveVo getTidInfo(KakaoPayApproveVo kakaoPayApproveVo);
    int kakaoPayUpdateVo(KakaoPayUpdateVo kakaoPayUpdateVo);
    int failUpdate(String tradeid);
    int getCurrentTotalDal(String memNo);
    int selectFirstPayInfo(String memNo);
}

