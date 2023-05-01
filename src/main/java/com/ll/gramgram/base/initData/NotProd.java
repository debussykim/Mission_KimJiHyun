package com.ll.gramgram.base.initData;

import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "test"})
public class NotProd {
    @Bean
    CommandLineRunner initData(
            MemberService memberService,
            InstaMemberService instaMemberService,
            LikeablePersonService likeablePersonService
    ) {
        return args -> {
            Member memberAdmin = memberService.join("admin", "1234").getData();
            Member memberUser1 = memberService.join("user1", "1234").getData();
            Member memberUser2 = memberService.join("user2", "1234").getData();
            Member memberUser3 = memberService.join("user3", "1234").getData();
            Member memberUser4 = memberService.join("user4", "1234").getData();

            Member memberUser5ByKakao = memberService.whenSocialLogin("KAKAO", "KAKAO__2733199372").getData();
            Member memberUser6ByGoogle = memberService.whenSocialLogin("GOOGLE", "GOOGLE__114103318057832179665").getData();
            Member memberUser7ByNaver = memberService.whenSocialLogin("NAVER", "NAVER__sZnEOYo8SmI_Gm34DgdvNqXthiAHIC48CEWrwAD64iU").getData();


            instaMemberService.connect(memberUser2, "insta_user2", "M");
            instaMemberService.connect(memberUser3, "insta_user3", "W");
            instaMemberService.connect(memberUser4, "insta_user4", "M");

            likeablePersonService.like(memberUser3, "insta_user4", 1);
            likeablePersonService.like(memberUser3, "insta_user100", 2);

            // 호감표시 10명 제한 테스트를 위해 추가
            likeablePersonService.like(memberUser3, "insta_user5", 2);
            likeablePersonService.like(memberUser3, "insta_user6", 2);
            likeablePersonService.like(memberUser3, "insta_user7", 2);
            likeablePersonService.like(memberUser3, "insta_user8", 2);
            likeablePersonService.like(memberUser3, "insta_user9", 2);
            likeablePersonService.like(memberUser3, "insta_user10", 2);
            likeablePersonService.like(memberUser3, "insta_user11", 2);
            likeablePersonService.like(memberUser3, "insta_user12", 2);
        };
    }
}
