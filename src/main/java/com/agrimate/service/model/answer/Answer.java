package com.agrimate.service.model.answer;

import com.agrimate.service.model.baseEntity.BaseEntity;
import com.agrimate.service.model.question.Question;
import com.agrimate.service.model.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "answers")
public class Answer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agronomist_id", nullable = false)
    private User agronomist;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "attachment_url", length = 1000)
    private String attachmentUrl;

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public User getAgronomist() { return agronomist; }
    public void setAgronomist(User agronomist) { this.agronomist = agronomist; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
}
