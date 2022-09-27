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

    private static Logger logger = LoggerFactory.getLogger(com.pgmate.cap.main.CaptureTest.class);

    Capture capture;
    TrxDAO trxDAO;

    @Before
    public void init() {
        capture = new Capture();
        trxDAO = new TrxDAO();
    }

    @Test
    public void 위험리스크_반영() {
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
                assertEquals(risk, "위험");
            }
        }
    }


}
