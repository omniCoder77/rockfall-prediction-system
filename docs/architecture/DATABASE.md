## Database Schema for Rockfall Prediction System

This document outlines the database schemas and their relationships for the Rockfall Prediction System, which utilizes both PostgreSQL (for core application data) and InfluxDB (for time-series sensor data).

---

### 1. PostgreSQL Database Schema

PostgreSQL is used for managing the core operational data of the system, including information about mining stations and administrators.

#### 1.1. `station` Table

Stores information about each monitoring station.

| Column Name | Data Type | Constraints           | Description                                   |
| :---------- | :-------- | :-------------------- | :-------------------------------------------- |
| `station_id` | `UUID`    | `PRIMARY KEY`, `NOT NULL` | Unique identifier for the monitoring station. |
| `name`      | `VARCHAR(255)` | `NOT NULL`         | Name of the station.                          |
| `site_engineer` | `UUID`  | `NOT NULL`            | ID of the assigned site engineer (references `admin` table). |
| `risk_level` | `DOUBLE PRECISION` | `NOT NULL`, `DEFAULT 0.0` | Current calculated risk level for the station. |

#### 1.2. `admin` Table

Stores administrator and supervisor user information for authentication and authorization.

| Column Name | Data Type | Constraints           | Description                                   |
| :---------- | :-------- | :-------------------- | :-------------------------------------------- |
| `admin_id`  | `UUID`    | `PRIMARY KEY`, `NOT NULL` | Unique identifier for the administrator.      |
| `name`      | `VARCHAR(255)` | `NOT NULL`         | Full name of the administrator.               |
| `phone_number` | `VARCHAR(20)` | `UNIQUE`, `NOT NULL` | Phone number of the administrator.            |
| `email`     | `VARCHAR(255)` | `UNIQUE`, `NOT NULL` | Email address of the administrator.           |
| `password`  | `VARCHAR(255)` | `NOT NULL`         | Hashed password of the administrator.         |
| `job_id`    | `VARCHAR(50)` | `UNIQUE`, `NOT NULL` | Unique job identifier, used for role determination. |

#### 1.3. Relationships

*   **`station` to `admin`:**
    *   The `station.site_engineer` column is a foreign key referencing `admin.admin_id`. This establishes a one-to-many relationship where one administrator can be assigned to multiple stations as a site engineer.

---

### 2. InfluxDB Database Schema

InfluxDB is utilized for storing high-volume, time-series sensor data from various monitoring devices at the mining sites. Data is organized into measurements, each corresponding to a type of sensor data, with `station` as a tag for efficient querying.

#### 2.1. Measurements and Fields

All timestamps in InfluxDB are written with **nanosecond precision** (`WritePrecision.NS`).

##### 2.1.1. `displacement` Measurement

Records displacement and tilt data from sensors.

*   **Tags:**
    *   `station`: String (e.g., "station-01") - Identifies the station.
*   **Fields:**
    *   `tilt_x`: Double - Tilt in the X-direction.
    *   `tilt_y`: Double - Tilt in the Y-direction.
    *   `temperature`: Double - Temperature at the sensor location.

##### 2.1.2. `strain` Measurement

Records strain values from strain gauges.

*   **Tags:**
    *   `station`: String (e.g., "station-01") - Identifies the station.
*   **Fields:**
    *   `strain_value`: Double - Measured strain value.
    *   `temperature`: Double - Temperature at the sensor location.
    *   `frequency`: Double - Measurement frequency.

##### 2.1.3. `pore_pressure` Measurement

Records pore pressure data.

*   **Tags:**
    *   `station`: String (e.g., "station-01") - Identifies the station.
*   **Fields:**
    *   `pressure`: Double - Measured pore pressure.
    *   `temperature`: Double - Temperature at the sensor location.
    *   `frequency`: Double - Measurement frequency.

##### 2.1.4. `rainfall` Measurement

Records rainfall data.

*   **Tags:**
    *   `station`: String (e.g., "station-01") - Identifies the station.
*   **Fields:**
    *   `rainfall_increment`: Double - Incremental rainfall since the last measurement.
    *   `total_rainfall`: Double - Cumulative total rainfall.

##### 2.1.5. `vibration` Measurement

Records vibration data (accelerometer readings).

*   **Tags:**
    *   `station`: String (e.g., "station-01") - Identifies the station.
*   **Fields:**
    *   `acc_x`: Double - Acceleration in the X-direction.
    *   `acc_y`: Double - Acceleration in the Y-direction.
    *   `acc_z`: Double - Acceleration in the Z-direction.
    *   `magnitude`: Double - Magnitude of vibration.

##### 2.1.6. `temperature` Measurement

Records ambient temperature and humidity.

*   **Tags:**
    *   `station`: String (e.g., "station-01") - Identifies the station.
*   **Fields:**
    *   `temperature`: Double - Ambient temperature.
    *   `humidity`: Double - Relative humidity.

##### 2.1.7. `drone_image_data` Measurement

Records metadata and image data from drones.

*   **Tags:**
    *   `station`: String (e.g., "station-01") - Identifies the station associated with the drone's flight.
*   **Fields:**
    *   `image_data`: String - The raw image data, converted to a string.
    *   `lat`: Double - GPS latitude where the image was captured.
    *   `long`: Double - GPS longitude where the image was captured.
    *   `altitude`: Double - Altitude at which the image was captured.
