package com.project.luckybocky.qna.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.luckybocky.common.DataResponseDto;
import com.project.luckybocky.common.ResponseDto;
import com.project.luckybocky.qna.dto.QnaDto;
import com.project.luckybocky.qna.dto.QnaListResDto;
import com.project.luckybocky.qna.dto.QnaUserReqDto;
import com.project.luckybocky.qna.exception.QnaNotFoundException;
import com.project.luckybocky.qna.exception.QnaSaveException;
import com.project.luckybocky.qna.service.QnaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "api/v1/qna", produces = "application/json; charset=UTF8")
@RequiredArgsConstructor
@Tag(name = "qna", description = "QnA 저장/조회 API")
@Slf4j
public class QnaController {
	private final QnaService qnaService;

	@Operation(
		summary = "질문 등록",
		description = "질문을 등록한다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "질문 등록 성공"),
		@ApiResponse(responseCode = "500", description = "서버 문제로 인해 질문 등록에 실패했습니다.",
			content = @Content(schema = @Schema(implementation = QnaSaveException.class))),
	})
	@PostMapping("/question")
	public ResponseEntity<ResponseDto> saveQuestion(@RequestBody QnaUserReqDto question, HttpSession session) {
		qnaService.saveQuestion(question, session);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(new ResponseDto("질문 등록 성공"));
	}

	@Operation(
		summary = "답변 등록",
		description = "질문에 대한 답변을 등록한다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "답변 등록 성공"),
		@ApiResponse(responseCode = "500", description = "서버 문제로 인해 답변 등록에 실패했습니다.",
			content = @Content(schema = @Schema(implementation = QnaSaveException.class))),
	})
	@PutMapping("/answer")
	public ResponseEntity<ResponseDto> saveAnswer(@RequestBody QnaDto answer) {
		qnaService.saveAnswer(answer);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(new ResponseDto("답변 등록 성공"));
	}

	@Operation(
		summary = "QnA 목록 조회",
		description = "저장된 QnA들을 최신순으로 가져온다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "QnA 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "QnA 목록 조회 실패",
			content = @Content(schema = @Schema(implementation = QnaSaveException.class))),
	})
	@GetMapping("/question")
	public ResponseEntity<DataResponseDto<QnaListResDto>> getQuestions(
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "5") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		QnaListResDto qnaListResDto = qnaService.getQuestions(pageable);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(new DataResponseDto<>("질문 목록 조회 완료", qnaListResDto));
	}

	@Operation(
		summary = "사용자 권한 조회",
		description = "QnA 상세 접근을 위한 권한 정보를 조회한다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "QnA 조회 성공"),
		@ApiResponse(responseCode = "404", description = "QnA 조회 실패",
			content = @Content(schema = @Schema(implementation = QnaNotFoundException.class))),
	})
	@GetMapping("/question/{qnaSeq}")
	public ResponseEntity<DataResponseDto<Integer>> checkAccess(@PathVariable(value = "qnaSeq") Integer qnaSeq,
		HttpSession session) {
		Integer checkResult = qnaService.checkAccess(qnaSeq, session);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(new DataResponseDto<>("#" + qnaSeq + " 질문 접근 조회 성공", checkResult));
	}

	@Operation(
		summary = "사용자 QnA 목록 조회",
		description = "사용자가 등록한 QnA들을 최신순으로 가져온다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "사용자 QnA 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "사용자 QnA 목록 조회 실패",
			content = @Content(schema = @Schema(implementation = QnaNotFoundException.class))),
	})
	@GetMapping("/question/me")
	public ResponseEntity<DataResponseDto<QnaListResDto>> getMyQuestions(
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "5") int size, HttpSession session) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		QnaListResDto qnaListResDto = qnaService.getMyQuestions(pageable, session);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(new DataResponseDto<>("사용자 질문 목록 조회 완료", qnaListResDto));
	}
}
