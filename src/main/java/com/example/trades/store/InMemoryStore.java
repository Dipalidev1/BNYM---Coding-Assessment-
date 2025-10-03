package com.example.trades.store;

import com.example.trades.model.CanonicalInstruction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Component
public class InMemoryStore {
    private final Map<String, CanonicalInstruction> store = new ConcurrentHashMap<>();

    public String save(CanonicalInstruction c) {
        String id = UUID.randomUUID().toString();
        store.put(id, c);
        return id;
    }

    public CanonicalInstruction get(String id) {
        return store.get(id);
    }
}
