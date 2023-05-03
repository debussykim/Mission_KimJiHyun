# Mission Record

## Title: [3Week] 김지현

### 미션 요구사항 분석 & 체크리스트

---

1. **필수미션 1 - 호감에 대한 수정/삭제 쿨타임 로직 추가**
    - [x]  호감사유 변경 후 개별 호감표시건에 대해 3시간 동안은 호감취소와 호감사유변경을 할 수 없도록 구현
    - [x]  호감취소 후 개별 호감표시건에 대해 3시간 동안은 호감취소와 호감사유변경을 할 수 없도록 구현

2. **필수미션 1 - 쿨타임 체크 로직 추가**
    - [x]  LikeablePersonService::canCancel
    - [x]  LikeablePersonService::canLike

3. **필수미션 2 - 네이버 클라우드 플랫폼을 통한 배포(도메인 없이, IP로 접속)**
    - [ ]  `https://서버IP:포트/` 형태로 접속 가능하도록

### 3주차 미션 요약

---

**[접근 방법]**

- `getModifyUnlockSecondsLeft()` 메서드

`modifyUnlockDate`에 도달할 때까지 남은 시간(초)을 계산하는 메서드로, 코드를 더 모듈화하고 읽기 쉽게 만들기 위해 별도로 구현했습니다.

남은 시간 계산을 자체 방법으로 분리하면 필요한 경우 코드의 다른 부분에서 재사용하기 쉬울거라 생각하여 분리했습니다. 또한 남은 시간(초) 계산과 남은 시간(시간 및 분) 계산을 분리하여 코드를 더 읽기 쉽고 이해하기 쉽게 유지할 수 있습니다.

- `getModifyUnlockDateRemainStrHuman()` 메서드

잠금 해제 시간까지 남은 시간(`getModifyUnlockSecondsLeft()`)을 계산한 다음 ,이것을 시간과 분으로 변환하는 메서드입니다.

처음에는 LikeablePersonService 클래스에서 관련된 기능을 구현해 보았습니다. 그러나 호감취소 및 수정 메서드 모두에 동일한 코드를 추가해야 하므로 중복 코드가 발생했습니다.

canModifyLike() 메서드에서 남은 시간을 계산하는 로직을 복제하는 대신 LikeablePerson 엔티티의 getModifyUnlockDateRemainStrHuman() 메서드를 호출하기만 하면 된다는 것을 알 수 있었습니다. 중복을 제거하는 것이 좋은 방향이라 생각했습니다. 그 결과 코드 중복을 피하기 위해 LikeablePerson 엔터티에서 직접 이 기능을 구현하기로 결정했습니다.


**[특이사항]**