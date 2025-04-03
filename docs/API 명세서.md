# 🎟 콘서트 예약 서비스 API 명세서


## 1. ✅ 유저 대기열 토큰 발급 API

- **URL**: `POST /concert/waiting-token`
- **Method**: POST
- **설명**:  
  - 사용자는 대기열에 진입하기 위해 토큰을 발급받습니다.  
  - 기존 토큰이 있다면 재사용하거나 만료되었을 경우 재발급합니다.

### 🔐 Authorization
- 필요 여부: 없음

### 📥 Request
**Headers**

 | 이름 | 필수 | 설명 |
|----|----|----|
| 없음 | -  | -  |


**Request Body**

```json
{
  "userId": 1
}
```
---

### 📤 Response 

**HTTP Status Codes**

| 코드     | 설명 |
|--------|----|
| 200 OK | 성공 | 
| 400 Bad Request | 잘못된 요청 (예: 필수 파라미터 누락) |
| 500 Internal Server Error | 서버 오류 |

**Response Body**

```json
{
  "result": "200",
  "message": "Success",
  "data": {
    "token": "abc123-def456",
    "status": "WAITING",
    "position": 17,
    "expiredAt": "2025-04-03T18:00:00Z"
  }
}
```
### ⚠️ 에러 예시

**400 Bad Request**

```json
{
  "result": "400",
  "message": "Missing or invalid userId"
}
```

**500 Internal Server Error**
```json
{
  "result": "500",
  "message": "Internal server error"
}
```

## 2. ✅ 예약 가능한 날짜 조회 API

- **URL**: `GET /concert/dates`
- **Method**: GET
- **설명**:  
  - 유효한 대기열 토큰을 가진 사용자가 예약 가능한 콘서트 일정을 조회합니다.

### 🔐 Authorization
- 필요 여부: Bearer {access_token}

### 📥 Request
**Headers**

| 이름 | 필수 | 설명     |
|----|----|-----------|
| Authorization | O  | Bearer 토큰 |


### 📤 Response

**HTTP Status Codes**

| 코드     | 설명    |
|--------|-------|
| 200 OK | 성공    | 
| 401 Unauthorized | 인증 실패 |
| 500 Internal Server Error | 서버 오류 |

**Response Body**

```json
{
  "concertScheduleDtos": [
    {
      "concertScheduleId": 1,
      "concertDate": "2024-07-04"
    },
    {
      "concertScheduleId": 2,
      "concertDate": "2024-07-05"
    }
  ]
}
```
### ⚠️ 에러 예시

**401 Unauthorized**

```json
{
  "result": "401",
  "message": "Invalid or expired token"
}
```

**500 Internal Server Error**
```json
{
  "result": "500",
  "message": "Internal server error"
}
```

## 3. ✅ 좌석 조회 API

- **URL**: `GET /concert/{concertId}/seats`
- **Method**: GET
- **설명**:  
  - 콘서트 ID에 해당하는 예약 가능한 좌석 목록을 조회합니다.

### 🔐 Authorization
- 필요 여부: Bearer {access_token}

### 📥 Request
**Headers**

| 이름 | 필수 | 설명     |
|----|----|-----------|
| Authorization | O  | Bearer 토큰 |

**Path Parameters**

| 이름        | 타입   | 필수 | 설명     |
|-----------|------|---|--------|
| concertId | Long | O | 콘서트 ID  |

### 📤 Response

**HTTP Status Codes**

| 코드     | 설명    |
|--------|-------|
| 200 OK | 성공    | 
| 401 Unauthorized | 인증 실패 |
| 500 Internal Server Error | 서버 오류 |

**Response Body**

```json
{
  "seatDtos": [
    {
      "seatId": 1,
      "seatNumber": "A1",
      "status": "AVAILABLE"
    },
    {
      "seatId": 2,
      "seatNumber": "A2",
      "status": "RESERVED"
    }
  ]
}
```
### ⚠️ 에러 예시

**401 Unauthorized**

```json
{
  "result": "401",
  "message": "Invalid or expired token"
}
```

**500 Internal Server Error**
```json
{
  "result": "500",
  "message": "Internal server error"
}
```

## 4. ✅ 좌석 예약 요청 API

- **URL**: `POST /concert/reserve`
- **Method**: POST
- **설명**:  
  - 유효한 토큰을 가진 사용자가 콘서트 ID와 좌석 정보를 포함해 좌석 예약을 요청합니다.
  - 좌석은 5분 동안 임시 배정되며 이 시간 내에 결제가 완료되어야 합니다.

### 🔐 Authorization
- 필요 여부: Bearer {access_token}

### 📥 Request
**Headers**

| 이름 | 필수 | 설명     |
|----|----|-----------|
| Authorization | O  | Bearer 토큰 |

**Request Body**

```json
{
  "userId": 1,
  "concertId": 2,
  "seatId": 12
}
```

### 📤 Response

**HTTP Status Codes**

| 코드     | 설명    |
|--------|-------|
| 200 OK | 성공    | 
|400 Bad Request|잘못된 요청 (예: 좌석 정보 누락)|
| 401 Unauthorized | 인증 실패 |
| 500 Internal Server Error | 서버 오류 |

**Response Body**

```json
{
  "result": "200",
  "message": "Success",
  "data": {
    "reservationId": 123,
    "reservedUntil": "2025-04-03T18:30:00Z"
  }
}
```
### ⚠️ 에러 예시

**400 Bad Request**

```json
{
  "result": "400",
  "message": "Missing or invalid parameters"
}
```

**401 Unauthorized**

```json
{
  "result": "401",
  "message": "Invalid or expired token"
}
```

**500 Internal Server Error**
```json
{
  "result": "500",
  "message": "Internal server error"
}
```

## 5. ✅ 잔액 조회 API

- **URL**: `GET /cash`
- **Method**: GET
- **설명**:
    - 사용자의 현재 잔액을 조회합니다.

### 🔐 Authorization
- 필요 여부: 없음

### 📥 Request
**Query Parameters**

| 이름     | 타입   | 필수 | 설명    |
|--------|------|----|-------|
| userId | Long | O  | 사용자ID |


### 📤 Response

**HTTP Status Codes**

| 코드     | 설명    |
|--------|-------|
| 200 OK | 성공    | 
|400 Bad Request|잘못된 요청|
| 500 Internal Server Error | 서버 오류 |

**Response Body**

```json
{
  "cash": 5000
}
```
### ⚠️ 에러 예시

**400 Bad Request**

```json
{
  "result": "400",
  "message": "Missing or invalid parameters"
}
```

**500 Internal Server Error**
```json
{
  "result": "500",
  "message": "Internal server error"
}
```

## 6. ✅ 충전 API

- **URL**: `PATCH /cash/charge`
- **Method**: PATCH
- **설명**:
    - 사용자 ID를 기준으로 보유한 잔액을 원하는 금액만큼 충전합니다.

### 🔐 Authorization
- 필요 여부: 없음

### 📥 Request
**Request Body**

```json
{
"userId": 1,
"amount": 10000 
}
```

### 📤 Response

**HTTP Status Codes**

| 코드     | 설명    |
|--------|-------|
| 200 OK | 성공    | 
|400 Bad Request|잘못된 요청|
| 500 Internal Server Error | 서버 오류 |

**Response Body**

```json
{
  "cash": 15000
}
```
### ⚠️ 에러 예시

**400 Bad Request**

```json
{
  "result": "400",
  "message": "Missing or invalid parameters"
}
```

**500 Internal Server Error**
```json
{
  "result": "500",
  "message": "Internal server error"
}
```

## 7. ✅ 결제 API

- **URL**: `POST/pay`
- **Method**: POST
- **설명**:
    - 유효한 토큰을 가진 사용자가 임시 예약된 좌석을 결제합니다.
    - 결제 완료 시 캐시 차감, 좌석 확정, 대기열 토큰 만료가 함께 처리됩니다.

### 🔐 Authorization
- 필요 여부: Bearer {access_token}

### 📥 Request

**Headers**

| 이름 | 필수 | 설명        |
|--------|----|-----------|
| Authorization | O  | Bearer 토큰 |

**Request Body**

```json
{
  "reservationId": 123,
  "amount": 10000
}
```

### 📤 Response

**HTTP Status Codes**

| 코드     | 설명    |
|--------|-------|
| 200 OK | 성공    | 
|400 Bad Request|잘못된 요청|
|401 Unauthorized|인증 실패|
| 500 Internal Server Error | 서버 오류 |

**Response Body**

```json
{
  "result": "200",
  "message": "Success",
  "data": {
    "paymentId": 456
  }
}
```
### ⚠️ 에러 예시

**400 Bad Request**

```json
{
  "result": "400",
  "message": "Missing or invalid parameters"
}
```

**401 Unauthorized**

```json
{
  "result": "401",
  "message": "Invalid or expired token"
}
```

**500 Internal Server Error**
```json
{
  "result": "500",
  "message": "Internal server error"
}
```