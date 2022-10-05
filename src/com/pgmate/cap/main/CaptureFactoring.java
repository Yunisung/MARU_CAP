package com.pgmate.cap.main;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pgmate.lib.util.lang.CommonUtil;
import com.pgmate.lib.util.map.SharedMap;

/**
 * @author Administrator
 *
 */
public class CaptureFactoring {
	private static Logger logger = LoggerFactory.getLogger(com.pgmate.cap.main.CaptureFactoring.class);
	private TrxDAO trxDAO = null;
	private TrxBatchDAO trxBatchDAO = null;
	private List<SharedMap<String,Object>> insertCapList = new ArrayList<SharedMap<String,Object>>();
	private List<SharedMap<String,Object>> insertCapDtlList = new ArrayList<SharedMap<String,Object>>();
	private List<String> warningList = new ArrayList<String>();
	private List<String[]> riskList  = new ArrayList<String[]>();
	
	
	public CaptureFactoring() {
		this.trxDAO = new TrxDAO();
		this.trxBatchDAO = new TrxBatchDAO();
		
	}
	
	public void start(){
		
		List<SharedMap<String,Object>> trxPayList = trxDAO.getTrxPayFactoring();
		int i=1;
		int size = trxPayList.size();
		
		if(size > 0){
			for(SharedMap<String,Object> trxPayMap : trxPayList ){
				logger.info("capture : {}/{}",i++,size);
				capture(trxPayMap);
			}
			
			logger.info("capture inserted : {}",trxBatchDAO.insertTrxCap(insertCapList, insertCapDtlList));
			logger.info("");
		}
		
		insertCapList 		= new ArrayList<SharedMap<String,Object>>();
		insertCapDtlList	= new ArrayList<SharedMap<String,Object>>();
		
		
		List<SharedMap<String,Object>> trxRfdList = trxDAO.getTrxRfdFactoring();
		i=1;
		size = trxRfdList.size();
		if(size > 0){
			for(SharedMap<String,Object> trxRfdMap : trxRfdList ){
				logger.info("refund  : {}/{}",i++,size);
				refund(trxRfdMap);
			}
			logger.info("refund inserted : {}",trxBatchDAO.insertTrxCap(insertCapList, insertCapDtlList));
		}
		
		if(warningList.size() > 0){
			for(String trxId : warningList ){
				logger.info("warning  : {}/{},{}",i++,size,trxId);
				warning(trxId);
			}
		}
		
		if(riskList.size() > 0){
			logger.info("risk iqr  : {}",size);
			trxBatchDAO.insertRisk(riskList);
			
		}
		
		insertCapList 		= null;
		insertCapDtlList	= null;
		warningList 		= null;
		riskList			= null;
		
		
		
		
	}
	
	
	public void capture(SharedMap<String,Object> trxPayMap){
		SharedMap<String,Object> mchtMap		= trxDAO.getMchtByMchtId(trxPayMap.getString("mchtId"));
		SharedMap<String,Object> mchtTmnMap		= trxDAO.getMchtTmnByTmnId(trxPayMap.getString("tmnId"));
		SharedMap<String,Object> mchtMngMap		= trxDAO.getMchtMngByMchtId(mchtMap.getString("mchtId"));
		SharedMap<String,Object> agencyMngMap	= trxDAO.getAgencyMngById(mchtMap.getString("agencyId"));

		
		SharedMap<String,Object> trxCapMap = new SharedMap<String,Object>();
		
		trxCapMap.put("capId"		, trxDAO.getCapId());
		trxCapMap.put("trxId"		, trxPayMap.getString("trxId"));
		trxCapMap.put("mchtId"		, trxPayMap.getString("mchtId"));
		trxCapMap.put("tmnId"		, trxPayMap.getString("tmnId"));
		trxCapMap.put("trackId"		, trxPayMap.getString("trackId"));
		trxCapMap.put("capType"		, "매입");
		trxCapMap.put("rfdType"		, "");
		trxCapMap.put("rootTrxId"	, "");
		trxCapMap.put("rootTrxDay"	, "");
		trxCapMap.put("amount"		, trxPayMap.getLong("amount"));
		trxCapMap.put("installment"	, trxPayMap.getString("installment"));
		trxCapMap.put("vat"			, calcRootVat(trxPayMap.getLong("amount")));
		trxCapMap.put("cardId"		, trxPayMap.getString("cardId"));
		trxCapMap.put("cardType"	, trxPayMap.getString("cardType"));
		trxCapMap.put("bin"			, trxPayMap.getString("bin"));
		trxCapMap.put("last4"		, trxPayMap.getString("last4"));
		trxCapMap.put("issuer"		, trxPayMap.getString("issuer"));
		trxCapMap.put("acquirer"	, trxPayMap.getString("acquirer"));
		trxCapMap.put("authCd"		, trxPayMap.getString("authCd"));
		trxCapMap.put("trxDay"		, trxPayMap.getString("regDay"));
		trxCapMap.put("regDay"		, trxPayMap.getString("regDay"));
		trxCapMap.put("regTime"		, trxPayMap.getString("regTime"));
		trxCapMap.put("regDate"		, CommonUtil.getCurrentTimestamp());
		
		logger.info("capture capId : {},{}",trxCapMap.getString("capId"),trxPayMap.getString("trxId"));
		//기본 매입 정로 리스트
		insertCapList.add(trxCapMap);
		
		
		SharedMap<String,Object> capDtlMap = new SharedMap<String,Object>();
		
		
		//중복 거래 조회
		capDtlMap.put("risk", "");
		
		//24시간 중복 > 야간 할부 > 주간 할부 > 야간 건한도 > 주간 건한도 > 위험(100만원) > 최소금액 (1005원 미만) > 고액  (300만원)
		//1일 중복 거래 
		String dup = trxDAO.getDuplicatedFactDaily(trxCapMap);
		if(!dup.equals("")){
			capDtlMap.put("risk",dup);
		}
		//중복거래
		dup = trxDAO.getDuplicatedFact(trxCapMap);
		if(!dup.equals("")){
			capDtlMap.put("risk",dup);
		}
		
		//야간할부 거래 
		if(trxCapMap.getLong("regTime") < 60000 && trxCapMap.getLong("installment") > 0){
			capDtlMap.put("risk","야간할부");
		}
		
		//주간할부 거래 
		if(trxCapMap.getLong("regTime") >= 60000 && trxCapMap.getLong("installment") > 6){
			capDtlMap.put("risk","주간할부");
		}
		
		/* 22.10.05 야간, 주간 건 한도 분리 주석 처리
		//야간 건 한도
		if(trxCapMap.getLong("regTime") < 60000 && trxCapMap.getLong("amount") > 300000){
			capDtlMap.put("risk","야간건한도");
		}
		
		//주간 건 한도 
		if(trxCapMap.getLong("regTime") >= 60000 && trxCapMap.getLong("amount") >= mchtMngMap.getLong("limitOnce")){
			capDtlMap.put("risk","건한도");
		}
		*/
		
		//위험,중복
		if(!trxCapMap.isNullOrSpace("bin") && !trxCapMap.isNullOrSpace("last4")){
			//위험 100 만원 이상 거래
			String capId = trxDAO.getWarningFact(trxPayMap);
			if(!capId.equals("")){
				warningList.add(capId);
				capDtlMap.put("risk"	,"위험");
			}
		}
		
		//기본 리스크 반영 최소금액/건한도/고액
		String riskStatus = calcRiskStatus(trxCapMap.getLong("amount"),mchtMngMap);
		if(!riskStatus.equals("")){
			capDtlMap.put("risk", riskStatus);
		}
		
		
		if(!capDtlMap.isNullOrSpace("risk")){
			logger.info("capId : {}, risk : {}",trxCapMap.getString("capId"),capDtlMap.getString("risk"));
			String[] riskData = {trxCapMap.getString("capId"),trxCapMap.getString("capId")+":RISK 설정 ,D+1 "+capDtlMap.getString("risk")+",수수료:"+(capDtlMap.getLong("stlFee")+capDtlMap.getLong("stlFeeVat"))};
			riskList.add(riskData);
		}
		
		capDtlMap.put("capId"		, trxCapMap.getString("capId"));
		capDtlMap.put("stlStatus"	, "정산대기");
		capDtlMap.put("stlLoanRate", mchtMngMap.getDouble("loanRate"));
		capDtlMap.put("stlType"     , "D+1");
		capDtlMap.put("stlDay"		, calcDay(capDtlMap.getString("stlType"), trxCapMap.getString("trxDay")));	
	
		capDtlMap.put("van"			, trxPayMap.getString("van"));
		capDtlMap.put("vanId"		, trxPayMap.getString("vanId"));
		capDtlMap.put("vanTrxId"	, trxPayMap.getString("vanTrxId"));
		capDtlMap.put("vanStatus"	, "입금대기");
		capDtlMap.put("stlVanRate"	, mchtMngMap.getDouble("rate"));		//가맹점 수수료가 원가 
		capDtlMap.put("stlVanDay"	, calcDay(mchtMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
		
		capDtlMap.put("stlAgencyDay", calcDay(agencyMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
		capDtlMap.put("stlAgencyId"	, "");
		capDtlMap.put("stlAgencyFee", 0);
		capDtlMap.put("stlAgencyRate",capDtlMap.getDouble("stlLoanRate")*mchtMngMap.getDouble("agencyRate"));
		
		if(capDtlMap.isNullOrSpace("risk")){	//RISK가 없는 경우는 선지급 수수료를 청구한다.
			capDtlMap.put("stlRate"		, mchtMngMap.getDouble("rate")+capDtlMap.getDouble("stlLoanRate"));
		}else{ 								
			capDtlMap.put("stlRate"		, mchtMngMap.getDouble("rate"));
		}
		
		if(trxPayMap.getString("van").equals("DANAL")){
			long stlVanFee				= calcDanalFee(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate"));
			capDtlMap.put("stlVanFee"	, stlVanFee + calcDanalVat(stlVanFee));
		}else if(trxPayMap.getString("van").startsWith("DAOU") || trxPayMap.getString("van").equals("IDM")
				|| trxPayMap.getString("van").startsWith("WOORIPAY")){
			long stlVanFee				= calcRoundUpFee(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate"));
			capDtlMap.put("stlVanFee"	, stlVanFee + calcRoundUpVat(stlVanFee));
		}else if(trxPayMap.getString("van").equals("PAYNURIOFF")){
			double vanRate 				= Double.parseDouble(String.format("%.4f", capDtlMap.getDouble("stlVanRate")*1.1));	//부가세 적용
			long stlVanFee				= (long)(Math.floor(trxCapMap.getLong("amount")*vanRate)) ;							//소수점 이하 절삭
			capDtlMap.put("stlVanFee"	, stlVanFee);
		}else if(trxPayMap.getString("van").equals("PAYNURION")){
			double vanRate 				= Double.parseDouble(String.format("%.4f", capDtlMap.getDouble("stlVanRate")*1.1));	//부가세 적용
			long stlVanFee				= calcRoundUpFee(trxCapMap.getLong("amount"),vanRate);								////소수점 이하 반올림 
			capDtlMap.put("stlVanFee"	, stlVanFee);
		}else{
			long stlVanFee				= calcRoundTrimFee(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate"));
			capDtlMap.put("stlVanFee"	, stlVanFee + calcRoundTrimVat(stlVanFee));
			
		}
		capDtlMap.put("stlFee"		, calcFee(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlRate")));
		capDtlMap.put("stlFeeVat"	, calcVat(capDtlMap.getLong("stlFee")));
		
		capDtlMap.put("stlAmount"	, trxCapMap.getLong("amount")-capDtlMap.getLong("stlFee")-capDtlMap.getLong("stlFeeVat"));
		capDtlMap.put("payOutDay"	, "");
		capDtlMap.put("stlId"		, "");
		

		if(capDtlMap.isNullOrSpace("risk")){
			capDtlMap.put("stlAgencyFee", calcFeeVat(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlAgencyRate")));
		}
		
		
		capDtlMap.put("benefit"		, capDtlMap.getLong("stlFee")+capDtlMap.getLong("stlFeeVat")-capDtlMap.getLong("stlAgencyFee")-capDtlMap.getLong("stlVanFee"));
		capDtlMap.put("taxId"		, mchtTmnMap.getString("taxId"));
		
		insertCapDtlList.add(capDtlMap);
		
	}
	
	
	public void refund(SharedMap<String,Object> trxRfdMap){
		
		SharedMap<String,Object> rootCapMap	 	= trxDAO.getTrxCap(trxRfdMap.getString("rootTrxId"));
		if(rootCapMap == null){
			//검색된 매입내역이 없으면 리턴한다.
			return;
		}
		
		SharedMap<String,Object> mchtMap		= trxDAO.getMchtByMchtId(trxRfdMap.getString("mchtId"));
		SharedMap<String,Object> mchtMngMap		= trxDAO.getMchtMngByMchtId(mchtMap.getString("mchtId"));
		SharedMap<String,Object> agencyMngMap	= trxDAO.getAgencyMngById(mchtMap.getString("agencyId"));
		
		//정산대기 이면서 리스크가 있었던 거래는 다시 선지급 수수료를 청구한다.
		if(rootCapMap.getString("stlStatus").equals("정산대기") && rootCapMap.getString("stlType").equals("D+1") && !rootCapMap.isNullOrSpace("risk")){
			SharedMap<String,Object> updateMap	= new SharedMap<String,Object>();
			//거래 정보를 원복한다.
			updateMap.put("stlRate"		, rootCapMap.getDouble("stlRate")+rootCapMap.getDouble("stlLoanRate"));			//선정산 수수료 적용
			updateMap.put("stlFee"		, calcFee(rootCapMap.getLong("amount"), updateMap.getDouble("stlRate")));
			updateMap.put("stlFeeVat"	, calcVat(updateMap.getLong("stlFee")));
			updateMap.put("stlAgencyFee", calcFeeVat(rootCapMap.getLong("amount"),rootCapMap.getDouble("stlAgencyRate")));
			updateMap.put("stlAmount"	, rootCapMap.getLong("amount")-updateMap.getLong("stlFee")-updateMap.getLong("stlFeeVat"));
			updateMap.put("risk", "");
			updateMap.put("capId", rootCapMap.getString("capId"));
			
			trxDAO.updateTrxCapDtlWithAgency(updateMap);
			String[] riskData = {updateMap.getString("capId"),updateMap.getString("capId")+":RISK 해지 ,D+1 ,거래취소,수수료:"+(rootCapMap.getLong("stlFee")+rootCapMap.getLong("stlFeeVat")) +"->"+(updateMap.getLong("stlFee")+updateMap.getLong("stlFeeVat"))};
			
			riskList.add(riskData);
			rootCapMap = trxDAO.getTrxCap(trxRfdMap.getString("rootTrxId"));
		}
		
		
		
		SharedMap<String,Object> trxCapMap = new SharedMap<String,Object>();
		
		trxCapMap.put("capId"		, trxDAO.getCapId());
		trxCapMap.put("trxId"		, trxRfdMap.getString("trxId"));
		trxCapMap.put("mchtId"		, trxRfdMap.getString("mchtId"));
		trxCapMap.put("tmnId"		, trxRfdMap.getString("tmnId"));
		trxCapMap.put("trackId"		, trxRfdMap.getString("trackId"));
		trxCapMap.put("capType"		, "매입취소");
		trxCapMap.put("rfdType"		, trxRfdMap.getString("rfdAll"));
		trxCapMap.put("rootTrxId"	, rootCapMap.getString("capId"));
		trxCapMap.put("rootTrxDay"	, rootCapMap.getString("trxDay"));
		trxCapMap.put("amount"		, trxRfdMap.getLong("rfdAmount"));
		trxCapMap.put("installment"	, rootCapMap.getString("installment"));
		trxCapMap.put("vat"			, trxRfdMap.getLong("rfdVat"));
		trxCapMap.put("cardId"		, trxRfdMap.getString("cardId"));
		trxCapMap.put("cardType"	, rootCapMap.getString("cardType"));
		trxCapMap.put("bin"			, trxRfdMap.getString("bin"));
		trxCapMap.put("last4"		, trxRfdMap.getString("last4"));
		trxCapMap.put("issuer"		, trxRfdMap.getString("issuer"));
		trxCapMap.put("acquirer"	, trxRfdMap.getString("acquirer"));
		trxCapMap.put("authCd"		, trxRfdMap.getString("authCd"));
		trxCapMap.put("trxDay"		, trxRfdMap.getString("regDay"));
		trxCapMap.put("regDay"		, trxRfdMap.getString("regDay"));
		trxCapMap.put("regTime"		, trxRfdMap.getString("regTime"));
		trxCapMap.put("regDate"		, CommonUtil.getCurrentTimestamp());
		
		logger.info("refund capId : {},{}",trxCapMap.getString("capId"),trxRfdMap.getString("trxId"));
		//기본 매입 정로 리스트
		insertCapList.add(trxCapMap);
		
		SharedMap<String,Object> capDtlMap = new SharedMap<String,Object>();
		
		capDtlMap.put("capId"		, trxCapMap.getString("capId"));
		capDtlMap.put("stlStatus"	, "정산대기");
		capDtlMap.put("stlRate"		, rootCapMap.getDouble("stlRate"));	
		capDtlMap.put("stlLoanRate" , rootCapMap.getDouble("stlLoanRate"));
		capDtlMap.put("stlType"     , rootCapMap.getString("stlType"));
		capDtlMap.put("stlDay"		, calcDay(capDtlMap.getString("stlType"), trxCapMap.getString("trxDay")));
		
		capDtlMap.put("van"			, trxRfdMap.getString("van"));
		capDtlMap.put("vanId"		, trxRfdMap.getString("vanId"));
		capDtlMap.put("vanTrxId"	, trxRfdMap.getString("vanTrxId"));
		capDtlMap.put("vanStatus"	, "입금대기");
		capDtlMap.put("stlVanRate"	, rootCapMap.getDouble("stlVanRate"));		//가맹점 수수료가 원가 
		capDtlMap.put("stlVanDay"	, calcDay(mchtMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
		capDtlMap.put("stlAgencyRate", rootCapMap.getDouble("stlAgencyRate"));		//에이전트 수수료
		capDtlMap.put("stlAgencyDay", calcDay(agencyMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
		
		if(trxRfdMap.getString("van").equals("DANAL")){
			long stlVanFee				= calcDanalFee(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate"));
			capDtlMap.put("stlVanFee"	, stlVanFee + calcDanalVat(stlVanFee));
		}else if(trxRfdMap.getString("van").startsWith("DAOU") || trxRfdMap.getString("van").equals("IDM")
				|| trxRfdMap.getString("van").equals("WOORIPAY")){
			long stlVanFee				= calcRoundUpFee(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate"));
			capDtlMap.put("stlVanFee"	, stlVanFee + calcRoundUpVat(stlVanFee));
		}else if(trxRfdMap.getString("van").equals("PAYNURIOFF")){
			double vanRate 				= Double.parseDouble(String.format("%.4f", capDtlMap.getDouble("stlVanRate")*1.1));//부가세 적용
			long stlVanFee				= -(long)(Math.floor(-trxCapMap.getLong("amount")*vanRate)) ;	//소수점 이하 절삭 
			capDtlMap.put("stlVanFee"	, stlVanFee);
		}else if(trxRfdMap.getString("van").equals("PAYNURION")){
			double vanRate 				= Double.parseDouble(String.format("%.4f", capDtlMap.getDouble("stlVanRate")*1.1));	//부가세 적용
			long stlVanFee				= calcRoundUpFee(trxCapMap.getLong("amount"),vanRate);								////소수점 이하 반올림 
			capDtlMap.put("stlVanFee"	, stlVanFee);
		}else{
			long stlVanFee				= calcRoundTrimFee(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate"));
			capDtlMap.put("stlVanFee"	, stlVanFee + calcRoundTrimVat(stlVanFee));
		}
		
		capDtlMap.put("stlFee"		, calcFee(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlRate")));
		capDtlMap.put("stlFeeVat"	, calcVat(capDtlMap.getLong("stlFee")));
		capDtlMap.put("stlAgencyFee", calcFeeVat(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlAgencyRate")));
		
		capDtlMap.put("stlAmount"	, trxCapMap.getLong("amount")-capDtlMap.getLong("stlFee")-capDtlMap.getLong("stlFeeVat"));
		
		
		capDtlMap.put("payOutDay"	, "");
		capDtlMap.put("stlId"		, "");
		
		
		capDtlMap.put("benefit"		, capDtlMap.getLong("stlFee")+capDtlMap.getLong("stlFeeVat")-capDtlMap.getLong("stlDistFee")-capDtlMap.getLong("stlAgencyFee")-capDtlMap.getLong("stlVanFee"));
		capDtlMap.put("taxId"		, rootCapMap.getString("taxId"));
		capDtlMap.put("risk"		, "");
		

		
		insertCapDtlList.add(capDtlMap);

	}
	
	
	
	/**
	 * 위험거래로 변경한다.
	 * @param capId
	 */
	public void warning(String trxId){
		SharedMap<String,Object> rootCapMap	 	= trxDAO.getTrxCap(trxId);
		if(rootCapMap == null){
			//검색된 매입내역이 없으면 리턴한다.
			return;
		}
		
		if(rootCapMap.getString("stlStatus").equals("정산대기") && rootCapMap.getString("stlType").equals("D+1") ){
			if(rootCapMap.isNullOrSpace("risk")){	//원거래가 리스크가 아닌 경우  
				SharedMap<String,Object> updateMap	= new SharedMap<String,Object>();
				
				//거래 정보를 원복한다.
				updateMap.put("stlRate"		, rootCapMap.getDouble("stlRate")-rootCapMap.getDouble("stlLoanRate"));			//선정산 수수료 적용
				updateMap.put("stlFee"		, calcFee(rootCapMap.getLong("amount"), updateMap.getDouble("stlRate")));
				updateMap.put("stlFeeVat"	, calcVat(updateMap.getLong("stlFee")));
				updateMap.put("stlAgencyFee", 0);
				updateMap.put("stlAmount"	, rootCapMap.getLong("amount")-updateMap.getLong("stlFee")-updateMap.getLong("stlFeeVat"));
				updateMap.put("risk", "위험");
				updateMap.put("capId", rootCapMap.getString("capId"));
				
				trxDAO.updateTrxCapDtlWithAgency(updateMap);
				
				String[] riskData = {updateMap.getString("capId"),updateMap.getString("capId")+":RISK 설정 ,D+1 ,위험,수수료:"+(rootCapMap.getLong("stlFee")+rootCapMap.getLong("stlFeeVat")) +"->"+(updateMap.getLong("stlFee")+updateMap.getLong("stlFeeVat") )};
				riskList.add(riskData);
			}else{
				String[] riskData = {rootCapMap.getString("capId"),rootCapMap.getString("capId")+":RISK '위험'으로 감지되나 이미 RISK로 설정된 거래 원 RISK : "+rootCapMap.getString("risk")};
				riskList.add(riskData);
			}
		}
		
	}
	
	
	
	public long calcFee(long amount,double rate){
		rate = rateFormat(rate);
		long decimal = 10000;
		long fee = 0;
		if(amount < 0){
			fee = -new Double(Math.round(-amount*(rate *decimal))).longValue()/decimal;
		}else{
			fee = new Double(Math.round(amount*(rate *decimal))).longValue()/decimal;
		}
		return fee;
	}
	
	public long calcVat(long fee){
		if(fee < 0){
			return -new Double(-fee *10 /100).longValue();
		}else{
			return new Double(fee *10 /100).longValue();
		}
	}
	
	public long calcFeeVat(long amount,double rate){
		long fee = calcFee(amount,rate);
		return fee+ calcVat(fee);
	}
	
	
	public long calcRootVat(long amount){
		if(amount < 0){
			return -new Double(-amount *10 /110).longValue();
		}else{
			return new Double(amount *10 /110).longValue();
		}
	}
	
	public long calcRoundTrimFee(long amount,double rate){
		rate = rateFormat(rate);
		if(amount < 0){
			return -new Double(-amount*rate).longValue();
		}else{
			return new Double(amount*rate).longValue();
		}
	}
	
	public long calcRoundTrimVat(long fee){
		if(fee < 0){
			return -new Double(-fee*0.1).longValue();
		}else{
			return new Double(fee*0.1).longValue();
		}
	}
	
	public long calcRoundUpFee(long amount,double rate){
		rate = rateFormat(rate);
		if(amount < 0){
			return -Math.round(-amount*rate);
		}else{
			return Math.round(amount*rate);
		}
	}
	
	public long calcRoundUpVat(long fee){
		
		if(fee < 0){
			return -Math.round(-fee*0.1);
		}else{
			return Math.round(fee*0.1);
		}
	}
	
	public long calcRoundUpFeeVat(long amount,double rate){
		long fee = calcRoundUpFee(amount,rate);
		return fee + calcRoundUpVat(fee);
	}
	
	
	
	
	public long calcDanalFee(long amount,double rate){
		rate = rateFormat(rate);
		long decimal = 1000;
		if(amount < 0){
			return -new Double(-amount*(rate *decimal)).longValue()/decimal;
		}else{
			return new Double(amount*(rate *decimal)).longValue()/decimal;
		}
	}
	
	public long calcDanalVat(long fee){
		if(fee < 0){
			return -new Double(-fee *10 /100).longValue();
		}else{
			return new Double(fee *10 /100).longValue();
		}
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
	
	
	public String calcRiskStatus(long amount,SharedMap<String,Object> mchtMngMap){
		if(amount < 1005){
			return "최소금액";
		}
		if(mchtMngMap.getLong("limitOnce") !=0 &&  mchtMngMap.getLong("limitOnce") <=  amount){
			return "건한도";
		}
		if(mchtMngMap.getLong("largeAmount") !=0 && mchtMngMap.getLong("largeAmount") <=  amount){
			return "고액";
		}
		
		return "";
	}
	
	public double rateFormat(double rate){
		String pattern = "#.#####";
		DecimalFormat format = new DecimalFormat(pattern);
		return new Double(format.format(rate)).doubleValue();
	}
	
	
	public String calcDay(String settleType,String today){
		int term = 1;
		if(settleType.startsWith("D")){
			term = CommonUtil.parseInt(settleType.replaceAll("D[+]", ""));
			String day =  trxDAO.getSettleDay(today, term);
			/*
			//오늘 정산 예정일이지만 8시 이후에 요청된 거래는 자동으로 내일로 정산일정이 밀린다.
			if(day.equals(currentDay) && CommonUtil.parseInt(CommonUtil.getCurrentDate("HH")) > 8){
				day =  trxDAO.getSettleDay(currentDay,1);
			}*/
			return day;
		}else if(settleType.startsWith("M")){
			term = CommonUtil.parseInt(settleType.replaceAll("M[+]", ""));
			String nextMonth = CommonUtil.getOpDate(GregorianCalendar.MONTH,1,today).substring(0,6);
			return trxDAO.getSettleDay(nextMonth+CommonUtil.zerofill(term,2));
		}else{
			/*
			term = CommonUtil.parseInt(settleType.replaceAll("D[+]", ""));
			String day =  trxDAO.getSettleDay(today, term);
			/*
			if(day.equals(currentDay) && CommonUtil.parseInt(CommonUtil.getCurrentDate("HH")) > 8){
				day =  trxDAO.getSettleDay(currentDay,1);
			}*/
			return "";
		}
	}
	
	public static LocalDate getToday(String today){
		return LocalDate.parse(today, DateTimeFormatter.ofPattern("yyyyMMdd"));
		
	}
	
	
	public static void main(String[] args){
		CaptureFactoring c = new CaptureFactoring();
		long fee = c.calcRoundTrimFee(25000, 0.036);
		System.out.println(fee);
		System.out.println(c.calcRoundTrimVat(fee));
		System.out.println(25000*0.036);
		
	}
	
	
	

	
	

}

