# Monitoring Concepts — Reference Guide

## Table of Contents
- [Overview](#overview)
- [Monitoring Tools Comparison](#monitoring-tools-comparison)
- [Prometheus](#1-prometheus)
- [Grafana](#2-grafana)
- [Splunk](#3-splunk)
- [Datadog](#4-datadog)
- [Tool Selection Guide](#tool-selection-guide)
- [Common Architectures](#common-architectures)
- [Key Monitoring Concepts](#key-monitoring-concepts)
- [Interview Quick Reference](#interview-quick-reference)

---

## Overview

Monitoring is the practice of collecting, analyzing, and alerting on data from systems and applications to ensure reliability, performance, and availability.

**Three pillars of observability:**

```
| Pillar  | What it answers               | Tooling               |
|---------|-------------------------------|-----------------------|
| Metrics | How is the system performing? | Prometheus, Datadog   |
| Logs    | What happened and when?       | Splunk, Loki, ELK     |
| Traces  | Where did the request go?     | Jaeger, Zipkin, Datadog APM 
```

---

## Monitoring Tools Comparison

```
| Tool       | Type            | Deployment          | Best For                  | Pricing            |
|------------|-----------------|---------------------|---------------------------|--------------------|
| Prometheus | Metrics         | Self-hosted         | Cloud-native, Kubernetes  | Open Source        |
| Grafana    | Visualization   | Self-hosted / Cloud | Dashboards for any source | Open Source / Paid |
| Splunk     | Log Analysis    | On-prem / Cloud     | Enterprise log management | Paid               | 
| Datadog    | All-in-one SaaS | Cloud               | Full-stack observability  | Paid SaaS          | 
```

---

## 1. Prometheus

**Rating: ⭐⭐⭐⭐⭐** — Most Popular Metrics Monitoring

Prometheus is an open-source, pull-based metrics monitoring system originally inspired by Google's Borgmon. It is a CNCF graduated project and the de facto standard for Kubernetes-native monitoring.

### Used By
- Google-inspired CNCF projects
- Kubernetes environments
- Most cloud-native companies

### Architecture

```
Application (metrics endpoint /metrics)
        ↓  pull (scrape)
   Prometheus Server
        ↓  query (PromQL)
      Grafana          ←→  Alertmanager → PagerDuty / Slack
```

### Use Cases
- Metrics collection from services and infrastructure
- CPU / Memory / Disk monitoring
- Application-level metrics (request rate, error rate, latency)
- Alerting via Alertmanager

### Key Concepts
```
| Concept | Description |
|--------------|---------------------------------------------------------|
| Scrape       | Prometheus pulls metrics by calling `/metrics` endpoint |
| Exporter     | Agent that exposes metrics (Node Exporter, JMX Exporter)|
| PromQL       | Prometheus Query Language for querying time-series data |
| Alert Rule   | Condition defined in YAML that fires an alert           |
| Alertmanager | Routes alerts to Slack, PagerDuty, email                |
```

### Metric Types
```
| Type      | Description                   | Example                   |
|-----------|-------------------------------|---------------------------|
| Counter   | Always increases              | Total HTTP requests       |
| Gauge     | Can go up or down             | Current memory usage      |
| Histogram | Bucketed distribution         | Request latency buckets   |
| Summary   | Quantiles over sliding window | 95th percentile latency   |
```
### PromQL Examples

```promql
# Request rate over last 5 minutes
rate(http_requests_total[5m])

# Error rate percentage
sum(rate(http_requests_total{status=~"5.."}[5m])) /
sum(rate(http_requests_total[5m])) * 100

# Memory usage
process_resident_memory_bytes / 1024 / 1024

# P95 latency from histogram
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))
```

### Often Used With
- **Grafana** — visualization and dashboards
- **Alertmanager** — alert routing and deduplication
- **Node Exporter** — OS-level metrics
- **JMX Exporter** — JVM metrics for Java apps
- **Kube-state-metrics** — Kubernetes object metrics

---

## 2. Grafana

**Rating: ⭐⭐⭐⭐⭐** — Visualization

Grafana is the industry-standard open-source visualization platform. Used by almost every organization that runs Prometheus.

### Use Cases
- Building operational dashboards
- Unified monitoring across multiple data sources
- Visualization of metrics, logs, and traces in one UI
- Alerting with notification routing

### Supported Data Sources
```
| Category  | Sources                                   |
|-----------|-------------------------------------------|
| Metrics   | Prometheus, InfluxDB, Graphite, Datadog   |
| Logs      | Loki, Elasticsearch, Splunk               |
| Databases | MySQL, PostgreSQL, MongoDB, Redis         |
| Cloud     | CloudWatch (AWS), Azure Monitor, GCP      |
| Tracing   | Jaeger, Zipkin, Tempo                     |

```
### Key Concepts
```
| Concept     | Description                                                    |
|-------------|----------------------------------------------------------------|
| Dashboard   | Collection of panels displaying data from one or more sources |
| Panel       | Single visualization unit (graph, table, stat, heatmap)       |
| Data Source | Connected backend that Grafana queries                        |
| Variable    | Template variable for dynamic dashboards (e.g., `$env`, `$service`) |
| Annotation  | Marks events on time series graphs (deployments, incidents)   |
| Alert       | Rule evaluated at intervals; sends notifications on breach    |

```
### Grafana Stack (LGTM)

```
Logs    → Loki
Metrics → Grafana (+ Prometheus)
Traces  → Tempo
         ↓
      Grafana UI
```

---

## 3. Splunk

**Rating: ⭐⭐⭐⭐⭐** — Log Analysis

Splunk is the dominant enterprise log management and SIEM platform. Widely adopted in large financial services, healthcare, and government organizations.

### Used By
- Large enterprise companies (banking, insurance, finance)
- Security Operations Centers (SOC)
- Organizations with strict compliance requirements

### Use Cases
- Application log aggregation and search
- Production debugging and root cause analysis
- Security monitoring and threat detection (SIEM)
- Compliance auditing
- Business intelligence from machine data

### Architecture

```
Application / Server
       ↓
  Splunk Forwarder (Universal Forwarder)
       ↓
  Splunk Indexer (data indexing + storage)
       ↓
  Splunk Search Head (query + dashboard UI)
```

### Key Concepts
```
| Concept              | Description                                                   |
|----------------------|---------------------------------------------------------------|
| SPL                  | Splunk Processing Language — used to search and analyze events |
| Index                | Data store where events are organized                         |
| Sourcetype           | Category of data format (e.g., `java_app`, `nginx`, `syslog`) |
| Universal Forwarder  | Lightweight agent deployed on servers to ship logs            |
| Search Head          | UI component for running SPL queries and viewing dashboards   |
| Indexer              | Component that processes and stores incoming data             |

```

### SPL Examples

```spl
-- Basic search
index=prod_logs sourcetype=java_app ERROR

-- Count errors by host
index=prod_logs level=ERROR | stats count by host

-- Exceptions in last 1 hour
index=prod_logs sourcetype=java_app "Exception"
| timechart span=5m count by exception_type

-- Top 10 slowest API calls
index=prod_logs | stats avg(response_time) as avg_ms by endpoint
| sort -avg_ms | head 10

-- Alert: error rate spike
index=prod_logs level=ERROR
| timechart span=1m count
| where count > 100
```

### Features
- SPL (Splunk Processing Language) for powerful log queries
- Dashboards and real-time visualizations
- Alerts with threshold-based and ML-based anomaly detection
- Role-based access control (RBAC)
- Integration with PagerDuty, ServiceNow, Jira

---

## 4. Datadog

**Rating: ⭐⭐⭐⭐⭐** — Full-Stack Observability SaaS

Datadog is one of the most popular commercial SaaS monitoring platforms, offering a unified solution for metrics, logs, traces, and security in a single product.

### Used By
- Mid-size to large tech companies
- Organizations preferring managed SaaS over self-hosted tooling
- Teams needing APM + infrastructure + logs in one place

### Use Cases
```
| Category                           | Details                                        |
|------------------------------------|------------------------------------------------|
| Infrastructure Monitoring          | Host metrics, cloud resources, containers      |
| APM (Application Performance Monitoring) | Distributed tracing, service maps, latency analysis |
| Log Management                     | Log ingestion, parsing, search, and archiving  |
| Security Monitoring                | SIEM, threat detection, compliance             |
| Kubernetes Monitoring              | Pod metrics, cluster health, container logs    |
| Cloud Monitoring                   | AWS, Azure, GCP native integrations            |
| Synthetics                         | Uptime checks, browser tests, API tests        |

```

### Architecture

```
Application (dd-agent / auto-instrumentation)
         ↓
   Datadog Agent (host/container)
         ↓  (HTTPS)
   Datadog Platform (SaaS)
         ↓
   Dashboards / Alerts / APM / Logs / Security
```

### Key Concepts
```
| Concept      | Description                                                            |
|--------------|------------------------------------------------------------------------|
| Agent        | Lightweight daemon installed on the host; collects and ships data      |
| APM          | Distributed tracing; visualizes request flow across microservices      |
| Service Map  | Auto-generated dependency map of all services                          |
| Log Pipeline | Parses, enriches, and routes incoming logs                             |
| Monitor      | Alert rule; supports threshold, anomaly, forecast, and composite rules |
| Integration  | Pre-built connectors for 600+ technologies                             |
| Tagging      | Key-value labels for filtering metrics/logs (`env:prod`, `service:payment`) |
```
### Datadog vs Prometheus
```
| Aspect        | Prometheus            | Datadog                  |
|---------------|-----------------------|--------------------------|
| Deployment    | Self-hosted           | SaaS                     |
| Cost          | Free                  | Paid (per host)          |
| Setup effort  | High                  | Low                      |
| Retention     | Limited (local)       | Long-term cloud          |
| APM / Tracing | Requires extra tools  | Built-in                 |
| Logs          | Requires Loki/ELK     | Built-in                 |
| Best for      | Cloud-native / Kubernetes | All-in-one enterprise|

```
---

## Tool Selection Guide

```
Do you need log analysis?
├── Yes, enterprise / compliance heavy → Splunk
└── Yes, cloud-native / cost-sensitive → Loki + Grafana

Do you need metrics monitoring?
├── Kubernetes / cloud-native → Prometheus + Grafana
└── All-in-one SaaS, less ops overhead → Datadog

Do you need visualization on top of existing data?
└── Grafana (works with all of the above)
```

---

## Common Architectures

### Cloud-Native Stack (Open Source)

```
Spring Boot App
    ↓ /metrics (Micrometer)
Prometheus (scrape every 15s)
    ↓ PromQL
Grafana (dashboards)
    ↓ alert rules
Alertmanager → Slack / PagerDuty
```

### Enterprise Stack

```
Java App (Log4j / Logback)
    ↓ Universal Forwarder
Splunk Indexer
    ↓ SPL queries
Splunk Dashboard / Alerts → SOC / On-call team
```

### Full SaaS Stack

```
Microservices (dd-agent sidecar)
    ↓
Datadog Platform
    ├── APM (traces)
    ├── Metrics (infra + custom)
    ├── Logs
    └── Security
```

### Hybrid Stack (Common in Large Companies)

```
Infrastructure Metrics → Prometheus → Grafana
Application Logs       → Splunk
Distributed Tracing    → Datadog APM  (or Jaeger)
Business Dashboards    → Grafana (federated sources)
```

---

## Key Monitoring Concepts

### RED Method (for Services)
```
| Metric   | Description                |
|----------|----------------------------|
| Rate     | Requests per second        |
| Errors   | Error rate (%)             |
| Duration | Latency (p50, p95, p99)    |

```
### USE Method (for Infrastructure)
```
| Metric      | Description                |
|-------------|----------------------------|
| Utilization | % time resource is busy    |
| Saturation  | Queue depth / backlog      |
| Errors      | Error count / rate         |

```
### SLI / SLO / SLA
```
| Term                             | Definition                  | Example                               |
|----------------------------------|-----------------------------|---------------------------------------|
| SLI (Service Level Indicator)    | The actual measured metric  | 99.95% availability over 30 days      |
| SLO (Service Level Objective)    | Internal target for the SLI | Availability must be >= 99.9%         |
| SLA (Service Level Agreement)    | External contractual commitment | 99.5% uptime or credits issued    |
```
### Alert Design Principles

- Alert on symptoms, not causes (alert on high error rate, not high CPU)
- Every alert must be actionable — no noise alerts
- Define severity: P1 (page), P2 (ticket), P3 (log)
- Set meaningful thresholds — avoid alert fatigue

---

## Interview Quick Reference
```

| Question                           | Answer                                                                                  |
|------------------------------------|-----------------------------------------------------------------------------------------|
| Prometheus pull vs push?           | Prometheus pulls (scrapes); Pushgateway exists for short-lived jobs                     |
| How does Alertmanager work?        | Receives alerts from Prometheus, deduplicates, groups, and routes them to receivers     |
| Grafana data source for logs?      | Loki (cloud-native), Elasticsearch, Splunk                                              |
| Splunk vs ELK?                     | Splunk = paid, easier, enterprise; ELK = free, more setup, flexible                     |
| Datadog APM vs Jaeger?             | Datadog = SaaS, full-stack; Jaeger = open-source, traces only                           |
| What is a cardinality explosion?   | Too many unique label combinations in Prometheus, causing high memory usage             |
| Micrometer in Spring Boot?         | Metrics facade; auto-exposes `/actuator/prometheus` endpoint                            |
| What is an exporter?               | Agent that translates existing metrics into Prometheus format                           |

```