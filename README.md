<div align="center">
  
# 📅 Schedulo: Intelligent Timetable SaaS

**A premium B2B Software-as-a-Service platform designed to solve highly complex scheduling and timetable generation for educational institutions, businesses, and hospitals.**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg?logo=spring)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg?logo=react)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)
</div>

<br />

## 🚀 Project Overview

**Schedulo** eliminates the massive overhead of manual schedule creation by dynamically mapping resources, instructors, and time constraints. Using an automated constraint-satisfaction engine, Schedulo generates conflict-free timetables in milliseconds. Built with a robust **Java Spring Boot backend**, a highly dynamic **React/Vite frontend**, and a scalable relational architecture.

---

## 🏗️ System Architecture & Design

Our system implements a modern, highly scalable 3-tier monolithic architecture designed for enterprise readiness, secure multi-tenancy, and high performance.

```mermaid
architecture-beta
    group client(cloud)[Client Tier]
    
    group app(server)[Application Server - Spring Boot]
    
    group db(database)[Data Tier]
    
    group external(cloud)[External Integrations]

    service frontend(internet)[React / Vite SPA] in client
    
    service gateway(server)[API Controller & JWT Auth] in app
    service engine(server)[Core Scheduling Engine] in app
    service core(server)[Resource & Multi-Tenant Core] in app
    
    service pg(database)[PostgreSQL DB] in db
    service redis(database)[Redis Cache & Sessions] in db

    service google(internet)[Google OAuth2] in external
    service stripe(internet)[Stripe Billing] in external

    frontend:R --> L:gateway
    gateway:B --> T:engine
    gateway:B --> T:core
    
    core:R --> L:pg
    engine:R --> L:pg
    
    gateway:R --> L:redis
    
    gateway:T --> B:google
    gateway:T --> B:stripe
```

### Component Breakdown
1. **Presentation Layer (React + Vite)**: A highly interactive Single Page Application capturing constraints, rendering dynamic calendars, and providing rich data insights via standard RESTful communication.
2. **Security & API Gateway (Spring Security)**: Stateless, distributed JWT-based authentication layered alongside Google OAuth2. Strictly enforces granular Role-Based Access Control (RBAC).
3. **Multi-Tenant Core**: Fully isolated organizational workspaces ensuring data privacy between distinct institutions.
4. **Timetable Engine**: The algorithm evaluates hardware/room capacities, instructor availability, and strict temporal rules to produce matrix-based zero-conflict schedules.
5. **Persistence Layer**: Structured relational data via **PostgreSQL** mapping complex associations between `Schedules`, `Resources`, and `Organizations` managed through JPA/Hibernate.

---

## ✨ Key Features

- **Automated Workflow Engine:** Input your resources and constraints, and Schedulo builds the entire timetable autonomously.
- **Organization Workspaces:** Multi-tenant architecture allowing users to invite team members and collaborate within isolated organizational boundaries.
- **Smart Resource Management:** Track room capacities, lab equipment, and instructor limits before conflicts ever happen.
- **Dynamic Interactive Calendar:** Seamless visualized schedules filterable by class, instructor, or room.
- **Enterprise Security:** JWT tokens with automated lifecycle refreshes, Bcrypt hashing, and integrated OAuth configurations.
- **Analytics Dashboard:** Read-at-a-glance telemetry on timetable health, resource usage, and team activity.

---

## 💻 Technology Stack

| Domain | Technologies |
| :--- | :--- |
| **Frontend** | React 18, Vite, Context API, Lucide Icons, Axios, Vanilla CSS |
| **Backend** | Java 21, Spring Boot 3.4.0, Spring Security, Hibernate ORM |
| **Database** | PostgreSQL (Primary), Redis (Session / Caching) |
| **Integrations** | Google OAuth2 API, Stripe Payment Gateway, Supabase Storage |
| **Tooling** | Maven, NPM, Git, Swagger / OpenAPI |

---

## 🛠️ Getting Started

### Prerequisites
- **Java 21** or later
- **Node.js** 18+ and npm
- **PostgreSQL** running locally on port 5432
- **Redis** running locally (or remote cloud)

### 1. Database Setup
Ensure PostgreSQL is running and create an empty database:
```sql
CREATE DATABASE timetable_db;
```
*Note: Hibernate will automatically create the schemas (`ddl-auto: update`).*

### 2. Backend Initialization
Navigate to the `Schedulo` backend directory, install maven dependencies, and start the Spring Boot application:
```bash
cd Schedulo
./mvnw clean install
./mvnw spring-boot:run
```
*The backend API will start on `http://localhost:8080/`.*

### 3. Frontend Initialization
In a separate terminal, navigate to the React `frontend` directory:
```bash
cd frontend
npm install
npm run dev
```
*The frontend application will start on `http://localhost:5173/`.*

---

## 🤝 Contributing & Showcase

This platform was engineered to demonstrate a deep understanding of full-stack scalability, intelligent back-end scheduling logic, and pristine UI/UX. For recruiters or technical interviewers viewing this repository, the primary implementation highlights reside within the `Timetable Generation Algorithm` and `Security Filters` within the Java architecture.

If you have any feedback or would like to discuss the underlying system design architecture in detail, feel free to reach out!

---
<div align="center">
<i>Architected & Built to handle complex scale elegantly.</i>
</div>
