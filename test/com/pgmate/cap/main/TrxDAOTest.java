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
    }

    @Test
    public void 위험리스크_조회() {
        SharedMap<String, Object> trxPayMap = new SharedMap<>();
        trxPayMap.put("regDay", "20220801");
        trxPayMap.put("mchtId", "tep000");
        trxPayMap.put("bin", "473867");
        trxPayMap.put("last4", "906*");
        trxPayMap.put("amount", "100000");
        //위험 100 만원 이상 거래
        String trxId = trxDAO.getWarningFact(trxPayMap);
        logger.debug("리스크 trxId {}", trxId);
        assertFalse("".equals(trxId));
    }
}
