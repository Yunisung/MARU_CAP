package com.pgmate.cap.main;

import com.pgmate.lib.util.lang.CommonUtil;
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
    public void testRisk() {
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

    @Test
    public void testRentMonthSum() {
        long monthSum = trxDAO.getRentMonthSum(CommonUtil.getCurrentDate("yyyyMM"), "bktest001");
        logger.debug("monthSum {}", monthSum);
    }

    @Test
    public void testRentFirstPaid() {
        String paid = trxDAO.getRentFirstPaid("bktest001");
        logger.debug("paid {}", paid);
    }

    @Test
    public void testRentStlAmount() {
        Capture capture = new Capture();
        long val = capture.calcRentStlAmount(104400, 0.04);
        logger.debug("result {}", val);

        long val2 = capture.calcRentStlAmount(104840, 0.044);
        logger.debug("result {}", val2);

        long val3 = capture.calcRentStlAmount(209900, 0.045);
        logger.debug("result {}", val3);

        // 0.04444 -> 0.048884
        long val4 = capture.calcRentStlAmount(1048884, 0.04444);
        logger.debug("result {}", val4);

        // 0.0444 -> 0.04884
        long val5 = capture.calcRentStlAmount(104884, 0.0444);
        logger.debug("result {}", val5);

        // 0.0444 -> 0.04884
        long val6 = capture.calcRentStlAmount(200000 + 9768, 0.0444);
        logger.debug("result {}", val6);

    }
}
