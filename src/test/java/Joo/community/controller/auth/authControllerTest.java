package Joo.community.controller.auth;

import Joo.community.dto.sign.LoginRequestDto;
import Joo.community.dto.sign.RegisterDto;
import Joo.community.dto.sign.TokenResponseDto;
import Joo.community.service.auth.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    AuthController authController;
    @Mock
    AuthService authService;
    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @DisplayName("회원가입 테스트")
    @Test
    void signUpTest() throws Exception {
        // given
        RegisterDto req = new RegisterDto("test123", "test", "username", "nickname");

        // when, then
        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        verify(authService).signup(req);
    }

    @DisplayName("로그인 테스트")
    @Test
    void signInTest() throws Exception {
        // given
        LoginRequestDto req = new LoginRequestDto("test123", "test");
        given(authService.signIn(req)).willReturn(new TokenResponseDto("access", "refresh"));

        // when, then
        mockMvc.perform(
                        post("/auth/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.accessToken").value("access"))
                .andExpect(jsonPath("$.result.data.refreshToken").value("refresh"));

        verify(authService).signIn(req);
    }

    @DisplayName("응답 JSON 테스트")
    @Test
    void ignoreNullValueInJsonResponseTest() throws Exception {
        // 응답결과로 반환되는 JSON 문자열이 올바르게 제거되는지 검증
        // given
        RegisterDto req = new RegisterDto("test123", "test", "username", "nickname");

        // when, then
        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").doesNotExist());

    }

}
