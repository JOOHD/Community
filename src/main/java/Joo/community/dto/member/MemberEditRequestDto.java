package Joo.community.dto.member;

import Joo.community.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberEditRequestDto {

    private String name;
    private String nickname;

    public static MemberEditRequestDto toDto(Member member) {
        return new MemberEditRequestDto(member.getName(), member.getNickname());
    }
}