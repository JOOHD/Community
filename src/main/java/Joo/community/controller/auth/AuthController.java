package Joo.community.controller.auth;

import Joo.community.dto.sign.LoginRequestDto;
import Joo.community.dto.sign.SignUpRequestDto;
import Joo.community.dto.sign.TokenRequestDto;
import Joo.community.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static Joo.community.response.Response.success; // 전역 변수
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;
    
    @ApiOperation(value = "회원가입", notes = "회원가입 진행")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public Response register(@Valid @RequestBody final SignUpRequestDto signUpRequestDto) {
        authService.signup(signUpRequestDto);
        return success();
    }

    @ApiOperation(value = "로그인", notes = "로그인을 한다.")
    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.OK)
    public Response signIn(@Valid @RequestBody final LoginRequestDto req) {
        return success(authService.signin(req));
    }

    @ApiOperation(value = "토큰 재발급", notes = "토큰 재발급 요청")
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/reissue")
    public Response reissue(@RequestBody final TokenRequestDto tokenRequestDto) {
        return success(authService.reissue(tokenRequestDto));
    }
}
