package com.pgmate.cap.main;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pgmate.cap.bean.Card;
import com.pgmate.lib.key.CPKEY;
import com.pgmate.lib.key.GenKey;
import com.pgmate.lib.util.cipher.Base64;
import com.pgmate.lib.util.cipher.SeedKisa;
import com.pgmate.lib.util.gson.GsonUtil;
import com.pgmate.lib.util.lang.ByteUtil;
import com.pgmate.lib.util.lang.CommonUtil;
import com.pgmate.lib.util.map.SharedMap;

/**
 * @author Administrator
 *
 */
public class Capture {
	private static Logger logger = LoggerFactory.getLogger(com.pgmate.cap.main.Capture.class);
	private TrxDAO trxDAO = null;
	private TrxBatchDAO trxBatchDAO = null;
	private List<SharedMap<String,Object>> insertCapList = new ArrayList<SharedMap<String,Object>>();
	private List<SharedMap<String,Object>> insertCapDtlList = new ArrayList<SharedMap<String,Object>>();
	private List<SharedMap<String,Object>> insertCapSubList = new ArrayList<SharedMap<String,Object>>();
	private List<SharedMap<String,Object>> insertChargeSettleList = new ArrayList<SharedMap<String,Object>>();
	
	private List<String> warningList = new ArrayList<String>();
	private List<String[]> riskList  = new ArrayList<String[]>();
	
	
	public Capture() {
		this.trxDAO = new TrxDAO();
		this.trxBatchDAO = new TrxBatchDAO();
		
	}
	
	public void start(){
		
		List<SharedMap<String,Object>> trxPayList = trxDAO.getTrxPay();
		int i=1;
		int size = trxPayList.size();
		
		if(size > 0){
			for(SharedMap<String,Object> trxPayMap : trxPayList ){
				logger.info("capture : {}/{}",i++,size);
				capture(trxPayMap);
				realTimePay(trxPayMap, "0");
			}
			       
			logger.info("capture inserted : {}",trxBatchDAO.insertTrxCap(insertCapList, insertCapDtlList));
			logger.info("capture sub inserted : {}",trxBatchDAO.insertTrxCapSub(insertCapSubList));
			logger.info("capture charge inserted : {}",trxBatchDAO.insertChargeSettle(insertChargeSettleList));
			logger.info("");
		}
		
		insertCapList 		= new ArrayList<SharedMap<String,Object>>();
		insertCapDtlList	= new ArrayList<SharedMap<String,Object>>();
		insertCapSubList 	= new ArrayList<SharedMap<String,Object>>();
		insertChargeSettleList = new ArrayList<SharedMap<String,Object>>();
		
		List<SharedMap<String,Object>> trxRfdList = trxDAO.getTrxRfd();
		i=1;
		size = trxRfdList.size();
		if(size > 0){
			for(SharedMap<String,Object> trxRfdMap : trxRfdList ){
				logger.info("refund  : {}/{}",i++,size);
				refund(trxRfdMap);
				realTimePay(trxRfdMap, "1");
			}
			logger.info("refund inserted : {}",trxBatchDAO.insertTrxCap(insertCapList, insertCapDtlList));
			logger.info("refund sub inserted : {}",trxBatchDAO.insertTrxCapSub(insertCapSubList));
			logger.info("refund charge inserted : {}",trxBatchDAO.insertChargeSettle(insertChargeSettleList));
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
		insertCapSubList 	= null;
		warningList 		= null;
		riskList			= null;
		
		
		SharedMap<String,Object> loadMap = trxDAO.getTrxLoad();
		if(loadMap != null && loadMap.size() > 0){
			trxDAO.setTrxLoadStatus(loadMap.getLong("idx"), "처리중", CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss") +" 등록 시작");
			logger.info("load start idx : {}",loadMap.getLong("idx"));
			List<SharedMap<String,Object>> loadList = trxDAO.getTrxLoadDtl(loadMap.getLong("idx"));
			if(loadList != null){
				loadData(loadMap,loadList);
			}else{
				trxDAO.setTrxLoadStatus(loadMap.getLong("idx"), "실패", CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss") +" 검색된 데이터가 없습니다.");
			}
			
		}
				
		loadMap = null;
	}
	
	
	public void capture(SharedMap<String,Object> trxPayMap){
		SharedMap<String,Object> mchtMap		= trxDAO.getMchtByMchtId(trxPayMap.getString("mchtId"));
		SharedMap<String,Object> mchtTmnMap		= trxDAO.getMchtTmnByTmnId(trxPayMap.getString("tmnId"));
		SharedMap<String,Object> mchtMngMap		= trxDAO.getMchtMngByMchtId(mchtMap.getString("mchtId"));
		SharedMap<String,Object> agencyMngMap	= trxDAO.getAgencyMngById(mchtMap.getString("agencyId"));
		SharedMap<String,Object> distMngMap		= trxDAO.getDistMngById(mchtMap.getString("distId"));
		SharedMap<String,Object> salesMngMap	= trxDAO.getSalesMngById(mchtMap.getString("salesId"));
		SharedMap<String,Object> orgFeeMap		= trxDAO.getOrgFee(trxPayMap.getString("van"));
		
		
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
		
		//PYS : 갤럭시아 면세용 단말기 M2247413
		//PYS : KSNET 면세용 단말기 2010000009
		if(trxPayMap.isEquals("vanId", "M2247413") || trxPayMap.isEquals("vanId", "2010000009") || trxPayMap.isEquals("vanId", "mtouch9")) {
			trxCapMap.put("vat"			, 0);
		}else{
			trxCapMap.put("vat"			, calcRootVat(trxPayMap.getLong("amount")));
		}
		
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
		String dup = trxDAO.getDuplicatedDaily(trxCapMap);
		if(!dup.equals("")){
			capDtlMap.put("risk",dup);
		}
		//중복거래
		dup = trxDAO.getDuplicated(trxCapMap);
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
		
		//야간 건 한도
		if(trxCapMap.getLong("regTime") < 60000 && trxCapMap.getLong("amount") > mchtMngMap.getLong("limitOnce") && mchtMngMap.getLong("limitOnce") != 0){
			capDtlMap.put("risk","야간건한도");
		}
		
		//주간 건 한도 
		if(trxCapMap.getLong("regTime") >= 60000 && trxCapMap.getLong("amount") > mchtMngMap.getLong("limitOnce") && mchtMngMap.getLong("limitOnce") != 0){
			capDtlMap.put("risk","건한도");		
		}
		
		//위험,중복
		if(!trxCapMap.isNullOrSpace("bin") && !trxCapMap.isNullOrSpace("last4")){
			//위험 100 만원 이상 거래
			String trxId = trxDAO.getWarningFact(trxPayMap);
			if(!trxId.equals("")){
				warningList.add(trxId);
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
		
		double vanInterRate = 0;
		double stlInterRate = 0;
		
		
		if(mchtMngMap.getString("interType").equals("사용") && !trxCapMap.getString("installment").equals("00") && trxPayMap.isEquals("vanId", "2006500009")) {
			// 가맹점 상점부담 무이자 수수료율 가져오기
			stlInterRate = trxDAO.getMchtInterFeeRate(trxCapMap).getDouble("m"+trxCapMap.getString("installment"));
			capDtlMap.put("stlInterRate", stlInterRate);
		
			SharedMap<String, Object> orgInterMap = trxDAO.getOrgInterRate(mchtTmnMap.getString("van"),trxCapMap.getString("acquirer"));
			vanInterRate = orgInterMap.getDouble("m"+trxCapMap.getString("installment"));
			capDtlMap.put("stlVanInterRate", vanInterRate);
		}
		
		if(trxPayMap.isEquals("vanId","OFFLINE")){
			double normalRate 	= 0;
			
			if(mchtMngMap.getDouble("rate") == 0.008){
				if(trxCapMap.isEquals("cardType", "체크")){
					normalRate 	= 0.005;
				}else{
					normalRate 	= 0.008;
				}
			}else if(mchtMngMap.getDouble("rate") == 0.013){
				if(trxCapMap.isEquals("cardType", "체크")){
					normalRate 	= 0.01;
				}else{
					normalRate 	= 0.013;
				}
			}else{
				//여기서부터는 설계
			}
			//OFFLINE  거래는 노멀 수수료 + loanRate 
			
			if(mchtMngMap.getString("settleType").equals("D+1") && capDtlMap.isNullOrSpace("risk")){	//D+1 리스크 없는 거래 
				capDtlMap.put("stlRate"		, normalRate+capDtlMap.getDouble("stlLoanRate")+stlInterRate);			//가맹점 수수료 + 선정산 수수료 
				capDtlMap.put("stlType"     , mchtMngMap.getString("settleType"));						//정산기간 
				long normalFee 				= calcFee(trxCapMap.getLong("amount"), normalRate);			//일반 수수료 산정
				long loanFee	 			= calcFee(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlLoanRate"));	//선정산 수수료 산정
				long loanFeeVat				= calcVat(loanFee);											//선정산 수수료에 대해서만 부가세 적용
				capDtlMap.put("stlFee"		, normalFee+loanFee);
				capDtlMap.put("stlFeeVat"	, loanFeeVat);
			}else{																						//D+1 이 아닌 리스크 없는 거래 
				capDtlMap.put("stlRate"		, normalRate + stlInterRate);												//가맹점 수수료만 적용 
				capDtlMap.put("stlType"     , mchtMngMap.getString("settleType"));						//정산기간
				capDtlMap.put("stlFee"		, calcFee(trxCapMap.getLong("amount"), normalRate));		//일반 수수료만적용
				capDtlMap.put("stlFeeVat"	, 0);														//일반 수수료에 부가세 없음.
			}
			capDtlMap.put("stlAmount"	, trxCapMap.getLong("amount")-capDtlMap.getLong("stlFee")-capDtlMap.getLong("stlFeeVat"));
			
		}else{
			
			if(mchtMngMap.getString("settleType").equals("D+1") && capDtlMap.isNullOrSpace("risk")){	//D+1 정산 주기에 RISK가 없는 경우는 선지급 수수료를 청구한다.
				capDtlMap.put("stlRate"		, mchtMngMap.getDouble("rate")+capDtlMap.getDouble("stlLoanRate")+stlInterRate);
			}else{ 																						//D+1 이 아닌 경우 또는 RISK가 있는 경우는 원 수수료를 청구한다.
				capDtlMap.put("stlRate"		, mchtMngMap.getDouble("rate")+stlInterRate);
			}
			capDtlMap.put("stlType"     , mchtMngMap.getString("settleType"));
			capDtlMap.put("stlFee"		, calcFee(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlRate")));
			capDtlMap.put("stlFeeVat"	, calcVat(capDtlMap.getLong("stlFee")));
			
			capDtlMap.put("stlAmount"	, trxCapMap.getLong("amount")-capDtlMap.getLong("stlFee")-capDtlMap.getLong("stlFeeVat"));
		}
		
		

		capDtlMap.put("stlDay"		, calcDay(capDtlMap.getString("stlType"), trxCapMap.getString("trxDay")));
		capDtlMap.put("payOutDay"	, "");
		capDtlMap.put("stlId"		, "");
		 
		if(agencyMngMap.size() >0 ){
			capDtlMap.put("stlAgencyRate", mchtMngMap.getDouble("rate")-mchtMngMap.getDouble("agencyRate"));
			capDtlMap.put("stlAgencyFee", calcFeeVat(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlAgencyRate")));
			capDtlMap.put("stlAgencyDay", calcDay(agencyMngMap.getString("settleType"),trxCapMap.getString("trxDay")));
			capDtlMap.put("stlAgencyId"	, "");
			
		}
		
		if(salesMngMap.size() >0 ){
			capDtlMap.put("stlSalesRate", mchtMngMap.getDouble("salesRate"));
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
		
		// 에이전시 최종 수수료 : 에이전시 수수료 - 지사 수수료
		capDtlMap.put("stlAgencyFee", capDtlMap.getLong("stlAgencyFee")-capDtlMap.getLong("stlSalesFee"));
		
		capDtlMap.put("van"			, trxPayMap.getString("van"));
		capDtlMap.put("vanId"		, trxPayMap.getString("vanId"));
		capDtlMap.put("vanTrxId"	, trxPayMap.getString("vanTrxId"));
		capDtlMap.put("vanStatus"	, "입금대기");
		if(orgFeeMap.size() >0){
			if(capDtlMap.isEquals("vanId", "OFFLINE")){
				if(trxCapMap.isEquals("cardType", "체크")){
					capDtlMap.put("stlVanRate"	, orgFeeMap.getDouble("checkRate")+vanInterRate);					//체크 수수료 적용
				}else{
					capDtlMap.put("stlVanRate"	, orgFeeMap.getDouble("creditRate")+vanInterRate);					//신용 수수료 적용 
				}
				capDtlMap.put("stlVanFee"	, calcDefaultFee(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate")));
				
			}else{
				capDtlMap.put("stlVanRate"	, orgFeeMap.getDouble("creditRate")+vanInterRate);
				if(capDtlMap.isEquals("van", "DANAL")){
					capDtlMap.put("stlVanFee"	, calcDanalFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")+vanInterRate));
				}else if(capDtlMap.startsWith("van", "DAOU") ){
					
					//if(mchtTmnMap.getString("ccType").equals("분리") && trxPayMap.getString("cardType").equals("체크") && !trxPayMap.getString("vanId").equals("FACTORING")){
					if(trxPayMap.getString("cardType").equals("체크") && !trxPayMap.getString("vanId").equals("FACTORING") && !mchtMap.getString("distId").equals("16")){
						logger.info("DAOU PG RATE 분리 적용 CTYPE :{},CHECK :{},{}",trxPayMap.getString("cardType"),orgFeeMap.getDouble("checkRate"),trxPayMap.getString("vanId"));
						logger.info("DAOU 거래번호 capId :{}",trxCapMap.getString("capId"));
						capDtlMap.put("stlVanRate"	, orgFeeMap.getDouble("checkRate")+vanInterRate);
						capDtlMap.put("stlVanFee"	, calcRoundUpFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("checkRate")+vanInterRate));
					}else{
						capDtlMap.put("stlVanFee"	, calcRoundUpFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")+vanInterRate));
					}
				}else if(capDtlMap.isEquals("van", "SBANK")){
					capDtlMap.put("stlVanFee"	, calcRoundTrimFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")+vanInterRate));
				}else if(capDtlMap.getString("van").startsWith("KSPAY")){
					capDtlMap.put("stlVanFee"	, calcKsnetFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")+vanInterRate));
				}else if(capDtlMap.getString("van").startsWith("NICE")){
					capDtlMap.put("stlVanFee"	, calcNiceFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")+vanInterRate));
				}else if(capDtlMap.getString("van").startsWith("ALLAT")){
					capDtlMap.put("stlVanFee"	, calcRoundUpFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")+vanInterRate));
				}else{
					capDtlMap.put("stlVanFee"	, calcDefaultFeeVat(trxCapMap.getLong("amount"),orgFeeMap.getDouble("creditRate")+vanInterRate));
				}
			}
			
			// 영중소 가맹점 수수료 추가 
			//PYS : 부국위너스 영중소 VANID로 변경 2010000007, 2010000008
			if(!mchtMngMap.getString("diffType").equals("일반") && capDtlMap.getString("van").startsWith("KSPAY") && (trxPayMap.isEquals("vanId", "2010000007") || trxPayMap.isEquals("vanId", "2010000008"))) {
				double diffRate = 0;
				double stlDiffAgencyRate = 0;
				double stlDiffDistRate = 0;
				double stlDiffSalesRate = 0;
				
				if(trxPayMap.getString("cardType").equals("체크") || trxPayMap.getString("cardType").equals("기프트카드") || trxPayMap.getString("cardType").equals("선불") || trxPayMap.getString("cardType").equals("구매") ) {
					switch(mchtMngMap.getString("diffType")) {
						case "영세":	diffRate = orgFeeMap.getDouble("diff1CheckRate");
								   	stlDiffAgencyRate = mchtMngMap.getDouble("diff0CheckAgencyRate");
								   	stlDiffDistRate = mchtMngMap.getDouble("diff0CheckDistRate");
								   	stlDiffSalesRate = mchtMngMap.getDouble("diff0CheckSalesRate");
								   	break;
						case "중소1": diffRate = orgFeeMap.getDouble("diff2CheckRate");
									 stlDiffAgencyRate = mchtMngMap.getDouble("diff1CheckAgencyRate");
									 stlDiffDistRate = mchtMngMap.getDouble("diff1CheckDistRate");
									 stlDiffSalesRate = mchtMngMap.getDouble("diff1ChecSaleskRate");
									 break;
						case "중소2": diffRate = orgFeeMap.getDouble("diff3CheckRate");
									 stlDiffAgencyRate = mchtMngMap.getDouble("diff2CheckAgencyRate");
									 stlDiffDistRate = mchtMngMap.getDouble("diff2CheckDistRate");
									 stlDiffSalesRate = mchtMngMap.getDouble("diff2CheckSalesRate");
									 break;
						case "중소3": diffRate = orgFeeMap.getDouble("diff4CheckRate");
									 stlDiffAgencyRate = mchtMngMap.getDouble("diff3CheckAgencyRate");
									 stlDiffDistRate = mchtMngMap.getDouble("diff3CheckDistRate");
									 stlDiffSalesRate = mchtMngMap.getDouble("diff3CheckRate");
									 break;
					}
				}else {
					switch(mchtMngMap.getString("diffType")) {
						case "영세": diffRate = orgFeeMap.getDouble("diff1Rate");
									stlDiffAgencyRate = mchtMngMap.getDouble("diff0AgencyRate");
									stlDiffDistRate = mchtMngMap.getDouble("diff0DistRate");
									stlDiffSalesRate = mchtMngMap.getDouble("diff0SalesRate");
									 break;
						case "중소1": diffRate = orgFeeMap.getDouble("diff2Rate");
									 stlDiffAgencyRate = mchtMngMap.getDouble("diff1AgencyRate");
									 stlDiffDistRate = mchtMngMap.getDouble("diff1DistRate");
									 stlDiffSalesRate = mchtMngMap.getDouble("diff1SalesRate");
									 break;
						case "중소2": diffRate = orgFeeMap.getDouble("diff3Rate");
									 stlDiffAgencyRate = mchtMngMap.getDouble("diff2AgencyRate");
									 stlDiffDistRate = mchtMngMap.getDouble("diff2DistRate");
									 stlDiffSalesRate = mchtMngMap.getDouble("diff2SalesRate");
									 break;
						case "중소3": diffRate = orgFeeMap.getDouble("diff4Rate");
									 stlDiffAgencyRate = mchtMngMap.getDouble("diff3AgencyRate");
									 stlDiffDistRate = mchtMngMap.getDouble("diff3DistRate");
									 stlDiffSalesRate = mchtMngMap.getDouble("diff3SalesRate");
									 break;
					}		
				}
				
				long stlDiffAgencyFee = calcFeeVat(trxCapMap.getLong("amount"), stlDiffAgencyRate);
				long stlDiffDistFee = calcFeeVat(trxCapMap.getLong("amount"), stlDiffDistRate);
				
				capDtlMap.put("stlDiffAgencyRate",stlDiffAgencyRate);
				capDtlMap.put("stlDiffAgencyFee",stlDiffAgencyFee);
				capDtlMap.put("stlDiffDistRate",stlDiffDistRate);
				capDtlMap.put("stlDiffDistFee",stlDiffDistFee);
				capDtlMap.put("stlDiffSalesRate", stlDiffSalesRate);
				capDtlMap.put("stlDiffSalesFee"	, calcFee(capDtlMap.getLong("stlDiffAgencyFee"), capDtlMap.getDouble("stlDiffSalesRate")));
				
				// 에이전시 차액정산 최종 수수료 : 에이전시 차액정산 수수료 - 지사 차액정산 수수료
				capDtlMap.put("stlDiffAgencyFee", capDtlMap.getLong("stlDiffAgencyFee")-capDtlMap.getLong("stlDiffSalesFee"));
				
				// 영중소 가맹점일 경우 대행사, 에이전시, 지사 일반 수수료는 0으로 한다.
				capDtlMap.put("stlDistFee", 0);
				capDtlMap.put("stlDistRate", 0);
				capDtlMap.put("stlAgencyFee", 0);
				capDtlMap.put("stlAgencyRate", 0);
				capDtlMap.put("stlSalesFee", 0);
				capDtlMap.put("stlSalesRate", 0);
				
				
				capDtlMap.put("stlDiffRate"	, diffRate);
				capDtlMap.put("stlDiffAmt"	, calcFeeVat(trxCapMap.getLong("amount"),diffRate));
				capDtlMap.put("stlDiffStatus", "결과대기");
				long benefit1 = capDtlMap.getLong("stlFee")+capDtlMap.getLong("stlFeeVat")-capDtlMap.getLong("stlDistFee")-capDtlMap.getLong("stlAgencyFee")-capDtlMap.getLong("stlSalesFee")-capDtlMap.getLong("stlVanFee");
				long benefit2 = capDtlMap.getLong("stlDiffAmt") - (stlDiffDistFee + stlDiffAgencyFee);
				capDtlMap.put("benefit"		,  benefit1 + benefit2);
			} else {
				capDtlMap.put("benefit"		, capDtlMap.getLong("stlFee")+capDtlMap.getLong("stlFeeVat")-capDtlMap.getLong("stlDistFee")-capDtlMap.getLong("stlAgencyFee")-capDtlMap.getLong("stlSalesFee")-capDtlMap.getLong("stlVanFee"));
			}
			capDtlMap.put("stlDiffType"	, mchtMngMap.getString("diffType"));
			capDtlMap.put("stlVanDay"	, calcDay(orgFeeMap.getString("settleType"),trxCapMap.getString("trxDay")));
		}
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
		
		//정상결제 완료 건인데 충전정산 실시간 전송 가맹점의 거래건일 경우 가맹점 충전정산 거래내역 테이블 저장
		if(mchtMngMap.isEquals("settleType", "C+0")) {
			SharedMap<String,Object> chargeSettlebMap = new SharedMap<String,Object>();
			String regDate = CommonUtil.getCurrentDate("yyyyMMddHHmmss");
			
			logger.info("===================================================");
			logger.info("PG_CHARGE_SETTLE 테이블 승인 INSERT");
			chargeSettlebMap.put("trxId"	, trxCapMap.getString("trxId"));
			chargeSettlebMap.put("mchtId"	, trxCapMap.getString("mchtId"));
			chargeSettlebMap.put("trxType"	, "입금");
			chargeSettlebMap.put("trxUnit"	, "신용카드정산");
			chargeSettlebMap.put("trxDay"	, regDate.substring(0, 8));
			chargeSettlebMap.put("trxTime"	, regDate.substring(8));
			chargeSettlebMap.put("amount"	, Math.abs(trxCapMap.getLong("amount")));
			chargeSettlebMap.put("fee"		, Math.abs(capDtlMap.getLong("stlFee")));
			chargeSettlebMap.put("feeVat"	, Math.abs(capDtlMap.getLong("stlFeeVat")));
			chargeSettlebMap.put("bankFee"	, 0);
			chargeSettlebMap.put("netAmount", Math.abs(capDtlMap.getLong("stlAmount")));
			chargeSettlebMap.put("balance"	, trxDAO.getMchtBalance(trxCapMap.getString("mchtId")).getLong("balance")+Math.abs(capDtlMap.getLong("stlAmount")));
			chargeSettlebMap.put("trackId"	, trxCapMap.getString("trackId"));
			chargeSettlebMap.put("refId"	, trxCapMap.getString("capId"));
			chargeSettlebMap.put("bankCd"	, "");
			chargeSettlebMap.put("bankName"	, "");
			chargeSettlebMap.put("account"	, "");
			chargeSettlebMap.put("holder"	, "");
			chargeSettlebMap.put("recordInfo"	, "");
			
			String stlDay = capDtlMap.getString("stlDay").substring(0, 4)+"-"+capDtlMap.getString("stlDay").substring(4,6)+"-"+capDtlMap.getString("stlDay").substring(6);
			chargeSettlebMap.put("summary"	, stlDay+"정산일자 실시간 신용카드 정산금 지급");
			chargeSettlebMap.put("regId"	, trxCapMap.getString("mchtId"));
			chargeSettlebMap.put("regDay"	, regDate.substring(0, 8));

			insertChargeSettleList.add(chargeSettlebMap);
			logger.info("===================================================");
		} 
	}
	
	
	public void refund(SharedMap<String,Object> trxRfdMap){
		
		SharedMap<String,Object> rootCapMap	 	= trxDAO.getTrxCap(trxRfdMap.getString("rootTrxId"));
		if(rootCapMap == null){
			//검색된 매입내역이 없으면 리턴한다.
			return;
		}
		
		SharedMap<String,Object> mchtMap		= trxDAO.getMchtByMchtId(trxRfdMap.getString("mchtId"));
		SharedMap<String,Object> agencyMngMap	= trxDAO.getAgencyMngById(mchtMap.getString("agencyId"));
		SharedMap<String,Object> distMngMap		= trxDAO.getDistMngById(mchtMap.getString("distId"));
		SharedMap<String,Object> salesMngMap	= trxDAO.getSalesMngById(mchtMap.getString("salesId"));
		SharedMap<String,Object> orgFeeMap		= trxDAO.getOrgFee(trxRfdMap.getString("van"));
		SharedMap<String,Object> mchtSvcMap 	= trxDAO.getRealTimeMchtSvc(trxRfdMap.getString("mchtId"));
		SharedMap<String,Object> mchtMngMap		= trxDAO.getMchtMngByMchtId(trxRfdMap.getString("mchtId"));
		
		//정산대기 이면서 리스크가 있었던 거래는 다시 선지급 수수료를 청구한다.
		if(rootCapMap.getString("stlStatus").equals("정산대기") && rootCapMap.getString("stlType").equals("D+1") && !rootCapMap.isNullOrSpace("risk")){
			SharedMap<String,Object> updateMap	= new SharedMap<String,Object>();
			//거래 정보를 원복한다.
			if(rootCapMap.isEquals("vanId", "OFFLINE")){
				updateMap.put("stlRate"		, rootCapMap.getDouble("stlRate")+rootCapMap.getDouble("stlLoanRate"));			//선정산 수수료 적용
				long normalFee 				= calcFee(rootCapMap.getLong("amount"), rootCapMap.getDouble("stlRate"));		//가맹점 수수료 산정
				long loanFee	 			= calcFee(rootCapMap.getLong("amount"), rootCapMap.getDouble("stlLoanRate"));	//선정산 수수료 산정
				long loanFeeVat				= calcVat(loanFee);																//선정산 부가세 적용
				
				updateMap.put("stlFee"		, normalFee+loanFee);
				updateMap.put("stlFeeVat"	, loanFeeVat);																	//부가세는 선정산 만 적용 
			}else{
				updateMap.put("stlRate"		, rootCapMap.getDouble("stlRate")+rootCapMap.getDouble("stlLoanRate"));		//선정산 수수료 적용
				updateMap.put("stlFee"		, calcFee(rootCapMap.getLong("amount"), updateMap.getDouble("stlRate")));
				updateMap.put("stlFeeVat"	, calcVat(updateMap.getLong("stlFee")));
				
				
			}
			updateMap.put("stlAmount"	, rootCapMap.getLong("amount")-updateMap.getLong("stlFee")-updateMap.getLong("stlFeeVat"));
			updateMap.put("risk", "");
			updateMap.put("capId", rootCapMap.getString("capId"));
			
			trxDAO.updateTrxCapDtl(updateMap);
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
		if(rootCapMap.isEquals("vanId", "M2247413") || rootCapMap.isEquals("vanId", "2010000009") || rootCapMap.isEquals("vanId", "mtouch9")) {	// 면세용 아이디 
			trxCapMap.put("vat"			, 0);
		}else{
			trxCapMap.put("vat"			, trxRfdMap.getLong("rfdVat"));
		}
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
		
		if(mchtSvcMap.isEquals("settle", "실시간정산") && mchtMngMap.isEquals("settleType", "D+0")) {
			capDtlMap.put("stlStatus"	, "정산완료");
			capDtlMap.put("payOutDay"	, CommonUtil.getCurrentDate("yyyyMMdd"));
			
			trxDAO.updateOrgRealtime(rootCapMap.getString("capId"));
		}else {
			capDtlMap.put("stlStatus"	, "정산대기");
			capDtlMap.put("payOutDay"	, "");
		}
		
		capDtlMap.put("stlRate"		, rootCapMap.getDouble("stlRate"));	
		
		if(trxRfdMap.isEquals("vanId", "OFFLINE")){
			double normalRate 	= rootCapMap.getDouble("stlRate")-rootCapMap.getDouble("stlLoanRate");				//선정산 수수료 제외
			long normalFee 		= calcFee(trxCapMap.getLong("amount"), normalRate);									//가맹점 수수료 산출
			long loanFee   		= calcFee(trxCapMap.getLong("amount"), rootCapMap.getDouble("stlLoanRate"));		//선정산 수수료 산출
			long loanFeeVat		= calcVat(loanFee);																	//선정산 VAT 산출
			capDtlMap.put("stlFee"		, normalFee+loanFee);
			capDtlMap.put("stlFeeVat"	, loanFeeVat);
		}else{
			capDtlMap.put("stlFee"		, calcFee(trxCapMap.getLong("amount"), capDtlMap.getDouble("stlRate")));
			capDtlMap.put("stlFeeVat"	, calcVat(capDtlMap.getLong("stlFee")));
			
		}
		
		capDtlMap.put("stlAmount"	, trxCapMap.getLong("amount")-capDtlMap.getLong("stlFee")-capDtlMap.getLong("stlFeeVat"));
		capDtlMap.put("stlType"		, rootCapMap.getString("stlType"));
		capDtlMap.put("stlDay"		, calcDay(capDtlMap.getString("stlType"), trxCapMap.getString("trxDay")));
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
		
		// 에이전시 수수료 : 에이전시 수수료 - 지사 수수료
		capDtlMap.put("stlAgencyFee", capDtlMap.getLong("stlAgencyFee")-capDtlMap.getLong("stlSalesFee"));
				
		
		capDtlMap.put("van"			, trxRfdMap.getString("van"));
		capDtlMap.put("vanId"		, trxRfdMap.getString("vanId"));
		capDtlMap.put("vanTrxId"	, trxRfdMap.getString("vanTrxId"));
		capDtlMap.put("vanStatus"	, "입금대기");
		if(orgFeeMap.size() >0){
			capDtlMap.put("stlVanRate"	, rootCapMap.getDouble("stlVanRate"));
			if(capDtlMap.isEquals("vanId", "OFFLINE")){
				capDtlMap.put("stlVanFee"	, calcDefaultFee(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate")));
			}else{
				if(capDtlMap.isEquals("van", "DANAL")){
					capDtlMap.put("stlVanFee"	, calcDanalFeeVat(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate")));
				}else if(capDtlMap.startsWith("van", "DAOU")){
					capDtlMap.put("stlVanFee"	, calcRoundUpFeeVat(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate")));
				}else if(capDtlMap.isEquals("van", "SBANK")){
					capDtlMap.put("stlVanFee"	, calcRoundTrimFeeVat(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate")));
				}else if(capDtlMap.getString("van").startsWith("KSPAY")){
					capDtlMap.put("stlVanFee"	, calcKsnetFeeVat(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate")));
				}else if(capDtlMap.getString("van").startsWith("NICE")){
					capDtlMap.put("stlVanFee"	, calcNiceFeeVat(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate")));
				}else if(capDtlMap.getString("van").startsWith("ALLAT")){
					capDtlMap.put("stlVanFee"	, calcRoundUpFeeVat(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate")));
				}else{
					capDtlMap.put("stlVanFee"	, calcDefaultFeeVat(trxCapMap.getLong("amount"),capDtlMap.getDouble("stlVanRate")));
				}
			}
			// 영중소 가맹점 수수료 추가
			//PYS : 부국위너스 영중소 VANID로 변경 2010000007, 2010000008
			if(!rootCapMap.getString("stlDiffType").equals("일반") && capDtlMap.getString("van").startsWith("KSPAY") && (trxRfdMap.isEquals("vanId", "2010000007") || trxRfdMap.isEquals("vanId", "2010000008"))) {

				long stlDiffAgencyFee = calcFeeVat(trxCapMap.getLong("amount"), rootCapMap.getDouble("stlDiffAgencyRate"));
				long stlDiffDistFee = calcFeeVat(trxCapMap.getLong("amount"), rootCapMap.getDouble("stlDiffDistRate"));
				
				capDtlMap.put("stlDiffAgencyRate",rootCapMap.getDouble("stlDiffAgencyRate"));
				capDtlMap.put("stlDiffAgencyFee",stlDiffAgencyFee);
				capDtlMap.put("stlDiffDistRate",rootCapMap.getDouble("stlDiffDistRate"));
				capDtlMap.put("stlDiffDistFee",stlDiffDistFee);
				capDtlMap.put("stlDiffSalesRate", rootCapMap.getDouble("stlDiffSalesRate"));
				capDtlMap.put("stlDiffSalesFee"	, calcFee(capDtlMap.getLong("stlDiffAgencyFee"), capDtlMap.getDouble("stlDiffSalesRate")));
				
				// 에이전시 차액정산 수수료 : 에이전시 차액정산 수수료 - 지사 차액정산 수수료
				capDtlMap.put("stlDiffAgencyFee", capDtlMap.getLong("stlDiffAgencyFee")-capDtlMap.getLong("stlDiffSalesFee"));
				
				// 영중소 가맹점일 경우 대행사, 에이전시, 지사 일반 수수료는 0으로 한다.
				capDtlMap.put("stlDistFee", 0);
				capDtlMap.put("stlDistRate", 0);
				capDtlMap.put("stlAgencyFee", 0);
				capDtlMap.put("stlAgencyRate", 0);
				capDtlMap.put("stlSalesFee", 0);
				capDtlMap.put("stlSalesRate", 0);
				
				capDtlMap.put("stlDiffRate"	, rootCapMap.getDouble("stlDiffRate"));
				capDtlMap.put("stlDiffAmt"	, calcFeeVat(trxCapMap.getLong("amount"),rootCapMap.getDouble("stlDiffRate")));
				capDtlMap.put("stlDiffStatus", "결과대기");
				long benefit1 = capDtlMap.getLong("stlFee")+capDtlMap.getLong("stlFeeVat")-capDtlMap.getLong("stlDistFee")-capDtlMap.getLong("stlAgencyFee")-capDtlMap.getLong("stlSalesFee")-capDtlMap.getLong("stlVanFee");
				long benefit2 = capDtlMap.getLong("stlDiffAmt") - (stlDiffDistFee + stlDiffAgencyFee);
				capDtlMap.put("benefit"		,  benefit1 + benefit2);
				
			} else {
				capDtlMap.put("benefit"		, capDtlMap.getLong("stlFee")+capDtlMap.getLong("stlFeeVat")-capDtlMap.getLong("stlDistFee")-capDtlMap.getLong("stlAgencyFee")-capDtlMap.getLong("stlSalesFee")-capDtlMap.getLong("stlVanFee"));
		}
			capDtlMap.put("stlDiffType"	, rootCapMap.getString("stlDiffType"));
			capDtlMap.put("stlVanDay"	, calcDay(orgFeeMap.getString("settleType"),trxCapMap.getString("trxDay")));
		}
		capDtlMap.put("taxId"		, rootCapMap.getString("taxId"));
		capDtlMap.put("risk"		, "");
		
		capDtlMap.put("stlInterRate", rootCapMap.getDouble("stlInterRate"));
		capDtlMap.put("stlVanInterRate", rootCapMap.getDouble("stlVanInterRate"));
		

		
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
		
		//정상결제 완료 건인데 충전정산 실시간 전송 가맹점의 거래건일 경우 가맹점 충전정산 거래내역 테이블 저장
		if(rootCapMap.isEquals("stlType", "C+0")) {
			SharedMap<String,Object> chargeSettlebMap = new SharedMap<String,Object>();
			String regDate = CommonUtil.getCurrentDate("yyyyMMddHHmmss");

			logger.info("===================================================");
			logger.info("PG_CHARGE_SETTLE 테이블 취소 INSERT");
			chargeSettlebMap.put("trxId"	, trxRfdMap.getString("trxId"));
			chargeSettlebMap.put("mchtId"	, rootCapMap.getString("mchtId"));
			chargeSettlebMap.put("trxType"	, "출금");
			chargeSettlebMap.put("trxUnit"	, "신용카드정산");
			chargeSettlebMap.put("trxDay"	, regDate.substring(0, 8));
			chargeSettlebMap.put("trxTime"	, regDate.substring(8));
			chargeSettlebMap.put("amount"	, Math.abs(rootCapMap.getLong("amount")));
			chargeSettlebMap.put("fee"		, Math.abs(rootCapMap.getLong("stlFee")));
			chargeSettlebMap.put("feeVat"	, Math.abs(rootCapMap.getLong("stlFeeVat")));
			chargeSettlebMap.put("bankFee"	, 0);
			chargeSettlebMap.put("netAmount", Math.abs(rootCapMap.getLong("stlAmount")));
			chargeSettlebMap.put("balance"	, trxDAO.getMchtBalance(rootCapMap.getString("mchtId")).getLong("balance")-Math.abs(rootCapMap.getLong("stlAmount")));
			chargeSettlebMap.put("trackId"	, trxCapMap.getString("trackId"));
			chargeSettlebMap.put("refId"	, trxCapMap.getString("capId"));
			chargeSettlebMap.put("bankCd"	, "");
			chargeSettlebMap.put("bankName"	, "");
			chargeSettlebMap.put("account"	, "");
			chargeSettlebMap.put("holder"	, "");
			chargeSettlebMap.put("recordInfo"	, "");
			
			String stlDay = capDtlMap.getString("stlDay").substring(0, 4)+"-"+capDtlMap.getString("stlDay").substring(4,6)+"-"+capDtlMap.getString("stlDay").substring(6);
			chargeSettlebMap.put("summary"	, stlDay+"정산일자 실시간 신용카드 정산금 지급");
			chargeSettlebMap.put("regId"	, trxCapMap.getString("mchtId"));
			chargeSettlebMap.put("regDay"	, regDate.substring(0, 8));

			insertChargeSettleList.add(chargeSettlebMap);
			logger.info("===================================================");
		} 
	}
	
	
	
	/**
	 * 위험거래로 변경한다.
	 * @param capId
	 */
	public void warning(String trxId){
		SharedMap<String,Object> rootCapMap	 	= trxDAO.getTrxCapId(trxId);
		if(rootCapMap == null){
			//검색된 매입내역이 없으면 리턴한다.
			return;
		}
		
		if(rootCapMap.getString("stlStatus").equals("정산대기") && rootCapMap.getString("stlType").equals("D+1") ){
			if(rootCapMap.isNullOrSpace("risk")){	//원거래가 리스크가 아닌 경우  
				SharedMap<String,Object> updateMap	= new SharedMap<String,Object>();
				
				if(rootCapMap.isEquals("vanId", "OFFLINE")){
					updateMap.put("stlRate"		, rootCapMap.getDouble("stlRate")-rootCapMap.getDouble("stlLoanRate"));		//선정산 수수료 차감
					long normalFee 				= calcFee(rootCapMap.getLong("amount"), updateMap.getDouble("stlRate"));
					long loanFee	 			= calcFee(rootCapMap.getLong("amount"), rootCapMap.getDouble("stlLoanRate"));
					long loanFeeVat				= calcVat(loanFee);
					
					updateMap.put("stlFee"		, normalFee+loanFee);
					updateMap.put("stlFeeVat"	, loanFeeVat);
				}else{
					updateMap.put("stlRate"		, rootCapMap.getDouble("stlRate")-rootCapMap.getDouble("stlLoanRate"));		//선정산 수수료 차감
					updateMap.put("stlFee"		, calcFee(rootCapMap.getLong("amount"), updateMap.getDouble("stlRate")));
					updateMap.put("stlFeeVat"	, calcVat(updateMap.getLong("stlFee")));
					
					
					updateMap.put("stlFeeVat"	, calcVat(updateMap.getLong("stlFee")));
				}
				
				updateMap.put("stlAmount"	, rootCapMap.getLong("amount")-updateMap.getLong("stlFee")-updateMap.getLong("stlFeeVat"));
				updateMap.put("risk", "위험");
				updateMap.put("capId", rootCapMap.getString("capId"));
				
				trxDAO.updateTrxCapDtl(updateMap);
				String[] riskData = {updateMap.getString("capId"),updateMap.getString("capId")+":RISK 설정 ,D+1 ,위험,수수료:"+(rootCapMap.getLong("stlFee")+rootCapMap.getLong("stlFeeVat")) +"->"+(updateMap.getLong("stlFee")+updateMap.getLong("stlFeeVat") )};
				riskList.add(riskData);
			}else{
				String[] riskData = {rootCapMap.getString("capId"),rootCapMap.getString("capId")+":RISK '위험'으로 감지되나 이미 RISK로 설정된 거래 원 RISK : "+rootCapMap.getString("risk")};
				riskList.add(riskData);
			}
		}
		
	}
	
	
	
	public void loadData(SharedMap<String,Object> loadMap,List<SharedMap<String,Object>> loadList ){
		List<SharedMap<String,Object>> payList = new ArrayList<SharedMap<String,Object>>();
		List<SharedMap<String,Object>> rfdList = new ArrayList<SharedMap<String,Object>>();
		List<SharedMap<String,Object>> loadUpdateList = new ArrayList<SharedMap<String,Object>>();
		
		for(SharedMap<String,Object> load : loadList){
			if(load.isEquals("trnType", "승인")){
				SharedMap<String,Object> rootMap = trxDAO.getPayDupliMap(loadMap, load);
				SharedMap<String,Object> rootVanTrxMap = trxDAO.getPayDupliMap2(loadMap, load);
				if(rootVanTrxMap != null && rootVanTrxMap.size() > 0){
					SharedMap<String,Object> loadUpdateMap = new SharedMap<String,Object>();
					loadUpdateMap.put("idx", load.getLong("idx"));
					loadUpdateMap.put("trxId", "");
					loadUpdateMap.put("status", "실패");
					loadUpdateMap.put("summary", "VAN 거래번호 (중복)");
					loadUpdateList.add(loadUpdateMap);
				}else if(rootMap != null && rootMap.size() > 0){
					SharedMap<String,Object> loadUpdateMap = new SharedMap<String,Object>();
					loadUpdateMap.put("idx", load.getLong("idx"));
					loadUpdateMap.put("trxId", "");
					loadUpdateMap.put("status", "실패");
					loadUpdateMap.put("summary", "BIN,LAST4,승인번호 일치거래(중복)");
					loadUpdateList.add(loadUpdateMap);
				}else{
					SharedMap<String,Object> pay = new SharedMap<String,Object>();
					pay.put("trxId"		, trxDAO.getTrxId());
					pay.put("mchtId"	, loadMap.getString("mchtId"));
					pay.put("tmnId"		, loadMap.getString("tmnId"));
					if(load.isNullOrSpace("trackId")){
						pay.put("trackId"	, load.getString("vanTrxId"));
					}else{
						pay.put("trackId"	, load.getString("trackId"));
					}
					
					pay.put("amount"	, load.getLong("amount"));
					pay.put("installment", load.getString("installment"));
					pay.put("cardId"	, GenKey.genKeys(CPKEY.CARD,pay.getString("trxId") ));
					pay.put("bin"		, load.getString("bin"));
					pay.put("last4"		, load.getString("last4"));
					pay.put("status"	, "승인");
					pay.put("prodId"	, GenKey.genKeys(CPKEY.PRODUCT,pay.getString("trxId") ));
					Card card = new Card();
					card.cardId 	= pay.getString("cardId");
					card.installment= pay.getInt("installment");
					card.bin		= pay.getString("bin");
					card.last4		= pay.getString("last4");
					
					
					if(!load.isNullOrSpace("issuer") || !load.isNullOrSpace("acquirer")){
						if(load.isNullOrSpace("issuer")){
							load.put("issuer", load.getString("acquirer"));
						}
						card.cardType = load.getString("cardType").replaceAll("카드", "");
						card.issuer   = CapUtil.getIssuer(load.getString("issuer"));
						card.acquirer = CapUtil.getAcquirer(load.getString("acquirer"));
					}else{
						SharedMap<String,Object> issuerMap = trxDAO.getDBIssuer(card.bin);
						if(issuerMap != null){
							card.cardType = issuerMap.getString("type") ;
							card.issuer = issuerMap.getString("issuer");
							card.acquirer = issuerMap.getString("acquirer");
						}
					}
					String encrypted = Base64.encodeToString(SeedKisa.encrypt(GsonUtil.toJson(card), ByteUtil.toBytes("696d697373796f7568616e6765656e61", 16)));
					trxDAO.insertCard(card.cardId,encrypted);
					
					
					pay.put("cardType"	, card.cardType);
					pay.put("issuer"	, card.issuer);
					pay.put("acquirer"	, card.acquirer);
					pay.put("reqDay"	, load.getString("trxDay"));
					pay.put("reqTime"	, load.getString("trxTime"));
					pay.put("authCd"	, load.getString("authCd"));
					pay.put("resultCd"	, "0000");
					pay.put("resultMsg"	, "정상");
					pay.put("van"		, loadMap.getString("van"));				
					pay.put("vanId"		, loadMap.getString("vanId"));
					pay.put("vanTrxId"	, load.getString("vanTrxId"));
					pay.put("regDay"	, load.getString("trxDay"));
					pay.put("regTime"	, load.getString("trxTime"));
					pay.put("regDate"	, CommonUtil.getCurrentTimestamp());
					payList.add(pay);
					
					SharedMap<String,Object> loadUpdateMap = new SharedMap<String,Object>();
					loadUpdateMap.put("idx", load.getLong("idx"));
					loadUpdateMap.put("trxId", pay.getString("trxId"));
					loadUpdateMap.put("status", "완료");
					loadUpdateMap.put("summary", "성공");
					loadUpdateList.add(loadUpdateMap);
				}
			}
		}
		
		logger.info("trxPay inserted : {}",trxBatchDAO.insertTrxPay(payList));
		payList = new ArrayList<SharedMap<String,Object>>();
		
		for(SharedMap<String,Object> load : loadList){
			// 승인취소 요청
			if(load.isEquals("trnType", "승인취소")){
				
				SharedMap<String,Object> rfd = new SharedMap<String,Object>();
				SharedMap<String,Object> rootCapMap = trxDAO.getTrxCap2(loadMap, load);
				SharedMap<String,Object> rootPayMap = trxDAO.getTrxPay2(loadMap, load);
				SharedMap<String,Object> rootVanTrxMap = trxDAO.getRfdDupliMap(loadMap, load);
				logger.info("root status : [{}]",rootPayMap.getString("status"));
				
				long amount = load.getLong("amount");
				
				if(amount > 0){
					amount = - amount;
				}
				
				// 입력된 VAN 거래번호가 없거나 VAN 거래번호 중복 시 - 실패
				if(rootVanTrxMap != null && rootVanTrxMap.size() > 0){
					SharedMap<String,Object> loadUpdateMap = new SharedMap<String,Object>();
					loadUpdateMap.put("idx", load.getLong("idx"));
					loadUpdateMap.put("trxId", "");
					loadUpdateMap.put("status", "실패");
					loadUpdateMap.put("summary", "VAN 거래번호 (중복)");
					loadUpdateList.add(loadUpdateMap);
					
				// 처음 승인취소 시
				}else if(rootPayMap.isEquals("status", "승인") && amount >= -rootCapMap.getLong("amount")){
					rfd.put("trxId", trxDAO.getTrxId());
					rfd.put("mchtId", rootCapMap.getString("mchtId"));
					rfd.put("tmnId", rootCapMap.getString("tmnId"));
					
					if(load.isNullOrSpace("trackId")){
						rfd.put("trackId"	, load.getString("vanTrxId"));
					}else{
						rfd.put("trackId"	, load.getString("trackId"));
					}
					
					rfd.put("status", "완료");
					
					if(amount == -rootCapMap.getLong("amount")){
						rfd.put("rfdAll", "전액");
					}else{
						rfd.put("rfdAll", "부분");
					}
					
					rfd.put("rfdAmount", amount);
					rfd.put("rfdVat", calcRootVat(amount));
					rfd.put("cardId", rootCapMap.getString("cardId"));
					rfd.put("bin", rootCapMap.getString("bin"));
					rfd.put("last4", rootCapMap.getString("last4"));
					rfd.put("issuer", rootCapMap.getString("issuer"));
					rfd.put("acquirer", rootCapMap.getString("acquirer"));
					rfd.put("rootTrnDay", rootCapMap.getString("regDay"));
					rfd.put("rootTrxId", rootCapMap.getString("trxId"));
					rfd.put("rootTrackId", rootCapMap.getString("trackId"));
					rfd.put("rootAmount", rootCapMap.getLong("amount"));
					rfd.put("rootVat", calcRootVat(rootCapMap.getLong("amount")));
					rfd.put("reqDay", load.getString("trxDay"));
					rfd.put("reqTime", load.getString("trxTime"));
					rfd.put("authCd", rootCapMap.getString("authCd"));
					rfd.put("resultCd"	, "0000");
					rfd.put("resultMsg"	, "정상");
					rfd.put("van"		, loadMap.getString("van"));				
					rfd.put("vanId"		, loadMap.getString("vanId"));
					rfd.put("vanTrxId"	, load.getString("vanTrxId"));
					rfd.put("regDay"	, load.getString("trxDay"));
					rfd.put("regTime"	, load.getString("trxTime"));
					rfd.put("regDate"	, CommonUtil.getCurrentTimestamp());
					
					
					rfdList.add(rfd);
					SharedMap<String,Object> loadUpdateMap = new SharedMap<String,Object>();
					loadUpdateMap.put("idx", load.getLong("idx"));
					loadUpdateMap.put("trxId", rfd.getString("trxId"));
					loadUpdateMap.put("status", "완료");
					loadUpdateList.add(loadUpdateMap);
					
				} else{
					List<SharedMap<String, Object>> trxRfdList = trxDAO.getTrxRfd2(loadMap, load);
					
					// 2차 이상 추가 승인취소 시도 시
					if(rootPayMap.isEquals("status", "승인취소")) {
						
						int i=0;
						int size = trxRfdList.size();
						Long totAmount = (long) 0;
						String rfdAll = "";
						
						// 1차 승인취소 데이터가 존재하면
						if(size > 0){
							
							for(SharedMap<String,Object> trxRfdMap : trxRfdList ){
								
								trxRfdMap.put("rfdAll", trxRfdList.get(i).getString("rfdAll"));
								if(trxRfdMap.isEquals("rfdAll", "전액")) {
									rfdAll = "전액";
								}
								trxRfdMap.put("rfdAmount", trxRfdList.get(i).getLong("rfdAmount"));
								totAmount += trxRfdMap.getLong("rfdAmount");
								i++;
							}
							
							// 원거래 금액이 [이전 부분취소 금액 + 현재 부분취소 금액] 보다 크거나 같고, 이전 승인취소들 중 전액 취소가 없을 경우(모두 부분 취소인 경우)
							if((rootCapMap.getLong("amount") >= -(totAmount + amount)) && !rfdAll.equals("전액")){
								
								rfd.put("trxId", trxDAO.getTrxId());
								rfd.put("mchtId", rootCapMap.getString("mchtId"));
								rfd.put("tmnId", rootCapMap.getString("tmnId"));
								
								if(load.isNullOrSpace("trackId")){
									rfd.put("trackId"	, load.getString("vanTrxId"));
								}else{
									rfd.put("trackId"	, load.getString("trackId"));
								}
								
								rfd.put("status", "완료");
								rfd.put("rfdAll", "부분");
								rfd.put("rfdAmount", amount);
								rfd.put("rfdVat", calcRootVat(amount));
								rfd.put("cardId", rootCapMap.getString("cardId"));
								rfd.put("bin", rootCapMap.getString("bin"));
								rfd.put("last4", rootCapMap.getString("last4"));
								rfd.put("issuer", rootCapMap.getString("issuer"));
								rfd.put("acquirer", rootCapMap.getString("acquirer"));
								rfd.put("rootTrnDay", rootCapMap.getString("regDay"));
								rfd.put("rootTrxId", rootCapMap.getString("trxId"));
								rfd.put("rootTrackId", rootCapMap.getString("trackId"));
								rfd.put("rootAmount", rootCapMap.getLong("amount"));
								rfd.put("rootVat", calcRootVat(rootCapMap.getLong("amount")));
								rfd.put("reqDay", load.getString("trxDay"));
								rfd.put("reqTime", load.getString("trxTime"));
								rfd.put("authCd", rootCapMap.getString("authCd"));
								rfd.put("resultCd"	, "0000");
								rfd.put("resultMsg"	, "정상");
								rfd.put("van"		, loadMap.getString("van"));				
								rfd.put("vanId"		, loadMap.getString("vanId"));
								rfd.put("vanTrxId"	, load.getString("vanTrxId"));
								rfd.put("regDay"	, load.getString("trxDay"));
								rfd.put("regTime"	, load.getString("trxTime"));
								rfd.put("regDate"	, CommonUtil.getCurrentTimestamp());
								
								
								rfdList.add(rfd);
								SharedMap<String,Object> loadUpdateMap = new SharedMap<String,Object>();
								loadUpdateMap.put("idx", load.getLong("idx"));
								loadUpdateMap.put("trxId", rfd.getString("trxId"));
								loadUpdateMap.put("status", "완료");
								loadUpdateList.add(loadUpdateMap);
							
							// 합계 금액이 원거래 금액보다 크거나, 이전 승인취소가 전액 취소인 경우
							}  else {
								SharedMap<String,Object> loadUpdateMap = new SharedMap<String,Object>();
								loadUpdateMap.put("idx", load.getLong("idx"));
								loadUpdateMap.put("trxId", "");
								loadUpdateMap.put("status", "실패");
								loadUpdateMap.put("summary", "합계 금액이 원거래 금액보다 크거나 이전 승인취소가 전액 취소 : "+rootPayMap.getString("status"));
								loadUpdateList.add(loadUpdateMap);
							}
						
						// 취소 데이터가 존재하지 않을 시
						}  else {
							SharedMap<String,Object> loadUpdateMap = new SharedMap<String,Object>();
							loadUpdateMap.put("idx", load.getLong("idx"));
							loadUpdateMap.put("trxId", "");
							loadUpdateMap.put("status", "실패");
							loadUpdateMap.put("summary", "승인취소된 상태이지만 승인취소 데이터가 없음 : "+rootPayMap.getString("status"));
							loadUpdateList.add(loadUpdateMap);
						}
						
					} else {
						SharedMap<String,Object> loadUpdateMap = new SharedMap<String,Object>();
						loadUpdateMap.put("idx", load.getLong("idx"));
						loadUpdateMap.put("trxId", "");
						loadUpdateMap.put("status", "실패");
						loadUpdateMap.put("summary", "승인내역이 없거나 기취소 거래 : "+rootPayMap.getString("status"));
						loadUpdateList.add(loadUpdateMap);
					}
				}
			}
		}
		
		logger.info("trxRfd inserted : {}",trxBatchDAO.insertTrxRfd(rfdList));
		rfdList = new ArrayList<SharedMap<String,Object>>();
		
		
		logger.info("load set updated : {}",trxBatchDAO.setLoadDtl(loadUpdateList));
		int success = 0;
		int failure = 0;
		for(SharedMap<String,Object> loadUpdateMap : loadUpdateList){
			if(loadUpdateMap.isEquals("status","완료")){
				success++;
			}else{
				failure++;
			}
		}
		
		trxDAO.setTrxLoadStatus(loadMap.getLong("idx"), "완료", CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss") +" 총:"+(success+failure)+", 성공:"+success+", 실패:"+failure);
		
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
		try {
			if(settleType.equals("D+0") || settleType.equals("C+0")) {
				return today;
			}
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
			}else if(settleType.startsWith("C")){
					term = CommonUtil.parseInt(settleType.replaceAll("C[+]", ""));
					String day =  trxDAO.getSettleDay(today, term);
					/*
				//오늘 정산 예정일이지만 8시 이후에 요청된 거래는 자동으로 내일로 정산일정이 밀린다.
				if(day.equals(currentDay) && CommonUtil.parseInt(CommonUtil.getCurrentDate("HH")) > 8){
					day =  trxDAO.getSettleDay(currentDay,1);
				}*/
					return day;
			}else if(settleType.startsWith("A")){
				term = CommonUtil.parseInt(settleType.replaceAll("A[+]", ""));
				String day = "";
				
				if(term == 0) {
					day = today;
					
					String status = trxDAO.getHolidayCheck(today);
					
					//휴일이면 다음영업일로 정산예정일 세팅
					if("yes".equals(status)) {
						day =  trxDAO.getSettleDay(today, 1);
					}else {
						//A+0은 당일정산으로 00~15시는 17시정산, 15~00시는 다음영업일 10시정산
						if(CommonUtil.parseInt(CommonUtil.getCurrentDate("HH")) >= 15){
							day =  trxDAO.getSettleDay(today, 1);
						}
					}
				}else {
					day = CommonUtil.getOpDate(GregorianCalendar.DATE,term,today);
				}

				return day;
			}else if(settleType.startsWith("B")){
				term = CommonUtil.parseInt(settleType.replaceAll("B[+]", ""));
				
				String day = CommonUtil.getOpDate(GregorianCalendar.DATE,term,today);
				return day;
			}else if(settleType.startsWith("M")){
				term = CommonUtil.parseInt(settleType.replaceAll("M[+]", ""));
				String nextMonth = CommonUtil.getOpDate(GregorianCalendar.MONTH,1,today).substring(0,6);
				return trxDAO.getSettleDay(nextMonth+CommonUtil.zerofill(term,2));
			}else{
				return "";
			}
		}catch(Exception e) {
			logger.error("calcDay Error : [{}][{}]", e.getMessage(), e.getStackTrace());
			
			return "";
		}
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
	
	/**
	 * 실시간정산 건 처리로직 
	 * @param trxPayMap
	 * @param trxType
	 */
	public void realTimePay(SharedMap<String,Object> trxPayMap, String trxType) {
		try {
			if("0".equals(trxType)) {
				//결제성공일 경우
				if ("0000".contentEquals(trxPayMap.getString("resultCd"))) {
					SharedMap<String,Object> mchtSvcMap = trxDAO.getRealTimeMchtSvc(trxPayMap.getString("mchtId"));
					SharedMap<String,Object> mchtMngMap = trxDAO.getMchtMngByMchtId(trxPayMap.getString("mchtId"));
					
					logger.info("settle : {}", mchtSvcMap.getString("settle"));
					logger.info("settle TYPE : {}", mchtMngMap.getString("settleType"));
					
					//정상결제 완료 건인데 실시간 전송 가맹점의 거래건일 경우 실시간 정산 승인거래 원장에 저장
					if(mchtSvcMap.isEquals("settle", "실시간정산") && mchtMngMap.isEquals("settleType", "D+0")) {
						logger.info("===================================================");
						logger.info("insertTrxRealTimePay 테이블 INSERT");
						trxDAO.insertTrxRealTimePay(trxPayMap, mchtMngMap);
						logger.info("===================================================");
					} 
				}
			}else if("1".equals(trxType)) {
				//취소건이 실시간 전송 거래건일 경우 실시간 정산 테이블에 취소거래 저장
				SharedMap<String, Object> realtimeTrx = trxDAO.getRealtimeTrx(trxPayMap.getString("rootTrxId"));

				if(realtimeTrx != null) {
					logger.info("===================================================");
					logger.info("실시간정산 취소 처리 : [{}][{}]", trxPayMap.getString("trxId"), trxPayMap.getString("resultCd"));
					
					logger.info("insertTrxRealTimePay 테이블 취소데이터 INSERT");
					trxDAO.insertRefundTrxRealTimePay(trxPayMap, realtimeTrx);
					/*
					 * 실시간 승인 건 출금전에 취소가 들어올 경우 문제가 있어서 실시간 배치에서 처리하도록 수정
					 * SharedMap<String, Object> realtimePayoutTrx =
					 * trxDAO.getRealtimePayout(trxPayMap.getString("rootTrxId"));
					 * 
					 * trxDAO.insertRefundRealTimePayOut(trxPayMap, realtimePayoutTrx);
					 */
					logger.info("===================================================");
				}
				
			}
		}catch(Exception e) {
			logger.error("realTimePay Error : [{}][{}]", e.getMessage(), e.getStackTrace());
		}
	}
}

