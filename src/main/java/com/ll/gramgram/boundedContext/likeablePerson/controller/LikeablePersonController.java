package com.ll.gramgram.boundedContext.likeablePerson.controller;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/usr/likeablePerson")
@RequiredArgsConstructor
public class LikeablePersonController {
    private final Rq rq;
    private final LikeablePersonService likeablePersonService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/like")
    public String showLike() {
        return "usr/likeablePerson/like";
    }

    @AllArgsConstructor
    @Getter
    public static class LikeForm {
        @NotBlank
        @Size(min = 3, max = 30)
        private final String username;
        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like")
    public String like(@Valid LikeForm likeForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.like(rq.getMember(), likeForm.getUsername(), likeForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public String showList(Model model) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            // 해당 인스타회원이 좋아하는 사람들 목록
            List<LikeablePerson> likeablePeople = instaMember.getFromLikeablePeople();
            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/list";
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public String cancel(@PathVariable Long id) {
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElse(null);

        RsData canDeleteRsData = likeablePersonService.canCancel(rq.getMember(), likeablePerson);

        if (canDeleteRsData.isFail()) return rq.historyBack(canDeleteRsData);

        RsData deleteRsData = likeablePersonService.cancel(likeablePerson);

        if (deleteRsData.isFail()) return rq.historyBack(deleteRsData);

        return rq.redirectWithMsg("/usr/likeablePerson/list", deleteRsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String showModify(@PathVariable Long id, Model model) {
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElseThrow();

        RsData canModifyRsData = likeablePersonService.canModify(rq.getMember(), likeablePerson);

        if (canModifyRsData.isFail()) return rq.historyBack(canModifyRsData);

        model.addAttribute("likeablePerson", likeablePerson);

        return "usr/likeablePerson/modify";
    }

    @AllArgsConstructor
    @Getter
    public static class ModifyForm {
        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(@PathVariable Long id, @Valid ModifyForm modifyForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.modifyAttractive(rq.getMember(), id, modifyForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/toList")
    public String showToList(Model model, String gender, @RequestParam(defaultValue = "0") int attractiveTypeCode, @RequestParam(defaultValue = "1") int sortCode) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            // 해당 인스타회원이 좋아하는 사람들 목록
            Stream<LikeablePerson> likeablePeopleStream = instaMember.getToLikeablePeople().stream();

            if (gender != null) {
                likeablePeopleStream = likeablePeopleStream.filter(person -> person.getFromInstaMember().getGender().equals(gender));
            }

            if (attractiveTypeCode != 0) {
                likeablePeopleStream = likeablePeopleStream.filter(person -> person.getAttractiveTypeCode() == attractiveTypeCode);
            }

            switch (sortCode) {
                case 1: // 최신순(기본) : 최근에 받은 호감표시를 우선적으로 표시
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(LikeablePerson::getCreateDate));
                    break;
                case 2: // 날짜순 : 오래전에 받은 호감표시를 우선적으로 표시
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(LikeablePerson::getCreateDate).reversed());
                    break;
                case 3: // 인기 많은 순 : 인기가 많은 사람의 호감표시를 우선적으로 표시
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparingInt(LikeablePerson::getAttractiveTypeCode));
                    break;
                case 4: // 인기 적은 순 : 인기가 적은 사람의 호감표시를 우선적으로 표시
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparingInt(LikeablePerson::getAttractiveTypeCode).reversed());
                    break;
                case 5: // 성별순(여성 우선순위), 2순위 정렬조건으로 최신순 : 여성에게 받은 호감표시를 먼저 표시하고, 그 다음 남자에게 받은 호감표시를 후에 표시
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(person -> person.getFromInstaMember().getGender().equals("여") ? 0 : 1))
                            .thenComparing(Comparator.comparing(LikeablePerson::getCreateDate)
                            );
                    break;
                case 6: // 호감사유순(우선순위 외모1, 성격2, 능력3), 2순위 정렬조건으로 최신순 : 외모 때문에 받은 호감표시를 먼저 표시하고, 그 다음 성격 때문에 받은 호감표시를 후에 표시, 마지막으로 능력 때문에 받은 호감표시를 후에 표시
                    likeablePeopleStream = likeablePeopleStream.sorted(
                            Comparator.comparingInt(LikeablePerson::getAttractiveTypeCode)
                                    .thenComparing(LikeablePerson::getCreateDate)
                    );
                    break;

            }

            List<LikeablePerson> likeablePeople = likeablePeopleStream.collect(Collectors.toList());

            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/toList";
    }
}