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
        long val = capture.calcRentStlAmount(104961, 4.51);
        logger.debug("result {}", val);

        long val2 = capture.calcRentStlAmount(-104840, 4.40);
        logger.debug("result {}", val2);

        long val3 = capture.calcRentStlAmount(104400, 4);
        logger.debug("result {}", val3);

        long val4 = capture.calcRentStlAmount(-104400, 4);
        logger.debug("result {}", val4);

        long val5 = capture.calcRentStlAmount(3132, 4);
        logger.debug("result {}", val5);

        // 4400 * 4.4 = 193.6 => 예외 케이스이며 4.4% 계산시 소수점이 나오면 안된다.
        // 4400 * 5.5 = 242
        //long val6 = capture.calcRentStlAmount(4400 + 193, 5);
        long val6 = capture.calcRentStlAmount(4400 + 242, 5);
        logger.debug("result {}", val6);
    }
}
