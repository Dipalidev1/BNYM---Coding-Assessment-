# Trades Capture Service - Demo

This is a minimal Spring Boot scaffold for the BNYM Coding Challenge (Trades Capture Service).

## Features
- File upload endpoint: POST /api/v1/instructions/upload
  - Accepts CSV (with header) or JSON array.
- Transformation to canonical format (masking, uppercasing security id, trade type normalization).
- In-memory storage (ConcurrentHashMap) for canonical records.
- Kafka publishing stub (disabled by default). When kafka.enabled=false, messages are placed on a local in-memory queue for demo and can be polled via GET /api/v1/instructions/outbound/poll
- Dockerfile included.

## Build & Run (local, without Kafka)
1. Install JDK 17 and Maven.
2. Build:
   ```
   mvn clean package
   ```
3. Run:
   ```
   java -jar target/trades-capture-service-0.0.1-SNAPSHOT.jar
   ```
4. Upload:
   - CSV example: `curl -v -F "file=@sample/sample.csv" http://localhost:8080/api/v1/instructions/upload`
   - JSON example: `curl -v -F "file=@sample/sample.json" http://localhost:8080/api/v1/instructions/upload`
5. Poll local outbound (demo):
   ```
   curl http://localhost:8080/api/v1/instructions/outbound/poll
   ```

## Enabling Kafka
Set `kafka.enabled=true` and provide Spring Kafka configuration (bootstrap servers, topic, etc.) in `application.yml` or environment. The project includes `KafkaProducerService` stub — adapt it to use `KafkaTemplate<String,String>` for real Kafka.

## Sample files
- `sample/sample.csv`
- `sample/sample.json`

