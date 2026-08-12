# 💰 Smart Expense Manager — AI-Powered Finance Platform

![Java](https://img.shields.io/badge/Java-17-red) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue) ![AI](https://img.shields.io/badge/AI-Powered-purple)

> Full-stack expense management platform with AI financial copilot, ML anomaly detection, and intelligent budgeting.

## 🚀 Live Demo

**https://smart-expense-manager.onrender.com**

**Demo Login:**
- Email: `demo@expense.com`
- Password: `password123`

## ✨ Key Features

### 🤖 AI Financial Copilot (FinSight)
- Answers natural language questions about your finances
- Analyzes spending patterns, compares months, suggests savings
- Detects unusual expenses and explains anomalies

### 🧠 Machine Learning
- **Anomaly Detection** — Z-score based unusual spending detection
- **Spending Prediction** — Moving average + trend forecasting
- **Subscription Detection** — Auto-detects recurring payments

### 💰 Smart Finance
- **Money Health Score** — 0-100 score based on 5 financial factors
- **Financial Goals** — Track savings goals with progress
- **Budget Management** — Category budgets with 90% alerts
- **Gamification** — 8 achievements to unlock

### 📊 Analytics
- Interactive charts (click categories to drill down)
- Monthly comparison with trend analysis
- Financial timeline with grouped transactions
- What-If savings simulator

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2 |
| Security | Spring Security + JWT |
| Database | PostgreSQL, JPA/Hibernate |
| AI/ML | Custom z-score anomaly detection, spending prediction |
| Frontend | Vanilla JS, Tailwind CSS, Chart.js |
| Deployment | Render.com |

## 📁 Architecture

```
com.finance.expensemanager
├── auth/          # JWT Authentication
├── expense/       # Expense CRUD + Search
├── budget/        # Budget management
├── goal/          # Financial goals
├── analytics/     # Spending analytics
├── intelligence/  # Health score, anomalies, predictions
├── ml/            # ML modules
└── ai/            # AI Copilot integration
```

## 🏆 Achievements System
- 🏆 First Saver — Save ₹1,000
- 🔥 7-Day Streak — Stay under budget for 7 days
- 👑 Budget Master — Stay within budget 3 months
- 🎯 Goal Crusher — Complete a financial goal
- ✂️ Expense Cutter — Reduce expenses by 10%

## 📊 Database Schema
- Users, Expenses, Budgets, Goals, Achievements, Chat Messages

## 🔧 Local Setup

```bash
git clone https://github.com/YOUR_USERNAME/smart-expense-manager.git
cd smart-expense-manager

# Configure database in src/main/resources/application.yml
# Then run:
mvn clean install
mvn spring-boot:run

# Open http://localhost:8080
```

## 📝 API Documentation

| Endpoint | Description |
|----------|-------------|
| `POST /api/auth/register` | Register new user |
| `POST /api/auth/login` | Login with JWT |
| `GET /api/dashboard` | Full dashboard data |
| `POST /api/expenses` | Add expense/income |
| `GET /api/budgets/alerts` | Budget warnings |
| `GET /api/intelligence/health-score` | Financial health score |
| `GET /api/intelligence/anomalies` | Unusual spending alerts |
| `GET /api/intelligence/predictions` | ML spending forecast |
| `POST /api/assistant/chat` | AI Copilot chat |
| `GET /api/achievements` | User achievements |
| `POST /api/goals` | Create financial goal |

---

Built with ❤️ using Java + Spring Boot + AI
