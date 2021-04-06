package com.dalbit.pay.controller.rest;

import com.dalbit.exception.GlobalException;
import com.dalbit.pay.sevice.SampleKakaoPayService;
import com.dalbit.pay.vo.request.KakaoPayVo;
import com.dalbit.util.DalbitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/rest/pay")
public class SampleKakaoPayRestController {

    @Autowired
    SampleKakaoPayService sampleKakaoPayService;


    /**
     * 카카오페이 결제준비
     */
    @PostMapping("/kakao/ready")
    public String kakaoPayReady(KakaoPayVo kakaoPayVo, BindingResult bindingResult, HttpServletRequest request) throws GlobalException, IOException {
        DalbitUtil.throwValidaionException(bindingResult);
        String result = sampleKakaoPayService.kakaoPayReady(kakaoPayVo, request);
        return result;
    }

}

