package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.QuestionDtos.AnswerRequest;
import com.agrimate.service.dto.QuestionDtos.QuestionDto;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.account.AgronomistStatus;
import com.agrimate.service.model.answer.Answer;
import com.agrimate.service.model.notification.Notification;
import com.agrimate.service.model.question.Question;
import com.agrimate.service.model.scan.Scan;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.notification.NotificationType;
import com.agrimate.service.model.question.QuestionStatus;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.repository.AnswerRepository;
import com.agrimate.service.repository.NotificationRepository;
import com.agrimate.service.repository.QuestionRepository;
import com.agrimate.service.repository.ScanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ScanRepository scanRepository;
    private final NotificationRepository notificationRepository;
    private final StorageService storageService;
    private final PushService pushService;

    public QuestionService(QuestionRepository questionRepository, AnswerRepository answerRepository,
                           ScanRepository scanRepository, NotificationRepository notificationRepository,
                           StorageService storageService, PushService pushService) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.scanRepository = scanRepository;
        this.notificationRepository = notificationRepository;
        this.storageService = storageService;
        this.pushService = pushService;
    }

    @Transactional
    public QuestionDto create(User farmer, String title, String body, Long scanId, MultipartFile image) {
        Question q = new Question();
        q.setFarmer(farmer);
        q.setTitle(title);
        q.setBody(body);
        if (image != null && !image.isEmpty()) {
            q.setImageUrl(storageService.upload(image, "agrimate/questions"));
        }
        if (scanId != null) {
            Scan scan = scanRepository.findById(scanId)
                    .filter(s -> s.getAccount().getId().equals(farmer.getAccount().getId()))
                    .orElseThrow(() -> ApiException.notFound("Scan not found"));
            q.setScan(scan);
        }
        q.setStatus(QuestionStatus.OPEN);
        q = questionRepository.save(q);
        return QuestionDto.from(q, List.of());
    }

    @Transactional
    public QuestionDto update(User farmer, Long id, String title, String body, MultipartFile image) {
        Question q = ownedQuestion(farmer, id);
        if (q.getStatus() != QuestionStatus.OPEN) {
            throw ApiException.badRequest("This question has already been answered and can no longer be edited");
        }
        q.setTitle(title);
        q.setBody(body);
        if (image != null && !image.isEmpty()) {
            q.setImageUrl(storageService.upload(image, "agrimate/questions"));
        }
        q = questionRepository.save(q);
        return QuestionDto.from(q, List.of());
    }

    @Transactional
    public void delete(User farmer, Long id) {
        Question q = ownedQuestion(farmer, id);
        answerRepository.deleteAll(answerRepository.findByQuestionIdOrderByCreatedAtAsc(id));
        questionRepository.delete(q);
    }

    private Question ownedQuestion(User farmer, Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Question not found"));
        if (!q.getFarmer().getId().equals(farmer.getId())) {
            throw ApiException.forbidden("Not your question");
        }
        return q;
    }

    @Transactional
    public QuestionDto updateAnswer(User agronomist, Long questionId, Long answerId, AnswerRequest req) {
        Answer a = ownedAnswer(agronomist, questionId, answerId);
        a.setBody(req.body());
        a.setAttachmentUrl(req.attachmentUrl());
        answerRepository.save(a);
        return QuestionDto.from(a.getQuestion(), answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId));
    }

    @Transactional
    public QuestionDto deleteAnswer(User agronomist, Long questionId, Long answerId) {
        Answer a = ownedAnswer(agronomist, questionId, answerId);
        Question q = a.getQuestion();
        answerRepository.delete(a);

        List<Answer> remaining = answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);
        if (remaining.isEmpty()) {
            q.setStatus(QuestionStatus.OPEN);
            q.setAgronomist(null);
        } else {
            q.setAgronomist(remaining.get(remaining.size() - 1).getAgronomist());
        }
        q = questionRepository.save(q);
        return QuestionDto.from(q, remaining);
    }

    private Answer ownedAnswer(User agronomist, Long questionId, Long answerId) {
        Answer a = answerRepository.findById(answerId)
                .filter(found -> found.getQuestion().getId().equals(questionId))
                .orElseThrow(() -> ApiException.notFound("Answer not found"));
        if (!a.getAgronomist().getId().equals(agronomist.getId()) && !agronomist.hasRole(RoleName.ADMIN)) {
            throw ApiException.forbidden("Not your answer");
        }
        return a;
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> list(User user) {
        boolean elevated = isElevated(user);
        List<Question> questions = elevated
                ? questionRepository.findAllByOrderByCreatedAtDesc()
                : questionRepository.findByFarmerIdOrderByCreatedAtDesc(user.getId());
        return questions.stream()
                .map(q -> QuestionDto.from(q, answerRepository.findByQuestionIdOrderByCreatedAtAsc(q.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionDto get(User user, Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Question not found"));
        boolean elevated = isElevated(user);
        if (!elevated && !q.getFarmer().getId().equals(user.getId())) {
            throw ApiException.forbidden("Not your question");
        }
        return QuestionDto.from(q, answerRepository.findByQuestionIdOrderByCreatedAtAsc(id));
    }

    @Transactional
    public QuestionDto answer(User agronomist, Long questionId, AnswerRequest req) {
        if (!isElevated(agronomist)) {
            throw ApiException.forbidden("Only approved agronomists can answer questions");
        }
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> ApiException.notFound("Question not found"));

        Answer a = new Answer();
        a.setQuestion(q);
        a.setAgronomist(agronomist);
        a.setBody(req.body());
        a.setAttachmentUrl(req.attachmentUrl());
        answerRepository.save(a);

        q.setAgronomist(agronomist);
        q.setStatus(QuestionStatus.ANSWERED);
        questionRepository.save(q);

        Notification n = new Notification();
        Account farmerAccount = q.getFarmer().getAccount();
        n.setAccount(farmerAccount);
        n.setType(NotificationType.QA_REPLY);
        n.setTitle("Your question was answered");
        String agronomistName = agronomist.getAccount() != null ? agronomist.getAccount().getName() : "An agronomist";
        n.setBody(agronomistName + " replied to: " + q.getTitle());
        notificationRepository.save(n);
        pushService.sendToTokens(new ArrayList<>(farmerAccount.getDeviceTokens().keySet()),
                n.getTitle(), n.getBody(), NotificationType.QA_REPLY.name());

        return QuestionDto.from(q, answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId));
    }

    private boolean isElevated(User user) {
        if (user.hasRole(RoleName.ADMIN)) return true;
        return user.hasRole(RoleName.AGRONOMIST)
                && user.getAccount() != null
                && user.getAccount().getAgronomistStatus() == AgronomistStatus.APPROVED;
    }
}
