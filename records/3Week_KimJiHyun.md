# Mission Record

## Title: [3Week] 김지현

### 미션 요구사항 분석 & 체크리스트

---

1. **필수미션 1 - 네이버 클라우드 플랫폼을 통한 배포(도메인 없이, IP로 접속)**
   - [ ]  `https://서버IP:포트/` 형태로 접속 가능하도록

  
2. **필수미션 2 - 호감표시에 대한 수정/삭제 쿨타임 로직 추가**
    - [x]  호감사유 변경 후 개별 호감표시건에 대해 3시간 동안은 호감취소와 호감사유변경을 할 수 없도록 구현
    - [x]  LikeablePersonService::canLike 쿨타임 체크 로직 추가
  
3. **필수미션 3 - 호감표시에 대한 수정/삭제 쿨타임 로직 추가**
   - [x]  호감취소 후 개별 호감표시건에 대해 3시간 동안은 호감취소와 호감사유변경을 할 수 없도록 구현
   - [x]  LikeablePersonService::canCancel 쿨타임 체크 로직 추가



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

오류가 난 부분에 대해 분석하고 그 내용을 정리합니다. 추후 리팩토링 때 멘토님께 도움을 요청하여 오류를 해결하도록 하겠습니다.

- 배포 준비를 위해 MariaDB를 설치하고 프로젝트와 연결하려 여러번 시도 했으나 정상적으로 실행되지 않았습니다. 이 이유 때문인지 빌드를 하였을 때 테스트 오류가 났습니다.
  - 오류 내용

    Task :test FAILED
    FAILURE: Build failed with an exception.

    java.lang.IllegalStateException at DefaultCacheAwareContextLoaderDelegate.java:142
    Caused by: org.springframework.beans.factory.BeanCreationException at AutowiredAnnotationBeanPostProcessor.java:488


- 현재 Sequeal Pro 사용 중입니다.
- 3306번, 3307번, 3308번으로 연결해서 시도 해봤으나 프로젝트가 실행되지 않았습니다.

```
# MariaDB 컨테이너 실행
docker run \
  --name mariadb_1 \
  -d \
  --restart unless-stopped \
  -e MARIADB_ROOT_PASSWORD=lldj123414 \
  -e TZ=Asia/Seoul \
  -p 3307:3306 \
  -v /docker_projects/mariadb_1/conf.d:/etc/mysql/conf.d \
  -v /docker_projects/mariadb_1/mysql:/var/lib/mysql \
  -v /docker_projects/mariadb_1/run/mysqld:/run/mysqld/ \
  mariadb:latest
```