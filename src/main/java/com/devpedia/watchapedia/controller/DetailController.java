package com.devpedia.watchapedia.controller;
import com.devpedia.watchapedia.dto.DetailDto;
import com.devpedia.watchapedia.security.JwtTokenProvider;
import com.devpedia.watchapedia.service.DetailService;
import com.devpedia.watchapedia.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class DetailController {
    private final DetailService detailService;
    private final RedisService redisService;

    /**
     * 상세페이지 정보 조회
     * @param accessToken 로그인되어있는지 체크하기 위한 엑세스 토큰
     * @param contentId 어떤 상세페이지 인지 확인하기 위한 콘텐츠 아이디
     * @return 상세페이지 정보
     */
    @GetMapping("/public/detail")
    public DetailDto.DetailContentInfoList getDetailInfoList(@RequestHeader(name = JwtTokenProvider.ACCESS_TOKEN_HEADER) @NotBlank String accessToken,
                                                             @RequestParam long contentId){
        //Long userId = redisService.getDetailAccessTokenOrThrow(accessToken);
        Long userId = 6838L;
        return detailService.detailInfoList(contentId, userId);
    }

    /**
     * 상세페이지 로그인된 평가 정보 개별 조회
     * @param accessToken
     * @param contentId
     * @return 유저 평가 정보
     */
    @PostMapping("/public/detail/user")
    public DetailDto.DetailUserCommentInfoList detailAccessToken(
            @RequestHeader(name = JwtTokenProvider.ACCESS_TOKEN_HEADER) @NotBlank String accessToken, @RequestParam long contentId) {
        Long userId = redisService.getDetailAccessTokenOrThrow(accessToken);
        DetailDto.DetailUserCommentInfoList detailUserCommentInfoList = null;
        if(userId != null) {
            detailUserCommentInfoList = detailService.searchDetailInfo(userId, contentId);
        }
        return detailUserCommentInfoList;
    }
    /**
     * 출연/제작 상세 페이지 조회
     * @param participantId 출연/배우 아이디 값
     * @return 출연/제작에 포함된 콘텐츠 정보
     */
    @GetMapping("public/detail/participant")
    public List<DetailDto.addDetailParticipantInfoList> getParticipantDetailInfo(@RequestParam long participantId){
        return detailService.searchParticipantDetailInfo(participantId);
    }

    /**
     * 상세페이지 좋아요 추가
     * @param request 컨텐츠 아이디, 좋아요 아이디, 코멘트 아이디
     * @return 1: 성공 0: 실패
     */
    @PostMapping("public/detail/like")
    public Long getDetailLikeInfo(@RequestBody DetailDto.detailCommentLikeInfo request){
        return detailService.insertDetailLike(request);
    }

    /**
     * 상세페이지 좋아요 삭제
     * @param request 컨텐츠 아이디, 좋아요 아이디, 코멘트 아이디
     * @return 1: 성공 0: 실패
     */
    @PostMapping("public/detail/unlike")
    public Long getDetailUnLikeInfo(@RequestBody DetailDto.detailCommentLikeInfo request){
        return detailService.deleteDetailLike(request);
    }

    /**
     * 상세페이지 코멘트 추가
     * @param request Comment 정보 코멘트에 추가 될 파라미터 저장
     */
    @PostMapping("public/detail/commentInsert")
    public void getDetailCommentInsert(@RequestBody DetailDto.detailCommentInsertInfo request){
        detailService.insertDetailComment(request);
    }

    /**
     * 상세페이지 코멘트 삭제
     * @param request 컨텐츠아이디, 유저아이디, 코멘트 상태
     * @return 1: 성공 0: 실패
     */
    @PutMapping("public/detail/commentDelete")
    public Long getDetailCommentDelete(@RequestBody DetailDto.detailCommentDeleteInfo request){
        return detailService.deleteDetailComment(request);
    }

    /**
     * 상세페이지 코멘트 수정
     * @param request 컨텐츠아이디, 유저아이디, 코멘트 상태, 코멘트 내용
     * @return 1: 성공 0: 실패
     */
    @PutMapping("public/detail/commentUpdate")
    public Long getDetailCommentUpdate(@RequestBody DetailDto.detailCommentUpdateInfo request){
        return detailService.updateDetailComment(request);
    }
}
