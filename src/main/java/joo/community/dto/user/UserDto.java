package joo.community.dto.user;

import joo.community.entity.user.User;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto {

    private int id;
    private String username;
    private String name;
    private String nickname;

    // 'Cannot be accessed from outside package' 에러 발생으로 인한, 생성자 추가.
    // 명시적으로 public 생성자 추가 후 디버깅 완료
    public UserDto(int id, String username, String name, String nickname) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.nickname = nickname;
    }

    // User 객체를 UserDto로 변환하는 메서드
    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .nickname(user.getNickname())
                .build();
    }
}
