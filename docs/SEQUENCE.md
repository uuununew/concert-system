## 시퀀스 다이어그램
<details>
<summary>유저 대기열 토큰 기능</summary>
<div markdown="1">

- 사용자는 5초 간격으로 대기열 상태를 조회합니다.
- 시스템은 대기 순번과 상태를 기준으로 진입 가능 여부를 판단합니다.
- 정원이 가득 찬 경우에는 사용자의 현재 대기 순번을 계산해 응답합니다.

```mermaid
sequenceDiagram
    participant 사용자
    participant UserController
    participant WaitingTokenService
    participant WaitingTokenRepository
    participant UserRepository

    사용자->>UserController: 대기열 토큰 요청
    UserController->>WaitingTokenService: generateToken(userId)
    WaitingTokenService->>WaitingTokenRepository: findByUserId(userId)
    alt 기존 토큰 존재
        note right of WaitingTokenService: 만료 여부 확인 후 재사용 또는 재발급
        WaitingTokenService-->>UserController: 기존 토큰 반환
    else
        WaitingTokenService->>UserRepository: findById(userId)
        WaitingTokenService->>WaitingTokenRepository: save(newToken)
        WaitingTokenService-->>UserController: 신규 토큰 반환
    end
    UserController-->>사용자: 토큰 응답

    loop 주기적 확인
        사용자->>UserController: 대기 상태 확인
        UserController->>WaitingTokenService: getStatus(userId)
        WaitingTokenService->>WaitingTokenRepository: findByUserId(userId)
        alt 통과 가능
            WaitingTokenService-->>UserController: 통과 응답
        else
            WaitingTokenService-->>UserController: 대기 순번 응답
        end
        UserController-->>사용자: 응답 전달
    end
```

</div>
</details>

<details>
<summary>예약 가능 날짜 조회 API</summary>
<div markdown="1">

```mermaid
sequenceDiagram
    participant 사용자
    participant UserController
    participant ConcertService
    participant ConcertRepository

    note over 사용자,ConcertRepository: 사용자는 유효한 대기열 토큰을 포함하여 요청합니다

    사용자->>UserController: 예약 가능한 날짜 요청
    UserController->>ConcertService: getAvailableDates()

    ConcertService->>ConcertRepository: findAvailableDates()
    ConcertService-->>UserController: 예약 가능한 날짜 목록 반환
    UserController-->>사용자: 날짜 목록 응답
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
    participant 사용자
    participant UserController
    participant ConcertSeatService
    participant ConcertSeatRepository

    note over 사용자, UserController: ※유효한 대기열 토큰을 가진 사용자만 호출 가능

    사용자->>UserController: 공연 좌석 목록 조회 요청
    UserController->>ConcertSeatService: getAvailableSeats(concertId)

    ConcertSeatService->>ConcertSeatRepository: findByConcertIdAndStatusAvailable(concertId)
    ConcertSeatRepository-->>ConcertSeatService: 좌석 목록 반환

    ConcertSeatService-->>UserController: 사용 가능한 좌석 목록 반환
    UserController-->>사용자: 좌석 목록 응답
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
    participant ConcertSeatRepository
    participant ReservationService
    participant ReservationRepository
    participant ReservationItemRepository

    note over 사용자, UserController: ※유효한 대기열 토큰을 가진 사용자만 호출 가능

    사용자 ->> UserController: 날짜와 좌석 번호로 예약 요청
    UserController ->> ConcertSeatService: holdSeat(concertSeatId)
    ConcertSeatService ->> ConcertSeatRepository: findById(concertSeatId)

    alt 좌석이 예약 가능할 경우
        ConcertSeatService ->> ConcertSeatRepository: updateStatusToHeld(concertSeatId)
        note right of ConcertSeatService: 5분간 임시 배정 유지 타이머 설정
        UserController ->> ReservationService: createReservation(userId, seat)
        ReservationService ->> ReservationRepository: save(임시 예약)
        ReservationService ->> ReservationItemRepository: save(임시 예약 아이템)
        UserController -->> 사용자: 좌석 임시 배정 성공 응답
    else 좌석이 이미 임시 배정 중일 경우
        ConcertSeatService -->> UserController: 예약 불가
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

    note over SeatReleaseScheduler, ConcertSeatService: ※ 임시 배정된 좌석을 일정 주기로 해제

    loop 일정 주기
        SeatReleaseScheduler ->> ConcertSeatService: checkExpiredHeldSeats()
        alt 만료된 좌석 존재
            ConcertSeatService ->> ConcertSeatService: releaseExpiredSeats()
        else 해제할 좌석 없음
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
    UserController ->> CashService: getCash(userId)
    CashService ->> CashRepository: findByUserId(userId)
    CashRepository -->> CashService: UserCash 반환
    CashService -->> UserController: amount 반환
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

    사용자 ->> UserController: 금액 충전 요청 (amount)
    UserController ->> CashService: charge(userId, amount)
    CashService ->> CashRepository: findByUserId(userId)

    alt 기존 Cash 레코드 존재
        CashService ->> CashRepository: updateAmount(userId, amount)
    else 최초 충전
        CashService ->> CashRepository: create(userId, amount)
    end

    CashService -->> UserController: 충전 완료 응답
    UserController -->> 사용자: 충전 성공 응답
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
    participant UserController as UserController
    participant WaitingTokenService as WaitingTokenService
    participant CashService as CashService
    participant ConcertSeatService as ConcertSeatService
    participant PaymentService as PaymentService

    사용자 ->> UserController: 결제 요청
    UserController ->> WaitingTokenService: 토큰 검증
    alt 토큰 무효
        UserController -->> 사용자: 에러 응답
    else 토큰 유효
        UserController ->> WaitingTokenService: 대기열 상태 조회
        alt 상태가 EXPIRED
            UserController -->> 사용자: 에러 응답 (대기 만료)
        else 상태 유효
            UserController ->> CashService: 잔액 조회 및 결제 가능 여부 확인
            alt 잔액 부족
                UserController -->> 사용자: 에러 응답 (잔액 부족)
            else 잔액 충분
                CashService ->> CashService: 잔액 차감
                CashService -->> UserController: 차감 완료
                UserController ->> ConcertSeatService: 좌석 확정 처리
                opt 전체 좌석 마감 시
                    ConcertSeatService ->> ConcertSeatService: 공연 상태 마감 처리
                end
                UserController ->> PaymentService: 결제 정보 저장 및 영수증 생성
                UserController ->> WaitingTokenService: 상태를 DONE으로 변경
                UserController -->> 사용자: 결제 완료 응답 (좌석 정보 포함)
            end
        end
    end

    rect rgba(0, 0, 255, .1)
        note over WaitingTokenService: PROGRESS 상태는 10분간 활동이 없으면 스케줄러에 의해 EXPIRED로 전환
    end
```

</div>
</details>