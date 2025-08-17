package com.example.githubList.controller;

import com.example.githubList.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class GitHubController {

    @Autowired
    private CacheService cacheService;

    @GetMapping("/")
    public String index() {
        return "redirect:/analyze";
    }

    @PostMapping("/api/cache/clear")
    @ResponseBody
    public String clearCache() {
        try {
            cacheService.clearAllCache();
            return "Cache başarıyla temizlendi";
        } catch (Exception e) {
            return "Cache temizleme hatası: " + e.getMessage();
        }
    }

    @GetMapping("/api/health")
    @ResponseBody
    public String healthCheck() {
        boolean cacheAvailable = cacheService.isCacheAvailable();
        return "Uygulama çalışıyor. Cache durumu: " + (cacheAvailable ? "Aktif" : "Pasif");
    }
}
