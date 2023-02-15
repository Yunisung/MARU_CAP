package com.pgmate.cap.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pgmate.cap.cache.Cache;
import com.pgmate.cap.util.SmsGw;
import com.pgmate.lib.util.lang.CommonUtil;

/**
 * @author Administrator
 *
 */
public class CaptureDaemon extends Thread{
	private static Logger logger = LoggerFactory.getLogger(com.pgmate.cap.main.CaptureDaemon.class);
	private static int INTERVAL = 5000;
	private static int COUNTER	= 1;
	
	public void run(){
		logger.info("capture daemon start");
		
		try {
			while(true){
				try {
					Capture capture = new Capture();
					capture.start();
					//22.03.16 captureFactoring 미사용으로 주석처리
					/*
					 * CaptureFactoring captureFactoring = new CaptureFactoring();
					 * captureFactoring.start();
					 */
					CapturePhone capturePhone = new CapturePhone();
					capturePhone.start();
					Thread.sleep(INTERVAL);
					COUNTER++;
					if(COUNTER % 200 == 0){
						logger.info("capture daemon running");
						COUNTER=1;
					}

					TrxDAO trxDAO = new TrxDAO();
					
					if(CommonUtil.getCurrentDate("HHmm").equals("0001")){
						//가맹점, 가상계좌 수수료 예약 변경
						if(trxDAO.setReserveRate() > 0 || trxDAO.setReserveVactRate() > 0){
							logger.info("cache init");
							for(String key : Cache.map.asMap().keySet()){
								Cache.map.delete(key);
								logger.info("cache delete : {}",key);
							}
						}

						// ------ 차감정산 추가 -------
						String stlDay = trxDAO.getSettleDay(CommonUtil.getCurrentDate("yyyyMMdd"));
						// 현재날짜 이전 정산대기건 다음 영업일로 변경
						trxDAO.changeDdctSettleDay(stlDay);
						// 차감정산 완료 건 종료로 변경
						trxDAO.setDdctClose(); 
					}
				}catch(Exception e) {
					logger.info("capture daemon error : {}",CommonUtil.getExceptionMessage(e));
					
					SmsGw smsGw = new SmsGw();
					
					String msgBody = "정산 프로세스 재기동 오류. 확인요망";
					smsGw.sendMessage("0", "1", msgBody);
		            
		            logger.error(e.getMessage(), e);
				}
			}
		} finally {
			logger.info("capture daemon stop");
		}
	}
	
	public static void main(String[] args){
		CaptureDaemon t = new CaptureDaemon();
		t.setDaemon(false);
		t.setName("CaptureDaemon");
		t.start();
		
		SmsGw smsGw = new SmsGw();
		
		String msgBody = "정산 프로세스 재기동 완료. 확인요망";
		smsGw.sendMessage("0", "1", msgBody);
	}
}
