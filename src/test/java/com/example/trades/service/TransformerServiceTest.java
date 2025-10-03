package com.example.trades.service;

import com.example.trades.model.Instruction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

public class TransformerServiceTest {

    private final TransformerService svc = new TransformerService();

    @Test
    public void testMaskAndNormalize() {
        Instruction in = new Instruction();
        in.accountNumber = "0000123412341234";
        in.securityId = "abC-1x";
        in.tradeType = "Buy";
        in.quantity = BigDecimal.TEN;
        in.tradeDate = "2025-10-01";
        var c = svc.toCanonical(in);
        assertEquals("****1234", c.accountNumberMasked);
        assertEquals("ABC-1X", c.securityId);
        assertEquals("B", c.tradeTypeCode);
    }
}
