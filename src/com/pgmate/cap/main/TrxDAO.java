package com.pgmate.cap.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pgmate.cap.cache.Cache;
import com.pgmate.lib.dao.DAO;
import com.pgmate.lib.dao.RecordSet;
import com.pgmate.lib.util.db.DBFactory;
import com.pgmate.lib.util.db.DBManager;
import com.pgmate.lib.util.lang.CommonUtil;
import com.pgmate.lib.util.map.SharedMap;

/**
 * @author Administrator
 *
 */
public class TrxDAO extends DAO {

	private static Logger logger = LoggerFactory.getLogger(com.pgmate.cap.main.TrxDAO.class);
	
	
	public TrxDAO() {
		// TODO Auto-generated constructor stub
	}
	
	

	public List<SharedMap<String, Object>> getTrxPay() { 
		super.setTable("VW_TRX_PAY_NOTCAP");
		super.setColumns("*");
		super.setLimit(1000);
		super.addWhere("vanId", "FACTORING",DAO.ne);
		super.setOrderBy("regDay,regTime asc");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRows();
	}
	
	
	public List<SharedMap<String, Object>> getTrxRfd() {
		super.setTable("VW_TRX_RFD_NOTCAP");
		super.setColumns("*");
		super.addWhere("vanId", "FACTORING",DAO.ne);
		super.setOrderBy("regDay,regTime asc");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRows();
	}
	
	
	public List<SharedMap<String, Object>> getTrxPay(String day) {
		super.setTable("VW_TRX_PAY_NOTCAP");
		super.setColumns("*");
		super.addWhere("vanId", "FACTORING",DAO.ne);
		super.addWhere("regDay", day);
		super.setOrderBy("regDay,regTime asc");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRows();
	}
	
	
	public List<SharedMap<String, Object>> getTrxRfd(String day) {
		super.setTable("VW_TRX_RFD_NOTCAP");
		super.setColumns("*");
		super.addWhere("vanId", "FACTORING",DAO.ne);
		super.addWhere("regDay", day);
		super.setOrderBy("regDay,regTime asc");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRows();
	}
	
	
	public List<SharedMap<String, Object>> getTrxPayFactoring() {
		super.setTable("VW_TRX_PAY_NOTCAP");
		super.setColumns("*");
		super.addWhere("vanId", "FACTORING");
		super.setLimit(1000);
		super.setOrderBy("regDay,regTime asc");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRows();
	}
	
	
	public List<SharedMap<String, Object>> getTrxRfdFactoring() {
		super.setTable("VW_TRX_RFD_NOTCAP");
		super.setColumns("*");
		super.addWhere("vanId", "FACTORING");
		super.setOrderBy("regDay,regTime asc");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRows();
	}
	
	
	public List<SharedMap<String, Object>> getTrxPayFactoring(String day) {
		super.setTable("VW_TRX_PAY_NOTCAP");
		super.setColumns("*");
		super.addWhere("vanId", "FACTORING");
		super.addWhere("regDay", day);
		super.setOrderBy("regDay,regTime asc");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRows();
	}
	
	
	public List<SharedMap<String, Object>> getTrxRfdFactoring(String day) {
		super.setTable("VW_TRX_RFD_NOTCAP");
		super.setColumns("*");
		super.addWhere("vanId", "FACTORING");
		super.addWhere("regDay", day);
		super.setOrderBy("regDay,regTime asc");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRows();
	}
	
	public SharedMap<String, Object> getMchtTmnByTmnId(String tmnId) {
		String key = "PG_MCHT_TMN_" + tmnId;
		logger.info(key);
		if (Cache.map.containsKey(key)) {
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("PG_MCHT_TMN");
			super.setColumns("*");
			super.addWhere("tmnId", tmnId, eq);
			RecordSet rset = super.search();
			super.initRecord();
			if(rset.size() > 0){
			return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
		}
	}
	
	public SharedMap<String, Object> getMchtByMchtId(String mchtId) {
		String key = "PG_MCHT_" + mchtId;
		if (Cache.map.containsKey(key)) {
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("PG_MCHT");
			super.setColumns("*");
			super.addWhere("mchtId", mchtId, eq);
			RecordSet rset = super.search();
			super.initRecord();
			if(rset.size() > 0){
				return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
			
		}
	}

	public SharedMap<String, Object> getMchtMngByMchtId(String mchtId) {
		String key = "PG_MCHT_MNG_" + mchtId;
		if (Cache.map.containsKey(key)) {
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("PG_MCHT_MNG");
			super.setColumns("*");
			super.addWhere("mchtId", mchtId, eq);
			RecordSet rset = super.search();
			super.initRecord();
			if(rset.size() > 0){
				return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
		}
	}

	public SharedMap<String, Object> getMchtTaxByTaxId(String taxId) {
		super.setTable("PG_MCHT_TAX");
		super.setColumns("*");
		super.addWhere("taxId", taxId, eq);
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}

	public SharedMap<String, Object> getAgencyMngById(String agencyId) {
		String key = "PG_MAM_AGENCY_MNG_" + agencyId;
		if (Cache.map.containsKey(key)) {
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("PG_MAM_AGENCY_MNG");
			super.setColumns("*");
			super.addWhere("agencyId", agencyId, eq);
			RecordSet rset = super.search();
			super.initRecord();
			if(rset.size() > 0){
				return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
		}
	}

	public SharedMap<String, Object> getDistMngById(String distId) {
		String key = "PG_MAM_DIST_MNG_" + distId;
		if (Cache.map.containsKey(key)) {
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("PG_MAM_DIST_MNG");
			super.setColumns("*");
			super.addWhere("distId", distId, eq);
			RecordSet rset = super.search();
			super.initRecord();
			if(rset.size() > 0){
				return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
		}
	}

	public SharedMap<String, Object> getSalesMngById(String salesId) {
		super.initRecord();
		String key = "PG_MAM_SALES_MNG_" + salesId;
		if (Cache.map.containsKey(key)) {
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("PG_MAM_SALES_MNG");
			super.setColumns("*");
			super.addWhere("salesId", salesId, eq);
			RecordSet rset = super.search();
			super.initRecord();
			if(rset.size() > 0){
				return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
		}
	}
	
	
	public SharedMap<String, Object> getOrgFee(String van) {
		if(van == null){
			return new SharedMap<String,Object>();
		}
		String key = "PG_ORG_FEE_" + van;
		if (Cache.map.containsKey(key)) {
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("PG_ORG_FEE");
			super.setColumns("*");
			super.addWhere("van", van, eq);
			RecordSet rset = super.search();
			super.initRecord();
			if(rset.size() > 0){
				return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
		}
	}

	public SharedMap<String, Object> getMchtRentByMchtId(String mchtId) {
		super.setTable("PG_MCHT_RENT");
		super.setColumns("*");
		super.addWhere("mchtId", mchtId, eq);
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}

	public SharedMap<String, Object> getTrxRentById(String rentId) {
		super.setTable("PG_TRX_RENT");
		super.setColumns("*");
		super.addWhere("rentId", rentId, eq);
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}
	
	public SharedMap<String, Object> getMchtTmnDtlByTmnId(String tmnId) {
		String key = "PG_MCHT_TMN_DTL_" + tmnId;
		if (Cache.map.containsKey(key)) {
			logger.debug("get key : {}", key);
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("PG_MCHT_TMN_DTL");
			super.setColumns("*");
			super.addWhere("tmnId", tmnId, eq);
			RecordSet rset = super.search();
			super.initRecord();
			logger.debug("load key : {}", key);
			if(rset.size() > 0){
				return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
		}
	}
	
	public String getSettleDay(String today,int term) {	
		String start = CommonUtil.toString(term-1);
		String q = "SELECT days FROM PG_CODE_HOLIDAY WHERE days > '"+today+"' AND status ='no' limit "+start+",1";
		RecordSet rset = super.query(q);
		super.initRecord();
		return rset.getRow(0).getString("days");
	}
	
	
	public String getSettleDay(String today) {	
		String q = "SELECT days FROM PG_CODE_HOLIDAY WHERE days >= '"+today+"' AND status ='no' limit 1";
		RecordSet rset = super.query(q);
		super.initRecord();
		return rset.getRow(0).getString("days");
	}

	public String getSalesDay(String today,String term) {
		String q = "select MAX(days) AS days from PG_CODE_HOLIDAY where days <= '"+today+"' -"+term+" and status = 'no'";
		RecordSet rset = super.query(q);
		super.initRecord();
		return rset.getRow(0).getString("days");
	}
	
	
	public String getDuplicated(SharedMap<String,Object> trxPayMap){
		super.setTable("PG_TRX_CAP A, PG_TRX_PAY B ");
		super.setColumns("capId");
		super.setWhere("A.trxId = B.trxId AND STR_TO_DATE(concat(A.regDay,A.regTime),'%Y%m%d%H%i%s') > DATE_ADD('"+trxPayMap.getString("reqDay")+trxPayMap.getString("reqTime")+"',INTERVAL -11 MINUTE)");
		super.addWhere("A.regDay"	,trxPayMap.getString("regDay"),eq);
		super.addWhere("A.mchtId"	,trxPayMap.getString("mchtId"),eq);
		super.addWhere("A.bin"	 	,trxPayMap.getString("bin"),eq);
		super.addWhere("A.last4"	,trxPayMap.getString("last4"),eq);
		super.addWhere("A.amount"	,trxPayMap.getLong("amount"),eq);
		super.addWhere("B.status"	,"승인",eq);
		super.setOrderBy("capId desc");
		
		RecordSet rset = super.search();
		super.initRecord();
		
		if(rset.size() > 0){
			return "중복";
		}else{
			return "";
		}
		
	}
	
	public String getDuplicatedDaily(SharedMap<String,Object> trxPayMap){
		super.setTable("PG_TRX_CAP A, PG_TRX_PAY B ");
		super.setColumns("capId");
		super.setWhere("A.trxId = B.trxId");
		super.addWhere("A.regDay"	,trxPayMap.getString("regDay"),eq);
		super.addWhere("A.mchtId"	,trxPayMap.getString("mchtId"),eq);
		super.addWhere("A.bin"	 	,trxPayMap.getString("bin"),eq);
		super.addWhere("A.last4"	,trxPayMap.getString("last4"),eq);
		super.addWhere("B.status"	,"승인",eq);
		super.setOrderBy("capId desc");
		
		RecordSet rset = super.search();
		super.initRecord();
		
		if(rset.size() > 0){
			return "1일중복";
		}else{
			return "";
		}
		
	}
	
	
	public String getDuplicatedFact(SharedMap<String,Object> trxPayMap){
		super.setTable("PG_TRX_PAY ");
		super.setColumns("trxId");
		super.addWhere("regDay"	,trxPayMap.getString("regDay"),eq);
		super.addWhere("mchtId"	,trxPayMap.getString("mchtId"),eq);
		super.addWhere("bin"	 ,trxPayMap.getString("bin"),eq);
		super.addWhere("last4"	,trxPayMap.getString("last4"),eq);
		super.addWhere("amount"	,trxPayMap.getLong("amount"),eq);
		super.addWhere("status"	,"승인",eq);
		super.setOrderBy("trxId desc");
		
		RecordSet rset = super.search();
		super.initRecord();
		
		if(rset.size() > 1){
			return "중복";
		}else{
			return "";
		}
		
	}
	
	public String getDuplicatedFactDaily(SharedMap<String,Object> trxPayMap){
		super.setTable("PG_TRX_PAY ");
		super.setColumns("trxId");
		super.addWhere("regDay"	,trxPayMap.getString("regDay"),eq);
		super.addWhere("mchtId"	,trxPayMap.getString("mchtId"),eq);
		super.addWhere("bin"	 ,trxPayMap.getString("bin"),eq);
		super.addWhere("last4"	,trxPayMap.getString("last4"),eq);
		super.addWhere("status"	,"승인",eq);
		super.setOrderBy("trxId desc");
		
		RecordSet rset = super.search();
		super.initRecord();
		
		if(rset.size() > 1){
			return "1일중복";
		}else{
			return "";
		}
		
	}
	
	
	public String getWarning(SharedMap<String,Object> trxPayMap){
		super.setTable("PG_TRX_CAP A, PG_TRX_PAY B ");
		super.setColumns("capId,A.amount");
		super.setWhere("A.trxId = B.trxId");
		super.addWhere("A.regDay"	,trxPayMap.getString("regDay"),eq);
		super.addWhere("A.mchtId"	,trxPayMap.getString("mchtId"),eq);
		super.addWhere("A.bin"	 	,trxPayMap.getString("bin"),eq);
		super.addWhere("A.last4"	,trxPayMap.getString("last4"),eq);
		super.addWhere("B.status"	,"승인",eq);
		super.setOrderBy("capId desc");
		
		RecordSet rset = super.search();
		super.initRecord();
		
		if(rset.size() > 0){
			SharedMap<String,Object> data = rset.getRow(0);
			long rootAmount = data.getLong("amount");
			if(rootAmount + trxPayMap.getLong("amount") >= 1000000){
				logger.info("위험거래 WARNING : {}, old :{},new : {}",data.getString("capId"),rootAmount,trxPayMap.getLong("amount"));
				return data.getString("capId");
			}
			
			return "";
		}else{
			return "";
		}
		
	}
	
	public String getWarningFact(SharedMap<String,Object> trxPayMap){
		super.setTable("PG_TRX_PAY");
		super.setColumns("trxId,amount");
		super.addWhere("regDay"	,trxPayMap.getString("regDay"),eq);
		super.addWhere("mchtId"	,trxPayMap.getString("mchtId"),eq);
		super.addWhere("bin"	 	,trxPayMap.getString("bin"),eq);
		super.addWhere("last4"	,trxPayMap.getString("last4"),eq);
		super.addWhere("status"	,"승인",eq);
		//22.10.25 pay에서 현재 거래건을 가져오지 않게하기 위해 조건문 추가
		super.addWhere("trxId", trxPayMap.getString("trxId"),lt);
		super.setOrderBy("trxId desc");
		
		RecordSet rset = super.search();
		super.initRecord();
		
		if(rset.size() > 0){
			SharedMap<String,Object> data = rset.getRow(0);
			long rootAmount = data.getLong("amount");
			if(rootAmount + trxPayMap.getLong("amount") >= 1000000){
				logger.info("위험거래 WARNING : {}, old :{},new : {}",data.getString("trxId"),rootAmount,trxPayMap.getLong("amount"));
				return data.getString("trxId");
			}
			
			return "";
		}else{
			return "";
		}
		
	}
	
	
	public SharedMap<String,Object> getTrxCap(String trxId){
		super.setTable("VW_TRX_CAP");
		super.setColumns("*");
		super.addWhere("trxId"	,trxId,eq);
		
		RecordSet rset = super.search();
		super.initRecord();
		
		return rset.getRow(0);
	}
	
	public SharedMap<String,Object> getTrxCapId(String capId){
		super.setTable("VW_TRX_CAP");
		super.setColumns("*");
		super.addWhere("capId"	,capId,eq);
		
		RecordSet rset = super.search();
		super.initRecord();
		
		return rset.getRow(0);
	}
	
	
	
	public String getCapId() {
		return "C" + getFunction("FN_NEXTVAL2", "CAP");
	}
	
	public  String getTrxId() {
		return "T" + getFunction("FN_NEXTVAL2", "TRN");
	}
	
	public String getFunction(String function, String value) {
		String returnVal = "";
		String query = "SELECT " + function + "(?) as val";

		DBManager db = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rset = null;

		try {

			db = DBFactory.getInstance();
			conn = db.getConnection();
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, value);
			rset = pstmt.executeQuery();

			while (rset.next()) {
				returnVal = rset.getString(1);
			}
			conn.commit();
		} catch (Exception t) {
			logger.debug("sql error : {}, query : {}", t.getMessage(), query);
		} finally {
			db.close(conn, pstmt, rset);
		}
		return returnVal;
	}
	
	
	public void updateTrxCapDtl(SharedMap<String,Object> updateMap){
		super.setTable("PG_TRX_CAP_DTL");
		super.setRecord("stlRate"	, updateMap.getDouble("stlRate"));
		/*
		if(!updateMap.isNullOrSpace("stlDay")){
			super.setRecord("stlDay"	, updateMap.getString("stlDay"));
		}*/
		super.setRecord("stlFee"	, updateMap.getLong("stlFee"));
		super.setRecord("stlFeeVat"	, updateMap.getLong("stlFeeVat"));
		super.setRecord("stlAmount"	, updateMap.getLong("stlAmount"));
		
		super.setRecord("risk"	, updateMap.getString("risk"));
		super.addWhere("capId", updateMap.getString("capId"));
		
		logger.info("update UNRISK PG_TRX_CAP_DTL SET : {},{}", updateMap.getString("capId"), super.update());
		super.initRecord();
	}
	
	public void updateTrxCapDtlWithAgency(SharedMap<String,Object> updateMap){
		super.setTable("PG_TRX_CAP_DTL");
		super.setRecord("stlRate"	, updateMap.getDouble("stlRate"));
		super.setRecord("stlFee"	, updateMap.getLong("stlFee"));
		super.setRecord("stlFeeVat"	, updateMap.getLong("stlFeeVat"));
		super.setRecord("stlAmount"	, updateMap.getLong("stlAmount"));
		super.setRecord("stlAgencyFee"	, updateMap.getLong("stlAgencyFee"));
		super.setRecord("risk"	, updateMap.getString("risk"));
		super.addWhere("capId", updateMap.getString("capId"));
		
		logger.info("update UNRISK PG_TRX_CAP_DTL SET : {},{}", updateMap.getString("capId"), super.update());
		super.initRecord();
	}
	
	
	
	public int setReserveRate() {
		int val =0;
		String query = "SELECT FN_RESERVE_RATE() as val";

		DBManager db = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rset = null;

		try {

			db = DBFactory.getInstance();
			conn = db.getConnection();
			pstmt = conn.prepareStatement(query);
			rset = pstmt.executeQuery();

			while (rset.next()) {
				val = rset.getInt(1);
			}
			conn.commit();
		} catch (Exception t) {
			logger.debug("sql error : {}, query : {}", t.getMessage(), query);
		} finally {
			db.close(conn, pstmt, rset);
		}
		return val;
	}

	public int setReserveVactRate() {
		int val =0;
		String query = "SELECT FN_RESERVE_VACT_RATE() as val";

		DBManager db = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rset = null;

		try {

			db = DBFactory.getInstance();
			conn = db.getConnection();
			pstmt = conn.prepareStatement(query);
			rset = pstmt.executeQuery();

			while (rset.next()) {
				val = rset.getInt(1);
			}
			conn.commit();
		} catch (Exception t) {
			logger.debug("sql error : {}, query : {}", t.getMessage(), query);
		} finally {
			db.close(conn, pstmt, rset);
		}
		return val;
	}
	
	public SharedMap<String, Object> getTrxLoad() {
		super.setTable("PG_TRX_LOAD");
		super.setColumns("*");
		super.setLimit(1);
		super.addWhere("status", "요청");
		super.setOrderBy("idx asc");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}
	
	
	
	public void setTrxLoadStatus(long idx , String status,String summary){
		super.setTable("PG_TRX_LOAD");
		super.setRecord("status", status);
		super.setRecord("summary", summary);
		super.addWhere("idx",idx);
		
		logger.info("update PG_TRX_LOAD SET : {},{}", idx, super.update());
		super.initRecord();
		return;
	}
	
	
	
	public List<SharedMap<String, Object>> getTrxLoadDtl(long batchIdx) {
		super.setTable("PG_TRX_LOAD_DTL");
		super.setColumns("*");
		super.setLimit(99999999);
		super.addWhere("batchIdx", batchIdx);
		super.setOrderBy("idx asc");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRows();
	}
	
	
	public void insertCard(String cardId, String value) {

		super.setTable("PG_TRX_BOX");

		super.setRecord("cardId", cardId);//1개
		super.setRecord("value", value);
		logger.info("set card : {}", super.insert());
		super.initRecord();

	}
	
	
	public SharedMap<String,Object> getDBIssuer(String bin){
		SharedMap<String,Object> issuerMap = new SharedMap<String,Object>();
		if(CommonUtil.isNullOrSpace(bin)){
			return issuerMap;
		}
		
		super.setTable("PG_CODE_BIN");
		super.addWhere("bin", bin, eq);
		super.setColumns("*");
		RecordSet rset = super.search();
		super.initRecord();
	
		if(rset.size() == 0){
			issuerMap.put("bin", bin);
			issuerMap.put("issuer", "기타");
			issuerMap.put("type", "신용");
			return issuerMap;
		}else{
			issuerMap = rset.getRowFirst();
		}
		return issuerMap;
	}
	
	
	public SharedMap<String,Object> getPayMap(SharedMap<String,Object> loadMap,SharedMap<String,Object> load){
		super.setTable("PG_TRX_PAY");
		if(!load.isNullOrSpace("rootTrxDay")){
			super.addWhere("reqDay", load.getString("rootTrxDay"), eq);
		}
		super.addWhere("mchtId", loadMap.getString("mchtId"), eq);
		super.addWhere("tmnId", loadMap.getString("tmnId"), eq);
		if(!load.isNullOrSpace("bin")){
			super.addWhere("bin", load.getString("bin"), eq);
		}
		if(!load.isNullOrSpace("last4")){
			super.addWhere("last4", load.getString("last4"), eq);
		}
		super.addWhere("authCd", load.getString("authCd"), eq);
		super.addWhere("van", loadMap.getString("van"), eq);
//		if(load.getLong("amount") < 0){
//			super.addWhere("amount", -load.getLong("amount"), eq);
//		}
		
		super.setColumns("*");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}
	
//	public SharedMap<String,Object> getPayMap2(SharedMap<String,Object> loadMap,SharedMap<String,Object> load){
//		super.setTable("PG_TRX_PAY A join PG_TRX_CAP B on A.trxId = B.trxId");
//		if(!load.isNullOrSpace("rootTrxDay")){
//			super.addWhere("A.reqDay", load.getString("rootTrxDay"), eq);
//		}
//		super.addWhere("A.mchtId", loadMap.getString("mchtId"), eq);
//		super.addWhere("A.tmnId", loadMap.getString("tmnId"), eq);
////		if(!load.isNullOrSpace("bin")){
////			super.addWhere("A.bin", load.getString("bin"), eq);
////		}
////		if(!load.isNullOrSpace("last4")){
////			super.addWhere("B.last4", load.getString("last4"), eq);
////		}
//		super.addWhere("A.authCd", load.getString("authCd"), eq);
//		super.addWhere("A.trxId", load.getString("rootTrxId"), eq);
//		super.addWhere("A.van", loadMap.getString("van"), eq);
////		if(load.getLong("amount") < 0){
////			super.addWhere("amount", -load.getLong("amount"), eq);
////		}
//		
//		super.setColumns("A.status, A.amount, A.mchtId, A.tmnId, A.cardId, A.bin, A.issuer, A.acquirer, A.reqDay, A.trxId, A.trackId, B.last4");
//		super.setOrderBy("A.regDay desc");
//		RecordSet rset = super.search();
//		super.initRecord();
//		return rset.getRowFirst();
//	}
	
	public SharedMap<String,Object> getTrxCap2(SharedMap<String,Object> loadMap,SharedMap<String,Object> load){
		super.setTable("VW_TRX_CAP");
		super.addWhere("trxId", loadMap.getString("rootTrxId"), eq);
		super.setColumns("*");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}
	public SharedMap<String,Object> getTrxPay2(SharedMap<String,Object> loadMap,SharedMap<String,Object> load){
		super.setTable("PG_TRX_PAY");
		super.addWhere("trxId", loadMap.getString("rootTrxId"), eq);
		super.setColumns("*");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}
	
	public List<SharedMap<String, Object>> getTrxRfd2(SharedMap<String,Object> loadMap,SharedMap<String,Object> load){
		super.setTable("PG_TRX_RFD");
		super.addWhere("rootTrxId", loadMap.getString("rootTrxId"), eq);	
		super.setColumns("*");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRows();
	}
	
	
	public SharedMap<String,Object> getPayDupliMap(SharedMap<String,Object> loadMap,SharedMap<String,Object> load){
		super.setTable("PG_TRX_PAY");
		super.addWhere("reqDay", load.getString("trxDay"), eq);
		super.addWhere("mchtId", loadMap.getString("mchtId"), eq);
		super.addWhere("tmnId", loadMap.getString("tmnId"), eq);
		if(!load.isNullOrSpace("bin")){
			super.addWhere("bin", load.getString("bin"), eq);
		}
		if(!load.isNullOrSpace("last4")){
			super.addWhere("last4", load.getString("last4"), eq);
		}
		super.addWhere("authCd", load.getString("authCd"), eq);
		super.addWhere("van", loadMap.getString("van"), eq);
		//super.addWhere("vanId", loadMap.getString("vanId"), eq);
		super.setColumns("*");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}
	
	
	public SharedMap<String,Object> getPayDupliMap2(SharedMap<String,Object> loadMap,SharedMap<String,Object> load){
		super.setTable("PG_TRX_PAY");
		super.addWhere("reqDay", load.getString("trxDay"), eq);
		super.addWhere("mchtId", loadMap.getString("mchtId"), eq);
		super.addWhere("tmnId", loadMap.getString("tmnId"), eq);
		super.addWhere("van", loadMap.getString("van"), eq);
		super.addWhere("vanTrxId", loadMap.getString("vanTrxId"), eq);
		//super.addWhere("vanId", loadMap.getString("vanId"), eq);
		super.setColumns("*");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}
	
	
	public SharedMap<String,Object> getRfdDupliMap(SharedMap<String,Object> loadMap,SharedMap<String,Object> load){
		super.setTable("PG_TRX_RFD");
		super.addWhere("reqDay", load.getString("trxDay"), eq);
		super.addWhere("mchtId", loadMap.getString("mchtId"), eq);
		super.addWhere("tmnId", loadMap.getString("tmnId"), eq);
		super.addWhere("van", loadMap.getString("van"), eq);
		super.addWhere("rootTrxId", loadMap.getString("rootTrxId"), eq);
		super.addWhere("vanTrxId", loadMap.getString("vanTrxId"), eq);
		super.addWhere("status", "완료", eq);
		super.setColumns("*");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}



	public void changeDdctSettleDay(String stlDay) {
		super.setTable("PG_SETTLE_DDCT");
		super.setRecord("stlDay", stlDay);
		super.addWhere("stlStatus", "정산대기");
		super.addWhere("stlDay < '"+stlDay+"'");
		super.update();
		super.initRecord();
	}



	public void setDdctClose() {
		String sql = "UPDATE PG_MCHT_DDCT A JOIN (SELECT sum(if(stlStatus = '정산대기',1,0)) AS cnt,ddctId FROM PG_SETTLE_DDCT "
				+ "GROUP BY ddctId) B ON A.ddctId = B.ddctId "
				+ "SET STATUS = '종료' "
				+ "WHERE A.status = '진행' AND B.cnt = 0";
		super.update(sql);
		super.initRecord();
		
	}



	public SharedMap<String, Object> getMchtInterFeeRate(SharedMap<String, Object> trxCapMap) {
		String key = "PG_MCHT_INTEREST_" + trxCapMap.getString("mchtId")+"_"+trxCapMap.getString("acquirer");
		if (Cache.map.containsKey(key)) {
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("VW_MCHT_INTEREST");
			super.setColumns("*");
			super.addWhere("mchtId", trxCapMap.getString("mchtId"), eq);
			super.addWhere("acqName", trxCapMap.getString("acquirer"), eq);
			RecordSet rset = super.search();
			super.initRecord();
			if(rset.size() > 0){
				return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
		}
	}



	public SharedMap<String, Object> getOrgInterRate(String van, String acquirer) {
		String key = "PG_ORG_INTEREST_" + van+"_"+acquirer;
		if (Cache.map.containsKey(key)) {
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("PG_ORG_FEE_INTEREST");
			super.setColumns("*");
			super.addWhere("van",van,eq);
			super.addWhere("acquirer", acquirer, eq);
			RecordSet rset = super.search();
			super.initRecord();
			if(rset.size() > 0){
				return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
		}
	}
	
	public List<SharedMap<String, Object>> getPhonePay() {
		String q = "SELECT T1.* FROM PG_PHONE_PAY T1 LEFT JOIN PG_PHONE_CAP T2 ON T1.trxId = T2.trxId WHERE T2.trxId is null AND T1.status in ('승인','취소') ORDER BY T1.regDate";
		RecordSet rset = super.query(q);
		
		super.initRecord();
		return rset.getRows();
	}
	
	public List<SharedMap<String, Object>> getPhoneRfd() {
		String q = "SELECT T1.* FROM PG_PHONE_RFD T1 LEFT JOIN PG_PHONE_CAP T2 ON T1.trxId = T2.trxId WHERE T2.trxId is null AND T1.status = '완료' ORDER BY T1.regDate";
		RecordSet rset = super.query(q);
		
		super.initRecord();
		return rset.getRows();
	}
	
	public SharedMap<String, Object> getMchtPhoneMngByMchtId(String mchtId) {
		String key = "PG_MCHT_PHONE_MNG" + mchtId;
		if (Cache.map.containsKey(key)) {
			return Cache.map.getUnchecked(key);
		} else {
			super.setTable("PG_MCHT_PHONE_MNG");
			super.setColumns("*");
			super.addWhere("mchtId", mchtId, eq);
			RecordSet rset = super.search();
			super.initRecord();
			if(rset.size() > 0){
				return Cache.map.put(key, rset.getRow(0));
			}else{
				return new SharedMap<String,Object>();
			}
		}
	}
	
	public SharedMap<String, Object> getVanByVanId(String vanId) {
		super.setTable("PG_VAN");
		super.setColumns("*");
		super.addWhere("vanId", vanId, eq);
		RecordSet rset = super.search();
		super.initRecord();
		
		return rset.getRow(0);
	}
	
	public SharedMap<String,Object> getRealTimeMchtSvc(String mchtId){
		super.setTable("PG_MCHT_SVC");
		super.setColumns("*");
		super.addWhere("mchtId", mchtId, eq);
		RecordSet rset = super.search();
		super.initRecord();
		if(rset.size() > 0) {
			return rset.getRowFirst();
		}else {
			return null;
		}
	}
	
	public SharedMap<String, Object> getRealtimeTrx(String trxId) {
		super.setTable("PG_TRX_REALTIME_PAY");
		super.setColumns("*");
		super.addWhere("trxId", trxId);
		RecordSet rset = super.search();
		super.initRecord();
		if(rset.size() > 0) {
			return rset.getRowFirst();
		}else {
			return null;
		}
	}

	public SharedMap<String, Object> getRealtimePayout(String trxId) {
		super.setTable("PG_REALTIME_PAYOUT");
		super.setColumns("*");
		super.addWhere("trxId", trxId);
		RecordSet rset = super.search();
		super.initRecord();
		if(rset.size() > 0) {
			return rset.getRowFirst();
		}else {
			return null;
		}
	}
	
	public SharedMap<String,Object> getPhoneCap(String trxId){
		super.setTable("PG_PHONE_CAP");
		super.setColumns("*");
		super.addWhere("trxId"	,trxId,eq);
		
		RecordSet rset = super.search();
		super.initRecord();
		
		return rset.getRow(0);
	}
	
	/**
	 * 실시간 정산 승인거래 원장 (PG_TRX_REALTIME_PAY) 테이블 저장
	 * @param sharedMap
	 * @param response
	 */
	public void insertTrxRealTimePay(SharedMap<String, Object> sharedMap, SharedMap<String,Object> mchtMngMap) {
		super.setTable("PG_TRX_REALTIME_PAY");
		String curDate = CommonUtil.getCurrentDate("yyyyMMddHHmmss");

		super.setRecord("trxId", sharedMap.getString("trxId"));
		super.setRecord("mchtId", sharedMap.getString("mchtId"));
		super.setRecord("tmnId", sharedMap.getString("tmnId"));
		super.setRecord("trackId", sharedMap.getString("trackId"));
		super.setRecord("amount", sharedMap.getLong("amount"));
		super.setRecord("authCd", sharedMap.getString("authCd"));
		super.setRecord("trxType", "0");
		super.setRecord("trxDay", curDate.substring(0, 8));
		super.setRecord("trxTime", curDate.substring(8));
		super.setRecord("resultCd", sharedMap.getString("resultCd"));
		super.setRecord("resultMsg", sharedMap.getString("resultMsg"));
		super.setRecord("van", sharedMap.getString("van"));
		super.setRecord("vanId", sharedMap.getString("vanId"));
		super.setRecord("vanTrxId", sharedMap.getString("vanTrxId"));
		super.setRecord("transferInterval", mchtMngMap.getString("transferInterval"));
		super.setRecord("sendYn", "N");
		super.setRecord("regDate", curDate);
		
		logger.info("transferInterval : {}", mchtMngMap.getString("transferInterval"));
		logger.info("set PG_TRX_REALTIME_PAY : {}", super.insert());

		super.initRecord();
	}
	
	/**
	 * 
	 * 실시간 정산 취소거래 원장 (PG_TRX_REALTIME_PAY) 테이블 저장
	 * @param sharedMap
	 * @param response
	 * @param resultCd
	 */
	public void insertRefundTrxRealTimePay(SharedMap<String, Object> trxPayMap, SharedMap<String, Object> realtimeTrxMap) {
		super.setTable("PG_TRX_REALTIME_PAY");
		String curDate = CommonUtil.getCurrentDate("yyyyMMddHHmmss");
		
		super.setRecord("trxId",  trxPayMap.getString("trxId"));
		super.setRecord("mchtId",  trxPayMap.getString("mchtId"));
		super.setRecord("tmnId",  trxPayMap.getString("tmnId"));
		super.setRecord("trackId",  trxPayMap.getString("trackId"));
		super.setRecord("amount",  trxPayMap.getLong("rfdAmount"));
		super.setRecord("authCd", realtimeTrxMap.getString("authCd"));
		super.setRecord("trxType", "1");
		super.setRecord("trxDay", curDate.substring(0, 8));
		super.setRecord("trxTime", curDate.substring(8));
		super.setRecord("resultMsg", trxPayMap.getString("resultMsg"));
		
		String resultCd = "";
		
		try {
			if("".equals(trxPayMap.getString("resultCd"))) {
				resultCd = "0000";
			}else {
				resultCd = trxPayMap.getString("resultCd");
			}
		}catch (Exception e) {
			resultCd = "0000";
			logger.error("insertRefundTrxRealTimePay Exception : [{}] ", e.getMessage());
		}
		
		super.setRecord("resultCd", resultCd);
		super.setRecord("van", trxPayMap.getString("van"));
		super.setRecord("vanId", trxPayMap.getString("vanId"));
		super.setRecord("vanTrxId",trxPayMap.getString("vanTrxId"));
		super.setRecord("transferInterval", "1");
		super.setRecord("sendYn", "N");
		super.setRecord("regDate", curDate);

		logger.info("set PG_TRX_REALTIME_PAY REFUND : {}", super.insert());

		super.initRecord();
	}
	
	/**
	 * 실시간 출금 취소 데이터 (PG_REALTIME_PAYOUT) 테이블 저장
	 * @param sharedMap
	 * @param response
	 */
	public void insertRefundRealTimePayOut(SharedMap<String, Object> trxPayMap, SharedMap<String, Object> realtimeTrxMap) {
		super.setTable("PG_REALTIME_PAYOUT");
		String curDate = CommonUtil.getCurrentDate("yyyyMMddHHmmss");
		
		super.setRecord("trxId", trxPayMap.getString("trxId"));
		super.setRecord("mchtId", realtimeTrxMap.getString("mchtId"));
		super.setRecord("tmnId", trxPayMap.getString("tmnId"));
		super.setRecord("trackId", trxPayMap.getString("trackId"));
		super.setRecord("trxDay", curDate.substring(0, 8));
		super.setRecord("trxTime", curDate.substring(8));
		super.setRecord("trxType", "1");
		super.setRecord("authCd", realtimeTrxMap.getString("authCd"));
		super.setRecord("amount", realtimeTrxMap.getLong("amount") * -1);
		super.setRecord("stlFee", realtimeTrxMap.getLong("stlFee") * -1);
		super.setRecord("stlFeeVat", realtimeTrxMap.getLong("stlFeeVat") * -1);
		super.setRecord("stlAmount", realtimeTrxMap.getLong("stlAmount") * -1);
		super.setRecord("payOutFee", realtimeTrxMap.getLong("payOutFee") * -1);
		super.setRecord("payOutFeeVat", realtimeTrxMap.getLong("payOutFeeVat") * -1);
		super.setRecord("payOutAmount", realtimeTrxMap.getLong("payOutAmount") * -1);
		super.setRecord("bankCd", realtimeTrxMap.getString("bankCd"));
		super.setRecord("bankName", realtimeTrxMap.getString("bankName"));
		super.setRecord("account", realtimeTrxMap.getString("account"));
		super.setRecord("accntHolder", realtimeTrxMap.getString("accntHolder"));
		super.setRecord("payOutDay", curDate.substring(0, 8));
		super.setRecord("payOutTime", curDate.substring(8));
		
		String resultCd = "";
		
		try {
			if("".equals(trxPayMap.getString("resultCd"))) {
				resultCd = "0000";
			}else {
				resultCd = trxPayMap.getString("resultCd");
			}
		}catch (Exception e) {
			resultCd = "0000";
			logger.error("insertRefundRealTimePayOut Exception : [{}] ", e.getMessage());
		}
		
		super.setRecord("resultCd", resultCd);
		super.setRecord("resultMsg", trxPayMap.getString("resultMsg"));
		super.setRecord("sendCnt", "1");
		super.setRecord("sendCheck", "Y");
		super.setRecord("regId", "REFUND");
		super.setRecord("regDate", curDate);

		logger.info("set PG_REALTIME_PAYOUT REFUND : {}", super.insert());

		super.initRecord();
	}
	
	/**
	 * 충전정산 실시간정산 거래내역 저장
	 * @param trxCapMap
	 * @param capDtlMap
	 */
	public void insertChargeSettle(SharedMap<String, Object> trxCapMap, SharedMap<String,Object> capDtlMap) {
		String regDate = CommonUtil.getCurrentDate("yyyyMMddHHmmss");
		
		super.setTable("PG_CHARGE_SETTLE");
		super.setRecord("trxId", trxCapMap.getString("trxId"));
		super.setRecord("mchtId", trxCapMap.getString("mchtId"));
		super.setRecord("trxType", "입금");
		super.setRecord("trxUnit", "신용카드정산");
		super.setRecord("trxDay",  regDate.substring(0, 8));
		super.setRecord("trxTime",  regDate.substring(8));
		super.setRecord("amount", Math.abs(capDtlMap.getLong("stlAmount")));
		super.setRecord("fee", 0);
		super.setRecord("feeVat", 0);
		super.setRecord("bankFee", 0);
		super.setRecord("netAmount", Math.abs(capDtlMap.getLong("stlAmount")));
		super.setRecord("balance", getMchtBalance(trxCapMap.getString("mchtId")).getLong("balance")+Math.abs(capDtlMap.getLong("stlAmount")));
		super.setRecord("trackId", trxCapMap.getString("trackId"));
		super.setRecord("refId", trxCapMap.getString("capId"));
		super.setRecord("bankCd", "");
		super.setRecord("bankName", "");
		super.setRecord("account", "");
		super.setRecord("holder", "");
		super.setRecord("recordInfo", "");
		
		String stlDay = capDtlMap.getString("stlDay").substring(0, 4)+"-"+capDtlMap.getString("stlDay").substring(4,6)+"-"+capDtlMap.getString("stlDay").substring(6);
		super.setRecord("summary", stlDay+"정산일자 실시간 신용카드 정산금 지급");
		super.setRecord("regId", trxCapMap.getString("mchtId"));
		super.setRecord("regDay", regDate.substring(0, 8));
		
		logger.info("set PG_CHARGE_SETTLE : [{}][{]}", trxCapMap.getString("trxId"), super.insert());

		super.initRecord();
	}
	
	/**
	 * 실시간정산 취소건의 원거래 정산 상태 업데이트
	 * @param capId
	 */
	public void updateOrgRealtime(String capId){
		super.setTable("PG_TRX_CAP_DTL");
		super.setRecord("stlStatus"	, "정산완료");
		super.addWhere("capId", capId);
		
		logger.info("update PG_TRX_CAP_DTL SET : {},{}", capId, super.update());
		
		super.initRecord();
	}
	
	/**
	 * 충전정산 잔액조회
	 * @param mchtId
	 * @return
	 */
	public SharedMap<String, Object> getMchtBalance(String mchtId){
		super.setTable("PG_MCHT_BALANCE");
		super.setColumns("*");
		super.addWhere("mchtId",mchtId,eq);
		super.setOrderBy("");
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst();
	}
	
	/**
	 * 해당일자가 휴일인지 체크
	 * @param today
	 * @return
	 */
	public String getHolidayCheck(String today) {	
		String q = "SELECT status FROM PG_CODE_HOLIDAY WHERE days = '"+today+"'";
		RecordSet rset = super.query(q);
		super.initRecord();
		return rset.getRow(0).getString("status");
	}

	public String getRentFirstPaid(String mchtId){
		/*super.setTable("PG_TRX_PAY A, PG_TRX_RENT B ");
		super.setColumns("A.trxId");
		super.setWhere("A.rentId = B.rentId");
		super.addWhere("A.mchtId", mchtId, eq);
		RecordSet rset = super.search();
		super.initRecord();
		if(rset.size() == 1){
			return "월세최초결제";
		}else{
			return "";
		}*/

		super.setTable("PG_TRX_CAP");
		super.setColumns("trxId");
		super.addWhere("mchtId", mchtId, eq);
		super.addWhere("serviceType", "월세앱", eq);
		RecordSet rset = super.search();
		super.initRecord();
		if(rset.size() == 0){
			return "월세최초결제";
		}else{
			return "";
		}
	}

	public long getRentDaySum(String today, String mchtId) {
		super.setTable("PG_TRX_CAP");
		super.setColumns("SUM(amount) as totAmount");
		super.addWhere("mchtId", mchtId, eq);
		super.addWhere("capType", "매입", eq);
		super.addWhere("trxDay", today, eq);
		super.addWhere("serviceType", "월세앱", eq);
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst().getLong("totAmount");
	}

	public long getRentMonthSum(String month, String mchtId) {
		super.setTable("PG_TRX_CAP");
		super.setColumns("SUM(amount) as totAmount");
		super.addWhere("mchtId", mchtId, eq);
		super.addWhere("capType", "매입", eq);
		super.addWhere("substr(trxDay, 1, 6)", month, eq);
		super.addWhere("serviceType", "월세앱", eq);
		RecordSet rset = super.search();
		super.initRecord();
		return rset.getRowFirst().getLong("totAmount");
	}

	public boolean insertRiskChangeNoti(SharedMap<String,Object> ntsMap) {
		int result = 0;
		String query = "INSERT INTO `PG_RISK_CHANGE_NOTI` (`mchtId`, `capId`, `trackId`, `risk`, `trxDay`, `hookAddr`, `retry`, `status`, `code`, `payLoad`, `resData`, `sentDate`, `regDay`, `regTime`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		DBManager db 	= null;
		PreparedStatement pstmt	= null;
		Connection conn			= null;
		ResultSet rset			= null;
		int i = 1;
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			pstmt.setString(i++,ntsMap.getString("mchtId"));
			pstmt.setString(i++,ntsMap.getString("capId"));
			pstmt.setString(i++,ntsMap.getString("trackId"));
			pstmt.setString(i++,ntsMap.getString("risk"));
			pstmt.setString(i++,ntsMap.getString("trxDay"));
			pstmt.setString(i++,ntsMap.getString("hookAddr"));
			pstmt.setInt(i++,ntsMap.getInt("retry"));
			pstmt.setString(i++,ntsMap.getString("status"));
			pstmt.setInt(i++,ntsMap.getInt("code"));
			pstmt.setString(i++,ntsMap.getString("payLoad"));
			pstmt.setString(i++,ntsMap.getString("resData"));
			pstmt.setTimestamp(i++,ntsMap.getTimestamp("sentDate"));
			pstmt.setString(i++,ntsMap.getString("regDay"));
			pstmt.setString(i++,ntsMap.getString("regTime"));

			result = pstmt.executeUpdate();
			conn.commit();
		}catch(Exception e){
			e.printStackTrace();
			logger.error("insertRiskChangeNoti ERROR : {}, query : {}", e.getMessage(), query);
		}finally{
			db.close(conn,pstmt,rset);
		}

		if(result > 0) {
			return true;
		}else {
			return false;
		}
	}

	public boolean deleteChargeSettleFirm(String trxId) {
		super.setTable("PG_CHARGE_SETTLE_FIRM_RESERVE");
		super.addWhere("trxId", trxId);
		super.addWhere("status", "지급완료", ne);

		boolean deleted = super.delete();
		super.initRecord();
		logger.info("set PG_CHARGE_SETTLE_FIRM_RESERVE delete : [{}][{}]", trxId, deleted);

		return deleted;
	}
}
