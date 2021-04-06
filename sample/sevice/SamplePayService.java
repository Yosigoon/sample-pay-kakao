package com.dalbit.pay.sevice;

import com.dalbit.common.service.CommonService;
import com.dalbit.common.vo.P_ErrorLogVo;
import com.dalbit.exception.GlobalException;
import com.dalbit.pay.dao.SamplePayDao;
import com.dalbit.pay.vo.StoreVo;
import com.dalbit.pay.vo.procedure.KakaoPayApproveVo;
import com.dalbit.pay.vo.procedure.PayInfoVo;
import com.dalbit.pay.vo.request.PayCheckVo;
import com.dalbit.util.DalbitUtil;
import com.dalbit.util.OkHttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class SamplePayService {

	@Autowired
	SamplePayDao samplePayDao;
	@Autowired
	CommonService commonService;

	/**
	 * 결제요청
	 */
	public int payInfoInsert(PayInfoVo payInfoVo) {
		return samplePayDao.payInfoInsert(payInfoVo);
	}


	/**
	 * 결제실패 업데이트
	 */
	public int failUpdate(String tradeid) {
		return samplePayDao.failUpdate(tradeid);
    }


	/**
	 * 첫 결제 여부 조회
	 */
    public int selectFirstPayInfo(String memNo) {
    	return samplePayDao.selectFirstPayInfo(memNo);
    }


	/**
	 * 결제요청 검증
	 */
	public PayCheckVo payCheck(PayCheckVo payCheckVo) throws GlobalException, IOException {
		// API 스토어 조회
		String storeUrl = DalbitUtil.getProperty("server.api.url")+"/paycall/store";
		RequestBody formBody = new FormBody.Builder()
				.add("os", String.valueOf(payCheckVo.getOs()))
				.add("itemNo", payCheckVo.getItemNo())
				.build();
		OkHttpClientUtil okHttpClientUtil = new OkHttpClientUtil();
		Response response = okHttpClientUtil.sendPost(storeUrl, formBody);
		String data = response.body().string();
		StoreVo itemInfo = new Gson().fromJson(new Gson().fromJson(data, JsonObject.class).get("data"), StoreVo.class);
		payCheckVo.setStoreVo(itemInfo);
		P_ErrorLogVo errorLogVo = new P_ErrorLogVo();
		errorLogVo.setMem_no(payCheckVo.getMemNo());
		errorLogVo.setOs("Pay");
		errorLogVo.setVersion("");

		if(DalbitUtil.isEmpty(itemInfo)){
			errorLogVo.setDtype("itemInfo error");
			errorLogVo.setCtype("아이템 정보 없음");
			commonService.saveErrorLog(errorLogVo);
			payCheckVo.setValid(false);
			payCheckVo.setMsgCode("itemInfo");

		//스토어 조회 결제상품코드와 아이템가격 일치여부 체크
		}else if (!(payCheckVo.getItemNo().equals(itemInfo.getItemNo()) && payCheckVo.getPrice() == Integer.parseInt(itemInfo.getSalePrice()) * payCheckVo.getItemCnt())) {
			log.error("결제 금액 상이 - payCheckVo : {} | itemInfo : {}", payCheckVo, itemInfo);
			errorLogVo.setDtype("price error");
			errorLogVo.setCtype("결제 금액 상이");
			commonService.saveErrorLog(errorLogVo);
			payCheckVo.setValid(false);
			payCheckVo.setMsgCode("price");
		}

		return payCheckVo;
	}

	/**
	 *	카카오페이 tid 가져오기
	 */
    public KakaoPayApproveVo getTidInfo(KakaoPayApproveVo kakaoPayApproveVo) {
		return samplePayDao.getTidInfo(kakaoPayApproveVo);
    }

	/**
	 * 현재 보유달 가져오기
	 */
	public int getCurrentTotalDal(String memNo) {
		return samplePayDao.getCurrentTotalDal(memNo);
    }
}