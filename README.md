# 🚀 GitHubList - AI-Powered Repository Analysis Platform

> Intelligent project evaluation with AI-powered GitHub repository analysis system

_
"Is this repository suitable for learning?"_

_"Does my repository contain authoritative content?"_



GitHubList is a modern web application that analyzes GitHub repositories with AI support and provides project evaluation for developers. With Groq AI technology, it analyzes repositories in detail across five different categories and offers improvement recommendations.

## 📱 Screenshots
<img width="2559" height="1185" alt="Ekran görüntüsü 2025-08-17 023430" src="https://github.com/user-attachments/assets/98ef5c05-f096-4d79-bd5c-f63632c2f3ed" />
<img width="2557" height="1182" alt="Ekran görüntüsü 2025-08-17 023836" src="https://github.com/user-attachments/assets/fac32967-cce9-4231-aea7-d0052246c11e" />



### 🖥️ Desktop Experience

**🔍 Analysis Results - Web Interface**  
Analysis Results Web

**📊 Repository Details - Web Interface**  
Repository Details Web

## ✨ Features

* **🤖 AI-Powered Analysis**: Powerful repository analysis with Groq AI (Llama3-70b-8192)
* **📊 5-Category Scoring**: Technology quality, learning value, career development, community activity, timeliness
* **🔍 Smart README Analysis**: Detaylı dokümantasyon değerlendirmesi
* **⚡ Real-time GitHub API**: Detailed documentation evaluation
* **📱 Modern UI**: TResponsive design with thymeleaf + Bootstrap
* **🔗 Redis Caching**: Quick analysis results
* **🎯 Detailed Recommendations**: Specific improvement suggestions for each category
* **📈 Performance Metrics**: Star, fork, commit date analysis

## 🛠️ Tech Stack

### Backend

* **Java 17** - Core programming language
* **Spring Boot 3.5.4** - Modern web framework
* **Spring Data JPA** - Database operations
* **Spring Data Redis** - Caching layer
* **Thymeleaf** - Server-side templating
* **PostgreSQL** - Primary database
* **Redis** - Cache database
* **Groq AI API** - AI analysis service

### DevOps & Tools

* **Docker** - Containerization
* **Docker Compose** - Multi-container orchestration
* **Maven** - Build automation
* **GitHub API** - Repository data fetching

### Dependencies

* **Jackson** - JSON processing
* **Commonmark** - Markdown processing
* **Spring Validation** - Input validation
* **Spring DevTools** - Development utilities

## 📁 Project Structure

```
githubList/
├── src/main/java/com/example/githubList/
│   ├── controller/           # Web controllers
│   │   └── RepositoryAnalysisController.java
│   ├── service/             # Business logic
│   │   ├── GitHubApiService.java
│   │   ├── GroqAIService.java
│   │   └── CacheService.java
│   ├── model/               # Data models
│   │   ├── GitHubRepository.java
│   │   └── RepositoryAnalysis.java
│   ├── config/              # Configuration classes
│   └── util/                # Utility classes
├── src/main/resources/
│   ├── templates/           # Thymeleaf templates
│   │   └── analyze.html
│   └── application.yml      # Application configuration
├── docker-compose.yml       # Docker services
├── Dockerfile              # Application container
├── pom.xml                 # Maven dependencies
└── README.md               # This file
```

## 🚀 Quick Start

### Prerequisites

* **Java 17+** - [Download Java](https://adoptium.net/)
* **Docker & Docker Compose** - [Download Docker](https://www.docker.com/)
* **Git** - [Download Git](https://git-scm.com/)

### API Keys

* **GitHub Token** - [GitHub Personal Access Token](https://github.com/settings/tokens)
* **Groq API Key** - [Groq API Key](https://console.groq.com/)

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/your-username/githubList.git
cd githubList
```

2. **Environment Configuration**

```bash
# Copy environment template
cp .env.example .env

# Edit .env file with your API keys
GITHUB_TOKEN=your_github_token_here
GROQ_API_KEY=your_groq_api_key_here
```

3. **Run with Docker**

```bash
# Start all services
docker-compose up --build -d

# Check logs
docker-compose logs -f app
```

4. **Access the Application**

Visit `http://localhost:8080/analyze` to use the application.

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the root directory:

```bash
# GitHub API
GITHUB_TOKEN=your_github_token_here

# Groq AI
GROQ_API_KEY=your_groq_api_key_here

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/githubList
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Redis
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379
```

### Docker Services

* **Application**: `http://localhost:8080`
* **PostgreSQL**: `localhost:5433`
* **Redis**: `localhost:6379`

### API Endpoints

* `GET /analyze` - Analysis page
* `POST /analyze` - Repository analysis
* `POST /clear-cache` - Cache clearing

## 📊 Analysis Categories

### 1. 🔧 Technology Quality (0-100)
* Code quality and architecture
* Best practices implementation
* Technology stack currency
* Project structure and organization

### 2. 📚 Learning Value (0-100)
* Documentation quality
* Code examples and tutorials
* README comprehensiveness
* Developer level suitability

### 3. 💼 Career Growth (0-100)
* Market value
* Technology popularity
* Portfolio suitability
* Career development contribution

### 4. 👥 Community Activity (0-100)
* Star and fork counts
* Recent update activity
* Community support
* Issue and PR numbers

### 5. ⏰ Recency (0-100)
* Last commit date analysis
* Regular update frequency
* Active development status
* Future potential

## 🎯 Usage Examples

### Analyze a Repository

1. Go to the web interface: `http://localhost:8080/analyze`
2. Enter GitHub repository URL or `owner/repo` format
3. Click "Analyze" button
4. Review detailed analysis results

### Supported Formats

```
https://github.com/owner/repo.git
owner/repo
repo-name (default owner: Baranll0)
```

### Cache Management

* Analysis results are cached in Redis
* Use "Clear Cache" button to clear cache
* Same repository analysis loads from cache

## 📈 Data Collection

The project analyzes GitHub repositories using:

```bash
# GitHub API integration
GET /repos/{owner}/{repo}
GET /repos/{owner}/{repo}/readme
GET /repos/{owner}/{repo}/commits

# Groq AI analysis
POST /api/groq/analyze
```

This provides comprehensive repository data and AI-powered analysis.

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit your changes** (`git commit -m 'Add amazing feature'`)
4. **Push to the branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### Development Guidelines

* Follow Java 17 coding standards
* Use Spring Boot best practices
* Write meaningful commit messages
* Add tests for new features
* Update documentation as needed
* Write documentation in Turkish

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

* **GitHub API** - Repository data source
* **Groq AI** - High-performance LLM services
* **Spring Boot** - Modern web framework
* **Docker** - Containerization platform
* **PostgreSQL & Redis** - Data storage solutions

## 📞 Contact & Support

* **Repository**: https://github.com/your-username/githubList
* **Issues**: https://github.com/your-username/githubList/issues
* **Discussions**: https://github.com/your-username/githubList/discussions

## 🚀 Roadmap

* [ ] **Mobile App** - React Native mobile application
* [ ] **Advanced Filtering** - Enhanced filtering options
* [ ] **User Reviews** - User comments and ratings
* [ ] **Comparison Feature** - Repository comparison
* [ ] **Personalized Recommendations** - AI-powered recommendations
* [ ] **Dark Mode** - Dark theme support
* [ ] **Multi-language UI** - Internationalization
* [ ] **Analytics Dashboard** - Advanced analytics panel
* [ ] **API Documentation** - Swagger/OpenAPI documentation
* [ ] **Webhook Support** - GitHub webhook integration

---

⭐ **If you find this project helpful, please give it a star!**

Made with ❤️ by [Your Name]
