package Joo.community.dto.member;

import Joo.community.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberSimpleNicknameResponseDto {

    private String name;
    private String nickname;

    public static MemberSimpleNicknameResponseDto toDto(Member member) {
        return new MemberSimpleNicknameResponseDto(member.getName(), member.getNickname());
    }
}