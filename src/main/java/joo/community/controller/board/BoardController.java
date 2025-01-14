package joo.community.controller.board;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import joo.community.dto.board.BoardCreateRequest;
import joo.community.dto.board.BoardUpdateRequest;
import joo.community.response.Response;
import joo.community.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "Board Controller", tags = "Board")
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api")
public class BoardController {

    private final BoardService boardService;

    /*
        게시글 생성에서는 form data 로 받기 때문에 (image 때문)
        기존 JSON 으로 데이터를 받을 수 있는 방식인 @RequestBody 사용 안 함.
     */
    @ApiOperation(value = "게시글 생성", notes = "게시글을 작성합니다.")
    @PostMapping("/boards")
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Valid @ModelAttribute BoardCreateRequest req) {
        return Response.success(boardService.create(req));
    }

    // DESC 사용 이유 : 최신순으로 보기 위해서
    @ApiOperation(value = "게시글 목록 조회", notes = "게시글 목록을 조회합니다.")
    @GetMapping("/boards")
    @ResponseStatus(HttpStatus.OK)
    public Response findAllBoards(@PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return Response.success(boardService.findAllBoards(pageable));
    }

    @ApiOperation(value = "게시글 단건 조회", notes = "게시글을 단건 조회합니다.")
    @GetMapping("/boards/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response findBoard(@ApiParam(value = "게시글 id", required = true) @PathVariable Long id) {
        return Response.success(boardService.findBoard(id));
    }

    /*
        @ModelAttribute Validation 조건 위반 시, BindException 발생,
        BindException 이 MethodArgumentNotValidException 의 상위 클래스라서 둘 다 처리 가능.
    */
    @ApiOperation(value = "게시글 수정", notes = "게시글을 수정합니다.")
    @PutMapping("/boards/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response editBoard(@PathVariable Long id, @Valid @ModelAttribute BoardUpdateRequest req) {
        return Response.success(boardService.editBoard(id, req));
    }

    @ApiOperation(value = "게시글 삭제", notes = "게시글을 삭제합니다.")
    @DeleteMapping("/boards/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return Response.success();
    }

    @ApiOperation(value = "게시글 검색", notes = "게시글을 검색합니다.")
    @GetMapping("/boards/search")
    @ResponseStatus(HttpStatus.OK)
    public Response search(String keyword, @PageableDefault(size = 5, sort.direction = Sort.Direction.DESC) Pageable pageable) {
        return Response.success(boardService.search(keyword, pageable));
    }
}
