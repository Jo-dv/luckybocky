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
import com.project.luckybocky.qna.service.QnaService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
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

	@RateLimiter(name = "saveRateLimiter")
	@PostMapping("/question")
	public ResponseEntity<ResponseDto> saveQuestion(@RequestBody QnaUserReqDto question, HttpSession session) {
		qnaService.saveQuestion(question, session);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(new ResponseDto("질문 등록 성공"));
	}

	@RateLimiter(name = "saveRateLimiter")
	@PutMapping("/answer")
	public ResponseEntity<ResponseDto> saveAnswer(@RequestBody QnaDto answer) {
		qnaService.saveAnswer(answer);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(new ResponseDto("답변 등록 성공"));
	}

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

	@GetMapping("/question/{qnaSeq}")
	public ResponseEntity<DataResponseDto<Integer>> checkAccess(@PathVariable(value = "qnaSeq") Integer qnaSeq,
		HttpSession session) {
		Integer checkResult = qnaService.checkAccess(qnaSeq, session);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(new DataResponseDto<>("#" + qnaSeq + " 질문 접근 조회 성공", checkResult));
	}

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
