## 시퀀스 다이어그램
<details>
<summary>유저 대기열 토큰 기능</summary>
<div markdown="1">

- 사용자는 5초 간격으로 대기열 상태를 조회합니다.
- 시스템은 사용자의 대기 순번과 토큰 상태를 기준으로 입장 가능 여부를 판단합니다.
- 정원이 가득 찬 경우에는 현재 대기 순번만 계산하여 응답합니다.

```mermaid
sequenceDiagram
    autonumber
    participant 사용자
    participant UserController
    participant WaitingTokenService
    participant WaitingTokenRepository
    participant UserRepository

    note over 사용자, UserController: 사용자 → 대기열 토큰 요청

    사용자->>UserController: 대기열 토큰 요청
    UserController->>WaitingTokenService: 토큰 발급 요청 (userId)
    WaitingTokenService->>WaitingTokenRepository: 사용자 ID로 기존 토큰 조회

    alt 기존 토큰 존재
        note right of WaitingTokenService: 만료 여부 확인 후 재사용 또는 재발급
        WaitingTokenService-->>UserController: 기존 토큰 반환
    else
        WaitingTokenService->>UserRepository: 사용자 정보 조회
        WaitingTokenService->>WaitingTokenRepository: 새 토큰 저장 (상태 = WAITING)
        WaitingTokenService-->>UserController: 신규 토큰 반환
    end
    UserController-->>사용자: 토큰 응답

    loop 5초 간격
        사용자->>UserController: 대기 상태 확인 요청
        UserController->>WaitingTokenService: 상태 확인 요청
        WaitingTokenService->>WaitingTokenRepository: 사용자 토큰 조회

        alt 상태 = ACTIVE
            WaitingTokenService-->>UserController: 입장 가능 응답
        else 상태 = WAITING
            note right of WaitingTokenService: 순번 조회 후 필요 시 ACTIVE로 갱신
            WaitingTokenService-->>UserController: 대기 순번 응답
        else 상태 = EXPIRED
            WaitingTokenService-->>UserController: 만료 안내 응답
        end

        UserController-->>사용자: 상태 응답
    end
```

</div>
</details>

<details>
<summary>예약 가능 날짜 조회 API</summary>
<div markdown="1">

- 사용자는 대기열 토큰을 포함해 예약 가능한 날짜를 요청합니다.
- 토큰이 유효한 경우에만 해당 콘서트의 예약 가능 일정을 조회해 응답합니다.

```mermaid
sequenceDiagram
    autonumber
    participant 사용자
    participant UserController
    participant ConcertService
    participant ConcertRepository

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
- 토큰이 유효한 경우에만 해당 콘서트의 회차의 좌석 목록을 조회해 응답합니다.

```mermaid
sequenceDiagram
    autonumber
    participant 사용자
    participant UserController
    participant ConcertSeatService
    participant ConcertSeatRepository

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

- 사용자는 날짜와 좌석 번호, 대기열 토큰을 포함해 좌석 예약을 요청합니다.
- 토큰이 유효하고 좌석이 예약 가능한 경우, 해당 좌석을 5분간 임시로 배정하며 예약 요청을 완료합니다.

```mermaid
sequenceDiagram
    autonumber
    actor 사용자
    participant UserController
    participant ConcertSeatService
    participant ReservationService

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
<summary>임시 배정 좌석 해제 스케줄러</summary>
<div markdown="1">

```mermaid
sequenceDiagram
    participant 스케줄러
    participant 좌석 as ConcertScheduleSeatService

    note over 스케줄러, 좌석: ※ 임시 배정된 좌석을 일정 주기로 해제

    loop 일정 주기
        스케줄러 ->> 좌석: 임시 배정 만료 여부 확인
        alt 만료된 좌석 존재
            좌석 ->> 좌석: 임시 배정 해제 처리
        else 해제할 좌석 없음
            note over 스케줄러: 대기
        end
    end

```

</div>
</details>

<details>
<summary>잔액 조회 API</summary>
<div markdown="1">

- 이 API는 대기열 토큰 없이도 호출 가능하며 사용자 ID를 통해 잔액 정보를 반환합니다.

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

    alt 캐시 정보 존재
        CashRepository -->> CashService: 잔액 정보 반환
    else 캐시 정보 없음
        CashRepository -->> CashService: 기본 잔액(0원) 반환
    end

    CashService -->> UserController: 잔액 반환
    UserController -->> 사용자: 잔액 응답
```

</div>
</details>

<details>
<summary>충전 API</summary>
<div markdown="1">

- 사용자는 금액을 입력해 자신의 잔액을 충전할 수 있습니다.
- 시스템은 사용자 ID를 기반으로 캐시 정보를 조회하고 기존 정보가 없을 경우 기본 잔액 0으로 새로 생성한 뒤 충전 금액을 추가합니다.

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
    else 캐시 정보 없음
        CashService ->> CashRepository: 기본 잔액 0으로 생성 후 금액 추가
    end

    CashService -->> UserController: 충전 완료 응답
    UserController -->> 사용자: 충전 성공 안내
```

</div>
</details>

<details>
<summary>결제 API</summary>
<div markdown="1">

- 사용자는 유효한 대기열 토큰을 포함하여 결제를 요청할 수 있습니다.
- 시스템은 사용자 잔액을 확인하고, 잔액이 충분한 경우에만 좌석을 확정하고 결제 처리를 진행합니다.
- 결제가 완료되면 좌석 소유권이 확정되고, 대기열 토큰은 만료 처리됩니다.
- 좌석 확정에 실패할 경우 잔액은 롤백되며, 결제는 진행되지 않습니다.


```mermaid
sequenceDiagram
    autonumber
    actor 사용자
    participant UserController
    participant CashService
    participant ConcertSeatService
    participant PaymentService

    사용자 ->> UserController: 결제 요청
    UserController ->> CashService: 사용자 잔액 확인

    alt 잔액 부족
        UserController -->> 사용자: 에러 응답 (잔액 부족)
    else 잔액 충분
        CashService ->> CashService: 잔액 차감
        UserController ->> ConcertSeatService: 좌석 확정 요청

        alt 좌석 확정 성공
            UserController ->> PaymentService: 결제 정보 저장
            PaymentService ->> PaymentService: 대기열 토큰 만료 처리
            UserController -->> 사용자: 결제 완료 응답
        else 좌석 확정 실패
            CashService ->> CashService: 잔액 롤백 처리
            UserController -->> 사용자: 에러 응답 (좌석 확정 실패)
        end
    end
```

</div>
</details>