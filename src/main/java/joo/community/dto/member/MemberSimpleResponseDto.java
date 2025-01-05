package joo.community.dto.member;

import joo.community.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberSimpleResponseDto {

    private String username;
    private String name;

    public static MemberSimpleResponseDto toDto(User member) {
        return new MemberSimpleResponseDto(member.getUsername(), member.getName());
    }
}