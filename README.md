Got it 🚀 — here’s the **final polished README** for your Quartz Scheduler project with updated API section.

---

# ⏰ Quartz Job Scheduler with Suspension & Management APIs

[![Build Status](https://github.com/sahil-salunke6/quartzSchedular/actions/workflows/ci.yml/badge.svg)](https://github.com/sahil-salunke6/quartzSchedular/actions)
[![codecov](https://codecov.io/gh/sahil-salunke6/quartzSchedular/branch/main/graph/badge.svg)](https://codecov.io/gh/sahil-salunke6/quartzSchedular)

A **Spring Boot + Quartz Scheduler** implementation that provides a **production-ready job scheduling system** with:

* Flexible **cron-based scheduling**
* Support for **daily, weekly, monthly, yearly** jobs
* **Instant job triggering**
* **Temporary & permanent suspension** with extensions/revocations
* **Job status & suspension tracking APIs**

This ensures **distributed scheduling**, **resiliency**, and easy integration into microservices.

---

## 📊 Features

* Schedule jobs with flexible intervals (`secondly`, `minutely`, `hourly`, `daily`, `weekly`, `monthly`, `yearly`).
* Trigger jobs instantly via API.
* Suspend jobs temporarily (with resume time) or permanently.
* Revoke or extend suspensions.
* Retrieve job status and all suspended jobs.
* Centralized exception handling for job operations.
* Backed by **Quartz Scheduler** and **H2 database**.

---

## ⚙️ Tech Stack

* **Java 17**
* **Spring Boot 3.5.3**
* **Quartz Scheduler**
* **H2 Database** (in-memory for local testing)
* **SLF4J + Logback** for logging
* **JUnit 5 + Mockito** for testing

---

## 🛠️ Setup & Run

### 1. Clone Repository

```bash
git clone https://github.com/sahil-salunke6/quartzSchedular.git
cd quartzSchedular
```

### 2. Build Project

```bash
./gradlew clean build
```

### 3. Run Application

```bash
./gradlew bootRun
```

Application will start at:
👉 [http://localhost:9091](http://localhost:9091)

Swagger UI available at:
👉 [http://localhost:9091/swagger-ui.html](http://localhost:9091/swagger-ui.html)

H2 console UI available at:
👉 [http://localhost:9091/h2-console.html](http://localhost:9091/h2-console.html)

---

## 📄 API Endpoints

Base Path:

```
/quartz/job
```

| Method   | Endpoint                        | Description                                                                        |
| -------- | ------------------------------- | ---------------------------------------------------------------------------------- |
| **POST** | `/scheduleJob`                  | Schedule a new job with flexible intervals (cron, daily, weekly, monthly, yearly). |
| **POST** | `/triggerInstantly`             | Trigger a job immediately.                                                         |
| **POST** | `/suspend/temporary`            | Suspend a job until a specific resume date/time.                                   |
| **POST** | `/suspend/permanent`            | Suspend a job permanently (until manually resumed).                                |
| **POST** | `/suspend/revoke`               | Revoke suspension and resume a job.                                                |
| **POST** | `/suspend/extend`               | Extend suspension period for a temporarily suspended job.                          |
| **GET**  | `/{jobName}/{groupName}/status` | Get status and suspension info of a specific job.                                  |
| **GET**  | `/suspended`                    | Retrieve all currently suspended jobs.                                             |

---

## 📦 Example Requests

### ✅ Schedule a Daily Job

```http
POST /quartz/job/scheduleJob
Content-Type: application/json
```

**Params:**

```
second=0&minute=0&hour=9&day=*&month=*&year=*&repeat=true&interval=daily&jobName=DailyReportJob
```

### ✅ Trigger Job Instantly

```json
POST /quartz/job/triggerInstantly
{
  "jobName": "DailyReportJob",
  "groupName": "QuartzGroup"
}
```

### ✅ Suspend Job Temporarily

```json
POST /quartz/job/suspend/temporary
{
  "jobName": "DailyReportJob",
  "groupName": "QuartzGroup",
  "resumeDateTime": "2025-09-20T10:00:00"
}
```

---

## ✅ Testing

Run all tests with:

```bash
./gradlew test
```

Includes unit tests for:

* Cron expression validation & generation
* Job scheduling logic
* Suspension management

---

## 📌 Roadmap

* [ ] Add **REST-based dynamic cron updates**
* [ ] Kafka/Redis integration for **distributed scheduling**
* [ ] Observability (metrics + tracing)

---

## 👨‍💻 Author

**Sahil D. Salunke**
🔗 [LinkedIn](https://www.linkedin.com/in/sahildsalunke/)

---
