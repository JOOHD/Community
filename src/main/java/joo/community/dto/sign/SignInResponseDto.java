package joo.community.dto.sign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignInResponseDto {
    private Long id;      // 사용자 ID
    private String username;  // 사용자 이름
    private String token;     // JWT 토큰 (또는 세션 토큰 등)
    private String message;   // 로그인 성공 메시지 (선택 사항)

    // 생성자 추가
    public SignInResponseDto(Long id, String username, String token) {
        this.id = id;
        this.username = username;
        this.token = token;
    }
}