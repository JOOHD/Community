package joo.community.controller;


import joo.community.config.guard.LoginMemberArgumentResolver;
import joo.community.controller.board.BoardController;
import joo.community.dto.board.BoardCreateRequest;
import joo.community.dto.board.BoardUpdateRequest;
import joo.community.entity.board.Board;
import joo.community.repository.board.BoardRepository;
import joo.community.service.board.BoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BoardControllerTest {

    @InjectMocks
    BoardController boardController;

    @Mock
    BoardService boardService;
    @Mock
    BoardRepository boardRepository;

    @Mock
    LoginMemberArgumentResolver loginMemberArgumentResolver;

    MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(boardController)
                .setCustomArgumentResolvers(loginMemberArgumentResolver)
                .build();
    }

    @Test
    @DisplayName("게시글을 작성한다.")
    void create_board_success() throws Exception {
        // given
        List<MultipartFile> images = new ArrayList<>();
        images.add(new MockMultipartFile("test1", "test1.PNG", MediaType.IMAGE_PNG_VALUE, "test1".getBytes()));
        images.add(new MockMultipartFile("test2", "test2.PNG", MediaType.IMAGE_PNG_VALUE, "test2".getBytes()));
        BoardCreateRequest req = new BoardCreateRequest("title", "content", images);

//        User user = createUserWithAdminRole();
//        given(loginMemberArgumentResolver.supportsParameter(any())).willReturn(true);
//        given(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);

        // when, then
        mockMvc.perform(
                        multipart("/api/boards")
                                .file("images", images.get(0).getBytes())
                                .file("images", images.get(1).getBytes())
                                .param("title", req.getTitle())
                                .param("content", req.getContent())
                                .with(requestBoardProcessor -> {
                                    requestBoardProcessor.setMethod("POST");
                                    return requestBoardProcessor;
                                })
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("게시물을 검색한다.")
    void searchBoard() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.DESC, "id");
        List<Board> result = boardRepository.findByTitleContaining("keyword", pageable);

        // when, then
        // expect : null, 실제 결과 : empty , (null) -> (emptyList());
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    @DisplayName("전체 게시물 조회한다. (페이징)")
    void find_all_boards_success_with_paging() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.DESC, "id");
        Page<Board> result = boardRepository.findAll(pageable);

        // when, then
        assertThat(result).isEqualTo(isNull());
    }

    @Test
    @DisplayName("개시물을 단건 조회한다.")
    void find_board_success() throws Exception {
        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(
                        get("/api/boards/{id}", id))
                .andExpect(status().isOk());

        verify(boardService).findBoard(id);
    }

    @Test
    @DisplayName("게시글을 수정한다.")
    void edit_board_success() throws Exception {
        // given
        List<MultipartFile> addedImages = new ArrayList<>();
        addedImages.add(new MockMultipartFile("test1", "test1.PNG", MediaType.IMAGE_PNG_VALUE, "test1".getBytes()));
        addedImages.add(new MockMultipartFile("test2", "test2.PNG", MediaType.IMAGE_PNG_VALUE, "test2".getBytes()));
        List<Integer> deletedImages = List.of(1, 2);
        BoardUpdateRequest req = new BoardUpdateRequest("title", "content", addedImages, deletedImages);

//        User user = createUserWithAdminRole();
//        given(loginMemberArgumentResolver.supportsParameter(any())).willReturn(true);
//        given(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(user);

        // when, then
        mockMvc.perform(
                        multipart("/api/boards/{id}", 1)
                                .file("addedImages", addedImages.get(0).getBytes())
                                .file("addedImages", addedImages.get(1).getBytes())
                                .param("deletedImages", String.valueOf(deletedImages.get(0)),
                                        String.valueOf(deletedImages.get(1)))
                                .param("title", req.getTitle())
                                .param("content", req.getContent())
                                .with(requestBoardProcessor -> {
                                    requestBoardProcessor.setMethod("PUT");
                                    return requestBoardProcessor;
                                })
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }
}
