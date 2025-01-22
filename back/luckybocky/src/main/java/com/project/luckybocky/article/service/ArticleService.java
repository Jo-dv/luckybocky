package com.project.luckybocky.article.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.luckybocky.article.dto.ArticleResponseDto;
import com.project.luckybocky.article.dto.ArticleSummaryDto;
import com.project.luckybocky.article.dto.CommentDto;
import com.project.luckybocky.article.dto.WriteArticleDto;
import com.project.luckybocky.article.entity.Article;
import com.project.luckybocky.article.exception.ArticleNotFoundException;
import com.project.luckybocky.article.exception.CommentConflictException;
import com.project.luckybocky.article.repository.ArticleRepository;
import com.project.luckybocky.fortune.entity.Fortune;
import com.project.luckybocky.fortune.exception.FortuneNotFoundException;
import com.project.luckybocky.fortune.repository.FortuneRepository;
import com.project.luckybocky.pocket.entity.Pocket;
import com.project.luckybocky.pocket.exception.PocketNotFoundException;
import com.project.luckybocky.pocket.repository.PocketRepository;
import com.project.luckybocky.push.dto.PushDto;
import com.project.luckybocky.push.enums.PushMessage;
import com.project.luckybocky.push.service.PushService;
import com.project.luckybocky.user.entity.User;
import com.project.luckybocky.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ArticleService {
	private final PushService pushService;

	private final ArticleRepository articleRepository;
	private final PocketRepository pocketRepository;
	private final UserRepository userRepository;
	private final FortuneRepository fortuneRepository;

	public ArticleResponseDto getArticleDetails(String userKey, int articleSeq) {
		Article article = articleRepository.findByArticleSeq(articleSeq)
			.orElseThrow(() -> new ArticleNotFoundException());

		ArticleResponseDto response = article.toArticleResponseDto();

		String pocketOwnerKey = article.getPocket().getUser().getUserKey();
		String articleOwnerKey = article.getUser().getUserKey();

		if (userKey == null ||
			(!userKey.equals(pocketOwnerKey) && !userKey.equals(articleOwnerKey))) {    // 복주머니,게시글의 주인이 아닐경우
			// 비공개 복주머니나 비공개 글인 경우 -> 전부 비공개로 설정
			if (!article.getPocket().getUser().isFortuneVisibility() || !article.isArticleVisibility()) {
				response.setArticleVisibility(false);
				response.setArticleContent("비밀글입니다.");
				response.setArticleComment("비밀글입니다.");
			}
		}
		//복주머니,게시글 주인의 경우 -> 비밀글 설정을 다 공개로 바꿈
		else {
			response.setArticleVisibility(true);
		}

		return response;
	}

	// 사용자가 보유한 복주머니 가져오기
	//    public List<ArticleResponseDto> getAllArticlesByDate(int userSeq, int year){
	//        LocalDate startDate = LocalDate.of(year, 7, 1);
	//        Pocket findPocket = pocketRepository.findPocketByUserAndDate(userSeq, startDate, LocalDate.now())
	//                .orElseThrow(() -> new NullPointerException("복주머니를 찾지 못했습니다."));
	//        List<ArticleResponseDto> result = findPocket.getArticles().stream()
	//                .map(a -> new ArticleResponseDto(a))
	//                .collect(Collectors.toList());
	//
	//        return result;
	//    }

	public void createArticle(String userKey, WriteArticleDto writeArticleDto) {
		Optional<User> user = userRepository.findByUserKey(userKey);
		//        if (user.isEmpty()){
		//            throw new IllegalArgumentException("Invalid userKey: " + userKey);
		//        }

		Fortune fortune = fortuneRepository.findByFortuneSeq(writeArticleDto.getFortuneSeq())
			.orElseThrow(() -> new FortuneNotFoundException());

		Pocket pocket = pocketRepository.findPocketByPocketSeq(writeArticleDto.getPocketSeq())
			.orElseThrow(() -> new PocketNotFoundException());

		Article article = Article.builder()
			.user(user.orElse(null))
			.userNickname(writeArticleDto.getNickname())
			.articleContent(writeArticleDto.getContent())
			.articleComment(null)
			.articleVisibility(writeArticleDto.isVisibility())
			.fortune(fortune)
			.pocket(pocket)
			.build();

		articleRepository.save(article);

		//push 알림추가(이후 비동기로 진행하면 더 좋을 듯)
		PushMessage pushMessage = PushMessage.ARTICLE;

		PushDto pushDto = PushDto.builder()
			.toUser(pocket.getUser())
			.address(pocket.getPocketAddress())
			.title(pushMessage.getTitle())
			.content(writeArticleDto.getNickname() + pushMessage.getBody())
			.build();

		pushService.sendPush(pushDto);
	}

	public ArticleResponseDto updateComment(CommentDto commentDto) {
		int findArticle = commentDto.getArticleSeq();
		Article article = articleRepository.findByArticleSeq(findArticle)
			.orElseThrow(() -> new ArticleNotFoundException());
		if (article.getArticleComment() != null) {
			throw new CommentConflictException();
		}
		article.updateComment(commentDto.getComment());
		articleRepository.save(article);

		//push 알림추가(이후 비동기로 진행하면 더 좋을 듯)
		PushMessage pushMessage = PushMessage.COMMENT;

		PushDto pushDto = PushDto.builder()
			.toUser(article.getUser())
			.address(article.getPocket().getPocketAddress())
			.title(pushMessage.getTitle())
			.content(article.getPocket().getUser().getUserNickname() + pushMessage.getBody())
			.build();

		pushService.sendPush(pushDto);

		return article.toArticleResponseDto();
	}

	public void deleteArticle(int articleSeq) {
		Article findArticle = articleRepository.findByArticleSeq(articleSeq)
			.orElseThrow(() -> new ArticleNotFoundException());
		articleRepository.delete(findArticle);
	}

	public int getOwnerByArticle(int articleSeq) {
		Article findArticle = articleRepository.findByArticleSeq(articleSeq)
			.orElseThrow(() -> new ArticleNotFoundException());
		Pocket pocket = findArticle.getPocket();
		return pocket.getUser().getUserSeq();
	}

	public List<ArticleSummaryDto> getAllArticles(int pocketSeq) {
		Pocket pocket = pocketRepository.findPocketByPocketSeq(pocketSeq)
			.orElseThrow(() -> new PocketNotFoundException());

		return pocket.getArticles().stream()
			.map(Article::summaryArticle)
			.collect(Collectors.toList());
	}

	//    // 모든 복의 공개여부 체크 후 가져오기
	//    public List<ArticleResponseDto> getAllArticlesCheck(int pocketSeq) {
	//        Pocket pocket = pocketRepository.findPocketByPocketSeq(pocketSeq)
	//                .orElseThrow(() -> new PocketNotFoundException("not found pocket"));
	//        List<ArticleResponseDto> result = pocket.getArticles().stream()
	//                .map(article -> {
	//                    ArticleResponseDto dto = new ArticleResponseDto(article);
	//                    if (!dto.isArticleVisibility()) {
	//                        dto.setArticleContent("비밀글입니다.");
	//                        dto.setArticleComment("비밀글입니다.");
	//                    }
	//                    return dto;
	//                })
	//                .collect(Collectors.toList());
	//        return result;
	//    }

	//    // 모든 복을 비공개로 가져오기
	//    public List<ArticleResponseDto> getAllArticlesInvisible(int pocketSeq) {
	//        Pocket pocket = pocketRepository.findPocketByPocketSeq(pocketSeq)
	//                .orElseThrow(() -> new PocketNotFoundException("not found pocket"));
	//        List<ArticleResponseDto> result = pocket.getArticles().stream()
	//                .map(article -> {
	//                    ArticleResponseDto dto = new ArticleResponseDto(article);
	//                    dto.setArticleContent("비밀글입니다.");
	//                    dto.setArticleComment("비밀글입니다.");
	//                    return dto;
	//                })
	//                .collect(Collectors.toList());
	//        return result;
	//    }

	//    public List<ArticleResponseDto> getArticlesByUser(String userKey){
	//        User user = userRepository.findByUserKey(userKey)
	//                .orElseThrow(() -> new UserNotFoundException("not found user"));
	//        Pocket pocket = pocketRepository.findFirstByUserOrderByCreatedAtDesc(user)
	//                .orElseThrow(() -> new PocketNotFoundException("not found pocket"));
	//        List<ArticleResponseDto> result = pocket.getArticles().stream()
	//                .map(a -> new ArticleResponseDto(a))
	//                .collect(Collectors.toList());
	//        return result;
	//    }
}
