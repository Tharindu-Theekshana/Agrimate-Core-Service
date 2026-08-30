package com.agrimate.service.service;

import com.agrimate.service.dto.QuestionDtos.AnswerRequest;
import com.agrimate.service.dto.QuestionDtos.QuestionDto;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.account.AgronomistStatus;
import com.agrimate.service.model.answer.Answer;
import com.agrimate.service.model.question.Question;
import com.agrimate.service.model.question.QuestionStatus;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.userRole.UserRole;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.repository.AnswerRepository;
import com.agrimate.service.repository.NotificationRepository;
import com.agrimate.service.repository.QuestionRepository;
import com.agrimate.service.repository.ScanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock private QuestionRepository questionRepository;
    @Mock private AnswerRepository answerRepository;
    @Mock private ScanRepository scanRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private StorageService storageService;
    @Mock private PushService pushService;
    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        questionService = new QuestionService(questionRepository, answerRepository, scanRepository,
                notificationRepository, storageService, pushService);
    }

    private User farmer(long id) {
        User u = new User();
        u.setId(id);
        Account account = new Account();
        account.setId(id);
        u.setAccount(account);
        return u;
    }

    private User approvedAgronomist(long id) {
        User u = new User();
        u.setId(id);
        Account account = new Account();
        account.setId(id);
        account.setAgronomistStatus(AgronomistStatus.APPROVED);
        u.setAccount(account);
        u.getUserRoles().add(new UserRole(u, new Role(RoleName.AGRONOMIST, "Agronomist")));
        return u;
    }

    private User admin(long id) {
        User u = new User();
        u.setId(id);
        Account account = new Account();
        account.setId(id);
        u.setAccount(account);
        u.getUserRoles().add(new UserRole(u, new Role(RoleName.ADMIN, "Admin")));
        return u;
    }

    private Question questionBy(User farmer, long id, QuestionStatus status) {
        Question q = new Question();
        q.setId(id);
        q.setFarmer(farmer);
        q.setTitle("Why are my leaves spotted?");
        q.setStatus(status);
        return q;
    }

    // BE-QA-01
    @Test
    void create_savesAnOpenQuestionForTheFarmer() {
        User farmer = farmer(1L);
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> {
            Question q = inv.getArgument(0);
            q.setId(100L);
            return q;
        });

        QuestionDto dto = questionService.create(farmer, "Title", "Body", null, null);

        assertThat(dto.status()).isEqualTo(QuestionStatus.OPEN);
    }

    // BE-QA-02
    @Test
    void create_throwsNotFound_whenTheLinkedScanDoesNotBelongToTheFarmer() {
        User farmer = farmer(1L);
        when(scanRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.create(farmer, "Title", "Body", 50L, null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-QA-03
    @Test
    void update_throwsBadRequest_onceTheQuestionHasAlreadyBeenAnswered() {
        User farmer = farmer(1L);
        Question q = questionBy(farmer, 100L, QuestionStatus.ANSWERED);
        when(questionRepository.findById(100L)).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> questionService.update(farmer, 100L, "New title", "New body", null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // BE-QA-04
    @Test
    void update_throwsForbidden_whenTheCallerDoesNotOwnTheQuestion() {
        User owner = farmer(1L);
        User stranger = farmer(2L);
        Question q = questionBy(owner, 100L, QuestionStatus.OPEN);
        when(questionRepository.findById(100L)).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> questionService.update(stranger, 100L, "x", "y", null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    // BE-QA-05
    @Test
    void delete_removesTheQuestionAndItsAnswers() {
        User owner = farmer(1L);
        Question q = questionBy(owner, 100L, QuestionStatus.OPEN);
        when(questionRepository.findById(100L)).thenReturn(Optional.of(q));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(100L)).thenReturn(List.of(new Answer()));

        questionService.delete(owner, 100L);

        org.mockito.Mockito.verify(questionRepository).delete(q);
        org.mockito.Mockito.verify(answerRepository).deleteAll(any());
    }

    // BE-QA-06
    @Test
    void answer_throwsForbidden_whenCallerIsAFarmerNotAnAgronomist() {
        User farmer = farmer(1L);
        assertThatThrownBy(() -> questionService.answer(farmer, 100L, new AnswerRequest("Body", null)))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    // BE-QA-07
    @Test
    void answer_success_marksTheQuestionAnsweredAndNotifiesTheFarmer() {
        User owner = farmer(1L);
        User agronomist = approvedAgronomist(2L);
        Question q = questionBy(owner, 100L, QuestionStatus.OPEN);
        when(questionRepository.findById(100L)).thenReturn(Optional.of(q));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        questionService.answer(agronomist, 100L, new AnswerRequest("Apply fungicide", null));

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        assertThat(q.getAgronomist()).isEqualTo(agronomist);
        org.mockito.Mockito.verify(pushService).sendToTokens(any(), any(), any(), any());
    }

    // BE-QA-08
    @Test
    void answer_isAllowedForAnAdminEvenWithoutAgronomistApproval() {
        User owner = farmer(1L);
        User admin = admin(9L);
        Question q = questionBy(owner, 100L, QuestionStatus.OPEN);
        when(questionRepository.findById(100L)).thenReturn(Optional.of(q));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        questionService.answer(admin, 100L, new AnswerRequest("Body", null));

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }

    // BE-QA-09
    @Test
    void list_returnsOnlyOwnQuestions_forAnOrdinaryFarmer() {
        User farmer = farmer(1L);
        when(questionRepository.findByFarmerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        questionService.list(farmer);

        org.mockito.Mockito.verify(questionRepository).findByFarmerIdOrderByCreatedAtDesc(1L);
        org.mockito.Mockito.verify(questionRepository, org.mockito.Mockito.never()).findAllByOrderByCreatedAtDesc();
    }

    // BE-QA-10
    @Test
    void list_returnsAllQuestions_forAnApprovedAgronomist() {
        User agronomist = approvedAgronomist(2L);
        when(questionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        questionService.list(agronomist);

        org.mockito.Mockito.verify(questionRepository).findAllByOrderByCreatedAtDesc();
    }

    // BE-QA-11
    @Test
    void get_throwsForbidden_whenAnOrdinaryFarmerRequestsSomeoneElsesQuestion() {
        User owner = farmer(1L);
        User stranger = farmer(2L);
        Question q = questionBy(owner, 100L, QuestionStatus.OPEN);
        when(questionRepository.findById(100L)).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> questionService.get(stranger, 100L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    // BE-QA-12
    @Test
    void deleteAnswer_reopensTheQuestion_whenNoAnswersRemain() {
        User owner = farmer(1L);
        User agronomist = approvedAgronomist(2L);
        Question q = questionBy(owner, 100L, QuestionStatus.ANSWERED);
        q.setAgronomist(agronomist);
        Answer a = new Answer();
        a.setId(500L);
        a.setQuestion(q);
        a.setAgronomist(agronomist);
        when(answerRepository.findById(500L)).thenReturn(Optional.of(a));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        questionService.deleteAnswer(agronomist, 100L, 500L);

        assertThat(q.getStatus()).isEqualTo(QuestionStatus.OPEN);
        assertThat(q.getAgronomist()).isNull();
    }
}
