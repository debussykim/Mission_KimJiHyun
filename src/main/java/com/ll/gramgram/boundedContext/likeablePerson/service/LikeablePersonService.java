package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        if ( member.hasConnectedInstaMember() == false ) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }


        List<LikeablePerson> fromLikeablePeopleList = member.getInstaMember().getFromLikeablePeople();
        if (fromLikeablePeopleList.size() >= 10) {
            return RsData.of("F-3", "호감상대는 최대 10명까지 등록 가능합니다.");
        }

        InstaMember fromInstaMember = member.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

        Optional<LikeablePerson> existingLikeablePerson = fromInstaMember.getFromLikeablePeople().stream()
                .filter(lp -> lp.getToInstaMember().getUsername().equals(username))
                .findFirst();

        LikeablePerson oldLikeablePerson = likeablePersonRepository.findByFromInstaMemberIdAndToInstaMember_username(member.getInstaMember().getId(), username);
        if (oldLikeablePerson != null) {
            oldLikeablePerson.setAttractiveTypeCode(attractiveTypeCode);
            likeablePersonRepository.save(oldLikeablePerson);

            return RsData.of("S-2", "%s에 대한 호감표시가 수정되었습니다.".formatted(username), oldLikeablePerson);
        }


        LikeablePerson likeablePerson;
        if (existingLikeablePerson.isPresent()) {
            // Update the existing likeable person with the new attractiveTypeCode value
            likeablePerson = existingLikeablePerson.get();
            likeablePerson.setAttractiveTypeCode(attractiveTypeCode);
        } else {
            likeablePerson = LikeablePerson
                    .builder()
                    .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                    .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                    .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                    .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                    .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                    .build();
            fromInstaMember.addFromLikeablePerson(likeablePerson);
            toInstaMember.addToLikeablePerson(likeablePerson);
        }

        likeablePersonRepository.save(likeablePerson); // 저장

        if (!fromLikeablePeopleList.contains(likeablePerson)) {
            return RsData.of("F-4", "Failed to add the likeable person to the user's list of likeable people.");
        }

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {

        String toInstaMemberUsername = likeablePerson.getToInstaMember().getUsername();
        likeablePersonRepository.delete(likeablePerson);

        return RsData.of("S-1", "%s에 대한 호감을 취소하였습니다.".formatted(toInstaMemberUsername));
    }

    @Transactional
    public RsData canActorDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        if (!Objects.equals(actor.getInstaMember().getId(), likeablePerson.getFromInstaMember().getId()))
            return RsData.of("F-2", "권한이 없습니다.");

        return RsData.of("S-1", "삭제가능합니다.");
    }

}
