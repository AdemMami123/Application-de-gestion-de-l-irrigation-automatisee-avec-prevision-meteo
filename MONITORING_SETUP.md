# Monitoring & Observability Stack - Setup Complete ✓

## Overview
Complete monitoring solution implemented for microservices with Prometheus, Grafana, Zipkin, and custom business metrics.

## Services Running

### Core Services
- **Gateway Service** (Port 8080)
  - Health: http://localhost:8080/actuator/health
  - Metrics: http://localhost:8080/actuator/prometheus

- **Meteo Service** (Port 8081)
  - Health: http://localhost:8081/actuator/health
  - Metrics: http://localhost:8081/actuator/prometheus

- **Arrosage Service** (Port 8082)
  - Health: http://localhost:8082/actuator/health
  - Metrics: http://localhost:8082/actuator/prometheus

### Monitoring Services
- **Prometheus** (Port 9090)
  - Metrics DB: http://localhost:9090
  - Targets: http://localhost:9090/targets
  - Query: http://localhost:9090/graph

- **Grafana** (Port 3000)
  - Dashboard: http://localhost:3000
  - Credentials: admin / admin
  - Default Datasource: Prometheus (http://prometheus:9090)

- **Zipkin** (Port 9411)
  - Tracing: http://localhost:9411
  - Query traces by service name

## Monitoring Architecture

### Metrics Collection
```
Microservices (Micrometer)
    ↓
/actuator/prometheus endpoints
    ↓
Prometheus (scrape every 15s)
    ↓
Grafana Dashboards & Alerts
```

### Distributed Tracing
```
Requests
    ↓
Spring Cloud Sleuth (correlation IDs)
    ↓
Zipkin (localhost:9411)
    ↓
Trace visualization & analysis
```

## Prometheus Configuration

**Global Scrape Settings:**
- Scrape Interval: 15 seconds
- Evaluation Interval: 15 seconds
- Timeout: 10 seconds

**Monitored Targets:**
1. **Gateway Service** (10s interval)
   - Endpoint: gateway-service:8080/actuator/prometheus
   - Metrics: HTTP requests, response times, error rates

2. **Meteo Service** (15s interval)
   - Endpoint: meteo-service:8081/actuator/prometheus
   - Metrics: Weather API calls, cache hits/misses, processing time

3. **Arrosage Service** (15s interval)
   - Endpoint: arrosage-service:8082/actuator/prometheus
   - Metrics: Irrigation programs, water usage, sensor readings

4. **Eureka Server** (30s interval)
   - Endpoint: eureka-server:8761/actuator/prometheus
   - Metrics: Service registration, heartbeats

5. **Redis Cache** (native exporter)
   - Endpoint: redis:6379
   - Metrics: Memory, connections, key operations

## Grafana Dashboards

### 1. Infrastructure Metrics Dashboard
**Purpose:** System health, performance, and reliability
**Time Range:** Last 6 hours
**URL:** http://localhost:3000/d/irrigation-infra

**Panels:**
- JVM Memory Usage (%)
- JVM Active Threads
- HTTP Request Rate (req/s)
- HTTP Response Time p95 (ms)
- HTTP Error Rate (5xx errors)
- Circuit Breaker Status (resilience4j)
- Weather Forecasts Fetched
- Irrigation Programs Executed
- External Weather API Calls

**Key Metrics:**
- `jvm_memory_usage_percent` - Java heap usage
- `jvm_threads_live` - Active threads
- `http_server_requests_seconds` - Request latency
- `resilience4j_circuitbreaker_state` - Circuit breaker status (0=CLOSED, 1=OPEN, 2=HALF_OPEN)

### 2. Business Metrics Dashboard
**Purpose:** Operational KPIs and business insights
**Time Range:** Last 24 hours
**URL:** http://localhost:3000/d/irrigation-business

**Panels:**
- Total Irrigation Programs Executed (stat card)
- Total Parcelles/Plots (stat card)
- Active Irrigation Programs Now (gauge)
- Total Irrigation Alerts Generated (stat card)
- Irrigation Programs Created (timeseries)
- Water Usage Over Time (m³/hour)
- Weather Forecasts Fetched Trend
- Program Execution Time p95 (ms)
- External Weather API Performance (calls vs errors)

**Key Metrics:**
- `irrigation_programs_executed` - Total programs run
- `irrigation_parcelles_total` - Total plots
- `irrigation_programs_active` - Currently running
- `irrigation_alerts_generated` - Total alerts
- `irrigation_water_usage_cubic_meters` - Water consumption
- `meteo_previsions_fetched` - Weather forecasts
- `meteo_api_calls_total` - API call count
- `meteo_api_errors_total` - API errors

## Custom Metrics Exported

### Meteo Service (MeteoMetricsCollector)
**Counters:**
- `meteo_previsions_fetched` - Weather forecasts retrieved
- `meteo_api_calls_total` - External API calls
- `meteo_api_errors_total` - API call failures
- `meteo_stations_created` - Stations created
- `meteo_cache_hits` - Cache hits
- `meteo_cache_misses` - Cache misses

**Timers (p50/p95/p99):**
- `meteo_weather_api_fetch_duration` - API response time
- `meteo_prevision_processing_duration` - Processing time

### Arrosage Service (IrrigationMetricsCollector)
**Counters:**
- `irrigation_programs_executed` - Programs run
- `irrigation_programs_created` - Programs created
- `irrigation_parcelles_created` - Plots created
- `irrigation_sensors_read` - Sensor readings
- `irrigation_alerts_generated` - Alerts triggered
- `irrigation_water_usage_cubic_meters` - Water used

**Timers (p50/p95/p99):**
- `irrigation_execution_duration` - Program run time
- `irrigation_sensor_read_duration` - Sensor read time

**Gauges:**
- `irrigation_programs_active` - Currently active programs
- `irrigation_parcelles_total` - Total plots

## Distributed Tracing Setup

**Zipkin Configuration:**
- Storage: In-memory (data cleared on restart)
- Sampling Rate: 100% (all requests traced)
- Base URL: http://localhost:9411

**Trace Propagation:**
- Sleuth context propagation across services
- Correlation IDs in logs
- Cross-service request tracing

**Access Traces:**
```
http://localhost:9411
→ Click "Search" 
→ Select service (gateway-service, meteo-service, arrosage-service)
→ View end-to-end request traces
```

## Health Checks

### All Services Healthy
```
✓ Gateway Service (port 8080) - Healthy
✓ Meteo Service (port 8081) - Healthy
✓ Arrosage Service (port 8082) - Healthy
✓ Prometheus (port 9090) - Healthy
✓ Grafana (port 3000) - Healthy
✓ Zipkin (port 9411) - Healthy
✓ Eureka Server (port 8761) - Healthy
✓ Redis Cache (port 6379) - Healthy
```

## Configuration Files

### Services Configuration
- **Gateway:** `backend/gateway-service/src/main/resources/application.properties`
  - Prometheus metrics endpoint enabled
  - Zipkin tracing configured
  - Spring Sleuth enabled

- **Meteo:** `backend/meteo-service/src/main/resources/application.properties`
  - Micrometer Prometheus registry
  - Custom metrics via MeteoMetricsCollector
  - Zipkin distributed tracing

- **Arrosage:** `backend/arrosage-service/src/main/resources/application.properties`
  - Micrometer Prometheus registry
  - Custom metrics via IrrigationMetricsCollector
  - Zipkin distributed tracing

### Monitoring Configuration
- **Prometheus:** `prometheus.yml`
  - 5 scrape job configurations
  - 15-second scrape interval
  - Automatic service discovery via static configs

- **Grafana:** 
  - Datasource: `grafana/provisioning/datasources/prometheus.yml`
  - Dashboards: `grafana/provisioning/dashboards/dashboard.yml`
  - Dashboard JSON: `grafana/dashboards/*.json`

### Docker Compose
- **Services Added:**
  - prometheus (prom/prometheus:latest)
  - grafana (grafana/grafana:latest)
  - zipkin (openzipkin/zipkin:latest)

- **Volumes:**
  - prometheus-data (metrics storage)
  - grafana-data (dashboard storage)

## Accessing Monitoring UIs

### Prometheus Queries

**CPU Usage:**
```promql
process_cpu_usage{job="gateway-service"}
```

**Memory Usage:**
```promql
jvm_memory_usage_bytes{area="heap",job=~".*-service"}
```

**HTTP Request Rate:**
```promql
rate(http_server_requests_seconds_count[1m])
```

**Business Metrics:**
```promql
irrigation_programs_executed{job="arrosage-service"}
meteo_previsions_fetched{job="meteo-service"}
```

### Grafana Login
```
URL: http://localhost:3000
Username: admin
Password: admin
```

**Pre-built Dashboards:**
1. Infrastructure Metrics (uid: irrigation-infra)
2. Business Metrics (uid: irrigation-business)

### Zipkin Tracing
```
URL: http://localhost:9411
Service Filter: gateway-service, meteo-service, arrosage-service
Trace Duration: Shows end-to-end request latency
```

## Dependencies Added

### Meteo Service (pom.xml)
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
    <version>2.2.8.RELEASE</version>
</dependency>
```

### Arrosage Service (pom.xml)
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
    <version>2.2.8.RELEASE</version>
</dependency>
```

## Application Properties

**All Services Include:**
```properties
# Prometheus Metrics
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.export.prometheus.enabled=true

# Distributed Tracing
spring.zipkin.base-url=http://localhost:9411
spring.zipkin.enabled=true
spring.sleuth.sampler.probability=1.0
spring.sleuth.log.slf4j.enabled=true
```

## Metrics Collection Flow

1. **Application Start**
   - MeteoMetricsCollector / IrrigationMetricsCollector beans initialized
   - MeterRegistry injected automatically by Spring
   - Endpoints ready at `/actuator/prometheus`

2. **Request Processing**
   - Sleuth assigns correlation IDs
   - Metrics recorded (counters, timers, gauges)
   - Span data sent to Zipkin

3. **Prometheus Scrape (every 15s)**
   - HTTP GET to `/actuator/prometheus`
   - Metrics formatted in Prometheus text format
   - Time-series data stored

4. **Grafana Visualization (every 30s)**
   - Queries Prometheus for metric data
   - Renders dashboards with latest values
   - Alerts evaluated

## Troubleshooting

### Prometheus Not Scraping
1. Check targets: http://localhost:9090/targets
2. Verify services are healthy: `docker ps`
3. Check metrics endpoint: `curl http://localhost:8081/actuator/prometheus`

### Grafana Dashboards Not Loading
1. Verify Prometheus datasource: http://localhost:3000/datasources
2. Check dashboard provisioning: `docker logs grafana`
3. Verify dashboard JSON syntax

### Zipkin Traces Not Appearing
1. Check microservices logs: `docker logs meteo-service`
2. Verify Zipkin is running: `curl http://localhost:9411`
3. Confirm trace sampling enabled: `spring.sleuth.sampler.probability=1.0`

### No Metrics in Dashboards
1. Make API requests to generate metrics
2. Wait 2-3 scrape intervals (30-45 seconds)
3. Check Prometheus graph: http://localhost:9090/graph
4. Search for metric name (e.g., `http_server_requests_seconds`)

## Next Steps

1. **Create Custom Alerts:**
   - Set up alert rules in Prometheus
   - Configure notification channels in Grafana

2. **Performance Tuning:**
   - Adjust scrape intervals based on workload
   - Optimize metric cardinality
   - Configure metric retention

3. **Extended Monitoring:**
   - Add ELK stack for log aggregation
   - Implement custom business metrics
   - Set up SLA monitoring

4. **Operational Dashboards:**
   - Create team-specific dashboards
   - Add incident response workflows
   - Set up on-call rotations

## Status: ✓ COMPLETE

All monitoring components deployed and operational:
- ✓ Prometheus metrics collection
- ✓ Grafana visualization dashboards
- ✓ Zipkin distributed tracing
- ✓ Custom business metrics
- ✓ Service health monitoring
- ✓ Request tracing across services
