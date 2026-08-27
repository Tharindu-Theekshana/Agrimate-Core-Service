package com.agrimate.service.controller;

import com.agrimate.service.dto.QuestionDtos.AnswerRequest;
import com.agrimate.service.dto.QuestionDtos.QuestionDto;
import com.agrimate.service.model.user.User;
import com.agrimate.service.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<QuestionDto> list(@AuthenticationPrincipal User user) {
        return questionService.list(user);
    }

    @GetMapping("/{id}")
    public QuestionDto get(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return questionService.get(user, id);
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto create(@AuthenticationPrincipal User user,
                              @RequestParam("title") String title,
                              @RequestParam(value = "body", required = false) String body,
                              @RequestParam(value = "scanId", required = false) Long scanId,
                              @RequestParam(value = "image", required = false) MultipartFile image) {
        return questionService.create(user, title, body, scanId, image);
    }

    @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
    public QuestionDto update(@AuthenticationPrincipal User user, @PathVariable Long id,
                              @RequestParam("title") String title,
                              @RequestParam(value = "body", required = false) String body,
                              @RequestParam(value = "image", required = false) MultipartFile image) {
        return questionService.update(user, id, title, body, image);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        questionService.delete(user, id);
    }

    @PostMapping("/{id}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto answer(@AuthenticationPrincipal User user, @PathVariable Long id,
                              @Valid @RequestBody AnswerRequest req) {
        return questionService.answer(user, id, req);
    }

    @PatchMapping("/{id}/answers/{answerId}")
    public QuestionDto updateAnswer(@AuthenticationPrincipal User user, @PathVariable Long id,
                                    @PathVariable Long answerId, @Valid @RequestBody AnswerRequest req) {
        return questionService.updateAnswer(user, id, answerId, req);
    }

    @DeleteMapping("/{id}/answers/{answerId}")
    public QuestionDto deleteAnswer(@AuthenticationPrincipal User user, @PathVariable Long id,
                                    @PathVariable Long answerId) {
        return questionService.deleteAnswer(user, id, answerId);
    }
}
