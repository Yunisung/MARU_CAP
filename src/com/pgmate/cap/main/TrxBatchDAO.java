package com.pgmate.cap.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pgmate.lib.util.db.DBFactory;
import com.pgmate.lib.util.db.DBManager;
import com.pgmate.lib.util.lang.CommonUtil;
import com.pgmate.lib.util.map.SharedMap;

/**
 * @author Administrator
 *
 */
public class TrxBatchDAO  {

	private static Logger logger = LoggerFactory.getLogger(com.pgmate.cap.main.TrxBatchDAO.class);
	
	
	public TrxBatchDAO() {
		// TODO Auto-generated constructor stub
	}
	
	
	public int insertTrxCap(List<SharedMap<String,Object>> insertCapList,List<SharedMap<String,Object>> insertCapDtlList){
		int inserted = 0;
		logger.debug("insert trxCap batch : {}",insertCapList.size());
		String query = "insert into PG_TRX_CAP (capId ,trxId ,mchtId,tmnId ,trackId,capType ,rfdType,rootTrxId,rootTrxDay,amount ,installment ,vat,cardId ,cardType,bin,last4 ,issuer,acquirer,authCd ,trxDay,serviceType ,regDay ,regTime,regDate)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			int batchSize = 100;
			int count = 0;
			
			for(SharedMap<String,Object> map : insertCapList){
				int i=1;
				pstmt.setString(i++, map.getString("capId"));
				pstmt.setString(i++, map.getString("trxId"));
				pstmt.setString(i++, map.getString("mchtId"));
				pstmt.setString(i++, map.getString("tmnId"));
				pstmt.setString(i++, map.getString("trackId"));
				pstmt.setString(i++, map.getString("capType"));
				pstmt.setString(i++, map.getString("rfdType"));
				pstmt.setString(i++, map.getString("rootTrxId"));
				pstmt.setString(i++, map.getString("rootTrxDay"));
				pstmt.setLong(i++  , map.getLong("amount"));
				pstmt.setString(i++, map.getString("installment"));
				pstmt.setLong(i++  , map.getLong("vat"));
				pstmt.setString(i++, map.getString("cardId"));
				pstmt.setString(i++, map.getString("cardType"));
				pstmt.setString(i++, map.getString("bin"));
				pstmt.setString(i++, map.getString("last4"));
				pstmt.setString(i++, map.getString("issuer"));
				pstmt.setString(i++, map.getString("acquirer"));
				pstmt.setString(i++, map.getString("authCd"));
				pstmt.setString(i++, map.getString("trxDay"));
				pstmt.setString(i++, map.getString("serviceType"));
				pstmt.setString(i++, map.getString("regDay"));
				pstmt.setString(i++, map.getString("regTime"));
				pstmt.setTimestamp(i++, map.getTimestamp("regDate"));
				pstmt.addBatch();
				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
			}
			
			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert batch cap error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		if(inserted == insertCapList.size()){
			inserted +=insertTrxCapDtl(insertCapDtlList);
		}
		return inserted;
		
	}
	
	public int insertTrxCapDtl(List<SharedMap<String,Object>> insertCapDtlList){
		int inserted = 0;
		logger.debug("insert trxCapDtl batch : {}",insertCapDtlList.size());
		String query = "insert into PG_TRX_CAP_DTL (capId,stlStatus,stlAmount,stlRate,stlInterFee,stlInterFeeVat,stlInterRate,stlLoanRate,stlFee,stlFeeVat,stlType,stlDay,payOutDay,stlId,stlDistFee,stlDistRate,stlDiffDistFee,stlDiffDistRate,stlDistDay,stlDistId,stlAgencyFee,stlAgencyRate,stlDiffAgencyFee,stlDiffAgencyRate,stlAgencyDay, "
				+"stlAgencyId,stlSalesFee,stlSalesRate,stlDiffSalesFee,stlDiffSalesRate,stlSalesDay,stlSalesId,van,vanId,vanTrxId,vanStatus,stlVanFee,stlVanRate,stlVanInterFee,stlVanInterRate,stlVanDay,stlDiffType,stlDiffStatus,stlDiffRate,stlDiffAmt,stlDiffVanType,stlDiffVanDay,stlDiffVanAmt,stlDiffResultMsg,benefit,taxId,risk,billingMethod,billingType)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			int batchSize = 100;
			int count = 0;
			
			for(SharedMap<String,Object> map : insertCapDtlList){
				int i=1;
				pstmt.setString(i++, map.getString("capId"));
				pstmt.setString(i++, map.getString("stlStatus"));
				pstmt.setLong(i++  , map.getLong("stlAmount"));
				pstmt.setDouble(i++, map.getDouble("stlRate"));
				pstmt.setLong(i++  , map.getLong("stlInterFee"));
				pstmt.setLong(i++  , map.getLong("stlInterFeeVat"));
				pstmt.setDouble(i++, map.getDouble("stlInterRate"));
				pstmt.setDouble(i++, map.getDouble("stlLoanRate"));
				pstmt.setLong(i++  , map.getLong("stlFee"));
				pstmt.setLong(i++  , map.getLong("stlFeeVat"));
				pstmt.setString(i++, map.getString("stlType"));
				pstmt.setString(i++, map.getString("stlDay"));
				pstmt.setString(i++, map.getString("payOutDay"));
				pstmt.setString(i++, map.getString("stlId"));
				pstmt.setLong(i++  , map.getLong("stlDistFee"));
				pstmt.setDouble(i++, map.getDouble("stlDistRate"));
				pstmt.setLong(i++  , map.getLong("stlDiffDistFee"));
				pstmt.setDouble(i++, map.getDouble("stlDiffDistRate"));
				pstmt.setString(i++, map.getString("stlDistDay"));
				pstmt.setString(i++, map.getString("stlDistId"));
				pstmt.setLong(i++  , map.getLong("stlAgencyFee"));
				pstmt.setDouble(i++, map.getDouble("stlAgencyRate"));
				pstmt.setLong(i++  , map.getLong("stlDiffAgencyFee"));
				pstmt.setDouble(i++, map.getDouble("stlDiffAgencyRate"));
				pstmt.setString(i++, map.getString("stlAgencyDay"));
				pstmt.setString(i++, map.getString("stlAgencyId"));
				pstmt.setLong(i++  , map.getLong("stlSalesFee"));
				pstmt.setDouble(i++, map.getDouble("stlSalesRate"));
				pstmt.setLong(i++  , map.getLong("stlDiffSalesFee"));
				pstmt.setDouble(i++, map.getDouble("stlDiffSalesRate"));
				pstmt.setString(i++, map.getString("stlSalesDay"));
				pstmt.setString(i++, map.getString("stlSalesId"));
				pstmt.setString(i++, map.getString("van"));
				pstmt.setString(i++, map.getString("vanId"));
				pstmt.setString(i++, map.getString("vanTrxId"));
				pstmt.setString(i++, map.getString("vanStatus"));
				pstmt.setLong(i++  , map.getLong("stlVanFee"));
				pstmt.setDouble(i++, map.getDouble("stlVanRate"));
				pstmt.setLong(i++  , map.getLong("stlVanInterFee"));
				pstmt.setDouble(i++, map.getDouble("stlVanInterRate"));
				pstmt.setString(i++, map.getString("stlVanDay"));
				pstmt.setString(i++, map.getString("stlDiffType"));
				pstmt.setString(i++, map.getString("stlDiffStatus"));
				pstmt.setDouble(i++  , map.getDouble("stlDiffRate"));
				pstmt.setLong(i++  , map.getLong("stlDiffAmt"));
				pstmt.setString(i++, map.getString("stlDiffVanType"));
				pstmt.setString(i++, map.getString("stlDiffVanDay"));
				pstmt.setLong(i++  , map.getLong("stlDiffVanAmt"));
				pstmt.setString(i++  , map.getString("stlDiffResultMsg"));
				pstmt.setLong(i++  , map.getLong("benefit"));
				pstmt.setString(i++, map.getString("taxId"));
				pstmt.setString(i++, map.getString("risk"));
				pstmt.setString(i++, map.getString("billingMethod"));
				pstmt.setString(i++, map.getString("billingType"));
				pstmt.addBatch();
				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
			}
			
			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert batch capDtl error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		return inserted;
		
		
	}
	
	
	
	
	public int insertTrxCapSub(List<SharedMap<String,Object>> insertCapSubList){
		int inserted = 0;
		logger.debug("insert trxCapSub batch : {}",insertCapSubList.size());
		String query = "insert into PG_TRX_CAP_SUB (capId ,trxId ,mchtId,tmnId ,capType ,rfdType,rootTrxId,amount,stlAmount,stlRate,stlFee,stlFeeVat,stlType,stlDay,stlId,benefit ,trxDay ,regDay ,regTime,regDate)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			int batchSize = 100;
			int count = 0;
			
			for(SharedMap<String,Object> map : insertCapSubList){
				int i=1;
				pstmt.setString(i++, map.getString("capId"));
				pstmt.setString(i++, map.getString("trxId"));
				pstmt.setString(i++, map.getString("mchtId"));
				pstmt.setString(i++, map.getString("tmnId"));
				pstmt.setString(i++, map.getString("capType"));
				pstmt.setString(i++, map.getString("rfdType"));
				pstmt.setString(i++, map.getString("rootTrxId"));
				pstmt.setLong(i++  , map.getLong("amount"));
				pstmt.setLong(i++  , map.getLong("stlAmount"));
				pstmt.setDouble(i++ , map.getDouble("stlRate"));
				pstmt.setLong(i++  , map.getLong("stlFee"));
				pstmt.setLong(i++  , map.getLong("stlFeeVat"));
				pstmt.setString(i++, map.getString("stlType"));
				pstmt.setString(i++, map.getString("stlDay"));
				pstmt.setString(i++, map.getString("stlId"));
				pstmt.setLong(i++  , map.getLong("benefit"));
				pstmt.setString(i++, map.getString("trxDay"));
				pstmt.setString(i++, map.getString("regDay"));
				pstmt.setString(i++, map.getString("regTime"));
				pstmt.setTimestamp(i++, map.getTimestamp("regDate"));
				pstmt.addBatch();
				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
			}
			
			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert batch cap error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		
		return inserted;
		
	}
	
	/**
	 * 신용카드 충전정산 실시간 정산 저장 배치
	 * @param insertChargeSettleList
	 * @return
	 */
	public int insertChargeSettle(List<SharedMap<String,Object>> insertChargeSettleList){
		int inserted = 0;
		logger.debug("insert chargeSettle batch : {}",insertChargeSettleList.size());
		String query = "insert into PG_CHARGE_SETTLE (trxId, mchtId, trxType, trxUnit, trxDay, trxTime, amount, fee, feeVat, bankFee, netAmount, balance, trackId, refId, bankCd, bankName, account, holder, recordInfo, summary, regId, regDay)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			int batchSize = 100;
			int count = 0;
			
			for(SharedMap<String,Object> map : insertChargeSettleList){
				int i=1;
				pstmt.setString(i++, map.getString("trxId"));
				pstmt.setString(i++, map.getString("mchtId"));
				pstmt.setString(i++, map.getString("trxType"));
				pstmt.setString(i++, map.getString("trxUnit"));
				pstmt.setString(i++, map.getString("trxDay"));
				pstmt.setString(i++, map.getString("trxTime"));
				pstmt.setLong(i++  , map.getLong("amount"));
				pstmt.setLong(i++  , map.getLong("fee"));
				pstmt.setLong(i++  , map.getLong("feeVat"));
				pstmt.setLong(i++  , map.getLong("bankFee"));
				pstmt.setLong(i++  , map.getLong("netAmount"));
				pstmt.setLong(i++  , map.getLong("balance"));
				pstmt.setString(i++, map.getString("trackId"));
				pstmt.setString(i++, map.getString("refId"));
				pstmt.setString(i++, map.getString("bankCd"));
				pstmt.setString(i++, map.getString("bankName"));
				pstmt.setString(i++, map.getString("account"));
				pstmt.setString(i++, map.getString("holder"));
				pstmt.setString(i++, map.getString("recordInfo"));
				pstmt.setString(i++, map.getString("summary"));
				pstmt.setString(i++, map.getString("regId"));
				pstmt.setString(i++, map.getString("regDay"));
				pstmt.addBatch();
				
				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
			}
			
			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert batch chargeSettle error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		
		return inserted;
	}

	public int insertChargeSettleFirm(List<SharedMap<String,Object>> insertChargeSettleFirmList){
		int inserted = 0;
		logger.debug("insert chargeSettle batch : {}",insertChargeSettleFirmList.size());
		String query = "insert into PG_CHARGE_SETTLE_FIRM_RESERVE (trxId, trxType, transferType, mchtId, trackId, pubDay, pubTime, status, retry, trxDay, trxTime, amount, fee, feeVat, bankFee, netAmount, balance, resultCd, resultMsg, refId, rootTrxId, account, bankCd, bankName, holder, recordInfo, regId, regDay)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;

		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);

			int batchSize = 100;
			int count = 0;

			for(SharedMap<String,Object> map : insertChargeSettleFirmList){
				int i=1;
				pstmt.setString(i++, map.getString("trxId"));
				pstmt.setString(i++, map.getString("trxType"));
				pstmt.setString(i++, map.getString("transferType"));
				pstmt.setString(i++, map.getString("mchtId"));
				pstmt.setString(i++, map.getString("trackId"));
				pstmt.setString(i++, map.getString("pubDay"));
				pstmt.setString(i++, map.getString("pubTime"));
				pstmt.setString(i++, map.getString("status"));
				pstmt.setInt(i++   , map.getInt("retry"));
				pstmt.setString(i++, map.getString("trxDay"));
				pstmt.setString(i++, map.getString("trxTime"));
				pstmt.setLong(i++  , map.getLong("amount"));
				pstmt.setLong(i++  , map.getLong("fee"));
				pstmt.setLong(i++  , map.getLong("feeVat"));
				pstmt.setLong(i++  , map.getLong("bankFee"));
				pstmt.setLong(i++  , map.getLong("netAmount"));
				pstmt.setLong(i++  , map.getLong("balance"));
				pstmt.setString(i++, map.getString("resultCd"));
				pstmt.setString(i++, map.getString("resultMsg"));
				pstmt.setString(i++, map.getString("refId"));
				pstmt.setString(i++, map.getString("rootTrxId"));
				pstmt.setString(i++, map.getString("account"));
				pstmt.setString(i++, map.getString("bankCd"));
				pstmt.setString(i++, map.getString("bankName"));
				pstmt.setString(i++, map.getString("holder"));
				pstmt.setString(i++, map.getString("recordInfo"));
				pstmt.setString(i++, map.getString("regId"));
				pstmt.setString(i++, map.getString("regDay"));
				pstmt.addBatch();

				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
			}

			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert batch chargeSettle error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}

		return inserted;
	}
	
	public int insertRisk(List<String[]> insertRisk){
		int inserted = 0;
		logger.debug("insert trxIQR batch : {}",insertRisk.size());
		String query = "insert into PG_TRX_IQR (capId,iqrType,telNo,summary,regId,regDay)  values (?,'리스크','',?,'SYSTEM',DATE_FORMAT(now(),'%Y%m%d'))";
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			int batchSize = 100;
			int count = 0;
			
			for(String[] data : insertRisk){
				int i=1;
				pstmt.setString(i++, data[0]);
				pstmt.setString(i++, data[1]);
				pstmt.addBatch();
				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
			}
			
			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert trxIQR cap error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		
		return inserted;
		
	}
	
	
	
	public int insertTrxPay(List<SharedMap<String,Object>> payList){
		int inserted = 0;
		logger.debug("insert trxPay batch : {}",payList.size());
		String query = "insert into PG_TRX_PAY (trxId,mchtId,tmnId,trackId,payerName,payerEmail,payerTel,amount,installment,cardId,cardType,bin,last4,status,prodId,issuer,acquirer,reqDay,reqTime,authCd,resultCd,resultMsg,van,vanId,vanTrxId,regDay,regTime,regDate)  values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			int batchSize = 100;
			int count = 0;
			
			for(SharedMap<String,Object> map : payList){
				int i=1;
				pstmt.setString(i++, map.getString("trxId"));
				pstmt.setString(i++, map.getString("mchtId"));
				pstmt.setString(i++, map.getString("tmnId"));
				pstmt.setString(i++, map.getString("trackId"));
				pstmt.setString(i++, map.getString("payerName"));
				pstmt.setString(i++, map.getString("payerEmail"));
				pstmt.setString(i++, map.getString("payerTel"));
				pstmt.setLong(i++  , map.getLong("amount"));
				pstmt.setString(i++, map.getString("installment"));
				pstmt.setString(i++, map.getString("cardId"));
				pstmt.setString(i++, map.getString("cardType"));
				pstmt.setString(i++, map.getString("bin"));
				pstmt.setString(i++, map.getString("last4"));
				pstmt.setString(i++, map.getString("status"));
				pstmt.setString(i++, map.getString("prodId"));
				pstmt.setString(i++, map.getString("issuer"));
				pstmt.setString(i++, map.getString("acquirer"));
				pstmt.setString(i++, map.getString("reqDay"));
				pstmt.setString(i++, map.getString("reqTime"));
				pstmt.setString(i++, map.getString("authCd"));
				pstmt.setString(i++, map.getString("resultCd"));
				pstmt.setString(i++, map.getString("resultMsg"));
				pstmt.setString(i++, map.getString("van"));
				pstmt.setString(i++, map.getString("vanId"));
				pstmt.setString(i++, map.getString("vanTrxId"));
				pstmt.setString(i++, map.getString("regDay"));
				pstmt.setString(i++, map.getString("regTime"));
				pstmt.setTimestamp(i++, map.getTimestamp("regDate"));
				pstmt.addBatch();
				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
			}
			
			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert batch pay error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		
		return inserted;
		
	}
	
	
	public int insertTrxRfd(List<SharedMap<String,Object>> rfdList){
		int inserted = 0;
		logger.debug("insert trxRfd batch : {}",rfdList.size());
		String query = "insert into PG_TRX_RFD (trxId,mchtId,tmnId,trackId,status,rfdType,rfdAll,rfdAmount,rfdVat,cardId,bin,last4,issuer,acquirer,rootTrnDay,rootTrxId,rootTrackId,rootAmount,rootVat,reqDay,reqTime,authCd,resultCd,resultMsg,van,vanId,vanTrxId,vanResultCd,vanResultMsg,regDay,regTime,regDate) "
				+" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			int batchSize = 100;
			int count = 0;
			
			for(SharedMap<String,Object> map : rfdList){
				int i=1;
				pstmt.setString(i++, map.getString("trxId"));
				pstmt.setString(i++, map.getString("mchtId"));
				pstmt.setString(i++, map.getString("tmnId"));
				pstmt.setString(i++, map.getString("trackId"));
				pstmt.setString(i++, map.getString("status"));
				pstmt.setString(i++, map.getString("rfdType"));
				pstmt.setString(i++, map.getString("rfdAll"));
				pstmt.setLong(i++  , map.getLong("rfdAmount"));
				pstmt.setLong(i++, map.getLong("rfdVat"));
				pstmt.setString(i++, map.getString("cardId"));
				pstmt.setString(i++, map.getString("bin"));
				pstmt.setString(i++, map.getString("last4"));
				pstmt.setString(i++, map.getString("issuer"));
				pstmt.setString(i++, map.getString("acquirer"));
				pstmt.setString(i++, map.getString("rootTrnDay"));
				pstmt.setString(i++, map.getString("rootTrxId"));
				pstmt.setString(i++, map.getString("rootTrackId"));
				pstmt.setLong(i++  , map.getLong("rootAmount"));
				pstmt.setLong(i++, map.getLong("rootVat"));
				pstmt.setString(i++, map.getString("reqDay"));
				pstmt.setString(i++, map.getString("reqTime"));
				pstmt.setString(i++, map.getString("authCd"));
				pstmt.setString(i++, map.getString("resultCd"));
				pstmt.setString(i++, map.getString("resultMsg"));
				pstmt.setString(i++, map.getString("van"));
				pstmt.setString(i++, map.getString("vanId"));
				pstmt.setString(i++, map.getString("vanTrxId"));
				pstmt.setString(i++, map.getString("resultCd"));
				pstmt.setString(i++, map.getString("resultMsg"));
				pstmt.setString(i++, map.getString("regDay"));
				pstmt.setString(i++, map.getString("regTime"));
				pstmt.setTimestamp(i++, map.getTimestamp("regDate"));
				pstmt.addBatch();
				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
			}
			
			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert batch rfd error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		
		if(inserted == rfdList.size()){
			inserted +=payToRefund(rfdList);
		}
		
		return inserted;
		
	}
	
	
	public int payToRefund(List<SharedMap<String,Object>> rfdList){
		int inserted = 0;
		logger.debug("insert trxPay to RFD batch : {}",rfdList.size());
		String query = "UPDATE PG_TRX_PAY set status='승인취소' WHERE trxId = ?";
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			int batchSize = 100;
			int count = 0;
			
			for(SharedMap<String,Object> map : rfdList){
				int i=1;
				pstmt.setString(i++, map.getString("rootTrxId"));
				pstmt.addBatch();
				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
			}
			
			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert batch pay to rfd error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		
		return inserted;
		
	}
	
	
	
	public int setLoadDtl(List<SharedMap<String,Object>> loadUpdateList){
		int inserted = 0;
		logger.debug("insert loaddtl batch : {}",loadUpdateList.size());
		String query = "UPDATE PG_TRX_LOAD_DTL set exeStatus=? , exeDate = CURRENT_TIMESTAMP, trxId=?,summary=? WHERE idx = ?";
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			int batchSize = 100;
			int count = 0;
			
			for(SharedMap<String,Object> map : loadUpdateList){
				int i=1;
				pstmt.setString(i++, map.getString("status"));
				pstmt.setString(i++, map.getString("trxId"));
				pstmt.setString(i++, map.getString("summary"));
				pstmt.setLong(i++, map.getLong("idx"));
				pstmt.addBatch();
				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
			}
			
			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert loaddtl error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		
		
		return inserted;
		
	}

	public int insertPhoneCap(List<SharedMap<String,Object>> insertCapList){
		int inserted = 0;
		logger.debug("insert PhoneCap batch : {}",insertCapList.size());
		String query = "insert into PG_PHONE_CAP (capId,trxId,mchtId,tmnId,taxId,trackId,trxDay,stlStatus,capType,rfdType,"
				+ "rootTrxId,rootTrxDay,amount,vat,stlAmount,stlRate,stlFee,stlFeeVat,stlType,stlDay,"
				+ "stlId,payOutDay,stlDistFee,stlDistRate,stlDistDay,stlDistId,stlAgencyFee,stlAgencyRate,stlAgencyDay,stlAgencyId,"
				+ "stlSalesFee,stlSalesRate,stlSalesDay,stlSalesId,stlVanFee,stlVanRate,van,vanId,vanTrxId,benefit,"
				+ "stlYn,regDay,regTime,regDate)  "
				+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			int batchSize = 100;
			int count = 0;
			
			for(SharedMap<String,Object> map : insertCapList){
				int i=1;
				pstmt.setString(i++	, map.getString("capId"));
				pstmt.setString(i++	, map.getString("trxId"));
				pstmt.setString(i++	, map.getString("mchtId"));
				pstmt.setString(i++	, map.getString("tmnId"));
				pstmt.setString(i++	, map.getString("taxId"));
				pstmt.setString(i++	, map.getString("trackId"));
				pstmt.setString(i++	, map.getString("trxDay"));
				pstmt.setString(i++	, map.getString("stlStatus"));
				pstmt.setString(i++	, map.getString("capType"));
				pstmt.setString(i++	, map.getString("rfdType"));
				pstmt.setString(i++	, map.getString("rootTrxId"));
				pstmt.setString(i++	, map.getString("rootTrxDay"));
				pstmt.setLong(i++  	, map.getLong("amount"));
				pstmt.setLong(i++  	, map.getLong("vat"));
				pstmt.setLong(i++  	, map.getLong("stlAmount"));
				pstmt.setDouble(i++ , map.getDouble("stlRate"));
				pstmt.setDouble(i++ , map.getDouble("stlFee"));
				pstmt.setDouble(i++ , map.getDouble("stlFeeVat"));
				pstmt.setString(i++ , map.getString("stlType"));
				pstmt.setString(i++ , map.getString("stlDay"));
				pstmt.setString(i++ , map.getString("stlId"));
				pstmt.setString(i++ , map.getString("payOutDay"));
				pstmt.setDouble(i++ , map.getDouble("stlDistFee"));
				pstmt.setDouble(i++ , map.getDouble("stlDistRate"));
				pstmt.setString(i++	, map.getString("stlDistDay"));
				pstmt.setString(i++	, map.getString("stlDistId"));
				pstmt.setDouble(i++ , map.getDouble("stlAgencyFee"));
				pstmt.setDouble(i++ , map.getDouble("stlAgencyRate"));
				pstmt.setString(i++	, map.getString("stlAgencyDay"));
				pstmt.setString(i++	, map.getString("stlAgencyId"));
				pstmt.setDouble(i++ , map.getDouble("stlSalesFee"));
				pstmt.setDouble(i++ , map.getDouble("stlSalesRate"));
				pstmt.setString(i++	, map.getString("stlSalesDay"));
				pstmt.setString(i++	, map.getString("stlSalesId"));
				pstmt.setDouble(i++ , map.getDouble("stlVanFee"));
				pstmt.setDouble(i++ , map.getDouble("stlVanRate"));
				pstmt.setString(i++	, map.getString("van"));
				pstmt.setString(i++	, map.getString("vanId"));
				pstmt.setString(i++	, map.getString("vanTrxId"));
				pstmt.setDouble(i++	, map.getDouble("benefit"));
				pstmt.setString(i++	, map.getString("stlYn"));
				pstmt.setString(i++	, map.getString("regDay"));
				pstmt.setString(i++	, map.getString("regTime"));
				pstmt.setTimestamp(i++, map.getTimestamp("regDate"));
				pstmt.addBatch();
				if(++count % batchSize == 0) {
					inserted += pstmt.executeBatch().length;
				}
				
				if("1".equals(map.getString("stlType"))) {
					updateStlYn(map.getString("rootTrxId"));
				}
			}
			
			inserted +=pstmt.executeBatch().length;
			conn.commit();
		}catch(Exception e){
			logger.debug("insert batch cap error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		
		return inserted;
	}
	
	/**
	 * 수납정산 취소건 같은경우 수납정산건에서 제외해야 해서 정산여보를 N으로 업데이트 
	 * @param trxId
	 * @return
	 */
	public int updateStlYn(String trxId){
		int inserted = 0;
		logger.debug("updateStlYn : {}",trxId);
		String query = "UPDATE PG_PHONE_CAP set stlYn='N' WHERE trxId = '" + trxId + "'";
		
		DBManager db = null ;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			db 		= DBFactory.getInstance();
			conn	= db.getConnection();
			pstmt	= conn.prepareStatement(query);
			
			inserted =pstmt.executeUpdate();
			conn.commit();
		}catch(Exception e){
			logger.debug("updateStlYn error : {}",CommonUtil.getExceptionMessage(e));
		}finally{
			db.close(pstmt);
			db.close(conn);
		}
		
		return inserted;
	}
}

