## Title: [1Week] 김지현

### 미션 요구사항 분석 & 체크리스트

---

### 체크리스트

---

- [ ]  호감상대 삭제 Test Case 작성
- [ ]  호감목록 페이지에서 삭제 버튼 클릭 시 DB에서 호감상대 삭제하는 로직 구현
- [ ]  승인되지 않은 사용자는 삭제하지 못하도록 소유권 검사
- [ ]  해당 데이터 삭제 후 메시지 반환
- [ ]  삭제 후 다시 호감 목록 페이지로 리다이렉트
<br/><br/><br/>
### 1주차 미션 요약

---

**[접근 방법]**

TDD 방식으로 시간이 걸리더라도 호감상대 삭제하는 기능을 실패하는 테스트 케이스 작성-오류수정-리팩토링 순으로 구현하려 했습니다. 또한 컨트롤러-서비스-리포지터리-엔티티 흐름 이러한 순으로 이해하는 것에 중점을 두고 구현해나갔습니다.

- 실패하는 테스트 케이스 작성
- delete 메서드에 대한 테스트 메서드 실행
- 테스트 실패 → delete 메서드 구현
- 구현한 delete 메서드에 대해 테스트 메서드 다시 실행
- 테스트 성공 후 리팩토링
  <br/><br/>

1. **호감 목록 페이지에서 삭제 버튼 클릭시 DB에서 `likeable_person` 엔티티를 삭제하는 테스트 케이스 작성**

```
@Test
@DisplayName("호감 상대 삭제")
@WithUserDetails("user3")
void t006() throws Exception {
        //WHEN
        ResultActions resultActions = mvc
                .perform(post("/likeablePerson/delete/1")
                        .param("id", "3"))
                .andDo(print());

        //THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/likeablePerson/list"));
    }
```

- 로그인되어 있는 상태에서 삭제가 가능하기 때문에 `@WithUserDetails` 로 로그인 상태를 주었습니다.
- user3으로 로그인한 상태로 두고 insta_user4를 삭제해보는 테스트 케이스로 작성해보았습니다.
- `LikeablePersonController`의 delete() 메서드로 삭제하도록 하였습니다.
- 삭제를 성공하면 호감목록(list)으로 돌아가기 위해 3xx로 리다이렉트합니다.
- 삭제를 위해 필요한 정보는 어떤 것이 있는지 파악
    - 삭제할 호감상대의 ID
    - 로그인한 사용자의 정보
    - 호감상대와 로그인한 사용자의 관계
    - 삭제 후 이동할 페이지


**2. 호감목록 페이지에서 삭제 버튼 클릭 시 DB에서 호감상대 삭제하는 로직 구현**

`LikeablePersonController`에서 삭제버튼을 눌렀을 때 `LikeablePersonService`의 delete 메소드를 호출

```
@PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        Member member = rq.getMember();
        RsData<LikeablePerson> deleteRsData = likeablePersonService.delete(id, member);

        return rq.redirectWithMsg("/likeablePerson/list", deleteRsData);
    }
```

- 삭제할 호감상대의 ID를 파라미터로 받는 delete 메서드 작성하였습니다.
- `LikeablePersonRepository`에서 delete 메서드 호출합니다.
- `RsData`객체를 받아 리다이렉트하도록 `delete`POST 요청 핸들러를 작성합니다.
- URL의 `id`매개변수를 메서드 서명의 `id`매개변수에 올바르게 매핑하기 위해 `@PathVariable("id")`사용
- `likeablePersonService`의 `delete` 메서드를 호출하고, 그 결과로 반환된 `RsData<LikeablePerson>` 객체를 `deleteRsData` 변수에 할당합니다.
- 삭제 후 다시 호감 목록 페이지로 리다이렉트
    - `rq.redirectWithMsg` 메서드를 사용하여 "likeablePerson/list" 경로로 리다이렉트하고 `deleteRsData` 객체를 함께 전달합니다.

```
@Transactional
    public RsData<LikeablePerson> delete(@PathVariable Integer id, Member member) {
        Optional<LikeablePerson> opLikeablePerson = likeablePersonRepository.findById(id);
        if (opLikeablePerson.isEmpty()) {
            return RsData.of("F-1", "호감상대가 존재하지 않습니다.");
        }

        // TODO : 삭제 권한 검사 추가

        likeablePersonRepository.delete(likeablePerson);
        return RsData.of("S-1", "호감 상대를 삭제하였습니다.", likeablePerson);
    }
```

- `RsData<LikeablePerson>`을 반환하는 새 메서드 `delete()`를 추가했습니다.
- 이 메소드는 `rq.getMember()`를 사용하여 로그인한 `Member`를 검색하고 `LikeablePersonService`의 `delete()` 메소드에 전달합니다.
- 서비스에서 반환된 'RsData'는 컨트롤러 메서드에서 반환되도록 하였습니다.

**3. 호감상대를 삭제 권한이 있는지 확인하는 로직 구현**

해당 항목에 대한 소유권이 본인(로그인한 사람)에게 있는지 체크

```
LikeablePerson likeablePerson = opLikeablePerson.get();

if (!likeablePerson.getFromInstaMember().getId().equals(member.getInstaMember().getId())) {
            return RsData.of("F-2", "호감 상대를 삭제할 수 있는 권한이 없습니다.");
}
```

- 주어진 `id`를 가진 `LikeablePerson`이 데이터베이스에 존재하는지 확인합니다. 그렇지 않으면 오류 메시지를 반환합니다.
- 그렇지 않으면 현재 사용자에게 `LikeablePerson`을 삭제할 수 있는 권한이 있는지 확인합니다. `LikeablePerson`의 `fromInstaMember`가 현재 사용자와 동일한지 확인하여 이를 수행하도록 하였습니다. 그렇지 않은 경우 오류 메시지를 반환합니다.
- `findByUsername` 메서드는 사용자 이름을 사용하고 저장소에서 선택적 `InstaMember` 엔터티를 반환합니다.
- 모든 것이 확인되면 `LikeablePerson`을 삭제하고 성공 메시지를 반환합니다.

**[특이사항]**

아쉬웠던 점

오류테스트 케이스 통과를 못한 점이 아쉽습니다. 어느 부분이 잘못된 건지 리팩토링하면서 오류를 해결해나가야할 것 같습니다.

> [SOLVED] 발생한 오류 (LikeablePersonService.java)
java: incompatible types: java.lang.Long cannot be converted to java.lang.Integer
>

```
// 수정 전
@Transactional
    public RsData<LikeablePerson> delete(@PathVariable ***Long id***, Member member) {
        Optional<LikeablePerson> opLikeablePerson = likeablePersonRepository.findById(id);
... }
-----------------------------------------
@PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{id}")
    public String delete(@RequestParam ***Long id***) {
```

```
// 수정 후
@Transactional
    public RsData<LikeablePerson> delete(@PathVariable **Integer *id***, Member member) {
        Optional<LikeablePerson> opLikeablePerson = likeablePersonRepository.findById(id);
... }
-----------------------------------------
@PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{id}")
    public String delete(@RequestParam ***Integer id***) {
```

`LikeablePersonService` 에서 `Integer` 인수를 예상하는 메서드에 `Long` 인수를 전달하기 때문에  `Long`유형을 `Integer`유형으로 변환하려고 시도해서 이러한 오류가 발생한 것 같습니다.

현재는 `delete`메서드의 `id`매개변수 유형을 `Long`에서 `Integer`로 변경해서 오류를 해결했지만 `Integer`대신 그대로 `Long`을 허용하도록 메서드를 변경할 수도 있을 것 같습니다.

> 삭제 시 발생한 오류
WARN 45241 --- [nio-8080-exec-1] .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.HttpRequestMethodNotSupportedException: Request method 'GET' is not supported]
>

요청 메소드가 GET이 아닌데 GET 메소드를 사용하려고 할 때 발생하는 오류

**참고: [Refactoring]**

- 삭제 시 발생한 오류 수정
- 테스트 케이스 통과하도록 수정