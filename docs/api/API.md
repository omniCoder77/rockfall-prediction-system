# API Documentation

This document describes the RESTful APIs for the services in the project.

---

## Auth Service API Documentation

This document describes the RESTful API for the Auth Service, which handles user registration, login, password management, token refreshing, and multi-factor authentication.

---

### **Base URL**

`/api/v1/auth`

---

### **1. Registration Endpoints**

#### **1.1. Register a New User**

Registers a new administrator account with the provided details.

*   **Endpoint:** `/register`
*   **Method:** `POST`
*   **Description:** Creates a new user account. Upon successful registration, returns an access token and a refresh token.
*   **Request Body:** `application/json`
    ```json
    {
      "name": "string",
      "phoneNumber": "string",
      "email": "string",
      "password": "string",
      "jobId": "string"
    }
    ```
    **Constraints:**
    *   `name`: Not blank.
    *   `phoneNumber`: Valid phone number format (e.g., `+11234567890`).
    *   `email`: Valid email format, max 100 characters.
    *   `password`: Not blank. (Password policy validation like min length, complexity is handled at the service layer).
    *   `jobId`: Not blank. This identifier is used to determine the user's role (e.g., ADMIN, SUPERVISOR, USER).
*   **Success Response:** `201 Created`
    ```json
    {
      "userId": "string (UUID)",
      "accessToken": "string",
      "refreshToken": "string"
    }
    ```
*   **Error Responses:**
    *   `409 Conflict`: If a user with the provided email, phone number, or job ID already exists.
        ```json
        {
          "userId": "",
          "accessToken": "",
          "refreshToken": ""
        }
        ```
    *   `400 Bad Request`: If request body validation fails (e.g., missing fields, invalid formats).
        ```json
        {
          "timestamp": "ISO-8601 string",
          "path": "/api/v1/auth/register",
          "status": 400,
          "error": "Bad Request",
          "message": "Validation error message"
        }
        ```
    *   `500 Internal Server Error`: For unexpected server errors.
        ```json
        {
          "userId": "",
          "accessToken": "",
          "refreshToken": ""
        }
        ```

---

### **2. Login Endpoints**

#### **2.1. User Login**

Authenticates a user either with email/password or initiates an OTP flow with a phone number.

*   **Endpoint:** `/login`
*   **Method:** `POST`
*   **Description:** Allows users to log in.
    *   If `email` and `password` are provided, it attempts a standard password-based login and returns tokens.
    *   If `phoneNumber` is provided (and `email` is null/blank), it initiates an OTP sending process.
*   **Request Body:** `application/json`
    ```json
    {
      "email": "string | null",
      "phoneNumber": "string | null",
      "password": "string"
    }
    ```
    **Constraints:**
    *   Either `email` or `phoneNumber` must be provided, but not both.
    *   If `email` is provided, `password` cannot be blank.
    *   `email`: Valid email format, max 100 characters.
    *   `phoneNumber`: Valid phone number format (e.g., `+11234567890`).
    *   `password`: Not blank.
*   **Success Responses:**
    *   **Email/Password Login (`200 OK`):**
        ```json
        {
          "accessToken": "string",
          "refreshToken": "string"
        }
        ```
    *   **Phone Number OTP Request (`200 OK`):**
        ```
        "OTP"
        ```
*   **Error Responses:**
    *   `400 Bad Request`: If request body validation fails (e.g., missing fields, both email and phone number, or password missing for email login).
        ```json
        {
          "timestamp": "ISO-8601 string",
          "path": "/api/v1/auth/login",
          "status": 400,
          "error": "Bad Request",
          "message": "Validation error message (e.g., 'Either email or phone number is required', 'Password is required for email login')"
        }
        ```
    *   `401 Unauthorized`: If email/password login fails due to invalid credentials.
        ```json
        {
          "error": "Invalid credentials"
        }
        ```
    *   `500 Internal Server Error`: If there's an issue sending the OTP.
        ```json
        {
          "error": "Failed to send OTP"
        }
        ```

#### **2.2. User Logout**

Invalidates the current user's refresh token, effectively logging them out.

*   **Endpoint:** `/logout`
*   **Method:** `POST`
*   **Description:** Logs out the currently authenticated user by invalidating their refresh token in the cache. Requires a valid access token in the `Authorization` header.
*   **Headers:**
    *   `Authorization`: `Bearer <access_token>`
*   **Success Response:** `200 OK`
    ```
    "Logged out successfully."
    ```
    or
    ```
    "No active session found or already logged out."
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If the access token is missing, invalid, or expired.
    *   `500 Internal Server Error`: For unexpected server errors during logout.
        ```
        "Failed to logout."
        ```

---

### **3. Password Management Endpoints**

#### **3.1. Request Password Reset**

Initiates the password reset process by sending a reset link to the user's email.

*   **Endpoint:** `/password/request`
*   **Method:** `POST`
*   **Description:** Sends a password reset email to the provided email address. For security reasons, it will always return a success message if the email format is valid, regardless of whether the email exists in the system.
*   **Request Body:** `application/json`
    ```json
    {
      "email": "string"
    }
    ```
    **Constraints:**
    *   `email`: Not blank, valid email format, max 100 characters.
*   **Success Response:** `200 OK`
    ```
    "If an account with that email exists, a password reset link has been sent."
    ```
*   **Error Responses:**
    *   `400 Bad Request`: If email format is invalid.
        ```json
        {
          "timestamp": "ISO-8601 string",
          "path": "/api/v1/auth/password/request",
          "status": 400,
          "error": "Bad Request",
          "message": "Validation error message (e.g., 'Invalid email format')"
        }
        ```
    *   `500 Internal Server Error`: For unexpected server errors during the process (e.g., email service failure).
        ```
        "An error occurred during password reset request."
        ```

#### **3.2. Reset Password**

Resets the user's password using a valid reset token.

*   **Endpoint:** `/password/reset`
*   **Method:** `POST`
*   **Description:** Allows a user to set a new password using a one-time reset token obtained from the `/password/request` endpoint.
*   **Query Parameters:**
    *   `token`: `string` (The reset token received in the email).
*   **Request Body:** `application/json`
    ```json
    {
      "newPassword": "string"
    }
    ```
    **Constraints:**
    *   `newPassword`: Not blank. (Password policy validation is handled at the service layer).
*   **Success Response:** `200 OK`
    ```
    "Password has been reset successfully."
    ```
*   **Error Responses:**
    *   `400 Bad Request`: If the token is invalid or expired, or if the new password fails validation.
        ```json
        {
          "timestamp": "ISO-8601 string",
          "path": "/api/v1/auth/password/reset",
          "status": 400,
          "error": "Bad Request",
          "message": "Invalid or expired token"
        }
        ```
        or
        ```json
        {
          "timestamp": "ISO-8601 string",
          "path": "/api/v1/auth/password/reset",
          "status": 400,
          "error": "Bad Request",
          "message": "Invalid or expired token, or user not found."
        }
        ```
        *   `500 Internal Server Error`: For unexpected server errors.
        ```
        "An error occurred during password reset."
        ```

---

### **4. Token Management Endpoints**

#### **4.1. Refresh Access Token**

Obtains a new access token and refresh token using a valid refresh token.

*   **Endpoint:** `/token/refresh`
*   **Method:** `POST`
*   **Description:** Exchanges an expired access token for a new pair of access and refresh tokens.
*   **Request Body:** `application/json`
    ```json
    {
      "refreshToken": "string"
    }
    ```
    **Constraints:**
    *   `refreshToken`: Not blank.
*   **Success Response:** `200 OK`
    ```json
    {
      "accessToken": "string",
      "refreshToken": "string"
    }
    ```
*   **Error Responses:**
    *   `401 Unauthorized`: If the refresh token is invalid, expired, or a token reuse attempt is detected.
        ```json
        {
          "accessToken": "",
          "refreshToken": ""
        }
        ```
    *   `400 Bad Request`: If request body validation fails (e.g., refresh token is blank).
        ```json
        {
          "timestamp": "ISO-8601 string",
          "path": "/api/v1/auth/token/refresh",
          "status": 400,
          "error": "Bad Request",
          "message": "Refresh token cannot be blank"
        }
        ```
    *   `500 Internal Server Error`: For unexpected server errors.
        ```json
        {
          "accessToken": "",
          "refreshToken": ""
        }
        ```

---
---

## Sensor Service API Documentation

This document describes the RESTful API for the Sensor Service, which handles sensor registration and historical data querying.

---

### **Base URL**

`/api/v1`

---

### **1. Sensor Management Endpoints**

Base Path: `/sensors`

#### **1.1. Add a New Sensor**

Registers a new sensor for a specific station. This allows the service to begin listening for MQTT messages for that sensor.

*   **Endpoint:** `/sensors`
*   **Method:** `POST`
*   **Description:** Adds a sensor to a station. If the sensor already exists, it may result in an error.
*   **Request Body:** `application/json`
    ```json
    {
      "stationId": "string",
      "sensorType": "string"
    }
    ```
    **Constraints:**
    *   `stationId`: The identifier for the station (e.g., `station-01`). Cannot be blank.
    *   `sensorType`: The type of sensor. Must be one of: `DISPLACEMENT`, `PORE_PRESSURE`, `TEMPERATURE`, `STRAIN`, `RAINFALL`, `DRONE`, `VIBRATION`.
*   **Success Response:** `200 OK` (No body content)
*   **Error Responses:**
    *   `400 Bad Request`: If the sensor is already registered for that station.
        ```
        DISPLACEMENT sensor is already present at station station-01
        ```
    *   `500 Internal Server Error`: For unexpected server errors.

#### **1.2. Update a Sensor**

Updates the status of a sensor, primarily to mark it as active.

*   **Endpoint:** `/sensors`
*   **Method:** `PATCH`
*   **Description:** This endpoint can be used to re-activate or update a sensor's registration details.
*   **Request Body:** `application/json`
    ```json
    {
      "stationId": "string",
      "sensorType": "string"
    }
    ```
*   **Success Response:** `200 OK` (No body content)
*   **Error Response:**
    *   `500 Internal Server Error`: For unexpected server errors.

#### **1.3. Remove a Sensor**

Removes a sensor registration from a station. The service will stop listening for its MQTT messages.

*   **Endpoint:** `/sensors`
*   **Method:** `DELETE`
*   **Description:** De-registers a sensor from a station.
*   **Request Body:** `application/json`
    ```json
    {
      "stationId": "string",
      "sensorType": "string"
    }
    ```
*   **Success Response:** `200 OK` (No body content)
*   **Error Response:**
    *   `500 Internal Server Error`: For unexpected server errors.

#### **1.4. Get All Registered Sensors**

Retrieves a list of all sensors registered in the service, regardless of their status.

*   **Endpoint:** `/sensors/sensors`
*   **Method:** `GET`
*   **Success Response:** `200 OK`
    ```json
    [
      {
        "stationId": "station-01",
        "sensor": "DISPLACEMENT"
      },
      {
        "stationId": "station-02",
        "sensor": "RAINFALL"
      }
    ]
    ```
*   **Error Response:**
    *   `500 Internal Server Error`: For unexpected server errors.

#### **1.5. Get Active Sensors**

Retrieves a list of all sensors that are currently considered active (i.e., connected via MQTT).

*   **Endpoint:** `/sensors/active-sensors`
*   **Method:** `GET`
*   **Success Response:** `200 OK`
    ```json
    [
      {
        "stationId": "station-01",
        "sensor": "DISPLACEMENT"
      }
    ]
    ```
*   **Error Response:**
    *   `500 Internal Server Error`: For unexpected server errors.

#### **1.6. Get Inactive Sensors**

Retrieves a list of sensors that are currently inactive, typically after an MQTT Last Will and Testament (LWT) message has been received.

*   **Endpoint:** `/sensors/unactive-sensors`
*   **Method:** `GET`
*   **Success Response:** `200 OK`
    ```json
    [
      {
        "stationId": "station-02",
        "sensor": "RAINFALL"
      }
    ]
    ```
*   **Error Response:**
    *   `500 Internal Server Error`: For unexpected server errors.

---

### **2. Sensor History Endpoints**

Base Path: `/history`

#### **2.1. Get History for All Sensors**

Retrieves aggregated historical data for all registered sensors across all stations.

*   **Endpoint:** `/history/sensors`
*   **Method:** `GET`
*   **Query Parameters:**
    *   `start` (optional, string): The start of the time range. Defaults to `-1d`. (e.g., `-6h`, `-30m`).
    *   `stop` (optional, string): The end of the time range. Defaults to `now()`.
    *   `interval` (optional, string): The time interval for data aggregation. Defaults to `1h`. (e.g., `30m`, `15m`).
*   **Success Response:** `200 OK` - A map where keys are measurement types, and values are maps of fields to data points.
    ```json
    {
      "displacement": {
        "tilt_x": [
          { "time": "2025-09-21T10:00:00Z", "value": 0.5, "measurement": "displacement", "field": "tilt_x", "station": "station-01" }
        ],
        "tilt_y": [
          { "time": "2025-09-21T10:00:00Z", "value": -0.2, "measurement": "displacement", "field": "tilt_y", "station": "station-01" }
        ]
      },
      "rainfall": {
        "total_rainfall": [
          { "time": "2025-09-21T10:00:00Z", "value": 15.2, "measurement": "rainfall", "field": "total_rainfall", "station": "station-02" }
        ]
      }
    }
    ```
*   **Error Response:**
    *   `500 Internal Server Error`: For unexpected server errors.

#### **2.2. Get History for a Specific Station**

Retrieves aggregated historical data for all sensors at a specific station.

*   **Endpoint:** `/history/sensors/station/{stationId}`
*   **Method:** `GET`
*   **Path Variable:**
    *   `stationId`: The ID of the station to query (e.g., `station-01`).
*   **Query Parameters:**
    *   `start` (optional, string): Defaults to `-1d`.
    *   `stop` (optional, string): Defaults to `now()`.
    *   `interval` (optional, string): Defaults to `1h`.
*   **Success Response:** `200 OK` - Similar structure to the "All Sensors" endpoint, but filtered to the specified station.
    ```json
    {
      "displacement": {
        "tilt_x": [
          { "time": "2025-09-21T11:00:00Z", "value": 0.52, "measurement": "displacement", "field": "tilt_x", "station": "station-01" }
        ]
      },
      "temperature": {
        "temperature": [
          { "time": "2025-09-21T11:00:00Z", "value": 22.5, "measurement": "temperature", "field": "temperature", "station": "station-01" }
        ]
      }
    }
    ```
*   **Error Response:**
    *   `500 Internal Server Error`: For unexpected server errors.

#### **2.3. Get History by Sensor Type**

Retrieves aggregated historical data for a specific type of sensor, optionally filtered by station.

*   **Endpoint:** `/history/sensors/type/{sensorType}`
*   **Method:** `GET`
*   **Path Variable:**
    *   `sensorType`: The measurement name of the sensor (e.g., `displacement`, `temperature`, `rainfall`).
*   **Query Parameters:**
    *   `stationId` (optional, string): Filter the results to a single station.
    *   `start` (optional, string): Defaults to `-1d`.
    *   `stop` (optional, string): Defaults to `now()`.
    *   `interval` (optional, string): Defaults to `1h`.
*   **Success Response:** `200 OK` - A map where keys are the fields for that sensor type, and values are lists of data points.
    ```json
    {
      "tilt_x": [
        { "time": "2025-09-21T10:00:00Z", "value": 0.5, "measurement": "displacement", "field": "tilt_x", "station": "station-01" },
        { "time": "2025-09-21T11:00:00Z", "value": 0.52, "measurement": "displacement", "field": "tilt_x", "station": "station-01" }
      ],
      "tilt_y": [
        { "time": "2025-09-21T10:00:00Z", "value": -0.2, "measurement": "displacement", "field": "tilt_y", "station": "station-01" },
        { "time": "2025-09-21T11:00:00Z", "value": -0.21, "measurement": "displacement", "field": "tilt_y", "station": "station-01" }
      ]
    }
    ```
*   **Error Response:**
    *   `500 Internal Server Error`: For unexpected server errors.
