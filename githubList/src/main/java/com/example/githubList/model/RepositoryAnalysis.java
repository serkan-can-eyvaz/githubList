package com.example.githubList.model;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class RepositoryAnalysis {
    private String repositoryName;
    private String repositoryUrl;
    private int technologyQuality; // 0-100
    private int learningValue; // 0-100
    private int careerGrowth; // 0-100
    private int communityActivity; // 0-100
    private int recency; // 0-100
    private String generalRecommendation;
    private String projectSummary; // Proje özeti
    private String technologyQualityDescription; // Teknoloji kalitesi açıklaması
    private String learningValueDescription; // Öğrenme değeri açıklaması
    private String careerGrowthDescription; // Kariyer gelişimi açıklaması
    private String communityActivityDescription; // Topluluk aktifliği açıklaması
    private String recencyDescription; // Güncellik açıklaması
    private String improvementRecommendations; // İyileştirme önerileri
    private String usedTechnologies; // Kullanılan teknolojiler
    private String targetAudience; // Hedef kitle

    public RepositoryAnalysis() {
        // Varsayılan değer yok - sadece Groq AI'dan gelen değerler kullanılacak
    }

    // Getter ve Setter metodları
    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public int getTechnologyQuality() {
        return technologyQuality;
    }

    public void setTechnologyQuality(int technologyQuality) {
        this.technologyQuality = technologyQuality;
    }

    public int getLearningValue() {
        return learningValue;
    }

    public void setLearningValue(int learningValue) {
        this.learningValue = learningValue;
    }

    public int getCareerGrowth() {
        return careerGrowth;
    }

    public void setCareerGrowth(int careerGrowth) {
        this.careerGrowth = careerGrowth;
    }

    public int getCommunityActivity() {
        return communityActivity;
    }

    public void setCommunityActivity(int communityActivity) {
        this.communityActivity = communityActivity;
    }

    public int getRecency() {
        return recency;
    }

    public void setRecency(int recency) {
        this.recency = recency;
    }

    public String getGeneralRecommendation() {
        return generalRecommendation;
    }

    public void setGeneralRecommendation(String generalRecommendation) {
        this.generalRecommendation = generalRecommendation;
    }

    public String getProjectSummary() {
        return projectSummary;
    }

    public void setProjectSummary(String projectSummary) {
        this.projectSummary = projectSummary;
    }

    public String getTechnologyQualityDescription() {
        return technologyQualityDescription;
    }

    public void setTechnologyQualityDescription(String technologyQualityDescription) {
        this.technologyQualityDescription = technologyQualityDescription;
    }

    public String getLearningValueDescription() {
        return learningValueDescription;
    }

    public void setLearningValueDescription(String learningValueDescription) {
        this.learningValueDescription = learningValueDescription;
    }

    public String getCareerGrowthDescription() {
        return careerGrowthDescription;
    }

    public void setCareerGrowthDescription(String careerGrowthDescription) {
        this.careerGrowthDescription = careerGrowthDescription;
    }

    public String getCommunityActivityDescription() {
        return communityActivityDescription;
    }

    public void setCommunityActivityDescription(String communityActivityDescription) {
        this.communityActivityDescription = communityActivityDescription;
    }

    public String getRecencyDescription() {
        return recencyDescription;
    }

    public void setRecencyDescription(String recencyDescription) {
        this.recencyDescription = recencyDescription;
    }

    public String getImprovementRecommendations() {
        return improvementRecommendations;
    }

    public void setImprovementRecommendations(String improvementRecommendations) {
        this.improvementRecommendations = improvementRecommendations;
    }

    public String getUsedTechnologies() {
        return usedTechnologies;
    }

    public void setUsedTechnologies(String usedTechnologies) {
        this.usedTechnologies = usedTechnologies;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    // Ortalama puan hesaplama
    public double getAverageScore() {
        return (technologyQuality + learningValue + careerGrowth + communityActivity + recency) / 5.0;
    }

    // Puan kategorisi
    public String getScoreCategory() {
        double avg = getAverageScore();
        if (avg >= 80) return "Mükemmel";
        else if (avg >= 70) return "Çok İyi";
        else if (avg >= 60) return "İyi";
        else if (avg >= 50) return "Orta";
        else if (avg >= 40) return "Zayıf";
        else return "Çok Zayıf";
    }

    // Renk sınıfı (CSS için)
    public String getScoreColorClass() {
        double avg = getAverageScore();
        if (avg >= 80) return "text-success";
        else if (avg >= 70) return "text-primary";
        else if (avg >= 60) return "text-info";
        else if (avg >= 50) return "text-warning";
        else return "text-danger";
    }

    // Progress bar rengi
    public String progressBarColor(int score) {
        if (score >= 80) return "bg-success";
        else if (score >= 70) return "bg-primary";
        else if (score >= 60) return "bg-info";
        else if (score >= 50) return "bg-warning";
        else return "bg-danger";
    }

    // Markdown'ı HTML'e çeviren yardımcı metod
    private String markdownToHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }
        
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }

    // İyileştirme önerilerini HTML formatında döndür
    public String getImprovementRecommendationsHtml() {
        return markdownToHtml(improvementRecommendations);
    }

    // Kullanılan teknolojileri HTML formatında döndür
    public String getUsedTechnologiesHtml() {
        return markdownToHtml(usedTechnologies);
    }

    // Hedef kitleyi HTML formatında döndür
    public String getTargetAudienceHtml() {
        return markdownToHtml(targetAudience);
    }

    // Genel öneriyi HTML formatında döndür
    public String getGeneralRecommendationHtml() {
        return markdownToHtml(generalRecommendation);
    }
}
