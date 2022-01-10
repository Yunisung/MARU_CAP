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
import com.pgmate.lib.util.math.RoundUtil;

/**
 * @author Administrator
 *
 */
public class CaptureManual {
	private static Logger logger = LoggerFactory.getLogger(com.pgmate.cap.main.CaptureManual.class);
	private TrxDAO trxDAO = null;
	private TrxBatchDAO trxBatchDAO = null;
	private List<SharedMap<String,Object>> insertCapList = new ArrayList<SharedMap<String,Object>>();
	private List<SharedMap<String,Object>> insertCapDtlList = new ArrayList<SharedMap<String,Object>>();
	private List<SharedMap<String,Object>> insertCapSubList = new ArrayList<SharedMap<String,Object>>();
	private List<String> warningList = new ArrayList<String>();
	private List<String[]> riskList  = new ArrayList<String[]>();
	
	public CaptureManual() {
		this.trxDAO = new TrxDAO();
		this.trxBatchDAO = new TrxBatchDAO();
		//this.trxDAO.setDebug(true);
	}
	
	public void start(String day){
		
		List<SharedMap<String,Object>> trxPayList = trxDAO.getTrxPay(day);
		int i=1;
		int size = trxPayList.size();
		
		if(size > 0){
			for(SharedMap<String,Object> trxPayMap : trxPayList ){
				logger.info("capture : {}/{}",i++,size);
				capture(trxPayMap);
			}
			
			logger.info("capture inserted : {}",trxBatchDAO.insertTrxCap(insertCapList, insertCapDtlList));
			logger.info("capture sub inserted : {}",trxBatchDAO.insertTrxCapSub(insertCapSubList));
			logger.info("");
		}
		
		insertCapList 		= new ArrayList<SharedMap<String,Object>>();
		insertCapDtlList	= new ArrayList<SharedMap<String,Object>>();
		insertCapSubList 	= new ArrayList<SharedMap<String,Object>>();
		
		
		List<SharedMap<String,Object>> trxRfdList = trxDAO.getTrxRfd(day);
		i=1;
		size = trxRfdList.size();
		if(size > 0){
			for(SharedMap<String,Object> trxRfdMap : trxRfdList ){
				logger.info("refund  : {}/{}",i++,size);
				refund(trxRfdMap);
			}
			logger.info("refund inserted : {}",trxBatchDAO.insertTrxCap(insertCapList, insertCapDtlList));
			logger.info("refund sub inserted : {}",trxBatchDAO.insertTrxCapSub(insertCapSubList));
		}
		
		if(warningList.size() > 0){
			for(String capId : warningList ){
				logger.info("warning  : {}/{},{}",i++,size,capId);
				warning(capId);
			}
		}
		
		if(riskList.size() > 0){
			logger.info("risk iqr  : {}",size);
			trxBatchDAO.insertRisk(riskList);
			
		}
		
		insertCapList 		= null;
		insertCapDtlList	= null;
		insertCapSubList 	= null;
		warningList 		= null;
		riskList			= null;
		
		
		
	}
	
	
	public void capture(SharedMap<String,Object> trxPayMap){
		SharedMap<String,Object> mchtMap		= trxDAO.getMchtByMchtId(trxPayMap.getString("mchtId"));
		SharedMap<String,Object> mchtTmnMap		= trxDAO.getMchtTmnByTmnId(trxPayMap.getString("tmnId"));
		SharedMap<String,Object> mchtMngMap		= trxDAO.getMchtMngByMchtId(mchtMap.getString("mchtId"));
		SharedMap<String,Object> agencyMngMap	= trxDAO.getAgencyMngById(mchtMap.getString("agencyId"));
		SharedMap<String,Object> distMngMap		= trxDAO.getDistMngById(mchtMap.getString("distId"));
		SharedMap<String,Object> salesMngMap	= trxDAO.getSalesMngById(mchtMap.getString("salesId"));
		SharedMap<String,Object> orgFeeMap		= trxDAO.getOrgFee(mchtTmnMap.getString("van"));
		
		
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
		if(mchtMngMap.getString("settleType").equals("D+1")){
			//기본 리스크 반영 최소금액/건한도/고액
			capDtlMap.put("risk", calcRiskStatus(trxCapMap.getLong("amount"),mchtMngMap));
			
			//야간할부 거래 
			if(trxCapMap.getLong("regTime") < 60000 && trxCapMap.getLong("installment") > 0){
				capDtlMap.put("risk","야간할부");
			}
			
			//위험,중복
			if(!trxCapMap.isNullOrSpace("bin") && !trxCapMap.isNullOrSpace("last4")){
				//위험 100 만원 이상 거래
				String capId = trxDAO.getWarning(trxPayMap);
				if(!capId.equals("")){
					warningList.add(capId);
					capDtlMap.put("risk"	,"위험");
				}
				
				//중복거래
				String dup = trxDAO.getDuplicated(trxCapMap);
				if(!dup.equals("")){
					capDtlMap.put("risk",dup);
				}
			}
		}
		if(!capDtlMap.isNullOrSpace("risk")){
			logger.info("capId : {}, risk : {}",trxCapMap.getString("capId"),capDtlMap.getString("risk"));
			String[] riskData = {trxCapMap.getString("capId"),trxCapMap.getString("capId")+":RISK 설정 완료, D+1 수수료 변경됨 to "+capDtlMap.getString("risk")};
			riskList.add(riskData);
		}
				
		
		capDtlMap.put("capId"		, trxCapMap.getString("capId"));
		capDtlMap.put("stlStatus"	, "정산대기");
		
		//D+1 정산 주기에 RISK가 없는 경우는 선지급 수수료를 청구한다.
		if(mchtMngMap.getString("settleType").equals("D+1") && capDtlMap.isNullOrSpace("risk")){
			capDtlMap.put("stlRate"		, mchtMngMap.getDouble("rate")+mchtMngMap.getDouble("loanRate"));
		}else{ //D+1 이 아닌 경우 또는 RISK가 있는 경우는 원 수수료를 청구한다.
			capDtlMap.put("stlRate"		, mchtMngMap.getDouble("rate"));
		}
		logger.info("{},{},{}",trxPayMap.getString("mchtId"),mchtMngMap.getDouble("rate"),mchtMngMap.getDouble("loanRate"));
		logger.info("{},{}",trxPayMap.getString("mchtId"),capDtlMap.getDouble("stlRate"));
		
		capDtlMap.put("stlType"     , mchtMngMap.getString("settleType"));
		capDtlMap.put("stlFee"		, calcFee(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlRate")));
		capDtlMap.put("stlFeeVat"	, calcVat(capDtlMap.getLong("stlFee")));
		capDtlMap.put("stlAmount"	, trxCapMap.getLong("amount")-capDtlMap.getLong("stlFee")-capDtlMap.getLong("stlFeeVat"));
		
		capDtlMap.put("stlDay"		, calcDay(capDtlMap.getString("stlType"), trxCapMap.getString("trxDay")));
		capDtlMap.put("payOutDay"	, "");
		capDtlMap.put("stlId"		, "");
		 
		if(agencyMngMap.size() >0 ){
			//리스크로 인한 
			capDtlMap.put("stlAgencyRate", mchtMngMap.getDouble("rate")-mchtMngMap.getDouble("agencyRate"));
			capDtlMap.put("stlAgencyFee", calcFeeVat(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlAgencyRate")));
			capDtlMap.put("stlAgencyDay", calcDay(agencyMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
			capDtlMap.put("stlAgencyId"	, "");
		}
		
		if(salesMngMap.size() >0 ){
			capDtlMap.put("stlSalesRate", salesMngMap.getDouble("rate"));
			capDtlMap.put("stlSalesFee"	, calcFee(capDtlMap.getLong("stlAgencyFee"), capDtlMap.getDouble("stlSalesRate")));
			capDtlMap.put("stlSalesDay"	, calcDay(salesMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
			capDtlMap.put("stlSalesId"	, "");
		}
		
		if(distMngMap.size() >0 ){
			capDtlMap.put("stlDistRate"	, mchtMngMap.getDouble("agencyRate")-mchtMngMap.getDouble("distRate"));
			if(capDtlMap.getDouble("stlDistRate") < 0){
				capDtlMap.put("stlDistFee"	, 0);
			}else{
				capDtlMap.put("stlDistFee"	, calcFeeVat(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlDistRate")));
			}
			capDtlMap.put("stlDistDay"	, calcDay(distMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
			capDtlMap.put("stlDistId"	, "");
		}
		
		capDtlMap.put("van"			, trxPayMap.getString("van"));
		capDtlMap.put("vanId"		, trxPayMap.getString("vanId"));
		capDtlMap.put("vanTrxId"	, trxPayMap.getString("vanTrxId"));
		capDtlMap.put("vanStatus"	, "입금대기");
		if(orgFeeMap.size() >0){
			capDtlMap.put("stlVanRate"	, orgFeeMap.getDouble("creditRate"));
			if(capDtlMap.isEquals("van", "DANAL") || capDtlMap.isEquals("van", "DAOU")){
				capDtlMap.put("stlVanFee"	, calcDanalFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")));
			}else{
				capDtlMap.put("stlVanFee"	, calcDefaultFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")));
			}
			capDtlMap.put("stlVanDay"	, calcDay(orgFeeMap.getString("settleType"),trxCapMap.getString("trxDay")));
		}
		capDtlMap.put("benefit"		, capDtlMap.getLong("stlFee")+capDtlMap.getLong("stlFeeVat")-capDtlMap.getLong("stlDistFee")-capDtlMap.getLong("stlAgencyFee")-capDtlMap.getLong("stlVanFee"));
		capDtlMap.put("taxId"		, mchtTmnMap.getString("taxId"));
		
		insertCapDtlList.add(capDtlMap);
		
		if(mchtMap.getString("aggregator").equals("Y")){
			SharedMap<String,Object> capSubMap = new SharedMap<String,Object>();
			SharedMap<String,Object> tmnDtlMap = trxDAO.getMchtTmnDtlByTmnId(trxCapMap.getString("tmnId"));
			capSubMap.put("capId"	, trxCapMap.getString("capId"));
			capSubMap.put("trxId"	, trxCapMap.getString("trxId"));
			capSubMap.put("mchtId"	, trxCapMap.getString("mchtId"));
			capSubMap.put("tmnId"	, trxCapMap.getString("tmnId"));
			capSubMap.put("capType" , trxCapMap.getString("capType"));
			capSubMap.put("rfdType"	, trxCapMap.getString("rfdType"));
			capSubMap.put("rootTrxId", trxCapMap.getString("rootTrxId"));
			capSubMap.put("amount"	, trxCapMap.getLong("amount"));
			capSubMap.put("stlRate"	, tmnDtlMap.getDouble("rate")*1.1);
			capSubMap.put("stlFee"	, calcFee(trxCapMap.getLong("amount"),tmnDtlMap.getDouble("rate")));
			capSubMap.put("stlFeeVat", calcVat(capSubMap.getLong("stlFee")));
			capSubMap.put("stlAmount", capSubMap.getLong("amount")-capSubMap.getLong("stlFee")-capSubMap.getLong("stlFeeVat"));
			capSubMap.put("stlType"	, "D+1");
			capSubMap.put("stlDay"	, calcDay(capSubMap.getString("stlType"),trxCapMap.getString("trxDay")));
			capSubMap.put("stlId"	, "");
			capSubMap.put("benefit"	, capDtlMap.getLong("stlAmount")-capSubMap.getLong("stlAmount"));
			capSubMap.put("trxDay", trxCapMap.getString("trxDay"));
			capSubMap.put("regDay", trxCapMap.getString("regDay"));
			capSubMap.put("regTime", trxCapMap.getString("regTime"));
			capSubMap.put("regDate"	, CommonUtil.getCurrentTimestamp());
			insertCapSubList.add(capSubMap);
		}
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
		SharedMap<String,Object> distMngMap		= trxDAO.getDistMngById(mchtMap.getString("distId"));
		SharedMap<String,Object> salesMngMap	= trxDAO.getSalesMngById(mchtMap.getString("salesId"));
		SharedMap<String,Object> orgFeeMap		= trxDAO.getOrgFee(trxRfdMap.getString("van"));
		
		//정산대기 이면서 리스크가 있었던 거래는 다시 선지급 수수료를 청구한다.
		if(rootCapMap.getString("stlStatus").equals("정산대기") && rootCapMap.getString("stlType").equals("D+1") && !rootCapMap.isNullOrSpace("risk")){
			SharedMap<String,Object> updateMap	= new SharedMap<String,Object>();
			//거래 정보를 원복한다.
			updateMap.put("stlRate"		, rootCapMap.getDouble("stlRate")+mchtMngMap.getDouble("loanRate"));		//선정산 수수료 적용 
			updateMap.put("stlFee"		, calcFee(rootCapMap.getLong("amount"), updateMap.getDouble("stlRate")));
			updateMap.put("stlFeeVat"	, calcVat(updateMap.getLong("stlFee")));
			updateMap.put("stlAmount"	, rootCapMap.getLong("amount")-updateMap.getLong("stlFee")-updateMap.getLong("stlFeeVat"));
			
			updateMap.put("risk", "");
			updateMap.put("capId", rootCapMap.getString("capId"));
			
			trxDAO.updateTrxCapDtl(updateMap);
			String[] riskData = {updateMap.getString("capId"),updateMap.getString("capId")+":RISK 해지 완료, D+1 수수료 변경됨 "};
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
		capDtlMap.put("stlFee"		, calcFee(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlRate")));
		capDtlMap.put("stlFeeVat"	, calcVat(capDtlMap.getLong("stlFee")));
		capDtlMap.put("stlAmount"	, trxCapMap.getLong("amount")-capDtlMap.getLong("stlFee")-capDtlMap.getLong("stlFeeVat"));
		capDtlMap.put("stlType"		, rootCapMap.getString("stlType"));
		capDtlMap.put("stlDay"		, calcDay(capDtlMap.getString("stlType"), trxCapMap.getString("trxDay")));
		capDtlMap.put("payOutDay"	, "");
		capDtlMap.put("stlId"		, "");
		
		if(!rootCapMap.isNullOrSpace("stlAgencyDay")){
			capDtlMap.put("stlAgencyRate", rootCapMap.getDouble("stlAgencyRate"));
			capDtlMap.put("stlAgencyFee", calcFeeVat(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlAgencyRate")));
			capDtlMap.put("stlAgencyDay", calcDay(agencyMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
			capDtlMap.put("stlAgencyId"	, "");
		}
		
		if(!rootCapMap.isNullOrSpace("stlSalesDay")){
			capDtlMap.put("stlSalesRate", rootCapMap.getDouble("stlSalesRate"));
			capDtlMap.put("stlSalesFee"	, calcFee(capDtlMap.getLong("stlAgencyFee"), capDtlMap.getDouble("stlSalesRate")));
			capDtlMap.put("stlSalesDay"	, calcDay(salesMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
			capDtlMap.put("stlSalesId"	, "");
		}
		
		if(!rootCapMap.isNullOrSpace("stlDistDay")){
			capDtlMap.put("stlDistRate"	, rootCapMap.getDouble("stlDistRate"));
			capDtlMap.put("stlDistFee"	, calcFeeVat(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlDistRate")));
			capDtlMap.put("stlDistDay"	, calcDay(distMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
			capDtlMap.put("stlDistId"	, "");
		}
		
		capDtlMap.put("van"			, trxRfdMap.getString("van"));
		capDtlMap.put("vanId"		, trxRfdMap.getString("vanId"));
		capDtlMap.put("vanTrxId"	, trxRfdMap.getString("vanTrxId"));
		capDtlMap.put("vanStatus"	, "입금대기");
		if(orgFeeMap.size() >0){
			capDtlMap.put("stlVanRate"	, orgFeeMap.getDouble("creditRate"));
			if(capDtlMap.isEquals("van", "DANAL") || capDtlMap.isEquals("van", "DAOU")){
				capDtlMap.put("stlVanFee"	, calcDanalFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")));
			}else{
				capDtlMap.put("stlVanFee"	, calcDefaultFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")));
			}
			capDtlMap.put("stlVanDay"	, calcDay(orgFeeMap.getString("settleType"),trxCapMap.getString("trxDay")));
		}
		capDtlMap.put("benefit"		, capDtlMap.getLong("stlFee")+capDtlMap.getLong("stlFeeVat")-capDtlMap.getLong("stlDistFee")-capDtlMap.getLong("stlAgencyFee")-capDtlMap.getLong("stlVanFee"));
		capDtlMap.put("taxId"		, rootCapMap.getString("taxId"));
		capDtlMap.put("risk"		, "");
		
		if(rootCapMap.getString("stlStatus").equals("정산완료")){
			capDtlMap.put("risk"		, "정산취소");
		}
		
		
		
		
		insertCapDtlList.add(capDtlMap);

		
		if(mchtMap.getString("aggregator").equals("Y")){
			SharedMap<String,Object> capSubMap = new SharedMap<String,Object>();
			SharedMap<String,Object> tmnDtlMap = trxDAO.getMchtTmnDtlByTmnId(trxCapMap.getString("tmnId"));
			capSubMap.put("capId"	, trxCapMap.getString("capId"));
			capSubMap.put("trxId"	, trxCapMap.getString("trxId"));
			capSubMap.put("mchtId"	, trxCapMap.getString("mchtId"));
			capSubMap.put("tmnId"	, trxCapMap.getString("tmnId"));
			capSubMap.put("capType" , trxCapMap.getString("capType"));
			capSubMap.put("rfdType"	, trxCapMap.getString("rfdType"));
			capSubMap.put("rootTrxId", trxCapMap.getString("rootTrxId"));
			capSubMap.put("amount"	, trxCapMap.getLong("amount"));
			capSubMap.put("stlRate"	, tmnDtlMap.getDouble("rate")*1.1);
			capSubMap.put("stlFee"	, calcFee(trxCapMap.getLong("amount"),tmnDtlMap.getDouble("rate")));
			capSubMap.put("stlFeeVat", calcVat(capSubMap.getLong("stlFee")));
			capSubMap.put("stlAmount", capSubMap.getLong("amount")-capSubMap.getLong("stlFee")-capSubMap.getLong("stlFeeVat"));
			capSubMap.put("stlType"	, "D+1");
			capSubMap.put("stlDay"	, calcDay(capSubMap.getString("stlType"),trxCapMap.getString("trxDay")));
			capSubMap.put("stlId"	, "");
			capSubMap.put("benefit"	, capDtlMap.getLong("stlAmount")-capSubMap.getLong("stlAmount"));
			capSubMap.put("trxDay", trxCapMap.getString("trxDay"));
			capSubMap.put("regDay", trxCapMap.getString("regDay"));
			capSubMap.put("regTime", trxCapMap.getString("regTime"));
			capSubMap.put("regDate"	, CommonUtil.getCurrentTimestamp());
			insertCapSubList.add(capSubMap);
		}
		
	}
	
	/**
	 * 위험거래로 변경한다.
	 * @param capId
	 */
	public void warning(String capId){
		SharedMap<String,Object> rootCapMap	 	= trxDAO.getTrxCapId(capId);
		if(rootCapMap == null){
			//검색된 매입내역이 없으면 리턴한다.
			return;
		}
		
		if(rootCapMap.getString("stlStatus").equals("정산대기")){
			SharedMap<String,Object> mchtMngMap		= trxDAO.getMchtMngByMchtId(rootCapMap.getString("mchtId"));
			SharedMap<String,Object> updateMap	= new SharedMap<String,Object>();
			updateMap.put("stlRate"		, mchtMngMap.getDouble("rate"));		//선정산 수수료 적용 
			updateMap.put("stlFee"		, calcFee(rootCapMap.getLong("amount"), updateMap.getDouble("stlRate")));
			updateMap.put("stlFeeVat"	, calcVat(updateMap.getLong("stlFee")));
			updateMap.put("stlAmount"	, rootCapMap.getLong("amount")-updateMap.getLong("stlFee")-updateMap.getLong("stlFeeVat"));
			
			updateMap.put("risk", "위험");
			updateMap.put("capId", rootCapMap.getString("capId"));
			
			trxDAO.updateTrxCapDtl(updateMap);
			String[] riskData = {updateMap.getString("capId"),updateMap.getString("capId")+":RISK 설정 완료, D+1 수수료 변경됨 to 위험"};
			riskList.add(riskData);
		}
		
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
	
	
	public String calcRiskStatus(long amount,SharedMap<String,Object> mchtMngMap){
		if(mchtMngMap.getLong("largeAmount") !=0 && mchtMngMap.getLong("largeAmount") <=  amount){
			return "고액";
		}
		if(mchtMngMap.getLong("limitOnce") !=0 &&  mchtMngMap.getLong("limitOnce") <=  amount){
			return "건한도";
		}
		if(amount < 1005){
			return "최소금액";
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
			term = CommonUtil.parseInt(settleType.replaceAll("D[+]", ""));
			String day =  trxDAO.getSettleDay(today, term);
			/*
			if(day.equals(currentDay) && CommonUtil.parseInt(CommonUtil.getCurrentDate("HH")) > 8){
				day =  trxDAO.getSettleDay(currentDay,1);
			}*/
			return day;
		}
	}
	
	public static LocalDate getToday(String today){
		return LocalDate.parse(today, DateTimeFormatter.ofPattern("yyyyMMdd"));
		
	}
	
	
	
	
	
	public static void main(String[] args){
		CaptureManual c = new CaptureManual();
		if(args != null ){
			if(args.length == 0 || args[0] == null ){
				System.out.println("거래일자를 지정하여 주시기 바랍니다.");
				System.exit(1);
			}else if(args[0].length() != 8){
				System.out.println("날짜 포맷이 잘못되었습니다.");
				System.exit(1);
			}else{
				System.out.println("거래일자 ["+args[0]+"]");
				c.start(args[0]);
			}
		}else{
			System.out.println("거래일자를 지정하여 주시기 바랍니다.");
			System.exit(1);
		}
	}
	
	

}
