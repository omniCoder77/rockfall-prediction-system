# MQTT Communication for Rockfall Prediction System

This document outlines the MQTT topics, message formats, and client setup for the Rockfall Prediction System's API Gateway. MQTT is used as the primary messaging protocol for transmitting sensor data from various devices to the central gateway for processing and storage.

## 1. MQTT Broker Details

*   **Broker Address:** `tcp://localhost:1883`
    *   *Note: For production deployments, this would typically be a remote server address with proper authentication and TLS/SSL.*
*   **Quality of Service (QoS):** 2 (Exactly once)
    *   *This ensures that messages are delivered exactly once to the subscriber, preventing data loss or duplication.*

## 2. Sensor Data Topics and Message Formats

All sensor data is published as JSON payloads. The `timestamp` field in all DTOs represents the epoch milliseconds.

### 2.1. Displacement Data

*   **Topic:** `/displacement`
*   **Description:** Measures ground movement and tilt.
*   **DTO:** `DisplacementData`
*   **JSON Example:**
    ```json
    {
      "timestamp": 1678886400000,
      "stationId": "STATION_1",
      "tiltX": 1.25,
      "tiltY": -0.87,
      "temperature": 25.3,
      "batteryLevel": 95
    }
    ```

### 2.2. Strain Data

*   **Topic:** `/strain`
*   **Description:** Measures deformation of material.
*   **DTO:** `StrainData`
*   **JSON Example:**
    ```json
    {
      "timestamp": 1678886400100,
      "stationId": "STATION_1",
      "strainValue": 55.6,
      "temperature": 24.9,
      "frequency": 1500.0
    }
    ```

### 2.3. Pore Pressure Data

*   **Topic:** `/pressure`
*   **Description:** Measures fluid pressure within porous soil or rock.
*   **DTO:** `PorePressureData`
*   **JSON Example:**
    ```json
    {
      "timestamp": 1678886400200,
      "stationId": "STATION_1",
      "pressure": 120.5,
      "temperature": 25.1,
      "frequency": 1600.0
    }
    ```

### 2.4. Rainfall Data

*   **Topic:** `/rainfall`
*   **Description:** Measures rainfall increment and total rainfall.
*   **DTO:** `RainfallData`
*   **JSON Example:**
    ```json
    {
      "timestamp": 1678886400300,
      "stationId": "STATION_1",
      "rainfallIncrement": 2.3,
      "totalRainfall": 56.8
    }
    ```

### 2.5. Vibration Data

*   **Topic:** `/vibration`
*   **Description:** Measures acceleration along X, Y, Z axes and overall magnitude.
*   **DTO:** `VibrationData`
*   **JSON Example:**
    ```json
    {
      "timestamp": 1678886400400,
      "stationId": "STATION_1",
      "accelX": 0.15,
      "accelY": -0.22,
      "accelZ": 0.08,
      "magnitude": 0.35
    }
    ```

### 2.6. Temperature Data

*   **Topic:** `/temperature`
*   **Description:** Measures ambient temperature and humidity.
*   **DTO:** `TemperatureData`
*   **JSON Example:**
    ```json
    {
      "timestamp": 1678886400500,
      "stationId": "STATION_1",
      "temperature": 28.7,
      "humidity": 75.2
    }
    ```

### 2.7. Drone Image Data

*   **Topic:** `/droneimagedata`
*   **Description:** Contains metadata about drone images, including GPS coordinates and image size. The actual image data is typically a byte array encoded (e.g., Base64 if sent over MQTT, or a reference to an external storage). In this implementation, `imageData` is directly included as a byte array (which will be JSON-encoded as an array of numbers).
*   **DTO:** `DroneImageData`
*   **JSON Example:**
    ```json
    {
      "timestamp": 1678886400600,
      "droneId": "DRONE_A",
      "gpsLatitude": 28.6139,
      "gpsLongitude": 77.2090,
      "altitude": 150.0,
      "imageData": [123, 45, 67, ..., 89, 0] // Mock byte array
    }
    ```
    *Note: For large images, it's generally recommended to store the image data in a dedicated storage (e.g., S3, Blob Storage) and send only the URL or reference via MQTT.*

## 3. Publisher Clients

The project includes example publishers for simulating sensor data.

*   **`MqttPublishAllSensors` (Kotlin Object):**
    *   Publishes a single batch of all sensor types to their respective topics.
    *   Useful for testing individual data ingestion.
    *   Can be run as a standalone Kotlin application.

*   **`MqttContinuousPublisher` (Kotlin Object):**
    *   Continuously publishes batches of all sensor types at a fixed interval (currently 5 seconds).
    *   Simulates ongoing sensor data streams for system load testing.
    *   Can be run as a standalone Kotlin application.

### Running Publishers:

To run the publishers, ensure an MQTT broker is running at `localhost:1883`. Then, execute the `main` function of `MqttPublishAllSensors` or `MqttContinuousPublisher` from your IDE or via `kotlin` command.

## 4. Subscriber Client (API Gateway)

The `MqttSensorSubscriber` class within the API Gateway is responsible for listening to all defined sensor topics.

*   **Class:** `com.sharingplate.apigateway.inbound.mqtt.MqttSensorSubscriber`
*   **Functionality:**
    *   Connects to the specified MQTT broker on application startup (`@PostConstruct`).
    *   Subscribes to all topics listed in Section 2 with QoS 1.
    *   Implements an `MqttCallback` to handle incoming messages.
    *   Parses the JSON payload into the corresponding DTO.
    *   Delegates the parsed data to `InfluxDBInitializer` for storage in InfluxDB.
    *   Includes basic connection loss logging and graceful disconnection.

## 5. InfluxDB Integration

Upon receiving MQTT messages, the `MqttSensorSubscriber` passes the data to the `InfluxDBInitializer`. This component buffers the incoming data and writes it to InfluxDB in batches every 3 seconds (`@Scheduled(fixedRate = 3000)`).

Each sensor type is stored as a separate measurement in InfluxDB, with `station_id` (or `drone_id` for drone data) as a tag for efficient querying.

*   **InfluxDB Measurements:**
    *   `displacement`
    *   `strain`
    *   `pore_pressure`
    *   `rainfall`
    *   `vibration`
    *   `temperature`
    *   `drone_image`

## 6. Setup and Testing

1.  **Start an MQTT Broker:** Ensure an MQTT broker (e.g., Mosquitto) is running on `localhost:1883`.
2.  **Start InfluxDB:** Ensure InfluxDB 2.x is running and configured with the correct URL, token, bucket, and organization as specified in `application.properties` (or directly in `InfluxDBInitializer`'s `main` function for local testing).
3.  **Start API Gateway:** Run the `ApiGatewayApplication` Spring Boot application. This will initialize the `MqttSensorSubscriber` and start listening for messages.
4.  **Run Publishers:** Execute `MqttPublishAllSensors` or `MqttContinuousPublisher` to send sample data.
5.  **Verify Data:** Check the InfluxDB instance using Flux queries or the InfluxDB UI to confirm data ingestion.
