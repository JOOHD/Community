package joo.community.controller.auth;

import joo.community.dto.sign.LoginRequestDto;
import joo.community.dto.sign.SignUpRequestDto;
import joo.community.dto.sign.TokenRequestDto;
import io.swagger.annotations.ApiOperation;
import joo.community.response.Response;
import joo.community.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static joo.community.response.Response.success;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @ApiOperation(value = "회원가입", notes = "회원가입 진행")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signUp")
    public Response register(@Valid @RequestBody SignUpRequestDto req) {
        return success(authService.signUp(req));
    }

    @ApiOperation(value = "로그인", notes = "로그인을 한다.")
    @PostMapping("/signIn")
    @ResponseStatus(HttpStatus.OK)
    public Response signIn(@Valid @RequestBody final LoginRequestDto req) {
        return success(authService.signIn(req));
    }

    @ApiOperation(value = "토큰 재발급", notes = "토큰 재발급 요청")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/reissue")
    public Response reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        return success(authService.reissue(tokenRequestDto));
    }

}
