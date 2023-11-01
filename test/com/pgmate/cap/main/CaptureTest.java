package com.pgmate.cap.main;

import com.pgmate.lib.util.map.SharedMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CaptureTest {

    private static Logger logger = LoggerFactory.getLogger(CaptureTest.class);

    Capture capture;
    TrxDAO trxDAO;

    @Before
    public void init() {
        capture = new Capture();
        trxDAO = new TrxDAO();
    }

    @Test
    public void testRisk() {
        List<String> warningList = new ArrayList<String>();
        // trxId 추가
        warningList.add("T220801037268");
        if(warningList.size() > 0){
            for(String trxId : warningList ){
                assertFalse("".equals(trxId));
                capture.warning(trxId);

                SharedMap<String,Object> map = trxDAO.getTrxCap(trxId);
                String capId = map.getString("capId");
                String risk = map.getString("risk");

                logger.debug("리스크 capId {}", capId);
                assertEquals("위험", risk);
            }
        }
    }

    @Test
    public void captureRentAppPay() {
        SharedMap<String,Object> trxPayMap = new SharedMap();
        String trxId = "T231025043414";
        String trackId = "test-1698218723708";
        trxPayMap.put("trxId", trxId);
        trxPayMap.put("mchtId", "bktest001");
        trxPayMap.put("tmnId", "TMN000029");
        trxPayMap.put("trackId", trackId);
        trxPayMap.put("payerName", "오세창");
        trxPayMap.put("payerEmail", "test@gmail.com");
        trxPayMap.put("payerTel", "01044336699");
        trxPayMap.put("amount", 104400);
        trxPayMap.put("installment", "00");
        trxPayMap.put("cardId", "card_1867-c6622d-316-114c4");
        trxPayMap.put("cardType", "신용");
        trxPayMap.put("bin", "944542");
        trxPayMap.put("last4", "404*");
        trxPayMap.put("status", "승인");
        trxPayMap.put("prodId", "pdt_139f-d07791-119-d3518");
        trxPayMap.put("rentId", "rent_ea77-3707ca-4e0-ac173");
        trxPayMap.put("issuer", "국민");
        trxPayMap.put("acquirer", "국민");
        trxPayMap.put("reqDay", "20231030");
        trxPayMap.put("reqTime", "162525");
        trxPayMap.put("authCd", "30039247");
        trxPayMap.put("resultCd", "0000");
        trxPayMap.put("resultMsg", "성공");
        trxPayMap.put("van", "GALAXIA온라인");
        trxPayMap.put("vanId", "M2245531");
        trxPayMap.put("vanTrxId", "2023102516C2067906");
        trxPayMap.put("regDay", "20231030");
        trxPayMap.put("regTime", "162541");

        capture.capture(trxPayMap);
    }


}
