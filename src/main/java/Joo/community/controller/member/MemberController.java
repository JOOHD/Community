package Joo.community.controller.member;

import Joo.community.domain.member.Member;
import Joo.community.dto.member.MemberEditRequestDto;
import Joo.community.dto.sign.TokenRequestDto;
import Joo.community.response.Response;
import Joo.community.service.member.MemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Api(value = "User Controller", tags = "User")
@RequestMapping("/api")
@RestController
public class MemberController {

    /**
     *  1. @ResponseStatus 로 상태코드를 먼저 내려주는 이유
        ㄴ 컨트롤러 실행 시, 지정해준 상태코드 리턴 -> 실패 시, 에러코드 리턴 -> Service 에서 처리.
     */

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @ApiOperation(value = "전체 회원 조회", notes = "전체 회원을 조회")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users")
    public Response findAllMembers() {
        return Response.success(memberService.findAllMembers());
    }

    @ApiOperation(value = "개별 회원 조회", notes = "개별 회원을 조회")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/{id}")
    public Response findMember(@ApiParam(value = "User ID", required = true) @PathVariable final Long id) {
        return Response.success(memberService.findMember(id));
    }

    @ApiOperation(value = "회원 정보 수정", notes = "회원의 정보를 수정")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/users")
    public Response editMemberInfo(@RequestBody final MemberEditRequestDto memberEditRequestDto, Member member, TokenRequestDto tokenRequestDto) {
        return Response.success(memberService.editMemberInfo(member, memberEditRequestDto, tokenRequestDto));
    }

    @ApiOperation(value = "회원 탈퇴", notes = "회원을 탈퇴 시킴")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/users")
    public Response deleteMemberInfo(Member member) {
        memberService.deleteMemberInfo(member);
        return Response.success();
    }

//    @ApiOperation(value = "즐겨찾기 한 글 조회", notes = "유저가 즐겨찾기 한 게시글들 조회")
//    @ResponseStatus(HttpStatus.OK)
//    @GetMapping("/users/favorites")
//    public Response findFavorites(Member member) {
//        return Response.success(memberService.findFavorites(member));
//    }
}


