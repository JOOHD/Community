package joo.community.controller.user;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import joo.community.dto.user.UserDto;
import joo.community.response.Response;
import joo.community.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Api(value = "User Controller", tags = "User")
@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class UserController {

    /*
     * @ResponseStatus 로 상태코드를 먼저 내려주는 이유
       ㄴ 컨트롤러 정상적 실행 시, 지정해준 상태코드 리턴.
          ㄴ 실패 시, Service 단에서 처리.
             ㄴ Service 내부에서 Exception -> Advice 지정한 ErrorCode, msg 출력
     */

    private final UserService userService;

    @ApiOperation(value = "전체 회원 조회", notes = "전체 회원을 조회")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users")
    public Response findAllUsers() {
        return Response.success(userService.findAllUsers());
    }

    @ApiOperation(value = "개별 회원 조회", notes = "개별 회원을 조회")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/{id}")
    public Response findUser(@ApiParam(value = "User ID", required = true) @PathVariable int id) {
        return Response.success(userService.findUser(id));
    }

    @ApiOperation(value = "회원 정보 수정", notes = "회원의 정보를 수정")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/users/{id}")
    public Response editUserInfo(@ApiParam(value = "User ID", required = true) @PathVariable int id, @RequestBody UserDto userDto) {
        return Response.success(userService.editUserInfo(id, userDto));
    }

    @ApiOperation(value = "회원 탈퇴", notes = "회원을 탈퇴 시킴")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/users/{id}")
    public Response deleteUserInfo(@ApiParam(value = "User ID", required = true) @PathVariable int id) {
        userService.deleteUserInfo(id);
        return Response.success();
    }
}
