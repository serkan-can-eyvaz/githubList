package com.example.githubList.controller;

import com.example.githubList.model.GitHubRepository;
import com.example.githubList.model.RepositoryAnalysis;
import com.example.githubList.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class RepositoryAnalysisController {

    @Autowired
    private GitHubApiService githubApiService;

    @Autowired
    private GroqAIService groqAIService;

    @Autowired
    private CacheService cacheService;

    @GetMapping("/analyze")
    public String analyzePage(Model model) {
        return "analyze";
    }

    @PostMapping("/analyze")
    public String analyzeRepository(
            @RequestParam String repoInput,
            Model model) {

        try {
            System.out.println("=== ANALIZ BASLADI ===");
            System.out.println("Repo Input: " + repoInput);
            
            // Repo input'unu parse et (owner/repo formatı veya URL)
            String[] repoInfo = parseRepoInput(repoInput);
            if (repoInfo == null) {
                System.out.println("Repo parse hatası");
                model.addAttribute("errorMessage", "Geçersiz repository formatı. Desteklenen formatlar: 1) https://github.com/owner/repo.git 2) owner/repo 3) repo-adı (varsayılan owner: Baranll0)");
                return "analyze";
            }

            String owner = repoInfo[0];
            String repo = repoInfo[1];
            System.out.println("Owner: " + owner + ", Repo: " + repo);

            // GitHub API'den repo bilgilerini al
            GitHubRepository repository = githubApiService.getRepository(owner, repo);
            
            if (repository == null) {
                System.out.println("Repository bulunamadı");
                model.addAttribute("errorMessage", "Repository bulunamadı: " + owner + "/" + repo);
                return "analyze";
            }
            System.out.println("Repository bulundu: " + repository.getFullName());

            // Cache'den analiz kontrol et
            String cacheKey = "analysis_" + owner + "_" + repo;
            Optional<String> cachedAnalysis = cacheService.getCachedSummary(cacheKey);
            
            RepositoryAnalysis analysis;
            if (cachedAnalysis.isPresent()) {
                System.out.println("Cache'den yüklendi");
                analysis = parseAnalysisFromCache(cachedAnalysis.get());
                model.addAttribute("cacheInfo", "Cache'den yüklendi");
            } else {
                System.out.println("Yeni analiz yapılıyor");
                // Groq AI ile detaylı analiz yap
                analysis = performDetailedAnalysis(repository);
                
                // Cache'e kaydet
                String analysisText = serializeAnalysis(analysis);
                cacheService.cacheRepositorySummary(cacheKey, analysisText);
                model.addAttribute("cacheInfo", "Groq AI analizi yapıldı");
            }

            System.out.println("Analysis null mu: " + (analysis == null));
            if (analysis != null) {
                System.out.println("Analysis puanları: " + analysis.getTechnologyQuality() + ", " + analysis.getLearningValue() + ", " + analysis.getCareerGrowth() + ", " + analysis.getCommunityActivity() + ", " + analysis.getRecency());
                System.out.println("Improvement Recommendations uzunluğu: " + (analysis.getImprovementRecommendations() != null ? analysis.getImprovementRecommendations().length() : "null"));
                System.out.println("Improvement Recommendations içeriği: " + analysis.getImprovementRecommendations());
            } else {
                System.out.println("Analysis null olduğu için hata mesajı gösterilecek");
                model.addAttribute("errorMessage", "Repository analizi başarısız oldu. Lütfen tekrar deneyin.");
                model.addAttribute("repository", repository);
                model.addAttribute("repoInput", repoInput);
                return "analyze";
            }

            model.addAttribute("repository", repository);
            model.addAttribute("analysis", analysis);
            model.addAttribute("repoInput", repoInput);
            
            System.out.println("=== ANALIZ TAMAMLANDI ===");

        } catch (Exception e) {
            System.err.println("Analiz hatası: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Analiz sırasında hata oluştu: " + e.getMessage());
        }

        return "analyze";
    }

    @PostMapping("/analyze/clear-cache")
    public String clearCacheAndReanalyze(
            @RequestParam String repoInput,
            Model model) {

        try {
            // Repo input'unu parse et
            String[] repoInfo = parseRepoInput(repoInput);
            if (repoInfo == null) {
                model.addAttribute("errorMessage", "Geçersiz repository formatı. Desteklenen formatlar: 1) https://github.com/owner/repo.git 2) owner/repo 3) repo-adı (varsayılan owner: Baranll0)");
                return "analyze";
            }

            String owner = repoInfo[0];
            String repo = repoInfo[1];

            // Cache'i temizle - hem eski hem yeni format için
            String cacheKey = "analysis_" + owner + "_" + repo;
            String cacheKeyNew = "github:summary:" + cacheKey;
            cacheService.clearCache(cacheKey);
            cacheService.clearCache(cacheKeyNew);
            System.out.println("Cache temizlendi: " + cacheKey + " ve " + cacheKeyNew);
            model.addAttribute("cacheInfo", "Önbellek temizlendi. Yeni analiz yapmak için repository URL'sini girin ve 'Analiz Et' butonuna tıklayın.");

            // Sadece repo input'unu koru, diğer her şeyi temizle
            model.addAttribute("repoInput", repoInput);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Cache temizleme sırasında hata oluştu: " + e.getMessage());
        }

        return "analyze";
    }

    @GetMapping("/api/analyze")
    @ResponseBody
    public RepositoryAnalysis analyzeRepositoryApi(
            @RequestParam String repoInput) {

        try {
            String[] repoInfo = parseRepoInput(repoInput);
            if (repoInfo == null) {
                throw new RuntimeException("Geçersiz repository formatı. Desteklenen formatlar: 1) https://github.com/owner/repo.git 2) owner/repo 3) repo-adı (varsayılan owner: Baranll0)");
            }

            String owner = repoInfo[0];
            String repo = repoInfo[1];

            GitHubRepository repository = githubApiService.getRepository(owner, repo);
            if (repository == null) {
                return null;
            }

            String cacheKey = "analysis_" + owner + "_" + repo;
            Optional<String> cachedAnalysis = cacheService.getCachedSummary(cacheKey);
            
            if (cachedAnalysis.isPresent()) {
                return parseAnalysisFromCache(cachedAnalysis.get());
            } else {
                RepositoryAnalysis analysis = performDetailedAnalysis(repository);
                String analysisText = serializeAnalysis(analysis);
                cacheService.cacheRepositorySummary(cacheKey, analysisText);
                return analysis;
            }

        } catch (Exception e) {
            return null;
        }
    }

    private RepositoryAnalysis performDetailedAnalysis(GitHubRepository repo) {
        try {
            RepositoryAnalysis analysis = new RepositoryAnalysis();
            
            // Groq AI ile detaylı analiz
            String aiResponse = groqAIService.analyzeRepository(
                repo.getFullName(),
                repo.getDescription(),
                repo.getLanguage(),
                repo.getStargazersCount(),
                repo.getForksCount(),
                repo.getUpdatedAt() != null ? repo.getUpdatedAt().toString() : null,
                repo.getReadmeContent()
            );
            
            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                System.err.println("Groq AI'dan boş yanıt alındı");
                return null;
            }
            
            // AI yanıtını parse et
            analysis = parseAIResponse(aiResponse);
            analysis.setRepositoryName(repo.getFullName());
            analysis.setRepositoryUrl(repo.getHtmlUrl());
            
            return analysis;
        } catch (Exception e) {
            System.err.println("performDetailedAnalysis hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private RepositoryAnalysis parseAIResponse(String aiResponse) {
        RepositoryAnalysis analysis = new RepositoryAnalysis();
        
        System.out.println("AI yanıtı parse ediliyor...");
        System.out.println("AI yanıtı uzunluğu: " + aiResponse.length());
        System.out.println("AI yanıtının tamamı:");
        System.out.println(aiResponse);
        System.out.println("=== AI YANITI SONU ===");
        
        try {
            // AI yanıtından puanları çıkar (markdown tablo formatı)
            String[] lines = aiResponse.split("\n");
            System.out.println("Toplam satır sayısı: " + lines.length);
            boolean inTable = false;
            
            for (String line : lines) {
                line = line.trim();
                
                // Tablo başlangıcını kontrol et
                if (line.startsWith("| Kategori |")) {
                    inTable = true;
                    continue;
                }
                
                // Tablo bitişini kontrol et
                if (inTable && (line.isEmpty() || !line.startsWith("|"))) {
                    inTable = false;
                    continue;
                }
                
                // Tablo satırlarını parse et
                if (inTable && line.startsWith("|")) {
                    String[] columns = line.split("\\|");
                    if (columns.length >= 4) {
                        String category = columns[1].trim();
                        String score = columns[2].trim();
                        
                        if (category.contains("🔧 Teknoloji Kalitesi") || category.contains("Teknoloji Kalitesi")) {
                            int scoreValue = extractScoreFromTable(score);
                            analysis.setTechnologyQuality(scoreValue);
                            analysis.setTechnologyQualityDescription(columns[3].trim());
                            System.out.println("Teknoloji Kalitesi puanı: " + scoreValue);
                        } else if (category.contains("📚 Öğrenme Değeri") || category.contains("Öğrenme Değeri")) {
                            int scoreValue = extractScoreFromTable(score);
                            analysis.setLearningValue(scoreValue);
                            analysis.setLearningValueDescription(columns[3].trim());
                            System.out.println("Öğrenme Değeri puanı: " + scoreValue);
                        } else if (category.contains("💼 Kariyer Gelişimi") || category.contains("Kariyer Gelişimi")) {
                            int scoreValue = extractScoreFromTable(score);
                            analysis.setCareerGrowth(scoreValue);
                            analysis.setCareerGrowthDescription(columns[3].trim());
                            System.out.println("Kariyer Gelişimi puanı: " + scoreValue);
                        } else if (category.contains("👥 Topluluk Aktifliği") || category.contains("Topluluk Aktifliği")) {
                            int scoreValue = extractScoreFromTable(score);
                            analysis.setCommunityActivity(scoreValue);
                            analysis.setCommunityActivityDescription(columns[3].trim());
                            System.out.println("Topluluk Aktifliği puanı: " + scoreValue);
                        } else if (category.contains("⏰ Güncellik") || category.contains("Güncellik")) {
                            int scoreValue = extractScoreFromTable(score);
                            analysis.setRecency(scoreValue);
                            analysis.setRecencyDescription(columns[3].trim());
                            System.out.println("Güncellik puanı: " + scoreValue);
                        }
                    }
                }
            }
            
            // Proje özetini bul
            String projectSummary = extractProjectSummary(aiResponse);
            if (projectSummary != null) {
                analysis.setProjectSummary(projectSummary);
            }
            
            // İyileştirme önerilerini bul
            String improvementRecommendations = extractImprovementRecommendations(aiResponse);
            if (improvementRecommendations != null) {
                analysis.setImprovementRecommendations(improvementRecommendations);
            }
            
            // Kullanılan teknolojileri bul
            String usedTechnologies = extractUsedTechnologies(aiResponse);
            if (usedTechnologies != null) {
                analysis.setUsedTechnologies(usedTechnologies);
            }
            
            // Hedef kitleyi bul
            String targetAudience = extractTargetAudience(aiResponse);
            if (targetAudience != null) {
                analysis.setTargetAudience(targetAudience);
            }
            
            // Genel öneriyi bul (markdown formatında)
            String recommendation = extractRecommendationFromMarkdown(aiResponse);
            if (recommendation != null) {
                analysis.setGeneralRecommendation(recommendation);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("AI analizi başarısız oldu: " + e.getMessage());
        }
        
        return analysis;
    }

    private int extractScoreFromTable(String scoreCell) {
        try {
            // Tablo hücresinden puan çıkar: "**85/100**" veya "85/100"
            String pattern = "\\*\\*(\\d+)/100\\*\\*|(\\d+)/100";
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(scoreCell);
            
            if (matcher.find()) {
                String scoreStr = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                int score = Integer.parseInt(scoreStr);
                return Math.min(100, Math.max(0, score));
            }
            
            // Alternatif: sadece sayı ara
            String[] words = scoreCell.split("\\s+");
            for (String word : words) {
                if (word.matches("\\d+")) {
                    int score = Integer.parseInt(word);
                    return Math.min(100, Math.max(0, score));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Tablo puan çıkarma hatası: " + e.getMessage());
        }
        throw new RuntimeException("Geçerli puan bulunamadı: " + scoreCell);
    }

    private String extractProjectSummary(String aiResponse) {
        try {
            // "## 📋 Proje Özeti" bölümünden sonraki metni bul
            String[] sections = aiResponse.split("## 📋 Proje Özeti");
            if (sections.length > 1) {
                String summarySection = sections[1];
                // Sonraki başlığa kadar olan metni al
                String[] nextSections = summarySection.split("## ");
                if (nextSections.length > 0) {
                    return nextSections[0].trim();
                }
            }
        } catch (Exception e) {
            // Hata durumunda null döndür
        }
        return null;
    }

    private String extractImprovementRecommendations(String aiResponse) {
        try {
            // "## 🎯 Detaylı İyileştirme Önerileri" bölümünden sonraki metni bul
            String[] sections = aiResponse.split("## 🎯 Detaylı İyileştirme Önerileri");
            
            if (sections.length > 1) {
                String recommendationsSection = sections[1];
                
                // Sonraki başlığa kadar olan metni al (## ile başlayan herhangi bir başlık)
                String[] nextSections = recommendationsSection.split("## ");
                
                if (nextSections.length > 0) {
                    String content = nextSections[0].trim();
                    
                    // Eğer içerik çok kısaysa, tüm bölümü döndür
                    if (content.length() < 10) {
                        return recommendationsSection.trim();
                    }
                    
                    // İlk satırı (başlık) kaldır
                    String[] lines = content.split("\n");
                    
                    if (lines.length > 1) {
                        StringBuilder result = new StringBuilder();
                        for (int i = 1; i < lines.length; i++) {
                            result.append(lines[i]).append("\n");
                        }
                        return result.toString().trim();
                    }
                    return content;
                }
            }
        } catch (Exception e) {
            System.err.println("İyileştirme önerileri parse hatası: " + e.getMessage());
        }
        return null;
    }

    private String extractUsedTechnologies(String aiResponse) {
        try {
            // "## 🛠️ Kullanılan Teknolojiler" bölümünden sonraki metni bul
            String[] sections = aiResponse.split("## 🛠️ Kullanılan Teknolojiler");
            if (sections.length > 1) {
                String technologiesSection = sections[1];
                // Sonraki başlığa kadar olan metni al
                String[] nextSections = technologiesSection.split("## ");
                if (nextSections.length > 0) {
                    String content = nextSections[0].trim();
                    // İlk satırı (başlık) kaldır
                    String[] lines = content.split("\n");
                    if (lines.length > 1) {
                        StringBuilder result = new StringBuilder();
                        for (int i = 1; i < lines.length; i++) {
                            result.append(lines[i]).append("\n");
                        }
                        return result.toString().trim();
                    }
                    return content;
                }
            }
        } catch (Exception e) {
            System.err.println("Kullanılan teknolojiler parse hatası: " + e.getMessage());
        }
        return null;
    }

    private String extractTargetAudience(String aiResponse) {
        try {
            // "## 🎓 Hedef Kitle" bölümünden sonraki metni bul
            String[] sections = aiResponse.split("## 🎓 Hedef Kitle");
            if (sections.length > 1) {
                String audienceSection = sections[1];
                // Sonraki başlığa kadar olan metni al
                String[] nextSections = audienceSection.split("## ");
                if (nextSections.length > 0) {
                    String content = nextSections[0].trim();
                    // İlk satırı (başlık) kaldır
                    String[] lines = content.split("\n");
                    if (lines.length > 1) {
                        StringBuilder result = new StringBuilder();
                        for (int i = 1; i < lines.length; i++) {
                            result.append(lines[i]).append("\n");
                        }
                        return result.toString().trim();
                    }
                    return content;
                }
            }
        } catch (Exception e) {
            System.err.println("Hedef kitle parse hatası: " + e.getMessage());
        }
        return null;
    }

    private String extractRecommendationFromMarkdown(String aiResponse) {
        try {
            // "## 💡 Genel Öneri" bölümünden sonraki metni bul
            String[] sections = aiResponse.split("## 💡 Genel Öneri");
            if (sections.length > 1) {
                String recommendationSection = sections[1];
                // Sonraki başlığa kadar olan metni al
                String[] nextSections = recommendationSection.split("## ");
                if (nextSections.length > 0) {
                    String content = nextSections[0].trim();
                    // İlk satırı (başlık) kaldır
                    String[] lines = content.split("\n");
                    if (lines.length > 1) {
                        StringBuilder result = new StringBuilder();
                        for (int i = 1; i < lines.length; i++) {
                            result.append(lines[i]).append("\n");
                        }
                        return result.toString().trim();
                    }
                    return content;
                }
            }
        } catch (Exception e) {
            System.err.println("Genel öneri parse hatası: " + e.getMessage());
        }
        return null;
    }

    private RepositoryAnalysis parseAnalysisFromCache(String cachedText) {
        RepositoryAnalysis analysis = new RepositoryAnalysis();
        
        try {
            String[] lines = cachedText.split("\n");
            for (String line : lines) {
                if (line.startsWith("TECH:")) analysis.setTechnologyQuality(Integer.parseInt(line.substring(5)));
                else if (line.startsWith("LEARN:")) analysis.setLearningValue(Integer.parseInt(line.substring(6)));
                else if (line.startsWith("CAREER:")) analysis.setCareerGrowth(Integer.parseInt(line.substring(7)));
                else if (line.startsWith("COMMUNITY:")) analysis.setCommunityActivity(Integer.parseInt(line.substring(10)));
                else if (line.startsWith("RECENCY:")) analysis.setRecency(Integer.parseInt(line.substring(8)));
                else if (line.startsWith("RECOMMENDATION:")) analysis.setGeneralRecommendation(line.substring(14));
                else if (line.startsWith("REPO:")) analysis.setRepositoryName(line.substring(5));
                else if (line.startsWith("URL:")) analysis.setRepositoryUrl(line.substring(4));
                else if (line.startsWith("PROJECT_SUMMARY:")) analysis.setProjectSummary(line.substring(16));
                else if (line.startsWith("TECH_DESC:")) analysis.setTechnologyQualityDescription(line.substring(10));
                else if (line.startsWith("LEARN_DESC:")) analysis.setLearningValueDescription(line.substring(11));
                else if (line.startsWith("CAREER_DESC:")) analysis.setCareerGrowthDescription(line.substring(12));
                else if (line.startsWith("COMMUNITY_DESC:")) analysis.setCommunityActivityDescription(line.substring(15));
                else if (line.startsWith("RECENCY_DESC:")) analysis.setRecencyDescription(line.substring(13));
                else if (line.startsWith("IMPROVEMENTS:")) analysis.setImprovementRecommendations(line.substring(13));
                else if (line.startsWith("TECHNOLOGIES:")) analysis.setUsedTechnologies(line.substring(13));
                else if (line.startsWith("TARGET_AUDIENCE:")) analysis.setTargetAudience(line.substring(16));
            }
        } catch (Exception e) {
            throw new RuntimeException("Cache parse hatası: " + e.getMessage());
        }
        
        return analysis;
    }

    private String[] parseRepoInput(String repoInput) {
        if (repoInput == null || repoInput.trim().isEmpty()) {
            return null;
        }

        String input = repoInput.trim();

        // 1. GitHub URL formatı: https://github.com/owner/repo.git veya https://github.com/owner/repo
        if (input.startsWith("https://github.com/")) {
            String path = input.substring("https://github.com/".length());
            // .git uzantısını kaldır
            if (path.endsWith(".git")) {
                path = path.substring(0, path.length() - 4);
            }
            String[] parts = path.split("/");
            if (parts.length >= 2) {
                return new String[]{parts[0], parts[1]};
            }
        }

        // 2. owner/repo formatı
        if (input.contains("/")) {
            String[] parts = input.split("/");
            if (parts.length == 2) {
                // .git uzantısını kaldır
                String repo = parts[1];
                if (repo.endsWith(".git")) {
                    repo = repo.substring(0, repo.length() - 4);
                    parts[1] = repo;
                }
                return parts;
            }
        }

        // 3. Sadece repo adı formatı (varsayılan owner: Baranll0)
        if (!input.contains("/") && !input.startsWith("http")) {
            // .git uzantısını kaldır
            if (input.endsWith(".git")) {
                input = input.substring(0, input.length() - 4);
            }
            // Varsayılan owner olarak "Baranll0" kullan
            return new String[]{"Baranll0", input};
        }

        return null;
    }

    private String serializeAnalysis(RepositoryAnalysis analysis) {
        if (analysis == null) {
            throw new RuntimeException("Analysis null olduğu için serialize edilemiyor");
        }
        return String.format(
            "TECH:%d\nLEARN:%d\nCAREER:%d\nCOMMUNITY:%d\nRECENCY:%d\nRECOMMENDATION:%s\nREPO:%s\nURL:%s\n" +
            "PROJECT_SUMMARY:%s\nTECH_DESC:%s\nLEARN_DESC:%s\nCAREER_DESC:%s\nCOMMUNITY_DESC:%s\nRECENCY_DESC:%s\n" +
            "IMPROVEMENTS:%s\nTECHNOLOGIES:%s\nTARGET_AUDIENCE:%s",
            analysis.getTechnologyQuality(),
            analysis.getLearningValue(),
            analysis.getCareerGrowth(),
            analysis.getCommunityActivity(),
            analysis.getRecency(),
            analysis.getGeneralRecommendation() != null ? analysis.getGeneralRecommendation() : "",
            analysis.getRepositoryName(),
            analysis.getRepositoryUrl(),
            analysis.getProjectSummary() != null ? analysis.getProjectSummary() : "",
            analysis.getTechnologyQualityDescription() != null ? analysis.getTechnologyQualityDescription() : "",
            analysis.getLearningValueDescription() != null ? analysis.getLearningValueDescription() : "",
            analysis.getCareerGrowthDescription() != null ? analysis.getCareerGrowthDescription() : "",
            analysis.getCommunityActivityDescription() != null ? analysis.getCommunityActivityDescription() : "",
            analysis.getRecencyDescription() != null ? analysis.getRecencyDescription() : "",
            analysis.getImprovementRecommendations() != null ? analysis.getImprovementRecommendations() : "",
            analysis.getUsedTechnologies() != null ? analysis.getUsedTechnologies() : "",
            analysis.getTargetAudience() != null ? analysis.getTargetAudience() : ""
        );
    }
}
