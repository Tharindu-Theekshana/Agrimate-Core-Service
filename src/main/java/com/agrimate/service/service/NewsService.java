package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.NewsDtos.NewsDto;
import com.agrimate.service.dto.NewsDtos.NewsRequest;
import com.agrimate.service.model.news.News;
import com.agrimate.service.model.notification.Notification;
import com.agrimate.service.model.user.User;
import com.agrimate.service.model.notification.NotificationType;
import com.agrimate.service.repository.NewsRepository;
import com.agrimate.service.repository.NotificationRepository;
import com.agrimate.service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public NewsService(NewsRepository newsRepository, UserRepository userRepository,
                       NotificationRepository notificationRepository) {
        this.newsRepository = newsRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
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
        for (User user : userRepository.findAll()) {
            Notification n = new Notification();
            n.setUser(user);
            n.setType(NotificationType.NEWS);
            n.setTitle(news.getTitle());
            n.setBody(news.getDescription());
            notificationRepository.save(n);
        }
    }
}
