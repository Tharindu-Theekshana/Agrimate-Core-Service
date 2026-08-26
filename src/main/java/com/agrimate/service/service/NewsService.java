package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.NewsDtos.NewsDto;
import com.agrimate.service.dto.NewsDtos.NewsRequest;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.news.News;
import com.agrimate.service.model.notification.Notification;
import com.agrimate.service.model.notification.NotificationType;
import com.agrimate.service.repository.AccountRepository;
import com.agrimate.service.repository.NewsRepository;
import com.agrimate.service.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;
    private final PushService pushService;

    public NewsService(NewsRepository newsRepository, AccountRepository accountRepository,
                       NotificationRepository notificationRepository, PushService pushService) {
        this.newsRepository = newsRepository;
        this.accountRepository = accountRepository;
        this.notificationRepository = notificationRepository;
        this.pushService = pushService;
    }

    public List<NewsDto> list() {
        return newsRepository.findAllByOrderByCreatedAtDesc().stream().map(NewsDto::from).toList();
    }

    @Transactional
    public NewsDto create(NewsRequest req) {
        News news = new News();
        news.setTitle(req.title().trim());
        news.setDescription(req.description().trim());
        news.setImageUrl(req.imageUrl());
        news = newsRepository.save(news);
        broadcast(news);
        return NewsDto.from(news);
    }

    @Transactional
    public void delete(Long id) {
        if (!newsRepository.existsById(id)) throw ApiException.notFound("News not found");
        newsRepository.deleteById(id);
    }

    private void broadcast(News news) {
        Notification n = new Notification();
        n.setAccount(null);
        n.setType(NotificationType.NEWS);
        n.setTitle(news.getTitle());
        n.setBody(news.getDescription());
        notificationRepository.save(n);

        List<String> tokens = new ArrayList<>();
        for (Account account : accountRepository.findAll()) {
            tokens.addAll(account.getDeviceTokens().keySet());
        }
        pushService.sendToTokens(tokens, news.getTitle(), news.getDescription(), NotificationType.NEWS.name());
    }
}
