package com.agrimate.service.controller;

import com.agrimate.service.dto.AdminDtos.Analytics;
import com.agrimate.service.dto.AdminDtos.BroadcastRequest;
import com.agrimate.service.dto.AdminDtos.CreateAdminRequest;
import com.agrimate.service.dto.AdminDtos.OutbreakPoint;
import com.agrimate.service.dto.AdminDtos.UpdateUserStatusRequest;
import com.agrimate.service.dto.NewsDtos.NewsDto;
import com.agrimate.service.dto.NewsDtos.NewsRequest;
import com.agrimate.service.dto.UserDto;
import com.agrimate.service.model.role.RoleName;
import com.agrimate.service.service.AdminService;
import com.agrimate.service.service.NewsService;
import com.agrimate.service.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final NewsService newsService;
    private final StorageService storageService;

    public AdminController(AdminService adminService, NewsService newsService, StorageService storageService) {
        this.adminService = adminService;
        this.newsService = newsService;
        this.storageService = storageService;
    }

    @GetMapping("/outbreaks")
    public List<OutbreakPoint> outbreaks(@RequestParam(value = "disease", required = false) String disease,
                                         @RequestParam(value = "days", defaultValue = "90") int days) {
        return adminService.outbreaks(disease, days);
    }

    @GetMapping("/users")
    public List<UserDto> users(@RequestParam(value = "role", required = false) RoleName role) {
        return adminService.users(role);
    }

    @PatchMapping("/users/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UpdateUserStatusRequest req) {
        return adminService.updateUser(id, req);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createAdmin(@Valid @RequestBody CreateAdminRequest req) {
        return adminService.createAdmin(req);
    }

    @GetMapping("/analytics")
    public Analytics analytics() {
        return adminService.analytics();
    }

    @GetMapping("/news")
    public List<NewsDto> news() {
        return newsService.list();
    }

    @PostMapping(value = "/news", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public NewsDto createNews(@RequestParam("title") String title,
                              @RequestParam("description") String description,
                              @RequestParam(value = "image", required = false) MultipartFile image) {
        String imageUrl = (image != null && !image.isEmpty()) ? storageService.upload(image) : null;
        return newsService.create(new NewsRequest(title, description, imageUrl));
    }

    @DeleteMapping("/news/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNews(@PathVariable Long id) {
        newsService.delete(id);
    }

    @PostMapping("/notifications")
    public java.util.Map<String, Object> broadcast(@org.springframework.web.bind.annotation.RequestBody BroadcastRequest req) {
        int count = adminService.broadcast(req);
        return java.util.Map.of("delivered", count);
    }
}
