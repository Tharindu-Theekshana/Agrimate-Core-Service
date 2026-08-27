package com.agrimate.service.dto;

import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.answer.Answer;
import com.agrimate.service.model.question.Question;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.question.QuestionStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public final class QuestionDtos {
    private QuestionDtos() {}

    public record AnswerRequest(
            @NotBlank String body,
            String attachmentUrl
    ) {}

    public record AnswerDto(
            Long id,
            Long agronomistId,
            String agronomistName,
            String agronomistUsername,
            String agronomistPhotoUrl,
            String body,
            String attachmentUrl,
            Instant createdAt
    ) {
        public static AnswerDto from(Answer a) {
            return new AnswerDto(a.getId(), a.getAgronomist().getId(), nameOf(a.getAgronomist()),
                    a.getAgronomist().getUsername(), photoOf(a.getAgronomist()), a.getBody(),
                    a.getAttachmentUrl(), a.getCreatedAt());
        }
    }

    private static String nameOf(User u) {
        Account acc = u != null ? u.getAccount() : null;
        return acc != null ? acc.getName() : null;
    }

    private static String locationOf(User u) {
        Account acc = u != null ? u.getAccount() : null;
        return acc != null ? acc.getLocation() : null;
    }

    private static String photoOf(User u) {
        Account acc = u != null ? u.getAccount() : null;
        return acc != null ? acc.getProfilePhotoUrl() : null;
    }

    public record QuestionDto(
            Long id,
            String farmerName,
            String farmerLocation,
            String farmerPhotoUrl,
            String title,
            String body,
            String imageUrl,
            QuestionStatus status,
            Instant createdAt,
            List<AnswerDto> answers
    ) {
        public static QuestionDto from(Question q, List<Answer> answers) {
            return new QuestionDto(q.getId(), nameOf(q.getFarmer()), locationOf(q.getFarmer()), photoOf(q.getFarmer()),
                    q.getTitle(), q.getBody(), q.getImageUrl(), q.getStatus(), q.getCreatedAt(),
                    answers.stream().map(AnswerDto::from).toList());
        }
    }
}
