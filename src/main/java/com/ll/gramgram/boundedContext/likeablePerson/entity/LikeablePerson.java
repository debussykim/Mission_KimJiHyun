package com.ll.gramgram.boundedContext.likeablePerson.entity;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.baseEntity.BaseEntity;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.standard.util.Ut;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class LikeablePerson extends BaseEntity {
    private LocalDateTime modifyUnlockDate;

    @ManyToOne
    @ToString.Exclude
    private InstaMember fromInstaMember; // 호감을 표시한 사람(인스타 멤버)
    private String fromInstaMemberUsername; // 혹시 몰라서 기록

    @ManyToOne
    @ToString.Exclude
    private InstaMember toInstaMember; // 호감을 받은 사람(인스타 멤버)
    private String toInstaMemberUsername; // 혹시 몰라서 기록

    private int attractiveTypeCode; // 매력포인트(1=외모, 2=성격, 3=능력)

    public boolean isModifyUnlocked() {
        return modifyUnlockDate.isBefore(LocalDateTime.now());
    }

    public long getModifyUnlockSecondsLeft() {
        return Duration.between(LocalDateTime.now(), modifyUnlockDate).getSeconds();
    }

    public String getModifyUnlockDateRemainStrHuman() {
        long secondsLeft = getModifyUnlockSecondsLeft();
        long hours = secondsLeft / 3600;
        long minutes = (secondsLeft % 3600) / 60;

        // 현재 분에 30초 이상이 남아 있으면 분을 1로 반올림

        if (secondsLeft % 60 >= 30) {
            minutes++;
        }
        // 분 수가 60보다 크거나 같으면 초과된 분을 시간으로 변환
        if (minutes >= 60) {
            hours += minutes / 60;
            minutes %= 60;
        }

        return String.format("%d시간 %d분", hours, minutes);
    }

    public RsData updateAttractionTypeCode(int attractiveTypeCode) {
        if (this.attractiveTypeCode == attractiveTypeCode) {
            return RsData.of("F-1", "이미 설정되었습니다.");
        }

        this.attractiveTypeCode = attractiveTypeCode;
        this.modifyUnlockDate = AppConfig.genLikeablePersonModifyUnlockDate();

        return RsData.of("S-1", "성공");
    }

    public String getAttractiveTypeDisplayName() {
        return switch (attractiveTypeCode) {
            case 1 -> "외모";
            case 2 -> "성격";
            default -> "능력";
        };
    }

    public String getAttractiveTypeDisplayNameWithIcon() {
        return switch (attractiveTypeCode) {
            case 1 -> "<i class=\"fa-solid fa-person-rays\"></i>";
            case 2 -> "<i class=\"fa-regular fa-face-smile\"></i>";
            default -> "<i class=\"fa-solid fa-people-roof\"></i>";
        } + "&nbsp;" + getAttractiveTypeDisplayName();
    }

    public String getJdenticon() {
        return Ut.hash.sha256(fromInstaMember.getId() + "_likes_" + toInstaMember.getId());
    }
}