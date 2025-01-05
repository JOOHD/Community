package joo.community.dto.member;

import joo.community.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberEditRequestDto {

    private String name;
    private String nickname;

    public static MemberEditRequestDto toDto(User member) {
        return new MemberEditRequestDto(member.getName(), member.getNickname());
    }
}