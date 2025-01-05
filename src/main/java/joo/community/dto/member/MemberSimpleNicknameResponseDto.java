package joo.community.dto.member;

import joo.community.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberSimpleNicknameResponseDto {

    private String name;
    private String nickname;

    public static MemberSimpleNicknameResponseDto toDto(User member) {
        return new MemberSimpleNicknameResponseDto(member.getName(), member.getNickname());
    }
}