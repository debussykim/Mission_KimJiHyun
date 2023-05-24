## Title: [4Week] 김지현

### 미션 요구사항 분석 & 체크리스트

---

**[ 필수 미션 ]**

- [ ]  네이버클라우드플랫폼을 통한 배포, 도메인, HTTPS 까지 적용
- [x]  **내가 받은 호감리스트(/usr/likeablePerson/toList)에서 성별 필터링 기능 구현**
    - [x]  사용자가 전달한 성별 값에 따라 호감 리스트를 필터링하여 해당 성별의 사람들만 남기도록 로직 구현
    - [x]  **`gender`** 파라미터를 받아 해당 성별에 해당하는 `LikeablePerson`만 필터링

**[ 선택 미션 ]**

- [x]  내가 받은 호감리스트(/usr/likeablePerson/toList)에서 호감사유 필터링 기능 구현
    - [x]  사용자가 전달한 호감사유 코드에 따라 호감 리스트를 필터링하여 해당 호감사유 코드와 일치하는 사람들만 남기도록 로직 구현
    - [x]  **`attractiveTypeCode`** 파라미터를 받아 해당 호감사유 코드에 해당하는 `LikeablePerson`만 필터링
- [x]  내가 받은 호감리스트(/usr/likeablePerson/toList)에서 정렬 기능
    - [x]  **`sortCode`** 파라미터를 받아 해당하는 정렬 방식에 따라 `LikeablePerson` 목록 정렬

### 4주차 미션 요약

---

**[접근 방법]**

성별 필터링 기능 구현:

- 호감리스트를 받아올 때 **`gender`** 파라미터를 이용하여 해당 성별과 일치하는지 확인하였습니다.
- **`likeablePeopleStream.filter`** 메서드를 사용하여 **`person.getFromInstaMember().getGender()`** 값이 `gender`와 일치하는지 확인했습니다. 이렇게 성별이 일치하는 사람들만 필터링하여 유지하고, 나머지는 제외하였습니다.

호감사유 필터링 기능 구현:

- 호감리스트를 받아올 때 **`attractiveTypeCode`** 매개변수를 사용하여 해당 호감 사유 코드와 일치하는지 확인하였습니다. 필터링할 호감 사유 코드를 전달받았습니다.
- **`likeablePeopleStream.filter`** 메서드를 사용하여 **`person.getAttractiveTypeCode()`** 값이 `attractiveTypeCode`와 일치하는지 확인하여 호감 사유 코드가 일치하는 사람들만 필터링하였습니다.

최종적으로 정렬된 호감리스트를 리스트로 변환하고 모델에 추가하여 뷰로 전달되도록 구현하였습니다.

정렬 기능 구현:

- 사용자가 선택한 정렬 방식에 따라 호감리스트를 정렬하고자 했습니다.
- 호감리스트를 받아올 때 **`sortCode`** 파라미터를 사용하여 정렬 방식을 전달받았습니다.
- 힌트 코드에서 **`switch`** 문을 사용하여 `sortCode`에 따라 다른 정렬 방식으로 구현하고자 하는 것을 이해했습니다.
- 각 case 별로 해당하는 정렬 방식을 적용하기 위해 **`likeablePeopleStream.sorted`** 메서드를 사용하는데, 정렬 조건에 따라 **`Comparator`** 인터페이스의 메서드를 활용하여 정렬 기준을 설정하였습니다.
- 필요에 따라 **`Comparator.reversed`** 메서드를 사용하여 역순으로 정렬하였습니다.

위와 같이 구현하여, 성별 필터링, 호감사유 필터링, 정렬 기능이 적용된 호감리스트를 반환합니다. **`showToList`** 메서드는 성별, 호감사유, 정렬에 따라 필터링되고 정렬된 호감리스트를 반환하며, 결과물은 **`likeablePeople`** 변수에 저장되어 모델에 추가되어 뷰로 전달됩니다.

위와 같은 로직 구현을 통해 이번 4주차 미션에서는 성별 필터링, 호감사유 필터링, 정렬 기능이 적용된 호감리스트를 반환하는 **`showToList`** 메서드를 구현해보았습니다. 이 메서드는 **`gender`** 파라미터가 주어지면 해당 성별로 필터링된 호감리스트를 반환하고, **`attractiveTypeCode`** 파라미터가 주어지면 해당 호감사유로 필터링된 호감리스트를 반환하며, **`sortCode`** 파라미터에 따라 다양한 정렬 기준으로 정렬된 호감리스트를 반환합니다. 최종 결과물은 정렬된 호감리스트를 리스트로 변환한 **`likeablePeople`** 변수에 저장되고 모델에 추가되어 뷰로 전달되도록 합니다.

**[특이사항]**

ShowToList에서 thenComparing 부분의 오류 해결

기존에 작성했던 로직은 다음과 같습니다.

```
case 5: // 성별순(여성 우선순위), 2순위 정렬조건으로 최신순
      likeablePeopleStream = likeablePeopleStream.sorted(
              Comparator.comparing(person -> person.getFromInstaMember().getGender(), Comparator.reverseOrder())
              .thenComparing(LikeablePerson::getCreateDate)
);
```

오류 발생 원인을 분석해보았습니다.

오류를 해결하기 위해, `case 5`의 구현을 다음과 같이 수정했습니다:

```
case 5:
    likeablePeopleStream = likeablePeopleStream
        .sorted(Comparator.comparing((LikeablePerson person) -> person.getFromInstaMember().getGender())
            .thenComparing(Comparator.comparing(LikeablePerson::getCreateDate))
            .reversed());
```

위 수정된 코드에서는 초기 비교를 위해 **`(LikeablePerson person) -> person.getFromInstaMember().getGender()`** 람다 함수를 사용하여 성별 값을 추출하도록 했습니다. 그리고 **`thenComparing`** 메서드를 연결하여 생성 날짜를 기준으로 두 번째 비교를 수행하도록 했습니다. 마지막으로 **`reversed`** 메서드를 사용하여 역순으로 정렬하게 했습니다.

thenComparing 메서드는 정렬 조건을 연결할 때 사용되며, 기존 정렬에 추가적인 정렬 조건에 적용할 수 있다는 것을 알게 되었습니다.