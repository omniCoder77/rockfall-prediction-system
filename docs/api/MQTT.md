# Sensor Service MQTT Documentation

This document describes the MQTT protocol used by the Sensor Service for real-time data ingestion from various sensors. All sensor clients are expected to adhere to this specification to ensure proper data processing.

---

## 1. Broker Configuration

The service connects to a central MQTT broker.

*   **Broker URL:** `tcp://localhost:1883` (default)
*   **Service Client ID:** `RockfallPredictionClient_<timestamp>`
*   **Sensor Client ID:** Each sensor client should have a unique client ID.

---

## 2. Topic Structure

The service uses a structured topic hierarchy to differentiate between sensor types and stations.

### 2.1. Sensor Data Topics

Sensor clients must publish their data to a topic that follows this pattern:

`{sensor_topic}/{stationId}`

*   `{sensor_topic}`: A predefined topic prefix for the specific sensor type.
*   `{stationId}`: The unique identifier for the station where the sensor is located (e.g., `station-01`).

**Available Sensor Topics:**

| Sensor Type      | Topic Prefix        | QoS Level |
| ---------------- | ------------------- | :-------: |
| `DISPLACEMENT`   | `/displacement`     |     1     |
| `PORE_PRESSURE`  | `/pore-pressure`    |     1     |
| `TEMPERATURE`    | `/temperature`      |     1     |
| `STRAIN`         | `/strain`           |     1     |
| `RAINFALL`       | `/rainfall`         |     1     |
| `DRONE`          | `/drone`            |     1     |
| `VIBRATION`      | `/vibration`        |     1     |

**Example:** A displacement sensor at `station-01` would publish its data to the topic `/displacement/station-01`.

### 2.2. Health & Last Will and Testament (LWT) Topic

To monitor the connection status of sensors, the service utilizes MQTT's LWT feature. Each sensor client **must** configure an LWT message before connecting.

*   **LWT Topic Pattern:** `/health/{sensor_type_lowercase}/{stationId}`
*   **LWT Message Payload:** A simple string, e.g., `"disconnected"`.
*   **QoS Level:** 2 (Ensures the LWT message is delivered exactly once).

**Example:**
The displacement sensor at `station-01` should set its LWT to publish to the topic `/health/displacement/station-01`. If the sensor disconnects ungracefully, the broker will automatically publish its LWT message to this topic. The Sensor Service listens on `/health/+/+` to receive these messages and mark the corresponding sensor as inactive.

---

## 3. Message Payloads

All data payloads must be in JSON format.

### 3.1. Displacement Sensor

*   **Topic:** `/displacement/{stationId}`
*   **Payload:**
    ```json
    {
      "tiltX": 1.5,
      "tiltY": -0.75,
      "temperature": 22.5
    }
    ```
    *   `tiltX`, `tiltY`: `double` - Tilt values on the X and Y axes.
    *   `temperature`: `double` - Temperature reading from the sensor.

### 3.2. Pore Pressure Sensor

*   **Topic:** `/pore-pressure/{stationId}`
*   **Payload:**
    ```json
    {
      "timestamp": 1677610200000,
      "pressure": 150.2,
      "temperature": 18.3,
      "frequency": 120.5
    }
    ```
    *   `timestamp`: `long` - Unix timestamp in milliseconds.
    *   `pressure`: `double` - Pore water pressure in kPa.
    *   `temperature`: `double` - Temperature reading.
    *   `frequency`: `double` - Sensor frequency reading.

### 3.3. Temperature Sensor

*   **Topic:** `/temperature/{stationId}`
*   **Payload:**
    ```json
    {
      "timestamp": 1677610200000,
      "temperature": 25.1,
      "humidity": 45.5
    }
    ```
    *   `timestamp`: `long` - Unix timestamp in milliseconds.
    *   `temperature`: `double` - Ambient temperature in Celsius.
    *   `humidity`: `double` - Relative humidity percentage.

### 3.4. Strain Sensor

*   **Topic:** `/strain/{stationId}`
*   **Payload:**
    ```json
    {
      "timestamp": 1677610200000,
      "strainValue": 350.7,
      "temperature": 23.8,
      "frequency": 60.1
    }
    ```
    *   `timestamp`: `long` - Unix timestamp in milliseconds.
    *   `strainValue`: `double` - Strain measurement in microstrains.
    *   `temperature`: `double` - Temperature reading.
    *   `frequency`: `double` - Sensor frequency reading.

### 3.5. Rainfall Sensor

*   **Topic:** `/rainfall/{stationId}`
*   **Payload:**
    ```json
    {
      "timestamp": 1677610200000,
      "rainfallIncrement": 1.2,
      "totalRainfall": 55.8
    }
    ```
    *   `timestamp`: `long` - Unix timestamp in milliseconds.
    *   `rainfallIncrement`: `double` - Rainfall since the last reading (in mm).
    *   `totalRainfall`: `double` - Cumulative rainfall (in mm).

### 3.6. Drone Sensor

*   **Topic:** `/drone/{stationId}`
*   **Payload:**
    ```json
    {
      "imageData": "base64-encoded-string-of-image",
      "gpsLatitude": 40.7128,
      "gpsLongitude": -74.0060,
      "altitude": 150.0
    }
    ```
    *   `imageData`: `string` - Base64 encoded image data.
    *   `gpsLatitude`, `gpsLongitude`: `double` - GPS coordinates.
    *   `altitude`: `double` - Altitude in meters.

### 3.7. Vibration Sensor

*   **Topic:** `/vibration/{stationId}`
*   **Payload:**
    ```json
    {
      "timestamp": 1677610200000,
      "accelX": 0.05,
      "accelY": -0.02,
      "accelZ": 0.98,
      "magnitude": 0.981
    }
    ```
    *   `timestamp`: `long` - Unix timestamp in milliseconds.
    *   `accelX`, `accelY`, `accelZ`: `double` - Acceleration on each axis.
    *   `magnitude`: `double` - The calculated magnitude of the acceleration vector.

---

## 4. Workflow Example

1.  **Registration (API):** An administrator uses the `POST /api/v1/sensors` endpoint to register a new `DISPLACEMENT` sensor for `station-03`. The Sensor Service subscribes to `/displacement/station-03`.

2.  **Sensor Connection:** The physical sensor at `station-03` connects to the MQTT broker.
    *   It sets its **LWT** to publish to `/health/displacement/station-03` with the payload `"disconnected"`.
    *   It establishes a successful connection. The Sensor Service now marks this sensor as "active".

3.  **Data Publishing:** The sensor periodically reads its data and publishes a JSON payload to the `/displacement/station-03` topic. The Sensor Service receives this message, parses it, and saves it to the InfluxDB database.

4.  **Disconnection:**
    *   **Graceful:** The sensor client sends an MQTT `DISCONNECT` packet.
    *   **Ungraceful:** The sensor loses power or network connectivity. The MQTT broker detects the lost connection and publishes the LWT message to `/health/displacement/station-03`. The Sensor Service receives this LWT message and marks the sensor as "inactive". It also calls `removeSensor` to unsubscribe and remove the sensor from its active list.
