## 시퀀스 다이어그램

<details>
<summary>유저 대기열 토큰 기능</summary>
<div markdown="1">

- 사용자는 5초 간격으로 대기열 상태를 조회하며 시스템은 대기 순번과 상태를 기준으로 진입 가능 여부를 판단합니다.
- 정원이 가득 찬 경우에는 사용자의 현재 대기 순번을 계산해 응답합니다.

```mermaid
sequenceDiagram
    participant 사용자
    participant API
    participant WaitingTokenService
    participant DB

    사용자->>API: 대기열 토큰 요청
    API->>WaitingTokenService: 토큰 생성 요청
    WaitingTokenService->>DB: 기존 토큰 조회
    alt 기존 토큰 존재
        note right of WaitingTokenService: 만료된 경우 새로 생성됨
        WaitingTokenService-->>API: 기존 토큰 반환
    else
        WaitingTokenService->>DB: 신규 토큰 생성
        WaitingTokenService-->>API: 신규 토큰 반환
    end
    API-->>사용자: 토큰 응답

    loop 주기적 확인
        사용자->>API: 대기 상태 확인
        API->>WaitingTokenService: 상태 조회 요청
        WaitingTokenService->>DB: 토큰 조회 및 상태 확인
        alt 통과 가능
            WaitingTokenService-->>API: 통과 응답
        else
            WaitingTokenService-->>API: 대기 순번 응답
        end
        API-->>사용자: 응답 전달
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
    participant API
    participant ConcertService
    participant DB

    note over 사용자, API: ※유효한 대기열 토큰을 가진 사용자만 호출 가능

    사용자->>API: 예약 가능한 날짜 요청 (토큰 포함)
    API->>ConcertService: 유효성 검사 및 조회 요청
    ConcertService->>DB: 공연 정보 조회
    ConcertService-->>API: 예약 가능한 날짜 목록 반환
    API-->>사용자: 날짜 목록 응답
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
    participant API
    participant ConcertService
    participant DB

    note over 사용자, API: ※유효한 대기열 토큰을 가진 사용자만 호출 가능

    사용자->>API: 좌석 조회 요청 (날짜 포함)
    API->>ConcertService: 좌석 정보 요청
    ConcertService->>DB: 좌석 정보 조회
    ConcertService-->>API: 좌석 목록 반환
    API-->>사용자: 좌석 정보 응답
```

</div>
</details>

<details>
<summary>좌석 예약 요청 API</summary>
<div markdown="1">

- 사용자는 날짜와 좌석 번호, 대기열 토큰을 포함해 예약 요청을 보냅니다.
- 서버는 토큰 유효성을 확인한 뒤 해당 좌석이 예약 가능한 상태일 경우 5분간 임시 배정합니다.

```mermaid
sequenceDiagram
    autonumber
    actor 사용자
    participant API as 예약 API
    participant 좌석 as ConcertScheduleSeatService

    note over 사용자, API: ※유효한 대기열 토큰을 가진 사용자만 호출 가능

    사용자 ->> API: 날짜와 좌석 번호로 예약 요청
    API ->> 좌석: 좌석 상태 조회
    alt 좌석이 예약 가능할 경우
        좌석 ->> 좌석: 임시 배정 상태로 변경 (5분 타이머 설정)
        좌석 -->> API: 임시 배정 완료
        API -->> 사용자: 좌석 임시 배정 성공 응답
    else 좌석이 이미 임시 배정 중일 경우
        좌석 -->> API: 예약 불가
        API -->> 사용자: 좌석 예약 불가 응답
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