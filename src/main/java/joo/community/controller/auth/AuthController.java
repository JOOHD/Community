package joo.community.controller.auth;

import joo.community.dto.sign.LoginRequestDto;
import joo.community.dto.sign.RegisterDto;
import joo.community.dto.sign.SignInResponseDto;
import joo.community.dto.sign.TokenRequestDto;
import joo.community.entity.user.User;
import joo.community.repository.user.UserRepository;
import joo.community.service.auth.AuthService;
import io.swagger.annotations.ApiOperation;
import joo.community.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static joo.community.response.Response.success;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @ApiOperation(value = "회원가입", notes = "회원가입 진행")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public Response register(@Valid @RequestBody RegisterDto registerDto) {
        log.info("Received signup request: {}", registerDto);
        authService.signup(registerDto);
        return success();
    }

    @ApiOperation(value = "로그인", notes = "로그인을 한다.")
    @PostMapping("/signIn")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "*") // 모든 출에서 요청을 허용
    public Response signIn(@Valid @RequestBody LoginRequestDto req) {
        log.info("Received login request: {}", req);  // 요청 객체 로그 찍기
        Object result = authService.signIn(req);
        log.info("SignIn result: {}", result);  // 로그인 결과 로그 찍기
        return success(result);
    }
//    public Object signIn(LoginRequestDto req) {
//        User user = userRepository.findByUsername(req.getUsername())
//                .orElseThrow(() -> {
//                    log.info("Username not found for: " + req.getUsername());
//                    return new UsernameNotFoundException("username not found");
//                });
//        if (user != null && passwordEncoder.matches(req.getPassword(), user.getPassword())) {
//            log.info("Login successful for: " + req.getUsername());
//            return new SignInResponseDto(user.getId(), user.getUsername(), "tokenValue");
//        }
//        log.info("Invalid login attempt for: " + req.getUsername());
//        return null;
//    }

    @ApiOperation(value = "토큰 재발급", notes = "토큰 재발급 요청")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/reissue")
    public Response reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        return success(authService.reissue(tokenRequestDto));
    }

}
