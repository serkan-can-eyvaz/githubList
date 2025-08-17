package com.example.githubList.service;

import com.example.githubList.model.GitHubRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${github.api.token:}")
    private String githubToken;

    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String GITHUB_REPO_URL = GITHUB_API_BASE_URL + "/repos/{owner}/{repo}";
    private static final String GITHUB_README_URL = GITHUB_API_BASE_URL + "/repos/{owner}/{repo}/readme";

    public GitHubApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }



    public GitHubRepository getRepository(String owner, String repo) {
        try {
            String url = GITHUB_REPO_URL.replace("{owner}", owner).replace("{repo}", repo);
            
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode repoNode = objectMapper.readTree(response.getBody());
                GitHubRepository repository = parseRepository(repoNode);
                
                // README içeriğini de al
                fetchReadmeContent(repository);
                
                return repository;
            }

        } catch (Exception e) {
            System.err.println("Repository getirme hatası: " + e.getMessage());
        }

        return null;
    }



    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("User-Agent", "GitHubList-App");
        
        if (githubToken != null && !githubToken.trim().isEmpty()) {
            headers.set("Authorization", "token " + githubToken);
        }
        
        return headers;
    }

    private GitHubRepository parseRepository(JsonNode itemNode) {
        GitHubRepository repo = new GitHubRepository();
        
        repo.setId(itemNode.get("id").asLong());
        repo.setName(itemNode.get("name").asText());
        repo.setFullName(itemNode.get("full_name").asText());
        repo.setDescription(itemNode.path("description").asText(null));
        repo.setLanguage(itemNode.path("language").asText(null));
        repo.setStargazersCount(itemNode.path("stargazers_count").asInt(0));
        repo.setForksCount(itemNode.path("forks_count").asInt(0));
        repo.setHtmlUrl(itemNode.get("html_url").asText());

        // Tarih parsing - Son commit tarihini al
        String pushedAtStr = itemNode.path("pushed_at").asText(null);
        if (pushedAtStr != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                repo.setUpdatedAt(LocalDateTime.parse(pushedAtStr, formatter));
            } catch (Exception e) {
                System.err.println("Tarih parsing hatası: " + e.getMessage());
            }
        }

        return repo;
    }

    private void fetchReadmeContent(GitHubRepository repo) {
        try {
            String[] parts = repo.getFullName().split("/");
            if (parts.length != 2) return;

            String owner = parts[0];
            String repoName = parts[1];

            String url = GITHUB_README_URL.replace("{owner}", owner).replace("{repo}", repoName);
            
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode readmeNode = objectMapper.readTree(response.getBody());
                String content = readmeNode.path("content").asText("");
                String encoding = readmeNode.path("encoding").asText("base64");
                
                if ("base64".equals(encoding) && !content.isEmpty()) {
                    try {
                        // Base64 decode - daha güvenli
                        content = content.replaceAll("\\s+", ""); // Boşlukları temizle
                        byte[] decodedBytes = java.util.Base64.getDecoder().decode(content);
                        String readmeContent = new String(decodedBytes, "UTF-8");
                        repo.setReadmeContent(readmeContent);
                        System.out.println("README başarıyla alındı: " + repo.getFullName() + " (" + readmeContent.length() + " karakter)");
                    } catch (Exception decodeException) {
                        System.err.println("Base64 decode hatası " + repo.getFullName() + ": " + decodeException.getMessage());
                        // Fallback: raw content'i kullan
                        repo.setReadmeContent(content);
                    }
                } else if (!content.isEmpty()) {
                    // Base64 değilse direkt kullan
                    repo.setReadmeContent(content);
                }
            }

        } catch (Exception e) {
            // README çekilemezse sessizce devam et
            System.err.println("README çekme hatası " + repo.getFullName() + ": " + e.getMessage());
        }
    }

}
