package com.example.trades.controller;

import com.example.trades.kafka.KafkaProducerService;
import com.example.trades.model.CanonicalInstruction;
import com.example.trades.model.Instruction;
import com.example.trades.service.TransformerService;
import com.example.trades.store.InMemoryStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/instructions")
public class FileUploadController {

    private final TransformerService transformer;
    private final InMemoryStore store;
    private final KafkaProducerService producer;
    private final ObjectMapper mapper = new ObjectMapper();

    public FileUploadController(TransformerService transformer, InMemoryStore store, KafkaProducerService producer) {
        this.transformer = transformer;
        this.store = store;
        this.producer = producer;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) throws Exception {
        String name = file.getOriginalFilename();
        List<Instruction> list = new ArrayList<>();
        if (name != null && name.toLowerCase().endsWith(".csv")) {
            Reader in = new InputStreamReader(file.getInputStream());
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(in);
            for (CSVRecord r : records) {
                Instruction inst = new Instruction();
                inst.accountNumber = r.get("accountNumber");
                inst.securityId = r.get("securityId");
                inst.tradeType = r.get("tradeType");
                inst.quantity = new BigDecimal(r.get("quantity"));
                inst.tradeDate = r.get("tradeDate");
                list.add(inst);
            }
        } else {
            // assume JSON array
            list = mapper.readValue(file.getInputStream(), new TypeReference<List<Instruction>>() {});
        }

        List<String> ids = new ArrayList<>();
        for (Instruction i : list) {
            CanonicalInstruction c = transformer.toCanonical(i);
            String id = store.save(c);
            ids.add(id);
            producer.publish("instructions.outbound", c);
        }
        return ResponseEntity.ok(java.util.Map.of("processed", ids.size(), "ids", ids));
    }

    @GetMapping("/outbound/poll")
    public ResponseEntity<?> pollOutbound() {
        String msg = producer.pollLocalOutbound();
        if (msg == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(java.util.Map.of("message", msg));
    }
}
