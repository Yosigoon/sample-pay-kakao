package com.dalbit.pay.controller;

import com.dalbit.common.code.Status;
import com.dalbit.common.vo.JsonOutputVo;
import com.dalbit.exception.GlobalException;
import com.dalbit.pay.sevice.SampleKakaoPayService;
import com.dalbit.pay.sevice.SamplePayService;
import com.dalbit.pay.vo.procedure.KakaoPayApproveVo;
import com.dalbit.util.DalbitUtil;
import com.dalbit.util.GsonUtil;
import com.dalbit.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class SampleKakaoPayController {

    @Autowired
    SampleKakaoPayService sampleKakaoPayService;
    @Autowired
    SamplePayService samplePayService;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    GsonUtil gsonUtil;


    /**
     * 카카오 테스트페이지
     */
    @GetMapping("/kakao")
    public String kakao(){
        return "/kakao/kakaoTest";
    }


    /**
     * 카카오페이 결제승인
     */
    @GetMapping("/kakao/approve")
    public String kakaoPayApprove(KakaoPayApproveVo kakaoPayApproveVo, BindingResult bindingResult, Model model) throws GlobalException{
        DalbitUtil.throwValidaionException(bindingResult);
        String result = sampleKakaoPayService.kakaoPayApprove(kakaoPayApproveVo);
        model.addAttribute("data", result);
        return "/kakao/kakaopay";
    }


    /**
     * 카카오페이 실패
     */
    @GetMapping("/kakao/fail")
    public String kakaoFail(KakaoPayApproveVo kakaoPayApproveVo){

        int failUpdate = samplePayService.failUpdate(kakaoPayApproveVo.getPartner_order_id());
        if(failUpdate > 0){
            log.info("[카카오페이] 결제실패 업데이트 확인 order_id: {}", kakaoPayApproveVo.getPartner_order_id());
        }
        log.error("[카카오페이] 결제 승인 실패: {}", kakaoPayApproveVo.getExtras().getMethod_result_message());
        log.error("[카카오페이] 결제 승인 실패 order_id: {}, memNo: {}", kakaoPayApproveVo.getPartner_order_id(), kakaoPayApproveVo.getPartner_user_id());

        return gsonUtil.toJson(new JsonOutputVo(Status.결제실패, kakaoPayApproveVo));
    }
}

