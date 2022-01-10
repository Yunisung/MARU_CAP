package com.pgmate.cap.main;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pgmate.lib.util.lang.CommonUtil;
import com.pgmate.lib.util.map.SharedMap;

/**
 * @author Administrator
 *
 */
public class CapturePhone {
	private static Logger logger = LoggerFactory.getLogger(com.pgmate.cap.main.CapturePhone.class);
	private TrxDAO trxDAO = null;
	private TrxBatchDAO trxBatchDAO = null;
	private List<SharedMap<String,Object>> insertCapList = new ArrayList<SharedMap<String,Object>>();
	
	public CapturePhone() {
		this.trxDAO = new TrxDAO();
		this.trxBatchDAO = new TrxBatchDAO();
	}
	
	public void start(){
		List<SharedMap<String,Object>> phonePayList = trxDAO.getPhonePay();
		int i=1;
		int size = phonePayList.size();
		
		if(size > 0){
			for(SharedMap<String,Object> phonePaytMap : phonePayList ){
				logger.info("CapturePhone : {}/{}",i++,size);
				CapturePhone(phonePaytMap);
			}
			logger.info("CapturePhone inserted : {}",trxBatchDAO.insertPhoneCap(insertCapList));
		}
		
		insertCapList 		= new ArrayList<SharedMap<String,Object>>();
		
		List<SharedMap<String,Object>> trxRfdList = trxDAO.getPhoneRfd();
		i=1;
		size = trxRfdList.size();
		if(size > 0){
			for(SharedMap<String,Object> trxRfdMap : trxRfdList ){
				logger.info("CapturePhone refund  : {}/{}",i++,size);
				refund(trxRfdMap);
			}
			logger.info("CapturePhone refund inserted : {}",trxBatchDAO.insertPhoneCap(insertCapList));
		}
		
		insertCapList 		= null;
	}
	
	/**
	 * 결제된 휴대폰 거래 건을 PG_PHONE_CAP테이블(휴대폰 결제 매입내역)에 맞게 세팅
	 * @param phonePaytMap
	 */
	public void CapturePhone(SharedMap<String,Object> phonePaytMap){
		SharedMap<String,Object> mchtMap		= trxDAO.getMchtByMchtId(phonePaytMap.getString("mchtId"));
		SharedMap<String,Object> mchtTmnMap		= trxDAO.getMchtTmnByTmnId(phonePaytMap.getString("tmnId"));
		SharedMap<String,Object> mchtPhoneMngMap= trxDAO.getMchtPhoneMngByMchtId(mchtMap.getString("mchtId"));
		SharedMap<String,Object> agencyMngMap	= trxDAO.getAgencyMngById(mchtMap.getString("agencyId"));
		SharedMap<String,Object> distMngMap		= trxDAO.getDistMngById(mchtMap.getString("distId"));
		SharedMap<String,Object> salesMngMap	= trxDAO.getSalesMngById(mchtMap.getString("salesId"));
		SharedMap<String,Object> orgFeeMap		= trxDAO.getOrgFee(mchtTmnMap.getString("van"));
		SharedMap<String,Object> vanMap			= trxDAO.getVanByVanId(phonePaytMap.getString("vanId"));
		
		logger.info("vanId : [{}]", phonePaytMap.getString("vanId"));
		logger.info("settleType : [{}]", vanMap.getString("settleType"));
		
		SharedMap<String,Object> phoneCapMap = new SharedMap<String,Object>();
		
		phoneCapMap.put("capId"				, trxDAO.getCapId());
		phoneCapMap.put("trxId"				, phonePaytMap.getString("trxId"));
		phoneCapMap.put("mchtId"			, phonePaytMap.getString("mchtId"));
		phoneCapMap.put("tmnId"				, phonePaytMap.getString("tmnId"));
		phoneCapMap.put("taxId"				, mchtTmnMap.getString("taxId"));
		phoneCapMap.put("trackId"			, phonePaytMap.getString("trackId"));
		phoneCapMap.put("trxDay"			, phonePaytMap.getString("reqDay"));
		phoneCapMap.put("stlStatus"			, "정산대기");
		phoneCapMap.put("capType"			, "매입");
		phoneCapMap.put("rfdType"			, "");
		phoneCapMap.put("rootTrxId"			, "");
		phoneCapMap.put("rootTrxDay"		, "");
		phoneCapMap.put("amount"			, phonePaytMap.getLong("amount"));
		phoneCapMap.put("vat"				, calcRootVat(phonePaytMap.getLong("amount")));
		phoneCapMap.put("stlRate"			, mchtPhoneMngMap.getDouble("rate"));
		phoneCapMap.put("stlFee"			, calcFee(phoneCapMap.getLong("amount"), phoneCapMap.getDouble("stlRate")));
		phoneCapMap.put("stlFeeVat"			, calcVat(phoneCapMap.getLong("stlFee")));
		phoneCapMap.put("stlAmount"			, phoneCapMap.getLong("amount")-phoneCapMap.getLong("stlFee")-phoneCapMap.getLong("stlFeeVat"));
		phoneCapMap.put("stlType"   		, vanMap.getString("settleType"));
		phoneCapMap.put("stlDay"			, calcDay(phoneCapMap.getString("stlType"), phoneCapMap.getString("trxDay")));		
		phoneCapMap.put("payOutDay"			, "");
		phoneCapMap.put("stlId"				, "");
		 
		if(agencyMngMap.size() >0 ){
			phoneCapMap.put("stlAgencyRate" , mchtPhoneMngMap.getDouble("rate")-mchtPhoneMngMap.getDouble("agencyRate"));
			phoneCapMap.put("stlAgencyFee"  , calcFeeVat(phoneCapMap.getLong("amount"), phoneCapMap.getDouble("stlAgencyRate")));
			phoneCapMap.put("stlAgencyDay"  , calcDay(agencyMngMap.getString("settleType"),phoneCapMap.getString("trxDay")));
			phoneCapMap.put("stlAgencyId"   , "");
		}
		
		if(salesMngMap.size() >0 ){
			phoneCapMap.put("stlSalesRate"  , mchtPhoneMngMap.getDouble("salesRate"));
			phoneCapMap.put("stlSalesFee"	, calcFee(phoneCapMap.getLong("stlAgencyFee"), phoneCapMap.getDouble("stlSalesRate")));
			phoneCapMap.put("stlSalesDay"	, calcDay(salesMngMap.getString("settleType"),phoneCapMap.getString("trxDay")));
			phoneCapMap.put("stlSalesId"	, "");
		}
		
		if(distMngMap.size() >0 ){
			phoneCapMap.put("stlDistRate"	, mchtPhoneMngMap.getDouble("agencyRate")-mchtPhoneMngMap.getDouble("distRate"));
			if(phoneCapMap.getDouble("stlDistRate") < 0){
				phoneCapMap.put("stlDistFee", 0);
			}else{
				phoneCapMap.put("stlDistFee", calcFeeVat(phoneCapMap.getLong("amount"), phoneCapMap.getDouble("stlDistRate")));
			}
			phoneCapMap.put("stlDistDay"	, calcDay(distMngMap.getString("settleType"),phoneCapMap.getString("trxDay")));
			phoneCapMap.put("stlDistId"		, "");
		}
		
		// 에이전시 최종 수수료 : 에이전시 수수료 - 지사 수수료
		phoneCapMap.put("stlAgencyFee"		, phoneCapMap.getLong("stlAgencyFee")-phoneCapMap.getLong("stlSalesFee"));
		
		if(orgFeeMap.size() >0){
			phoneCapMap.put("stlVanRate"	, orgFeeMap.getDouble("phoneRate"));
			phoneCapMap.put("stlVanFee"		, calcDefaultFee(phoneCapMap.getLong("amount"),phoneCapMap.getDouble("stlVanRate")));
			phoneCapMap.put("benefit"		, phoneCapMap.getLong("stlFee")+phoneCapMap.getLong("stlFeeVat")-phoneCapMap.getLong("stlDistFee")-phoneCapMap.getLong("stlAgencyFee")-phoneCapMap.getLong("stlSalesFee")-phoneCapMap.getLong("stlVanFee"));
		}
		
		phoneCapMap.put("van"				, phonePaytMap.getString("van"));
		phoneCapMap.put("vanId"				, phonePaytMap.getString("vanId"));
		phoneCapMap.put("vanTrxId"			, phonePaytMap.getString("vanTrxId"));	
		phoneCapMap.put("stlYn"			, "N");
		phoneCapMap.put("regDay"			, phonePaytMap.getString("regDay"));
		phoneCapMap.put("regTime"			, phonePaytMap.getString("regTime"));
		phoneCapMap.put("regDate"			, CommonUtil.getCurrentTimestamp());
		
		logger.info("CapturePhone capId : {},{}",phoneCapMap.getString("capId"),phonePaytMap.getString("trxId"));
		//기본 매입 정로 리스트
		insertCapList.add(phoneCapMap);
	}
	
	/**
	 * 취소된 휴대폰 거래 건을 PG_PHONE_CAP테이블(휴대폰 결제 매입내역)에 맞게 세팅
	 * @param trxRfdMap
	 */
	public void refund(SharedMap<String,Object> trxRfdMap){
		
		SharedMap<String,Object> rootCapMap	 	= trxDAO.getPhoneCap(trxRfdMap.getString("rootTrxId"));
		if(rootCapMap == null){
			//검색된 매입내역이 없으면 리턴한다.
			return;
		}
		
		SharedMap<String,Object> mchtMap		= trxDAO.getMchtByMchtId(trxRfdMap.getString("mchtId"));
		SharedMap<String,Object> mchtPhoneMngMap= trxDAO.getMchtPhoneMngByMchtId(mchtMap.getString("mchtId"));
		SharedMap<String,Object> agencyMngMap	= trxDAO.getAgencyMngById(mchtMap.getString("agencyId"));
		SharedMap<String,Object> distMngMap		= trxDAO.getDistMngById(mchtMap.getString("distId"));
		SharedMap<String,Object> salesMngMap	= trxDAO.getSalesMngById(mchtMap.getString("salesId"));
		SharedMap<String,Object> orgFeeMap		= trxDAO.getOrgFee(trxRfdMap.getString("van"));

		SharedMap<String,Object> phoneCapMap = new SharedMap<String,Object>();
		
		phoneCapMap.put("capId"				, trxDAO.getCapId());
		phoneCapMap.put("trxId"				, trxRfdMap.getString("trxId"));
		phoneCapMap.put("mchtId"			, trxRfdMap.getString("mchtId"));
		phoneCapMap.put("tmnId"				, trxRfdMap.getString("tmnId"));
		phoneCapMap.put("taxId"				, rootCapMap.getString("taxId"));
		phoneCapMap.put("trackId"			, trxRfdMap.getString("trackId"));
		phoneCapMap.put("trxDay"			, trxRfdMap.getString("regDay"));
		phoneCapMap.put("stlStatus"			, "정산대기");
		phoneCapMap.put("capType"			, "매입취소");
		phoneCapMap.put("rfdType"			, "전체");
		phoneCapMap.put("rootTrxId"			, rootCapMap.getString("trxId"));
		phoneCapMap.put("rootTrxDay"		, rootCapMap.getString("trxDay"));
		phoneCapMap.put("amount"			, trxRfdMap.getLong("rfdAmount"));
		phoneCapMap.put("vat"				, calcRootVat(trxRfdMap.getLong("rfdAmount")));
		phoneCapMap.put("stlRate"			, rootCapMap.getDouble("stlRate"));	
		phoneCapMap.put("stlFee"			, calcFee(phoneCapMap.getLong("amount"), phoneCapMap.getDouble("stlRate")));
		phoneCapMap.put("stlFeeVat"			, calcVat(phoneCapMap.getLong("stlFee")));
		phoneCapMap.put("stlAmount"			, phoneCapMap.getLong("amount")-phoneCapMap.getLong("stlFee")-phoneCapMap.getLong("stlFeeVat"));
		phoneCapMap.put("stlType"			, rootCapMap.getString("stlType"));
		phoneCapMap.put("stlDay"			, calcDay(phoneCapMap.getString("stlType"), phoneCapMap.getString("trxDay")));
		phoneCapMap.put("stlId"				, "");
		phoneCapMap.put("payOutDay"			, "");
		
		if(agencyMngMap.size() >0 ){
			phoneCapMap.put("stlAgencyRate" , mchtPhoneMngMap.getDouble("rate")-mchtPhoneMngMap.getDouble("agencyRate"));
			phoneCapMap.put("stlAgencyFee"  , calcFeeVat(phoneCapMap.getLong("amount"), phoneCapMap.getDouble("stlAgencyRate")));
			phoneCapMap.put("stlAgencyDay"  , calcDay(agencyMngMap.getString("settleType"),phoneCapMap.getString("trxDay")));
			phoneCapMap.put("stlAgencyId"   , "");
		}
		
		if(salesMngMap.size() >0 ){
			phoneCapMap.put("stlSalesRate"  , mchtPhoneMngMap.getDouble("salesRate"));
			phoneCapMap.put("stlSalesFee"	, calcFee(phoneCapMap.getLong("stlAgencyFee"), phoneCapMap.getDouble("stlSalesRate")));
			phoneCapMap.put("stlSalesDay"	, calcDay(salesMngMap.getString("settleType"),phoneCapMap.getString("trxDay")));
			phoneCapMap.put("stlSalesId"	, "");
		}
		
		if(distMngMap.size() >0 ){
			phoneCapMap.put("stlDistRate"	, mchtPhoneMngMap.getDouble("agencyRate")-mchtPhoneMngMap.getDouble("distRate"));
			if(phoneCapMap.getDouble("stlDistRate") < 0){
				phoneCapMap.put("stlDistFee", 0);
			}else{
				phoneCapMap.put("stlDistFee", calcFeeVat(phoneCapMap.getLong("amount"), phoneCapMap.getDouble("stlDistRate")));
			}
			phoneCapMap.put("stlDistDay"	, calcDay(distMngMap.getString("settleType"),phoneCapMap.getString("trxDay")));
			phoneCapMap.put("stlDistId"		, "");
		}
		
		// 에이전시 최종 수수료 : 에이전시 수수료 - 지사 수수료
		phoneCapMap.put("stlAgencyFee"		, phoneCapMap.getLong("stlAgencyFee")-phoneCapMap.getLong("stlSalesFee"));
		
		if(orgFeeMap.size() >0){
			phoneCapMap.put("stlVanRate"	, orgFeeMap.getDouble("phoneRate"));
			phoneCapMap.put("stlVanFee"		, calcDefaultFee(phoneCapMap.getLong("amount"),phoneCapMap.getDouble("stlVanRate")));
			phoneCapMap.put("benefit"		, phoneCapMap.getLong("stlFee")+phoneCapMap.getLong("stlFeeVat")-phoneCapMap.getLong("stlDistFee")-phoneCapMap.getLong("stlAgencyFee")-phoneCapMap.getLong("stlSalesFee")-phoneCapMap.getLong("stlVanFee"));
		}
		
		phoneCapMap.put("van"				, trxRfdMap.getString("van"));
		phoneCapMap.put("vanId"				, trxRfdMap.getString("vanId"));
		phoneCapMap.put("vanTrxId"			, trxRfdMap.getString("vanTrxId"));
		
		if("1".equals(rootCapMap.getString("stlType"))) {
			phoneCapMap.put("stlYn"			, "N");
		}else {
			phoneCapMap.put("stlYn"			, "Y");
		}
		
		phoneCapMap.put("regDay"			, trxRfdMap.getString("regDay"));
		phoneCapMap.put("regTime"			, trxRfdMap.getString("regTime"));
		phoneCapMap.put("regDate"			, CommonUtil.getCurrentTimestamp());

		logger.info("CapturePhone Refund capId : {},{}",phoneCapMap.getString("capId"),trxRfdMap.getString("trxId"));
		//기본 매입 정로 리스트
		insertCapList.add(phoneCapMap);
	}
	
	public long calcFee(long amount,double rate){
		rate = rateFormat(rate);
		long decimal = 10000;
		if(amount < 0){
			return -new Double(Math.round(-amount*(rate *decimal))).longValue()/decimal;
		}else{
			return new Double(Math.round(amount*(rate *decimal))).longValue()/decimal;
		}
	}
	
	public long calcRootVat(long amount){
		if(amount < 0){
			return -new Double(-amount *10 /110).longValue();
		}else{
			return new Double(amount *10 /110).longValue();
		}
	}
	
	public long calcVat(long amount){
		if(amount < 0){
			return -new Double(-amount *10 /100).longValue();
		}else{
			return new Double(amount *10 /100).longValue();
		}
	}
	
	public long calcFeeVat(long amount,double rate){
		rate = rateFormat(rate);
		long decimal = 10000;
		long fee = 0;
		if(amount < 0){
			fee = -new Double(Math.round(-amount*(rate *decimal))).longValue()/decimal;
		}else{
			fee = new Double(Math.round(amount*(rate *decimal))).longValue()/decimal;
		}
		long vat = calcVat(fee);
		return fee+vat;
	}
	
	public long calcDefaultFee(long amount,double rate){
		long decimal = 1000;
		long fee = 0;
		if(amount < 0){
			fee = -new Double(-amount*(rate *decimal)).longValue()/decimal;
		}else{
			fee = new Double(amount*(rate *decimal)).longValue()/decimal;
		}
		
		return fee;
	}
	
	public long calcDefaultFeeVat(long amount,double rate){
		long decimal = 1000;
		long fee = 0;
		if(amount < 0){
			fee = -new Double(-amount*(rate *decimal)).longValue()/decimal;
		}else{
			fee = new Double(amount*(rate *decimal)).longValue()/decimal;
		}
		long vat = calcVat(fee);
		return fee+vat;
	}
	
	public long calcDanalFeeVat(long amount,double rate){
		long decimal = 10000;
		long fee = 0;
		rate = rate*1.1;
		if(amount < 0){
			fee = -new Double(-amount*(rate *decimal)).longValue()/decimal;
		}else{
			fee = new Double(amount*(rate *decimal)).longValue()/decimal;
		}
		
		return fee;
	}
	
	public long calcRoundUpFeeVat(long amount,double rate){
		long fee = 0;
		long vat = 0;
		if(amount < 0){
			fee = -Math.round(-amount*rate);
			vat = -Math.round(-fee*0.1);
		}else{
			fee = Math.round(amount*rate);
			vat = Math.round(fee*0.1);
		}
		
		return fee+vat;
	}
	
	public long calcRoundTrimFeeVat(long amount,double rate){
		long fee = 0;
		long vat = 0;
		if(amount < 0){
			fee = -new Double(-amount*rate).longValue();
			vat =-new Double(-fee*0.1).longValue();
		}else{
			fee = new Double(amount*rate).longValue();
			vat =new Double(fee*0.1).longValue();
		}
		
		return fee+vat;
	}
	
	
	public long calcKsnetFee(long amount,double rate){
		long fee = 0;
		if(amount < 0){
			fee = -Math.round(-amount*rate);
		}else{
			fee = Math.round(amount*rate);
		}
		
		return fee;
	}
	
	public long calcKsnetFeeVat(long amount,double rate){
		long fee = 0;
		if(amount < 0){
			fee = -Math.round(-amount*rate);
		}else{
			fee = Math.round(amount*rate);
		}
		long vat = calcKsnetVat(fee);
		
		return fee+vat;
	}
	
	public long calcKsnetVat(long amount){
		if(amount < 0){
			return -Math.round(-amount*0.1);
		}else{
			return Math.round(amount*0.1);
		}
	}
	
	public long calcNiceFeeVat(long amount,double rate){
		long fee = 0;
		long vat = 0;
		if(amount < 0){
			fee = -(long)Math.ceil(-amount*rate);
			vat = -(long)Math.floor(-vat*0.1);
		}else{
			fee = (long)Math.ceil(amount*rate);
			vat = (long)Math.floor(fee*0.1);
		}
		return fee+vat;
	}
	
	public double rateFormat(double rate){
		String pattern = "#.#####";
		DecimalFormat format = new DecimalFormat(pattern);
		return new Double(format.format(rate)).doubleValue();
	}
	
	public String calcDay(String settleType,String today){
		String nextDate = "";
		String dateFormat = "yyyyMMdd";

		if(today.length() < 8) {
			return nextDate;
		}

		int year = Integer.parseInt(today.substring(0,4));
		int month = Integer.parseInt(today.substring(4,6));
		int day = Integer.parseInt(today.substring(6,8));
		
		Calendar cal = Calendar.getInstance();
		cal.set(year, month-1, day);
		
		if("0".equals(settleType)){
			//주정산
			cal.add(Calendar.DATE, 7);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
			nextDate = new SimpleDateFormat(dateFormat).format(cal.getTime());
		}else if("1".equals(settleType)){
			nextDate = "";
		}
		
		logger.info("nextDate : " + nextDate);
				
		return nextDate;
	}
	
	public static LocalDate getToday(String today){
		return LocalDate.parse(today, DateTimeFormatter.ofPattern("yyyyMMdd"));
	}
	
	public static void main(String[] args){
		double a = 0.027;
		double b = a*1.1;
		
		double c = Double.parseDouble(String.format("%.4f", b));
		long d = (long)(Math.floor(31313.6333));
		System.out.println(c);
		System.out.println(d);
	}
}

