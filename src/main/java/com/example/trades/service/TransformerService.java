package com.example.trades.service;

import com.example.trades.model.CanonicalInstruction;
import com.example.trades.model.Instruction;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TransformerService {

    private static final Pattern SECURITY_PATTERN = Pattern.compile("^[A-Z0-9\\-]{1,20}$");

    public CanonicalInstruction toCanonical(Instruction in) throws IllegalArgumentException {
        validate(in);
        CanonicalInstruction c = new CanonicalInstruction();
        c.accountNumberMasked = maskAccount(in.accountNumber);
        c.securityId = in.securityId == null ? null : in.securityId.toUpperCase();
        if (!SECURITY_PATTERN.matcher(c.securityId == null ? "" : c.securityId).matches()) {
            throw new IllegalArgumentException("Invalid security_id format: " + in.securityId);
        }
        c.tradeTypeCode = normalizeTradeType(in.tradeType);
        c.quantity = in.quantity;
        c.tradeDate = in.tradeDate;
        return c;
    }

    private void validate(Instruction in) {
        if (in == null) throw new IllegalArgumentException("Instruction is null");
        if (in.accountNumber == null || in.accountNumber.trim().isEmpty()) throw new IllegalArgumentException("accountNumber required");
        if (in.securityId == null || in.securityId.trim().isEmpty()) throw new IllegalArgumentException("securityId required");
        if (in.tradeType == null || in.tradeType.trim().isEmpty()) throw new IllegalArgumentException("tradeType required");
    }

    private String maskAccount(String acct) {
        String digits = acct.replaceAll("[^0-9]", "");
        if (digits.length() <= 4) return "****" + digits;
        String last4 = digits.substring(digits.length() - 4);
        return "****" + last4;
    }

    private String normalizeTradeType(String t) {
        if (t == null) return null;
        t = t.trim().toLowerCase();
        if (t.startsWith("b")) return "B";
        if (t.startsWith("s")) return "S";
        return t.toUpperCase();
    }
}
