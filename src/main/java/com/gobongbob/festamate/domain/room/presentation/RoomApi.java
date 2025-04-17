package com.gobongbob.festamate.domain.room.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.room.dto.request.FilteringCondition;
import com.gobongbob.festamate.domain.room.dto.request.RoomCreateRequest;
import com.gobongbob.festamate.domain.room.dto.request.RoomUpdateRequest;
import com.gobongbob.festamate.domain.room.dto.response.IsMemberHostResponse;
import com.gobongbob.festamate.domain.room.dto.response.RoomListResponse;
import com.gobongbob.festamate.domain.room.dto.response.RoomResponse;
import com.gobongbob.festamate.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Room", description = "방 관련 API")
public interface RoomApi {

    @Operation(summary = "모임방 생성", description = "새로운 모임방을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("")
    SuccessResponse<Void> create(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(description = "방 생성 요청 정보")
            @RequestPart("request") @Valid RoomCreateRequest request,
            @Parameter(description = "방 이미지")
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> multipartFiles
    );

    @Operation(summary = "필터에 따른 모임방 조회", description = """
            필터에 따라 방 목록을 무한 스크롤 방식으로 조회합니다. gender 및 status로 전달할 수 있는 값은 다음과 같습니다.
            <br><br>gender: [MALE, FEMALE] (남성, 여성)
            <br>status: [MATCHING, MATCHED, CLOSED] (매칭중, 매칭 완료, 모임 종료)
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @GetMapping("")
    SuccessResponse<Slice<RoomListResponse>> findBySearchCondition(
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "필터링 정보")
            @ModelAttribute FilteringCondition filteringCondition
    );

    @Operation(summary = "참여 중인 모임방 조회", description = "사용자가 참여 중인 방 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @GetMapping("/participations")
    SuccessResponse<List<RoomListResponse>> findParticipatingRooms(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    );

    @Operation(summary = "모임방 상세 조회", description = "모임방 ID로 방 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "모임방이 존재하지 않습니다.")
    })
    @GetMapping("/{roomId}")
    SuccessResponse<RoomResponse> findRoomById(
            @Parameter(name = "roomId", description = "모임방 ID") @PathVariable("roomId") Long roomId
    );

    @Operation(summary = "모임방 정보 수정", description = """
            모임방 정보를 수정합니다. gender로 전달할 수 있는 값은 다음과 같습니다.
            <br><br>gender: [MALE, FEMALE] (남성, 여성)
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "모임방이 존재하지 않습니다.")
    })
    @PatchMapping("/{roomId}")
    SuccessResponse<Void> updateById(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "roomId", description = "모임방 ID") @PathVariable("roomId") Long roomId,
            @Parameter(description = "모임방 수정 요청 정보")
            @RequestBody @Valid RoomUpdateRequest request
    );

    @Operation(summary = "모임방 삭제", description = "모임방을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "모임방이 존재하지 않습니다.")
    })
    @DeleteMapping("/{roomId}")
    SuccessResponse<Void> deleteById(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "roomId", description = "모임방 ID") @PathVariable("roomId") Long roomId
    );

    @Operation(summary = "방 참여", description = "방에 참여합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "모임방이 존재하지 않습니다.")
    })
    @PostMapping("/{roomId}/participations")
    SuccessResponse<Void> participateAlone(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "roomId", description = "모임방 ID") @PathVariable("roomId") Long roomId
    );

    @Operation(summary = "모임방 나가기", description = "모임방에서 나갑니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "모임방이 존재하지 않습니다.")
    })
    @PostMapping("/{roomId}/leave")
    SuccessResponse<Void> leave(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "roomId", description = "모임방 ID") @PathVariable("roomId") Long roomId
    );

    @Operation(summary = "방장 여부 확인", description = "사용자가 방장인지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "참여중인 모임방이 존재하지 않습니다.")
    })
    @GetMapping("/{roomId}/host")
    SuccessResponse<IsMemberHostResponse> isMemberHost(
            @Parameter(name = "roomId", description = "모임방 ID") @PathVariable("roomId") Long roomId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    );
}
