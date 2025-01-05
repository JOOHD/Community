package joo.community.service.user;

import joo.community.dto.user.UserDto;
import joo.community.entity.user.Authority;
import joo.community.entity.user.User;
import joo.community.exception.MemberNotEqualsException;
import joo.community.exception.MemberNotFoundException;
import joo.community.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    /*
     * Service 클래스 목표
     * 1. 권한처리 및 기능 수행
     * 2. 오류시 exception return
     */

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Object findAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(UserDto.toDto(user)); // entity -> dto
        }
        return userDtos; // = entity -> dto 모음
    }

    @Transactional(readOnly = true)
    public UserDto findUser(int id) {
        return UserDto.toDto(userRepository.findById(id).orElseThrow(MemberNotFoundException::new));
    }

    @Transactional
    public UserDto editUserInfo(int id, UserDto updateInfo) {
        User user = userRepository.findById(id).orElseThrow(MemberNotFoundException::new);


        // 권한 처리
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.getName().equals(user.getUsername())) {
            throw new MemberNotEqualsException();
        } else {
            user.setNickname(updateInfo.getNickname());
            user.setName(updateInfo.getName());
            return UserDto.toDto(user);
        }
    }

    @Transactional
    public void deleteUserInfo(int id) { // user 탈퇴
        User user = userRepository.findById(id).orElseThrow(MemberNotFoundException::new);

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 로그인한 사용자 또는 관리자일 경우 삭제 처리
        if (authentication.getName().equals(user.getUsername())
                || authentication.getAuthorities().stream().anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals(Authority.ROLE_ADMIN.name()))) {
            userRepository.deleteById(id);
        } else {
            throw new MemberNotEqualsException();
        }
    }
}
