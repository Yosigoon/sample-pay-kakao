package com.dalbit.pay.sevice;

import com.dalbit.common.code.Status;
import com.dalbit.common.service.CommonService;
import com.dalbit.common.vo.DeviceVo;
import com.dalbit.common.vo.JsonOutputVo;
import com.dalbit.common.vo.TokenVo;
import com.dalbit.exception.GlobalException;
import com.dalbit.pay.dao.SamplePayDao;
import com.dalbit.pay.vo.KakaoPayCancelVo;
import com.dalbit.pay.vo.ReturnKakaoVo;
import com.dalbit.pay.vo.StoreVo;
import com.dalbit.pay.vo.kakao.FailVo;
import com.dalbit.pay.vo.kakao.KakaoPayApprovalVo;
import com.dalbit.pay.vo.kakao.KakaoPayReadyVo;
import com.dalbit.pay.vo.procedure.KakaoPayApproveVo;
import com.dalbit.pay.vo.procedure.KakaoPayUpdateVo;
import com.dalbit.pay.vo.procedure.PayInfoVo;
import com.dalbit.pay.vo.procedure.PurchaseDalVo;
import com.dalbit.pay.vo.request.KakaoPayVo;
import com.dalbit.pay.vo.request.PayCheckVo;
import com.dalbit.util.DalbitUtil;
import com.dalbit.util.GsonUtil;
import com.dalbit.util.JwtUtil;
import com.dalbit.util.OkHttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

@Slf4j
@Service
public class SampleKakaoPayService {

	@Autowired
	SamplePayDao samplePayDao;
	@Autowired
	GsonUtil gsonUtil;
	@Autowired
	JwtUtil jwtUtil;
	@Autowired
	SamplePayService samplePayService;
	@Autowired
	PayCancelService payCancelService;
	@Autowired
	CommonService commonService;

	/**
	 * 카카오페이 결제준비
	 */
	public String kakaoPayReady(KakaoPayVo kakaoPayVo, HttpServletRequest request) throws GlobalException, IOException {
		String customHeader = request.getHeader(DalbitUtil.getProperty("rest.custom.header.name"));
		customHeader = java.net.URLDecoder.decode(customHeader);
		HashMap<String, Object> headers = new Gson().fromJson(customHeader, HashMap.class);
		int os = DalbitUtil.getIntMap(headers,"os");
		String isHybrid = DalbitUtil.getStringMap(headers,"isHybrid");
		isHybrid = DalbitUtil.isEmpty(isHybrid) ? "N" : isHybrid;

		String authToken = request.getHeader("authToken");
		TokenVo tokenVo = jwtUtil.getTokenVoFromJwt(authToken);

		log.info("authToken: {}", authToken);
		log.info("memNo: {}", tokenVo.getMemNo());

		//비회원일 경우
		if(tokenVo.getMemNo().startsWith("8")){
			return gsonUtil.toJson(new JsonOutputVo(Status.로그인필요));
		}

		int itemCnt = kakaoPayVo.getItemAmt() == 0 ? 1 : kakaoPayVo.getItemAmt();
		String orderId = DalbitUtil.getTradeId(DalbitUtil.getProperty("kakao.service.id"));
		String startUrl = request.getHeader("referer");
		String gubun = "pc";
		if(startUrl.startsWith("https://m.") || startUrl.startsWith("https://devm.") || startUrl.startsWith("https://devm2.")){
			gubun = "mobile";
		}

		String cancel_url = "";
		if(isHybrid.equals("Y") || startUrl.startsWith("https://m.") || startUrl.startsWith("https://devm.") || startUrl.startsWith("https://devm2.")){
			cancel_url = (kakaoPayVo.getPageCode().equals("1") || kakaoPayVo.getPageCode().equals("3")) ? DalbitUtil.getProperty("server.mobile.url")+"/pay?canceltype=store" : DalbitUtil.getProperty("server.mobile.url")+"/pay?canceltype=room&webview=new";
		} else {
			cancel_url = DalbitUtil.getProperty("server.www.url")+"/pay?result=cancel";
		}

		// 서버로 요청할 Body
		RequestBody kakaoFormBody = new FormBody.Builder()
				.add("cid", DalbitUtil.getProperty("kakao.cid"))
				.add("partner_order_id", orderId)
				.add("partner_user_id", tokenVo.getMemNo())
				.add("item_name", kakaoPayVo.getPrdtnm() + " x " + itemCnt)
				.add("quantity", String.valueOf(kakaoPayVo.getItemAmt()))
				.add("total_amount", kakaoPayVo.getPrdtprice())
				.add("tax_free_amount", "0")
				.add("approval_url", DalbitUtil.getProperty("server.pay.url")+"/kakao/approve?partner_order_id="+orderId+"&partner_user_id="+tokenVo.getMemNo()+"&total_amount="+kakaoPayVo.getPrdtprice()+"&isHybrid="+isHybrid+"&gubun="+gubun+"&pageCode="+kakaoPayVo.getPageCode()+"&itemNo="+kakaoPayVo.getItemNo()+"&itemAmt="+kakaoPayVo.getItemAmt())
				.add("cancel_url", cancel_url)
				.add("fail_url", DalbitUtil.getProperty("server.pay.url")+"/kakao/fail")
				.build();
		OkHttpClientUtil okHttpClientUtilKakao = new OkHttpClientUtil();
		Response responseKakao = okHttpClientUtilKakao.sendKakaoPost(DalbitUtil.getProperty("kakao.host") + "/v1/payment/ready", kakaoFormBody);
		String kakaoData = responseKakao.body().string();
		String result;
		try {
			KakaoPayReadyVo kakaoPayReadyVo = new Gson().fromJson(kakaoData, KakaoPayReadyVo.class);
			log.info("kakaoPayReadyVo: " + kakaoPayReadyVo);

			//결제정보 저장
			PayInfoVo payInfoVo = new PayInfoVo();
			payInfoVo.setOrderId(orderId);
			payInfoVo.setMemNo(tokenVo.getMemNo());
			payInfoVo.setPayWay("kakaoMoney");
			payInfoVo.setPayDtComein(DalbitUtil.convertCalendarDateFormat(kakaoPayReadyVo.getCreated_at(), "yyyy-MM-dd HH:mm:ss"));
			payInfoVo.setPayAmt(Integer.valueOf(kakaoPayVo.getPrdtprice()));
			payInfoVo.setPayCode(kakaoPayVo.getPrdtnm());
			payInfoVo.setPayIp(DalbitUtil.getIp(request));
			payInfoVo.setLoginMedia(isHybrid.equals("N") ? String.valueOf(os) : "4");
			payInfoVo.setAppVer(new DeviceVo(request).getAppVersion());
			payInfoVo.setOs(os);
			payInfoVo.setItemCode(kakaoPayVo.getItemNo());
			payInfoVo.setItemAmt(itemCnt);
			payInfoVo.setBillId(kakaoPayReadyVo.getTid());

			// API 스토어 조회
			String storeUrl = DalbitUtil.getProperty("server.api.url")+"/paycall/store";
			RequestBody formBody = new FormBody.Builder()
					.add("os", String.valueOf(os))
					.add("itemNo", kakaoPayVo.getItemNo())
					.build();
			OkHttpClientUtil okHttpClientUtil = new OkHttpClientUtil();
			Response response = okHttpClientUtil.sendPost(storeUrl, formBody);
			String data = response.body().string();
			StoreVo itemInfo = new Gson().fromJson(new Gson().fromJson(data, JsonObject.class).get("data"), StoreVo.class);

			if(DalbitUtil.isEmpty(itemInfo)){
				log.info("[카카오페이] 아이템 정보 없음: {}", gsonUtil.toJson(new JsonOutputVo(Status.결제_아이템정보없음)));
				return gsonUtil.toJson(new JsonOutputVo(Status.결제_아이템정보없음, kakaoPayVo.getItemNo()));
			}

			//지급 달 수 세팅 저장
			payInfoVo.setDalCnt(itemInfo.getGivenDal() * itemCnt);

			log.info("결제정보 저장: {}", payInfoVo.toString());

			int success = samplePayService.payInfoInsert(payInfoVo);
			if(success > 0){
				result = gsonUtil.toJson(new JsonOutputVo(Status.결제요청, kakaoPayReadyVo));
			}else {
				result = gsonUtil.toJson(new JsonOutputVo(Status.결제정보저장실패));
			}

		} catch (RestClientException e) {
			e.printStackTrace();
			result = gsonUtil.toJson(new JsonOutputVo(Status.결제정보저장실패));
		} catch (IOException e) {
			e.printStackTrace();
			result = gsonUtil.toJson(new JsonOutputVo(Status.결제정보저장실패));
		}
		return result;
	}


	/**
	 * 카카오페이 결제승인
	 */
	public String kakaoPayApprove(KakaoPayApproveVo kakaoPayApproveVo) throws GlobalException {
		log.info("kakaoPayApproveVo: {}", kakaoPayApproveVo);

		//DB TID 검즘
		KakaoPayApproveVo payInfoVo = samplePayService.getTidInfo(kakaoPayApproveVo);
		payInfoVo.setGubun(kakaoPayApproveVo.getGubun());
		payInfoVo.setPageCode(kakaoPayApproveVo.getPageCode());
		payInfoVo.setIsHybrid(kakaoPayApproveVo.getIsHybrid());

		String result = "";
		try {
			// 서버로 요청할 Body
			RequestBody formBody = new FormBody.Builder()
					.add("cid", DalbitUtil.getProperty("kakao.cid"))
					.add("tid", payInfoVo.getTid())
					.add("partner_order_id", kakaoPayApproveVo.getPartner_order_id())
					.add("partner_user_id", kakaoPayApproveVo.getPartner_user_id())
					.add("pg_token", kakaoPayApproveVo.getPg_token())
					.add("total_amount", kakaoPayApproveVo.getTotal_amount())
					.build();

			OkHttpClientUtil okHttpClientUtil = new OkHttpClientUtil();
			Response response = okHttpClientUtil.sendKakaoPost(DalbitUtil.getProperty("kakao.host") + "/v1/payment/approve", formBody);
			String data = response.body().string();

			if(response.code() == 200){
				KakaoPayApprovalVo kakaoPayApprovalVo = new Gson().fromJson(data, KakaoPayApprovalVo.class);

				//API스토어 & 요청수량과 요청금액 DB 검증
				PayCheckVo payCheckVo = new PayCheckVo();
				payCheckVo.setMemNo(kakaoPayApproveVo.getPartner_user_id());
				payCheckVo.setOs(kakaoPayApproveVo.getOs());
				payCheckVo.setItemNo(kakaoPayApproveVo.getItemNo());
				payCheckVo.setItemCnt(kakaoPayApproveVo.getItemAmt());
				payCheckVo.setPrice(Integer.parseInt(kakaoPayApproveVo.getTotal_amount()));
				PayCheckVo resultPayCheckVo = samplePayService.payCheck(payCheckVo);
				if(!resultPayCheckVo.isValid()){
					if("itemInfo".equals(resultPayCheckVo.getMsgCode()) || "price".equals(resultPayCheckVo.getMsgCode())){
						//검증 오류시 결제 자동취소
						try{
							KakaoPayCancelVo kakaoPayCancelVo = new KakaoPayCancelVo();
							kakaoPayCancelVo.setTid(payInfoVo.getTid());
							kakaoPayCancelVo.setCancel_amount(Integer.parseInt(kakaoPayApproveVo.getTotal_amount()));
							kakaoPayCancelVo.setOrderId(kakaoPayApproveVo.getPartner_order_id());

							payCancelService.payCancelKakaoPay(kakaoPayCancelVo);
						}catch (Exception e){
							log.error("결제취소 에러 resultPayCheckVo:{}, kakaoPayApproveVo: {}", resultPayCheckVo, kakaoPayApproveVo);
						}

						if("itemInfo".equals(resultPayCheckVo.getMsgCode())){
							return gsonUtil.toJson(new JsonOutputVo(Status.결제_아이템정보없음, kakaoPayApproveVo));
						}else{
							return gsonUtil.toJson(new JsonOutputVo(Status.결제금액불일치, kakaoPayApproveVo));
						}
					}else{
						return gsonUtil.toJson(new JsonOutputVo(Status.결제실패, kakaoPayApproveVo));
					}
				}

				KakaoPayUpdateVo kakaoPayUpdateVo = new KakaoPayUpdateVo();
				kakaoPayUpdateVo.setOrderId(kakaoPayApproveVo.getPartner_order_id());
				kakaoPayUpdateVo.setPayOkDate(DalbitUtil.stringToDatePattern(kakaoPayApprovalVo.getApproved_at().replace("T", " "), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"));
				kakaoPayUpdateVo.setPayOkTime(DalbitUtil.stringToDatePattern(kakaoPayApprovalVo.getApproved_at().replace("T", " "), "yyyy-MM-dd HH:mm:ss", "HH:mm:ss"));
				kakaoPayUpdateVo.setAid(kakaoPayApprovalVo.getAid());
				//첫 결제 여부 조회
				int isFirst = samplePayService.selectFirstPayInfo(kakaoPayApproveVo.getPartner_user_id());
				if(isFirst > 0){
					kakaoPayUpdateVo.setFirstPayYn("n");
				} else {
					kakaoPayUpdateVo.setFirstPayYn("y");
				}

				//카카오페이(money) 결제정보 업이트
				log.info("[카카오페이(money)] 결제정보 업데이트: {}", kakaoPayUpdateVo.toString());
				kakaoPayUpdateVo(kakaoPayUpdateVo);

				//회원 달 구매 업데이트
				PurchaseDalVo apiData = new PurchaseDalVo();
				apiData.setMem_no(kakaoPayApproveVo.getPartner_user_id());
				apiData.setOs(String.valueOf(kakaoPayApproveVo.getOs()));
				apiData.setItemCode(resultPayCheckVo.getStoreVo().getItemNo());
				apiData.setItemPrice(resultPayCheckVo.getStoreVo().getSalePrice());
				apiData.setItemCnt(kakaoPayApproveVo.getItemAmt());
				apiData.setOrder_id(kakaoPayApproveVo.getPartner_order_id());

				String totalMyDalCnt = commonService.purchaseDal(apiData);
				payInfoVo.setDalCnt(Integer.parseInt(totalMyDalCnt));

				result = gsonUtil.toJson(new JsonOutputVo(Status.결제성공, new ReturnKakaoVo(kakaoPayApprovalVo, payInfoVo)));
			}else{
				FailVo failVo = new Gson().fromJson(data, FailVo.class);
				int failUpdate =samplePayService.failUpdate(kakaoPayApproveVo.getPartner_order_id());
				if(failUpdate > 0){
					log.info("[카카오페이] 결제실패 업데이트 확인 order_id: {}", kakaoPayApproveVo.getPartner_order_id());
				}
				log.error("[카카오페이] 결제 승인 실패: {}", failVo.getExtras().getMethod_result_message());
				log.error("[카카오페이] 결제 승인 실패 order_id: {}, memNo: {}", kakaoPayApproveVo.getPartner_order_id(), kakaoPayApproveVo.getPartner_user_id());
				return gsonUtil.toJson(new JsonOutputVo(Status.결제실패, failVo));
			}
		} catch (RestClientException | IOException | ParseException e) {
			e.printStackTrace();
			return gsonUtil.toJson(new JsonOutputVo(Status.결제실패));
		}

		return result;
	}


	/**
	 * 카카오페이(money) 결제정보 업이트
	 */
	public int kakaoPayUpdateVo(KakaoPayUpdateVo kakaoPayUpdateVo) {
		return samplePayDao.kakaoPayUpdateVo(kakaoPayUpdateVo);
	}


}