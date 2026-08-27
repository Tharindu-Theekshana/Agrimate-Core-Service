package com.agrimate.service.repository;

import com.agrimate.service.model.question.Question;
import com.agrimate.service.model.question.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);
    List<Question> findByStatusOrderByCreatedAtDesc(QuestionStatus status);
    List<Question> findAllByOrderByCreatedAtDesc();
}
