package joo.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import joo.community.controller.message.MessageController;
import joo.community.dto.message.MessageCreateRequest;
import joo.community.service.message.MessageService;
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

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class) // JUnit5, Mockito 사용 위함 & @InjectMocks 활성화
class MessageControllerTest {
    @InjectMocks // 테스트 대상
    MessageController messageController;

    @Mock        // (= Autowired)
    MessageService messageService;
    MockMvc mockMvc; // controller test 에 필요한 HTTP request/response 시뮬레이션 제공
    ObjectMapper objectMapper = new ObjectMapper(); // JSON 역/직렬화 설정

    @BeforeEach
    void beforeEach() { // standaloneSetup() : 단독 컨트롤러 테스트
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
    }

    @Test
    @DisplayName("쪽지 작성")
    void createMessageTest() throws Exception {

        // given
        MessageCreateRequest req = new MessageCreateRequest("타이틀", "내용", "유저닉네임");

        // when
        mockMvc.perform(
                        post("/api/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        verify(messageService).createMessage(req);
    }

    @Test
    @DisplayName("받은 쪽지함 확인")
    void receiveMessagesTest() throws Exception {

        // given

        // when, then
        mockMvc.perform(
                        get("/api/messages/receiver"))
                .andExpect(status().isOk());
        verify(messageService).receiveMessages();
    }

    @Test
    @DisplayName("받은 쪽지 개별 확인")
    void receiveMessageTest() throws Exception {

        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(
                        get("/api/messages/receiver/{id}", id))
                .andExpect(status().isOk());
        verify(messageService).receiveMessage(id);
    }

    @Test
    @DisplayName("보낸 쪽지함 확인")
    void sendMessagesTest() throws Exception {
        // given

        // when, then
        mockMvc.perform(
                        get("/api/messages/sender"))
                .andExpect(status().isOk());
        verify(messageService).sendMessages();
    }

    @Test
    @DisplayName("보낸 쪽지 개별 확인")
    void sendMessageTest() throws Exception {
        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(
                        get("/api/messages/sender/{id}", id))
                .andExpect(status().isOk());
        verify(messageService).sendMessage(id);
    }

    @Test
    @DisplayName("받은 쪽지 삭제")
    void deleteReceiveMessageTest() throws Exception {
        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(
                        delete("/api/messages/receiver/{id}", id))
                .andExpect(status().isOk());
        verify(messageService).deleteMessageByReceiver(id);
    }

    @Test
    @DisplayName("보낸 쪽지 삭제")
    void deleteSenderMessageTest() throws Exception {
        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(
                        delete("/api/messages/sender/{id}", id))
                .andExpect(status().isOk());
        verify(messageService).deleteMessageBySender(id);
    }
}
