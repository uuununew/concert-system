## 시퀀스 다이어그램
<details>
<summary>유저 대기열 토큰 기능</summary>
<div markdown="1">

- 사용자는 5초 간격으로 대기열 상태를 조회합니다.
- 시스템은 대기 순번과 상태를 기준으로 진입 가능 여부를 판단합니다.
- 정원이 가득 찬 경우에는 사용자의 현재 대기 순번을 계산해 응답합니다.

```mermaid
sequenceDiagram
    autonumber
    participant 사용자
    participant UserController
    participant WaitingTokenService
    participant WaitingTokenRepository
    participant UserRepository

    사용자 ->> UserController: 대기열 토큰 요청
    UserController ->> WaitingTokenService: 토큰 생성 요청
    WaitingTokenService ->> WaitingTokenRepository: 사용자 토큰 조회

    alt 기존 토큰 존재
        note right of WaitingTokenService: 만료 여부 확인 후 재사용 또는 재발급
        WaitingTokenService -->> UserController: 기존 토큰 반환
    else 토큰 없음 또는 만료됨
        WaitingTokenService ->> UserRepository: 사용자 정보 조회
        WaitingTokenService ->> WaitingTokenRepository: 신규 토큰 저장
        WaitingTokenService -->> UserController: 신규 토큰 반환
    end

    UserController -->> 사용자: 토큰 응답

    loop 주기적 상태 확인
        사용자 ->> UserController: 대기 상태 확인 요청
        UserController ->> WaitingTokenService: 대기열 상태 확인 요청
        WaitingTokenService ->> WaitingTokenRepository: 사용자 토큰 조회

        alt 통과 가능
            WaitingTokenService -->> UserController: 통과 가능 응답
        else 대기 중
            WaitingTokenService -->> UserController: 대기 순번 응답
        end

        UserController -->> 사용자: 상태 응답
    end
```

</div>
</details>

<details>
<summary>예약 가능 날짜 조회 API</summary>
<div markdown="1">

```mermaid
sequenceDiagram
    autonumber
    participant 사용자
    participant UserController
    participant ConcertService
    participant ConcertRepository

    note over 사용자, ConcertRepository: 사용자는 유효한 대기열 토큰을 포함하여 요청합니다

    사용자 ->> UserController: 예약 가능한 날짜 조회 요청
    UserController ->> ConcertService: 예약 가능한 날짜 목록 요청
    ConcertService ->> ConcertRepository: 공연 일정 조회
    ConcertService -->> UserController: 예약 가능한 날짜 목록 반환
    UserController -->> 사용자: 날짜 목록 응답
```

</div>
</details>

<details>
<summary>좌석 조회 API</summary>
<div markdown="1">

- 사용자는 대기열 토큰을 포함해 좌석 정보를 요청합니다.
- 토큰이 유효한 경우에만 해당 공연 회차의 좌석 목록을 조회해 응답합니다

```mermaid
sequenceDiagram
    autonumber
    participant 사용자
    participant UserController
    participant ConcertSeatService
    participant ConcertSeatRepository

    note over 사용자, UserController: ※ 유효한 대기열 토큰을 가진 사용자만 호출 가능

    사용자 ->> UserController: 공연 좌석 목록 조회 요청
    UserController ->> ConcertSeatService: 사용 가능한 좌석 요청
    ConcertSeatService ->> ConcertSeatRepository: 공연 좌석 목록 조회
    ConcertSeatService -->> UserController: 사용 가능한 좌석 목록 반환
    UserController -->> 사용자: 좌석 목록 응답
```

</div>
</details>

<details>
<summary>좌석 예약 요청 API</summary>
<div markdown="1">

- 사용자는 날짜와 좌석 번호, 대기열 토큰을 포함하여 예약을 요청합니다.
- 해당 좌석이 예약 가능한 상태일 경우 5분간 임시 배정 처리합니다.

```mermaid
sequenceDiagram
    autonumber
    actor 사용자
    participant UserController
    participant ConcertSeatService
    participant ReservationService

    note over 사용자, UserController: ※ 유효한 대기열 토큰을 가진 사용자만 호출 가능

    사용자 ->> UserController: 좌석 예약 요청 (날짜, 좌석 정보 포함)
    UserController ->> ConcertSeatService: 좌석 임시 배정 요청

    alt 좌석 예약 가능
        note right of ConcertSeatService: 5분간 임시 배정 유지
        UserController ->> ReservationService: 임시 예약 생성 요청
        UserController -->> 사용자: 좌석 예약 성공 응답
    else 좌석이 이미 임시 배정됨
        UserController -->> 사용자: 좌석 예약 불가 응답
    end
```

</div>
</details>

<details>
<summary>임시 배정 해제 스케줄러</summary>
<div markdown="1">

```mermaid
sequenceDiagram
    autonumber
    participant SeatReleaseScheduler
    participant ConcertSeatService

    loop 일정 주기
        SeatReleaseScheduler ->> ConcertSeatService: 만료된 임시 좌석 확인 요청

        alt 만료된 좌석 존재
            ConcertSeatService ->> ConcertSeatService: 임시 배정 해제 처리
        else 해제 대상 없음
            note over SeatReleaseScheduler: 대기
        end
    end
```

</div>
</details>

<details>
<summary>잔액 조회 API</summary>
<div markdown="1">

```mermaid
sequenceDiagram
    autonumber
    actor 사용자
    participant UserController
    participant CashService
    participant CashRepository

    사용자 ->> UserController: 잔액 조회 요청
    UserController ->> CashService: 사용자 잔액 확인 요청
    CashService ->> CashRepository: 사용자 캐시 정보 조회
    CashRepository -->> CashService: 잔액 정보 반환
    CashService -->> UserController: 잔액 반환
    UserController -->> 사용자: 잔액 응답
```

</div>
</details>

<details>
<summary>충전 API</summary>
<div markdown="1">

```mermaid
sequenceDiagram
    autonumber
    actor 사용자
    participant UserController
    participant CashService
    participant CashRepository

    사용자 ->> UserController: 금액 충전 요청
    UserController ->> CashService: 사용자 캐시에 금액 추가 요청
    CashService ->> CashRepository: 사용자 캐시 정보 조회

    alt 기존 캐시 정보 있음
        CashService ->> CashRepository: 잔액에 금액 추가
    else 최초 충전
        CashService ->> CashRepository: 새로운 캐시 정보 생성
    end

    CashService -->> UserController: 충전 완료 응답
    UserController -->> 사용자: 충전 성공 안내
```

</div>
</details>

<details>
<summary>결제 API</summary>
<div markdown="1">

```mermaid
sequenceDiagram
    autonumber
    actor 사용자
    participant UserController
    participant CashService
    participant ConcertSeatService
    participant PaymentService

    note over 사용자, UserController: ※ 유효한 대기열 토큰을 가진 사용자만 호출 가능

    사용자 ->> UserController: 결제 요청
    UserController ->> CashService: 사용자 잔액 확인

    alt 잔액 부족
        UserController -->> 사용자: 에러 응답 (잔액 부족)
    else 잔액 충분
        CashService ->> CashService: 잔액 차감
        UserController ->> ConcertSeatService: 좌석 확정 요청
        UserController ->> PaymentService: 결제 정보 저장
        UserController -->> 사용자: 결제 완료 응답
    end
```

</div>
</details>