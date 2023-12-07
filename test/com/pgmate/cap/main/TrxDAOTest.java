package com.pgmate.cap.main;

import com.pgmate.lib.util.lang.CommonUtil;
import com.pgmate.lib.util.map.SharedMap;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        // 반올림
//        long val = capture.calcRentStlAmount(195445, 0.036);
//        logger.debug("result {}", val);
//
//        long val2 = capture.calcRentStlAmount(138267, 0.036);
//        logger.debug("result {}", val2);
//
//        long val3 = capture.calcRentStlAmount(449, 0.036);
//        logger.debug("result {}", val3);
//
//        // 0.04444 -> 0.048884
//        long val4 = capture.calcRentStlAmount(577093, 0.036);
//        logger.debug("result {}", val4);
//
//        // 0.0444 -> 0.04884
//        long val5 = capture.calcRentStlAmount(10267673, 0.036);
//        logger.debug("result {}", val5);

        // 올림
        // 0.0444 -> 0.04884
        long val6 = capture.calcRentStlAmount(1043, 0.036);
        logger.debug("result {}", val6);

        long val7 = capture.calcRentStlAmount(115741, 0.036);
        logger.debug("result {}", val7);

    }

    @Test
    public void sample() {
        /*long aaa = CommonUtil.parseLong("20231205");
        logger.debug("aaa : {}", aaa);

        long bbb = CommonUtil.parseLong(CommonUtil.getCurrentDate("yyyyMMdd"));
        logger.debug("bbb : {}", bbb);

        assertTrue(aaa <= bbb);*/


        List<SharedMap<String, Object>> insertChargeSettleFirmList = new ArrayList<>();
        SharedMap<String, Object> a = new SharedMap<>();
        a.put("mchtId", "a");
        a.put("pubDay", "20230101");

        SharedMap<String, Object> b = new SharedMap<>();
        b.put("mchtId", "b");
        b.put("pubDay", "20230101");

        SharedMap<String, Object> c = new SharedMap<>();
        c.put("mchtId", "a");
        c.put("pubDay", "20230120");

        SharedMap<String, Object> d = new SharedMap<>();
        d.put("mchtId", "a");
        d.put("pubDay", "20230101");

        SharedMap<String, Object> e = new SharedMap<>();
        e.put("mchtId", "b");
        e.put("pubDay", "20230120");

        insertChargeSettleFirmList.add(a);
        insertChargeSettleFirmList.add(b);
        insertChargeSettleFirmList.add(c);
        insertChargeSettleFirmList.add(d);
        insertChargeSettleFirmList.add(e);

        Map<String, Map<String, List<SharedMap<String, Object>>>> sameTransferDayMap =
                insertChargeSettleFirmList.stream().collect(Collectors.groupingBy(i -> i.getString("mchtId"), Collectors.groupingBy(i -> i.getString("pubDay"))));

        logger.info("size: {}", sameTransferDayMap.size());
    }
}
