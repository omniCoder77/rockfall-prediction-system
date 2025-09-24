# Rockfall Prediction System

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-%237F52FF?style=for-the-badge&logo=kotlin)![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-%236DB33F?style=for-the-badge&logo=spring)![Microservices](https://img.shields.io/badge/Architecture-Microservices-blue?style=for-the-badge)

A comprehensive, real-time system designed to monitor, analyze, and predict the risk of rockfall events in a mining environment. This project leverages a distributed, event-driven microservices architecture to handle high-volume sensor data, user authentication, and machine learning-based risk assessment.

## Table of Contents

- [Architectural Overview](#architectural-overview)
- [Technology Stack](#technology-stack)
  - [Backend & Core Frameworks](#backend-and-core-frameworks)
  - [Databases & Data Storage](#databases-and-data-storage)
  - [Messaging & Event Streaming](#messaging-and-event-streaming)
  - [Authentication & Security](#authentication-and-security)
  - [External Integrations](#external-integrations)
  - [Testing & Logging](#testing-and-logging)
- [Services](#services)
- [Key Features](#key-features)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Endpoints](https://github.com/omniCoder77/rockfall-prediction-system/blob/master/docs/api/API.md)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [License](#license)


<a name="architectural-overview"></a>
## Architectural Overview

The system is built on a reactive, microservices-based architecture. An **API Gateway** serves as the single entry point for all clients, routing requests to the appropriate downstream services. The services communicate with each other asynchronously via **Apache Kafka** where necessary, ensuring loose coupling and high resilience.

```
+----------------+      +------------------+      +-----------------+
|   Clients      |----->|   API Gateway    |----->|   Auth Service  |
| (Web/Mobile)   |      +------------------+      +-----------------+
+----------------+               |
                                 |
         +-----------------------+-----------------------+
         |                       |                       |
+-----------------+     +------------------+     +-----------------+
|   Mine Service  |     |  Sensor Service  |     |    ML Service   |
+-----------------+     +------------------+     +-----------------+
         |                       |                       |
+-----------------+     +------------------+     +-----------------+
|   PostgreSQL    |     |    InfluxDB      |     |  Python ML API  |
+-----------------+     |      (MQTT)      |     +-----------------+
                        +------------------+
                                 |
                        +------------------+
                        |   Apache Kafka   |
                        +------------------+
```

<a name="technology-stack"></a>
## Technology Stack

This project utilizes a modern, robust, and scalable technology stack.

<a name="backend-and-core-frameworks"></a>
#### **Backend & Core Frameworks**

| Technology        | Description                                           |
| ----------------- | ----------------------------------------------------- |
| **Kotlin**        | Primary programming language.                         |
| **Spring Boot**   | Core framework for building all microservices.        |
| **Spring WebFlux**| For building reactive, non-blocking APIs.             |
| **Project Reactor**| Implements the reactive streams specification.      |
| **Spring Security**| Handles authentication, authorization, and security.|
| **Spring Cloud Gateway** | Provides the API Gateway for request routing. |

<a name="databases-and-data-storage"></a>
#### **Databases & Data Storage**

| Technology | Description |
|---|---|
| **PostgreSQL** | Relational database for `mine-service` and `auth-service`. |
| **Spring Data R2DBC** | Reactive database access for PostgreSQL. |
| **InfluxDB** | Time-series database for storing all sensor data. |
| **Redis** | In-memory data store for caching, rate limiting, and session management. |

<a name="messaging-and-event-streaming"></a>
#### **Messaging & Event Streaming**

| Technology | Description |
|---|---|
| **Apache Kafka** | Distributed event streaming platform for inter-service communication. |
| **MQTT (Eclipse Paho)** | Lightweight messaging protocol for ingesting data from IoT sensors. |

<a name="authentication-and-security"></a>
#### **Authentication & Security**

| Technology | Description |
|---|---|
| **JWT (JSON Web Tokens)** | Used for securing the API with access and refresh tokens. |
| **BCrypt** | Password hashing algorithm. |
| **Java KeyStore (JCEKS)** | For secure management of JWT signing keys. |

<a name="external-integrations"></a>
#### **External Integrations**

| Technology | Description |
|---|---|
| **Twilio** | For sending SMS-based One-Time Passwords (OTP). |
| **Python ML API** | An external REST API that serves machine learning predictions. |

<a name="testing-and-logging"></a>
#### **Testing & Logging**

| Technology | Description |
|---|---|
| **JUnit 5 & Mockito** | For unit and integration testing. |
| **SLF4J & Logback** | Logging abstraction and implementation. |

<a name="services"></a>
## Services

| Service | Description | Port |
| :--- | :--- | :---: |
| **`api-gateway`** | The single entry point for all incoming client requests. Manages routing, rate limiting, and cross-cutting concerns. | `8080` |
| **`auth-service`** | Manages user registration, login (email/password & OTP), token generation (JWT), and role-based access control. | `8081` |
| **`mine-service`** | Manages core domain entities like mining stations, their configurations, and associated metadata. | `8082` |
| **`sensor-service`** | Ingests real-time data from various sensors (e.g., displacement, strain, vibration) via MQTT, stores it in InfluxDB, and publishes events to Kafka. | `8083` |
| **`ml-service`** | Interfaces with an external Python-based machine learning model to get rockfall risk predictions based on aggregated sensor data. | `8084` |

<a name="key-features"></a>
## Key Features

- **Secure Authentication**: Robust login/registration system with JWT, refresh tokens, and OTP capabilities.
- **Role-Based Access Control**: Differentiated access levels for ADMIN, SUPERVISOR, and USER roles.
- **Real-Time Sensor Ingestion**: High-throughput data pipeline using MQTT, InfluxDB, and Kafka.
- **Dynamic Sensor Management**: APIs to add, remove, and view the status of active sensors at each station.
- **Historical Data Analysis**: Endpoints to query and aggregate historical sensor data over custom time ranges.
- **ML-Powered Risk Prediction**: Integrates with a machine learning model to provide real-time risk scores for rockfall events.
- **Reactive and Scalable**: Built with a non-blocking stack to handle a high degree of concurrency.

<a name="prerequisites"></a>
## Prerequisites

- JDK 17 or higher
- Docker and Docker Compose
- An MQTT Broker (e.g., Mosquitto) running on `localhost:1883`
- An external Machine Learning API endpoint

<a name="getting-started"></a>
## Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/rockfall-prediction-system.git
    cd rockfall-prediction-system
    ```

2.  **Set up Infrastructure with Docker Compose:**
    A `docker-compose.yml` file is recommended to easily spin up the required backing services.

    ```yaml
    # Example docker-compose.yml
    version: '3.8'
    services:
      postgres:
        image: postgres:14
        environment:
          POSTGRES_USER: user
          POSTGRES_PASSWORD: password
          POSTGRES_DB: rockfall_db
        ports:
          - "5432:5432"
      redis:
        image: redis:6-alpine
        ports:
          - "6379:6379"
      influxdb:
        image: influxdb:2.7
        ports:
          - "8086:8086"
        volumes:
          - influxdb_data:/var/lib/influxdb2
    # ... add Kafka, Zookeeper, and MQTT Broker if needed
    volumes:
      influxdb_data:
    ```
    Start the services:
    ```bash
    docker-compose up -d
    ```

3.  **Configure Services:**
    Each service (`auth-service`, `mine-service`, etc.) has an `application.properties` or `application.yml` file in `src/main/resources`. Update the database URLs, credentials, JWT secrets, and other properties as needed to match your environment.

4.  **Build and Run the Services:**
    You can run each Spring Boot application using your IDE or by building a JAR file with Maven/Gradle.

    Using Gradle:
    ```bash
    # From the root directory of a specific service (e.g., auth-service)
    ./gradlew bootRun
    ```    
    Repeat this for each of the five microservices. It's recommended to start them in this order: `auth-service`, `mine-service`, `sensor-service`, `ml-service`, `api-gateway`.

<a name="configuration"></a>
## Configuration

Key configuration properties are managed in each service's `application.properties` file. It is highly recommended to use environment variables to override default values for sensitive information in production.

Example properties:
- `spring.r2dbc.url`, `spring.r2dbc.username`, `spring.r2dbc.password`
- `spring.data.redis.host`, `spring.data.redis.port`
- `influxdb.url`, `influxdb.token`, `influxdb.bucket`
- `jwt.keystore.password`, `jwt.key.password`
- `twilio.account.sid`, `twilio.auth.token`

<a name="running-tests"></a>
## Running Tests

To run the tests for any service, navigate to its root directory and execute:

```bash
# Using Gradle
./gradlew test
```

<a name="license"></a>
## License

This project is licensed under the MIT License. See the [LICENSE](https://github.com/omniCoder77/rockfall-prediction-system/blob/master/LICENSE) file for details.
