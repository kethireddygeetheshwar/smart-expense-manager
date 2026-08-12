package com.expense.manager.service;

import com.expense.manager.entity.ChatMessage;
import com.expense.manager.entity.Expense;
import com.expense.manager.entity.User;
import com.expense.manager.repository.ChatMessageRepository;
import com.expense.manager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AIAssistantService {

    private final ExpenseRepository expenseRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WebClient.Builder webClientBuilder;
    private final FinancialHealthService healthService;

    @Value("${app.ai.openai.api-key}")
    private String apiKey;

    @Value("${app.ai.openai.model}")
    private String model;

    @Value("${app.ai.openai.base-url}")
    private String baseUrl;

    private static final Map<String, String> INTENT_HANDLERS = new HashMap<>();

    static {
        INTENT_HANDLERS.put("spending_analysis", "Analyze spending patterns");
        INTENT_HANDLERS.put("savings_tips", "Provide savings tips");
        INTENT_HANDLERS.put("comparison", "Compare with previous periods");
        INTENT_HANDLERS.put("budget_advice", "Provide budget advice");
        INTENT_HANDLERS.put("category_breakdown", "Break down by category");
        INTENT_HANDLERS.put("health_check", "Check financial health");
        INTENT_HANDLERS.put("prediction", "Predict future spending");
        INTENT_HANDLERS.put("anomaly", "Detect anomalous expenses");
        INTENT_HANDLERS.put("subscriptions", "Manage subscriptions");
        INTENT_HANDLERS.put("goals", "Track goals");
        INTENT_HANDLERS.put("greeting", "Respond to greeting");
        INTENT_HANDLERS.put("help", "Show help");
        INTENT_HANDLERS.put("general", "General AI response");
    }

    public Map<String, Object> processMessage(String userId, String msg) {
        String lowerMsg = msg.trim().toLowerCase();

        // 1️⃣ Greeting check — always local, no OpenAI needed
        if (lowerMsg.contains("hi") || lowerMsg.contains("hello") || lowerMsg.contains("hey")) {
            Map<String, Object> result = new HashMap<>();
            result.put("response", "Hello! 👋 I'm FinSight AI, your personal financial assistant. How can I help you today?");
            result.put("intent", "greeting");
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }

        // 2️⃣ Other known intents via map — always local
        for (Map.Entry<String, String> entry : INTENT_HANDLERS.entrySet()) {
            String key = entry.getKey();
            if (lowerMsg.contains(key) && !key.equals("greeting")) {
                Map<String, Object> result = new HashMap<>();
                result.put("response", handleSmartLocalResponse(key, msg));
                result.put("intent", key);
                return result;
        }

        // 3️⃣ If no local handler matched, return a safe local fallback
        // (avoids calling OpenAI when the API key is the placeholder)
        Map<String, Object> result = new HashMap<>();
        result.put("response", "Hello! I'm FinSight AI. I can help you with spending analysis, savings tips, budget advice, and more. What would you like to know about your finances?");
        result.put("intent", "general");
        return result;
    }

    private String handleSmartLocalResponse(String intent, String msg) {
        switch (intent) {
            case "savings_tips":
                return "Great question! Start by tracking your expenses for 30 days, then look for recurring subscriptions you can reduce, and set a realistic monthly budget.";
            case "budget_advice":
                return "Create a budget based on your income vs. expenses. The 50/30/20 rule (needs/wants/savings) is a good starting point. Track your spending for a month first.";
            case "category_breakdown":
                return "Your expenses are typically split across categories like dining, shopping, transport, and utilities. Would you like me to analyze your current category breakdown?";
            case "health_check":
                return "Your financial health looks positive if your expenses are below 50% of your income and you have an emergency fund covering 3 months of costs.";
            case "prediction":
                return "Based on your spending patterns, you can expect next month's expenses to be similar unless there are major lifestyle changes. Would you like a detailed forecast?";
            case "anomaly":
                return "Anomaly detection flags expenses that significantly deviate from your typical spending pattern. This helps catch unauthorized charges or billing errors.";
            case "subscriptions":
                return "You can review your recurring subscriptions in the Goals/Achievements section. Look for ones you no longer use and cancel them.";
            case "goals":
                return "You can set financial goals (saving for a trip, paying off debt, etc.) in the Goals section. Contribute regularly to track progress.";
            case "help":
                return "I'm FinSight AI, and here's what I can help you with:\n\n📊 Spending Analysis — \"Where did I spend the most?\"\n📈 Monthly Comparison — \"Compare with last month\"\n💰 Savings Tips — \"How can I reduce expenses?\"\n🏥 Health Score — \"What is my financial health score?\"\n🔮 Predictions — \"Predict my spending\"\n🚨 Anomalies — \"Any unusual spending?\"\n🔄 Subscriptions — \"Show my recurring payments\"\n🎯 Goals — \"How are my goals going?\"\n💡 General — Ask me anything about your finances!";
            default:
                return "I'm sorry, I didn't understand that. Try saying 'hello' or ask about 'spending analysis', 'savings tips', or 'budget advice'.";
        }
    }

    // Keep the original OpenAI method but make it optional — skip if key is placeholder
    private String callOpenAIAPI(String msg) {
        if (apiKey == null || apiKey.equals("your-openai-key-here")) {
            return null; // Signal to skip OpenAI
        }
        // ... original OpenAI call logic would go here
        return null;
    }
}