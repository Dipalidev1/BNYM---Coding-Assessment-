package com.example.trades.kafka;

import com.example.trades.model.CanonicalInstruction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class KafkaProducerService {

    private final boolean kafkaEnabled;
    private final ObjectMapper mapper = new ObjectMapper();

    // In-memory queue used when kafka.enabled=false (for local runs)
    private final BlockingQueue<String> localOutboundQueue = new LinkedBlockingQueue<>();

    public KafkaProducerService(@Value("${kafka.enabled:false}") boolean kafkaEnabled) {
        this.kafkaEnabled = kafkaEnabled;
    }

    public void publish(String topic, CanonicalInstruction c) {
        try {
            String json = mapper.writeValueAsString(toPlatformJson(c));
            if (kafkaEnabled) {
                // Real Kafka publishing will be wired by Spring Kafka if kafkaEnabled=true and beans configured.
                // For brevity, we assume Spring Kafka template is configured externally.
                // Placeholder: log to console (non-sensitive).
                System.out.println("[KAFKA] publishing to topic=" + topic + " payloadSize=" + json.length());
            } else {
                // Push to in-memory queue for local testing/demo
                localOutboundQueue.offer(json);
                System.out.println("[LOCAL-QUEUE] enqueued message size=" + json.length());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object toPlatformJson(CanonicalInstruction c) {
        // Simple mapping to platform JSON structure hinted by challenge.
        return new java.util.LinkedHashMap<String, Object>() {{
            put("account", c.accountNumberMasked);
            put("security", c.securityId);
            put("tradeType", c.tradeTypeCode);
            put("quantity", c.quantity);
            put("tradeDate", c.tradeDate);
        }};
    }

    // Helper to retrieve local queue content (for demo/testing)
    public String pollLocalOutbound() {
        return localOutboundQueue.poll();
    }
}
