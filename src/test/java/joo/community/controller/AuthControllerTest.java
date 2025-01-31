package joo.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import joo.community.controller.auth.AuthController;
import joo.community.service.auth.AuthService;
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
import joo.community.dto.sign.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    /*
        perform : MockMvc 사용하여 HTTP 요청을 시뮬레이션하는 메서드
                    ex) get/post("api/messages/receiver/{id}", id)
                    
        andExpect : 요청 결과를 검증하는 메서드
                    - perform 으로 요청한 결과의 상태 코드, 응답 데이터, 헤더 등을 확인
                    - 예상된 결과와 실제 결과가 일치하는지 비교
                    ex) .andExpect(status().isOk()); // 응답 코드 200(OK)인지 확인

        verify : Mockito 메서드 호출 검증 도구
                 - Mock 객체에서 특정 메서드가 호출되었는지 확인
                 - 호출 횟수와 전달된 매개변수까지 검증
                 ex) verify(messageService).receiveMessage(id); // 검증
                     verify(messageService).times(1), never().. // 횟수, 호출되지 않았는지

     */

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
        SignUpRequestDto req = new SignUpRequestDto("test123", "test", "username", "nickname");

        // when, then
        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        verify(authService).signUp(req);
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
        SignUpRequestDto req = new SignUpRequestDto("test123", "test", "username", "nickname");

        // when, then
        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").doesNotExist());

    }
}
