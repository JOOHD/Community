package Joo.community.controller.auth;

import Joo.community.dto.sign.LoginRequestDto;
import Joo.community.dto.sign.RegisterDto;
import Joo.community.dto.sign.SignUpRequestDto;
import Joo.community.dto.sign.TokenRequestDto;
import Joo.community.response.Response;
import Joo.community.service.auth.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static Joo.community.response.Response.success; // 전역 변수

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;

    @ApiOperation(value = "회원가입", notes = "회원가입 진행")
    @PostMapping("/signup")
    public Response register(@Valid @RequestBody RegisterDto registerDto) {
        authService.signup(registerDto);

        // 디버깅용 로그
        Response response = Response.success();
        System.out.println("Response: " + response);

        return response;
    }

    @ApiOperation(value = "로그인", notes = "로그인을 한다.")
    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.OK)
    public Response signIn(@Valid @RequestBody LoginRequestDto req) {
        return success(authService.signIn(req));
    }


    @ApiOperation(value = "토큰 재발급", notes = "토큰 재발급 요청")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/reissue")
    public Response reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        return success(authService.reissue(tokenRequestDto));
    }
}
