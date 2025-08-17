package com.example.githubList.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class GroqAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama3-70b-8192"; // Hızlı ve güçlü model

    public GroqAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String analyzeRepository(String repoName, String description, String language, 
                                  Integer stars, Integer forks, String lastUpdate, String readmeContent) {
        
        try {
            System.out.println("Groq AI analizi başlatılıyor: " + repoName);
            
            // Detaylı analiz prompt'u
            String prompt = buildAnalysisPrompt(repoName, description, language, stars, forks, lastUpdate, readmeContent);
            System.out.println("Prompt oluşturuldu, uzunluk: " + prompt.length());
            
            // Groq API çağrısı
            System.out.println("Groq API çağrısı yapılıyor...");
            String response = callGroqAPI(prompt);
            System.out.println("Groq API yanıtı alındı, uzunluk: " + response.length());
            // Debug logları kaldırıldı
            
            return response;
            
        } catch (Exception e) {
            System.err.println("Groq AI analiz hatası: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Groq AI analiz hatası: " + e.getMessage());
        }
    }

    private String buildAnalysisPrompt(String repoName, String description, String language, 
                                     Integer stars, Integer forks, String lastUpdate, String readmeContent) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("Sen bir yazılım geliştirici ve teknoloji uzmanısın. Bu GitHub repository'sini detaylı analiz et:\n\n");
        
        prompt.append("REPOSITORY BİLGİLERİ:\n");
        prompt.append("- İsim: ").append(repoName).append("\n");
        prompt.append("- Açıklama: ").append(description != null ? description : "Açıklama yok").append("\n");
        prompt.append("- Programlama Dili: ").append(language != null ? language : "Bilinmiyor").append("\n");
        prompt.append("- Yıldız Sayısı: ").append(stars != null ? stars : 0).append("\n");
        prompt.append("- Fork Sayısı: ").append(forks != null ? forks : 0).append("\n");
        prompt.append("- Son Güncelleme: ").append(lastUpdate != null ? lastUpdate : "Bilinmiyor").append("\n");
        
        if (readmeContent != null && !readmeContent.trim().isEmpty()) {
            prompt.append("\nREADME İÇERİĞİ (Tam içerik - DİKKATLİCE OKU!):\n");
            prompt.append(readmeContent).append("\n");
        }
        
        prompt.append("\n\nANALİZ GÖREVİ:\n");
        prompt.append("Bu repository'yi 5 kategoride 0-100 arası puanla ve her kategori için detaylı açıklama ver:\n\n");
        
        prompt.append("1. TEKNOLOJİ KALİTESİ (0-100)\n");
        prompt.append("- Kod kalitesi, mimari, best practices\n");
        prompt.append("- Kullanılan teknolojilerin güncelliği\n");
        prompt.append("- Proje yapısı ve organizasyon\n\n");
        
        prompt.append("2. ÖĞRENME DEĞERİ (0-100)\n");
        prompt.append("- Dokümantasyon kalitesi ve detaylılığı\n");
        prompt.append("- Kod örnekleri, tutorial'lar ve rehberler\n");
        prompt.append("- Kendi seviyesindeki geliştiriciler için öğrenme değeri (başlangıç, orta, ileri seviye)\n");
        prompt.append("- Best practices ve modern yaklaşımların gösterilmesi\n\n");
        
        prompt.append("3. KARİYER GELİŞİMİ (0-100)\n");
        prompt.append("- İş piyasasında değeri\n");
        prompt.append("- Kullanılan teknolojilerin popülerliği\n");
        prompt.append("- Portfolio için uygunluk\n\n");
        
        prompt.append("4. TOPLULUK AKTİFLİĞİ (0-100)\n");
        prompt.append("- Yıldız ve fork sayısı\n");
        prompt.append("- Son güncelleme tarihi\n");
        prompt.append("- Topluluk desteği\n\n");
        
        prompt.append("5. GÜNCELLİK VE SÜRDÜRÜLEBİLİRLİK (0-100)\n");
        prompt.append("- Son commit tarihine göre aktiflik (1-7 gün: 90-100, 8-30 gün: 70-89, 31-90 gün: 50-69, 90+ gün: 30-49)\n");
        prompt.append("- Düzenli güncelleme sıklığı\n");
        prompt.append("- Aktif geliştirme durumu ve gelecek potansiyeli\n\n");
        
        prompt.append("ÇIKTI FORMATI (Markdown - Sadece Türkçe):\n");
        prompt.append("ZORUNLU: Yanıtını markdown formatında ver ve SADECE TÜRKÇE kullan! İngilizce kelime yazma! Tüm açıklamalar, yorumlar ve değerlendirmeler Türkçe olmalı!\n");
        prompt.append("PUANLAMA KURALLARI:\n");
        prompt.append("- GÜNCELLİK: 8 gün içindeyse 80+ puan, 30 gün içindeyse 60+ puan ver\n");
        prompt.append("- ÖĞRENME DEĞERİ: README'de kurulum rehberi, ekran görüntüleri, API dokümantasyonu, teknik detaylar, kullanım örnekleri varsa 80+ puan ver\n");
        prompt.append("- ÖĞRENME DEĞERİ: 70+ puan verdiysen 'yüksek' veya 'iyi' kullan, 30- puan verdiysen 'düşük' kullan\n");
        prompt.append("- İYİLEŞTİRME ÖNERİLERİ: Kesinlikle diğer bölümlerden kopya olmasın, her biri bu projeye özel olsun\n\n");
        prompt.append("# Repository Analizi\n\n");
        prompt.append("## 💡 Genel Öneri ve Değerlendirme\n");
        prompt.append("[ZORUNLU: Bu bölümde README ve about kısmını DİKKATLİCE oku ve KENDİ YORUMUNU yap! Puanlama tablosundaki ifadeleri ASLA kopyalama! README'de gördüğün özellikleri, teknolojileri, proje amacını analiz et ve gerçek bir değerlendirme yaz. Örneğin: 'README'de gördüğüm kadarıyla bu proje...', 'About kısmında belirtilen...', 'Proje yapısına baktığımda...' gibi ifadeler kullan. En az 3-4 paragraf yaz ve her açıdan değerlendir. Eğer kopya yaparsan yanıtı geçersiz sayılacak!]\n\n");
        prompt.append("## 📋 Proje Özeti\n");
        prompt.append("Bu proje [projenin ne yaptığını açıkla]. Bu repository'yi inceleyerek [hangi becerileri kazanabileceğini, hangi teknolojileri öğrenebileceğini] detaylı olarak açıkla.\n\n");
        prompt.append("## 📊 Puanlama Tablosu\n\n");
        prompt.append("| Kategori | Puan | Açıklama |\n");
        prompt.append("|----------|------|----------|\n");
        prompt.append("| 🔧 Teknoloji Kalitesi | **[PUAN]/100** | [Kod kalitesi, mimari, best practices, proje yapısı ve organizasyon hakkında detaylı açıklama] |\n");
        prompt.append("| 📚 Öğrenme Değeri | **[PUAN]/100** | [Dokümantasyon kalitesi, kod örnekleri, tutorial'lar, kendi seviyesindeki geliştiriciler için öğrenme değeri ve best practices gösterimi hakkında detaylı açıklama] |\n");
        prompt.append("| 💼 Kariyer Gelişimi | **[PUAN]/100** | [İş piyasasında değeri, kullanılan teknolojilerin popülerliği, portfolio için uygunluk hakkında detaylı açıklama] |\n");
        prompt.append("| 👥 Topluluk Aktifliği | **[PUAN]/100** | [Yıldız ve fork sayısı, son güncelleme tarihi, topluluk desteği, issue ve PR sayıları hakkında detaylı açıklama] |\n");
        prompt.append("| ⏰ Güncellik | **[PUAN]/100** | [ZORUNLU: Son commit tarihini hesapla ve 'X gün içinde', 'X hafta önce', 'X ay önce' formatında belirt. Örnek: '8 gün içinde commit yapılmış', '2 hafta önce son güncelleme'. Sonra düzenli güncelleme sıklığı, aktif geliştirme durumu ve gelecek potansiyeli hakkında detaylı açıklama] |\n\n");
        prompt.append("Sen bir README inceleme uzmanısın. README içeriğini SATIR SATIR oku ve sadece GERÇEK eksikliklere dayalı somut öneriler ver.\n\n");
        prompt.append("❌ YASAK: 'Daha fazla', 'Geliştirilebilir', 'İyileştirilebilir' gibi genel ifadeler.\n");
        prompt.append("❌ YASAK: Aşağıdaki örnekleri doğrudan çıktıya yazmak.\n");
        prompt.append("✅ ZORUNLU: README'de olmayan veya yetersiz olan kısımları açıkça yaz.\n");
        prompt.append("✅ Eğer ilgili kategori için eksik yoksa 'Eksik bulunmadı' yaz.\n\n");

        prompt.append("📌 Örnek Format (sadece kılavuz, çıktıya yazma!):\n");
        prompt.append("- 'README'de kullanılan veritabanı teknolojisi belirtilmemiş, eklenebilir.'\n");
        prompt.append("- 'Kurulum talimatları eksik, eklenebilir.'\n");
        prompt.append("- 'API dokümantasyonu bulunmuyor, eklenebilir.'\n\n");

        prompt.append("## 🎯 Detaylı İyileştirme Önerileri\n\n");

        prompt.append("**🔧 Teknoloji Kalitesi İyileştirmeleri:**\n");
        prompt.append("- [Öneri 1]\n");
        prompt.append("- [Öneri 2]\n");
        prompt.append("- [Öneri 3]\n\n");

        prompt.append("**📚 Öğrenme Değeri İyileştirmeleri:**\n");
        prompt.append("- [Öneri 1]\n");
        prompt.append("- [Öneri 2]\n");
        prompt.append("- [Öneri 3]\n\n");

        prompt.append("**💼 Kariyer Gelişimi İyileştirmeleri:**\n");
        prompt.append("- [Öneri 1]\n");
        prompt.append("- [Öneri 2]\n");
        prompt.append("- [Öneri 3]\n\n");

        prompt.append("**👥 Topluluk Aktifliği İyileştirmeleri:**\n");
        prompt.append("- [Öneri 1]\n");
        prompt.append("- [Öneri 2]\n");
        prompt.append("- [Öneri 3]\n\n");

        prompt.append("**⏰ Güncellik İyileştirmeleri:**\n");
        prompt.append("- [Öneri 1]\n");
        prompt.append("- [Öneri 2]\n");
        prompt.append("- [Öneri 3]\n\n");
        prompt.append("## 🛠️ Kullanılan Teknolojiler\n");
        prompt.append("[Projede kullanılan tüm teknolojileri listele]\n\n");
        prompt.append("## 🎓 Hedef Kitle\n");
        prompt.append("[Bu repo kimler için uygun, hangi seviyedeki geliştiriciler için ideal]\n\n");
        prompt.append("---\n");
        prompt.append("*Analiz tarihi: ").append(java.time.LocalDate.now()).append("*");
        
        return prompt.toString();
    }

    private String callGroqAPI(String prompt) throws Exception {
        System.out.println("Groq API anahtarı kontrol ediliyor...");
        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            System.err.println("Groq API anahtarı bulunamadı!");
            throw new Exception("Groq API anahtarı bulunamadı");
        }
        System.out.println("Groq API anahtarı mevcut, uzunluk: " + groqApiKey.length());

        // Request body oluştur
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("temperature", 0.3); // Tutarlı sonuçlar için düşük temperature
        requestBody.put("max_tokens", 2000);
        
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        requestBody.put("messages", new Object[]{message});

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + groqApiKey);

        // Request entity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // API çağrısı
        ResponseEntity<String> response = restTemplate.exchange(
            GROQ_API_URL, 
            HttpMethod.POST, 
            entity, 
            String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode choices = rootNode.get("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode messageNode = firstChoice.get("message");
                return messageNode.get("content").asText();
            }
        }

        throw new Exception("Groq API yanıtı başarısız: " + response.getStatusCode());
    }

    public boolean isApiKeyConfigured() {
        return groqApiKey != null && !groqApiKey.trim().isEmpty();
    }
}
