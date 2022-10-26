package com.pgmate.cap.main;

import com.pgmate.lib.util.map.SharedMap;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;

public class TrxDAOTest {

    private static Logger logger = LoggerFactory.getLogger(TrxDAOTest.class);

    TrxDAO trxDAO;

    @Before
    public void init() {
        trxDAO = new TrxDAO();
        trxDAO.setDebug(true);
    }

    @Test
    public void 위험리스크_조회() {
        SharedMap<String, Object> trxPayMap = new SharedMap<>();
        trxPayMap.put("trxId", "T220603010569");
        trxPayMap.put("mchtId", "bktest001");
        trxPayMap.put("bin", "943016");
        trxPayMap.put("last4", "****");
        trxPayMap.put("regDay", "20220603");
        trxPayMap.put("amount", "1004");
        //위험 100 만원 이상 거래
        String trxId = trxDAO.getWarningFact(trxPayMap);
        logger.debug("리스크 trxId {}", trxId);
        assertFalse("".equals(trxId));
    }
}
