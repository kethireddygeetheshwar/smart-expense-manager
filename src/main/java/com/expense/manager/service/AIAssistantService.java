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
import java.math.RoundingMode;
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
        INTENT_HANDLERS.put("budget_advice", "Provide budget advice");
        INTENT_HANDLERS.put("comparison", "Compare with previous periods");
        INTENT_HANDLERS.put("savings_tips", "Provide savings tips");
        INTENT_HANDLERS.put("category_breakdown", "Break down by category");
        INTENT_HANDLERS.put("general", "General finance question");
    }

    public Map<String, Object> processMessage(Long userId, String message) {
        String intent = detectIntent(message);
        String response;

        switch (intent) {
            case "spending_analysis":
                response = handleSpendingAnalysis(userId);
                break;
            case "budget_advice":
                response = handleBudgetAdvice(userId);
                break;
            case "comparison":
                response = handleComparison(userId);
                break;
            case "savings_tips":
                response = handleSavingsTips(userId, message);
                break;
            case "category_breakdown":
                response = handleCategoryBreakdown(userId);
                break;
            case "health_check":
                response = handleHealthCheck(userId);
                break;
            case "prediction":
                response = handlePrediction(userId);
                break;
            case "anomaly":
                response = handleAnomalyDetection(userId);
                break;
            case "subscriptions":
                response = handleSubscriptions(userId);
                break;
            default:
                response = handleWithLLM(userId, message, intent);
        }

        User user = new User();
        user.setId(userId);

        ChatMessage chatMessage = ChatMessage.builder()
                .message(message)
                .response(response)
                .sender(ChatMessage.Sender.USER)
                .user(user)
                .build();
        chatMessageRepository.save(chatMessage);

        ChatMessage assistantMessage = ChatMessage.builder()
                .message(message)
                .response(response)
                .sender(ChatMessage.Sender.ASSISTANT)
                .user(user)
                .build();
        chatMessageRepository.save(assistantMessage);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", message);
        result.put("response", response);
        result.put("intent", intent);
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    private String detectIntent(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("spend") && (lower.contains("most") || lower.contains("where") || lower.contains("this month"))) {
            return "spending_analysis";
        }
        if (lower.contains("reduce") || lower.contains("save") || lower.contains("cut") || lower.contains("lower")) {
            return "savings_tips";
        }
        if (lower.contains("compare") || lower.contains("last month") || lower.contains("previous")) {
            return "comparison";
        }
        if (lower.contains("budget") && (lower.contains("advice") || lower.contains("suggest") || lower.contains("recommend"))) {
            return "budget_advice";
        }
        if (lower.contains("breakdown") || lower.contains("category") || lower.contains("categories")) {
            return "category_breakdown";
        }
        if (lower.contains("total") || lower.contains("how much") || lower.contains("income") || lower.contains("expense")) {
            return "spending_analysis";
        }
        if (lower.contains("health") || lower.contains("score") || lower.contains("financial status")) {
            return "health_check";
        }
        if (lower.contains("predict") || lower.contains("forecast") || lower.contains("next month") || lower.contains("projected")) {
            return "prediction";
        }
        if (lower.contains("anomaly") || lower.contains("unusual") || lower.contains("strange") || lower.contains("abnormal")) {
            return "anomaly";
        }
        if (lower.contains("goal") || lower.contains("saving for") || lower.contains("target")) {
            return "goals";
        }
        if (lower.contains("subscription") || lower.contains("recurring") || lower.contains("membership")) {
            return "subscriptions";
        }

        return "general";
    }

    private String handleSpendingAnalysis(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByDateRange(userId, startOfMonth, endOfMonth);
        BigDecimal totalIncome = expenseRepository.getTotalIncomeByDateRange(userId, startOfMonth, endOfMonth);
        Expense.Category topCategory = expenseRepository.getTopSpendingCategory(userId, startOfMonth, endOfMonth);

        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (topCategory == null) topCategory = Expense.Category.OTHER_INCOME;

        BigDecimal topCategoryAmount = expenseRepository.getTotalByCategoryAndDateRange(userId, topCategory, startOfMonth, endOfMonth);
        if (topCategoryAmount == null) topCategoryAmount = BigDecimal.ZERO;

        double topCategoryPercent = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                ? topCategoryAmount.multiply(BigDecimal.valueOf(100)).divide(totalExpenses, 1, RoundingMode.HALF_UP).doubleValue()
                : 0;

        return String.format(
            "Here's your spending analysis for %s %d:\n\n" +
            "Total Expenses: ₹%,.2f\n" +
            "Total Income: ₹%,.2f\n" +
            "Net Savings: ₹%,.2f\n\n" +
            "Top spending category: %s (₹%,.2f - %.1f%% of total)\n\n" +
            "Your biggest expense area is %s. Consider reviewing these transactions to find potential savings.",
            now.getMonth(), now.getYear(), totalExpenses, totalIncome,
            totalIncome.subtract(totalExpenses), topCategory.name(),
            topCategoryAmount, topCategoryPercent, topCategory.name()
        );
    }

    private String handleBudgetAdvice(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        List<Object[]> categorySpending = expenseRepository.getCategorySpending(userId, startOfMonth, endOfMonth);

        StringBuilder advice = new StringBuilder();
        advice.append("Here's my budget advice based on your spending:\n\n");

        if (categorySpending.isEmpty()) {
            advice.append("You haven't recorded any expenses this month. Start tracking to get personalized advice!");
            return advice.toString();
        }

        advice.append("Top spending areas:\n");
        int rank = 1;
        for (Object[] row : categorySpending) {
            Expense.Category cat = (Expense.Category) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            advice.append(String.format("  %d. %s: ₹%,.2f\n", rank++, cat.name(), amount));
        }

        advice.append("\nRecommendations:\n");
        advice.append("1. Follow the 50/30/20 rule: 50% needs, 30% wants, 20% savings\n");
        advice.append("2. Set category limits and track them weekly\n");
        advice.append("3. Review subscriptions and recurring payments\n");
        advice.append("4. Use the envelope method for discretionary spending\n");

        return advice.toString();
    }

    private String handleComparison(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate thisMonthStart = now.withDayOfMonth(1);
        LocalDate thisMonthEnd = now.withDayOfMonth(now.lengthOfMonth());
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());

        BigDecimal thisMonthExpenses = expenseRepository.getTotalExpensesByDateRange(userId, thisMonthStart, thisMonthEnd);
        BigDecimal lastMonthExpenses = expenseRepository.getTotalExpensesByDateRange(userId, lastMonthStart, lastMonthEnd);

        if (thisMonthExpenses == null) thisMonthExpenses = BigDecimal.ZERO;
        if (lastMonthExpenses == null) lastMonthExpenses = BigDecimal.ZERO;

        BigDecimal difference = thisMonthExpenses.subtract(lastMonthExpenses);
        String trend;
        double changePercent = 0;

        if (lastMonthExpenses.compareTo(BigDecimal.ZERO) > 0) {
            changePercent = difference.multiply(BigDecimal.valueOf(100))
                    .divide(lastMonthExpenses, 1, RoundingMode.HALF_UP).doubleValue();
        }

        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            trend = "increased";
        } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
            trend = "decreased";
        } else {
            trend = "unchanged";
        }

        StringBuilder comparison = new StringBuilder();
        comparison.append(String.format("Spending Comparison: %s %d vs %s %d\n\n",
                now.getMonth(), now.getYear(), now.minusMonths(1).getMonth(), now.minusMonths(1).getYear()));
        comparison.append(String.format("This Month: ₹%,.2f\n", thisMonthExpenses));
        comparison.append(String.format("Last Month: ₹%,.2f\n", lastMonthExpenses));
        comparison.append(String.format("Difference: ₹%,.2f (%s by %.1f%%)\n\n",
                difference.abs(), trend, Math.abs(changePercent)));

        if ("increased".equals(trend)) {
            comparison.append("Your spending went up this month. Consider reviewing non-essential expenses.");
        } else if ("decreased".equals(trend)) {
            comparison.append("Great job! Your spending decreased this month. Keep it up!");
        } else {
            comparison.append("Your spending is consistent. Look for areas to optimize.");
        }

        return comparison.toString();
    }

    private String handleSavingsTips(Long userId, String message) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByDateRange(userId, startOfMonth, endOfMonth);
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        List<Object[]> categorySpending = expenseRepository.getCategorySpending(userId, startOfMonth, endOfMonth);

        StringBuilder tips = new StringBuilder();
        tips.append("Personalized Savings Tips:\n\n");

        if (!categorySpending.isEmpty()) {
            Expense.Category topCategory = (Expense.Category) categorySpending.get(0)[0];
            BigDecimal topAmount = (BigDecimal) categorySpending.get(0)[1];

            tips.append(String.format("1. Your highest spending is %s (₹%,.2f). ", topCategory.name(), topAmount));

            switch (topCategory) {
                case FOOD:
                    tips.append("Try meal planning and cooking at home more often. Limit eating out to weekends.\n");
                    break;
                case SHOPPING:
                    tips.append("Implement a 48-hour rule before making non-essential purchases.\n");
                    break;
                case TRAVEL:
                    tips.append("Look for deals, use public transport, and plan trips in advance.\n");
                    break;
                case ENTERTAINMENT:
                    tips.append("Look for free events, share subscriptions, and set a monthly entertainment budget.\n");
                    break;
                default:
                    tips.append("Review these expenses and identify what's essential vs. discretionary.\n");
            }
        }

        tips.append("\n2. Automate savings: Set up auto-transfer to savings account on payday\n");
        tips.append("3. Track daily expenses: Small purchases add up quickly\n");
        tips.append("4. Review subscriptions: Cancel unused services\n");
        tips.append("5. Use cashback and rewards: Maximize credit card benefits\n");
        tips.append("6. Set specific savings goals: Having targets motivates saving\n");

        return tips.toString();
    }

    private String handleCategoryBreakdown(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        List<Object[]> results = expenseRepository.getCategorySpending(userId, startOfMonth, endOfMonth);
        BigDecimal total = BigDecimal.ZERO;
        for (Object[] row : results) {
            total = total.add((BigDecimal) row[1]);
        }

        StringBuilder breakdown = new StringBuilder();
        breakdown.append(String.format("Category Breakdown for %s %d:\n\n", now.getMonth(), now.getYear()));

        if (results.isEmpty()) {
            breakdown.append("No expenses recorded this month.");
            return breakdown.toString();
        }

        for (Object[] row : results) {
            Expense.Category category = (Expense.Category) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            double percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0;

            int barLength = (int) (percentage / 5);
            String bar = "█".repeat(Math.max(barLength, 1));

            breakdown.append(String.format("%-15s ₹%,10.2f (%5.1f%%) %s\n",
                    category.name(), amount, percentage, bar));
        }

        breakdown.append(String.format("\n%-15s ₹%,10.2f", "TOTAL", total));

        return breakdown.toString();
    }

    private String handleHealthCheck(Long userId) {
        Map<String, Object> health = healthService.calculateHealthScore(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("Financial Health Score\n\n");
        sb.append("Score: ").append(health.get("score")).append("/100\n");
        sb.append("Status: ").append(health.get("status")).append("\n\n");
        sb.append("Score Breakdown:\n");
        List<Map<String, Object>> factors = (List<Map<String, Object>>) health.get("factors");
        if (factors != null) {
            for (Map<String, Object> f : factors) {
                sb.append(String.format("  %s: %s/%s\n", f.get("name"), f.get("score"), f.get("max")));
            }
        }
        List<String> suggestions = (List<String>) health.get("suggestions");
        if (suggestions != null && !suggestions.isEmpty()) {
            sb.append("\nSuggestions:\n");
            suggestions.forEach(s -> sb.append("  • ").append(s).append("\n"));
        }
        return sb.toString();
    }

    private String handlePrediction(Long userId) {
        LocalDate now = LocalDate.now();
        List<Object[]> monthlyData = expenseRepository.getMonthlySpending(userId, now.getYear());
        if (monthlyData == null || monthlyData.isEmpty()) {
            return "Not enough data to make predictions. Keep tracking your expenses!";
        }
        double total = 0;
        int count = 0;
        for (Object[] row : monthlyData) {
            total += ((BigDecimal) row[1]).doubleValue();
            count++;
        }
        BigDecimal avg = BigDecimal.valueOf(total / Math.max(count, 1)).setScale(0, RoundingMode.HALF_UP);
        return String.format(
            "Monthly Spending Forecast\n\n" +
            "Based on your spending pattern:\n" +
            "Predicted spending: ₹%,.0f\n\n" +
            "At your current rate, you're projected to spend around this amount this month.\n" +
            "Tip: Set budgets below this amount to build savings!",
            avg.doubleValue()
        );
    }

    private String handleAnomalyDetection(Long userId) {
        LocalDate now = LocalDate.now();
        List<Object[]> current = expenseRepository.getCategorySpending(userId, now.withDayOfMonth(1), now);
        List<Object[]> historical = expenseRepository.getMonthlyAvgByCategory(userId, now.minusMonths(3), now.minusDays(1));
        if (current == null || current.isEmpty()) {
            return "No recent spending data to analyze for anomalies.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Anomaly Detection\n\n");
        boolean found = false;
        for (Object[] row : current) {
            Expense.Category cat = (Expense.Category) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            if (amount.compareTo(new BigDecimal("5000")) > 0) {
                found = true;
                sb.append(String.format("  %s: ₹%,.0f — significantly high\n", cat.name(), amount.doubleValue()));
            }
        }
        if (!found) {
            sb.append("No unusual spending detected. Your expenses look normal!");
        }
        return sb.toString();
    }

    private String handleSubscriptions(Long userId) {
        List<Expense> recurring = expenseRepository.findRecurringExpenses(userId);
        if (recurring.isEmpty()) {
            return "No recurring subscriptions detected. Mark expenses as 'recurring' to track subscriptions here.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Recurring Payments Detected\n\n");
        BigDecimal total = BigDecimal.ZERO;
        for (Expense e : recurring) {
            sb.append(String.format("  %s: ₹%,.2f/month\n", e.getDescription(), e.getAmount()));
            total = total.add(e.getAmount());
        }
        sb.append(String.format("\nTotal recurring: ₹%,.2f/month\n", total));
        sb.append(String.format("Annual cost: ₹%,.2f/year\n", total.multiply(BigDecimal.valueOf(12))));
        return sb.toString();
    }

    private String handleWithLLM(Long userId, String message, String intent) {
        String lower = message.toLowerCase();

        try {
            String context = buildUserContext(userId);
            String prompt = String.format(
                "You are FinSight AI, a personal finance assistant. Based on the following user data, answer their question helpfully.\n\n" +
                "User Financial Context:\n%s\n\n" +
                "User Question: %s\n\n" +
                "Provide a concise, helpful response with specific numbers when possible. Be conversational and friendly.",
                context, message
            );

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> Mono.just(Map.of("error", true)))
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> msg = (Map<String, Object>) choice.get("message");
                    return (String) msg.get("content");
                }
            }
        } catch (Exception e) {
            // Fallback to smart local response
        }

        return handleSmartLocalResponse(userId, lower);
    }

    private String handleSmartLocalResponse(Long userId, String lower) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal income = expenseRepository.getTotalIncomeByDateRange(userId, startOfMonth, endOfMonth);
        BigDecimal expenses = expenseRepository.getTotalExpensesByDateRange(userId, startOfMonth, endOfMonth);
        if (income == null) income = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;
        BigDecimal savings = income.subtract(expenses);

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hello! I'm FinSight, your AI financial assistant. 👋\n\nI can help you with:\n• Analyzing your spending patterns\n• Comparing months\n• Finding savings opportunities\n• Checking your financial health\n• Detecting unusual expenses\n• Predicting future spending\n\nWhat would you like to know about your finances?";
        }
        if (lower.contains("how am i doing") || lower.contains("financial status") || lower.contains("summary")) {
            return String.format("Financial Summary for %s %d\n\nIncome: ₹%,.0f\nExpenses: ₹%,.0f\nSavings: ₹%,.0f\nSavings Rate: %.1f%%\n\n%s", now.getMonth(), now.getYear(), income, expenses, savings, income.compareTo(BigDecimal.ZERO) > 0 ? savings.multiply(BigDecimal.valueOf(100)).divide(income, 1, RoundingMode.HALF_UP).doubleValue() : 0, savings.compareTo(BigDecimal.ZERO) > 0 ? "Great! You're saving money this month. Keep it up! 💪" : "You're spending more than you earn this month. Let's find ways to cut back.");
        }
        if (lower.contains("increase") || lower.contains("why") || lower.contains("reason")) {
            return handleWhyExpensesIncreased(userId);
        }
        if (lower.contains("afford") || lower.contains("can i buy") || lower.contains("should i buy")) {
            return "Based on your current financial situation:\n\nMonthly Income: ₹" + String.format("%,.0f", income) + "\nMonthly Expenses: ₹" + String.format("%,.0f", expenses) + "\nAvailable: ₹" + String.format("%,.0f", savings) + "\n\nBefore making a large purchase, consider:\n• Will this leave you with at least ₹5,000 emergency funds?\n• Can you afford it without going into debt?\n• Is this a need or a want?\n\nTip: If the cost is more than your monthly savings, consider waiting and saving up first.";
        }
        if (lower.contains("invest") || lower.contains("investment") || lower.contains("sip") || lower.contains("mutual fund")) {
            return "Investment Guidance\n\nYour monthly surplus: ₹" + String.format("%,.0f", savings) + "\n\nGeneral recommendations:\n• Emergency fund first (3-6 months of expenses)\n• Start a SIP with 20-30% of your savings\n• Diversify across equity and debt\n• Consider tax-saving investments (ELSS)\n\nWith ₹" + String.format("%,.0f", savings) + " available, you could invest ₹" + String.format("%,.0f", savings.multiply(BigDecimal.valueOf(30)).divide(BigDecimal.valueOf(100))) + "/month while keeping the rest as buffer.";
        }
        if (lower.contains("help") || lower.contains("what can you do") || lower.contains("features")) {
            return "I'm FinSight AI, and here's what I can help you with:\n\n📊 Spending Analysis — \"Where did I spend the most?\"\n📈 Monthly Comparison — \"Compare with last month\"\n💰 Savings Tips — \"How can I reduce expenses?\"\n🏥 Health Score — \"What is my financial health score?\"\n🔮 Predictions — \"Predict my spending\"\n🚨 Anomalies — \"Any unusual spending?\"\n🔄 Subscriptions — \"Show my recurring payments\"\n🎯 Goals — \"How are my goals going?\"\n💡 General — Ask me anything about your finances!";
        }

        return String.format("Based on your current month's data:\n\nIncome: ₹%,.0f\nExpenses: ₹%,.0f\nNet: ₹%,.0f\n\nI can give you more specific insights if you ask about:\n• Top spending categories\n• Month-over-month comparison\n• Budget recommendations\n• Savings strategies\n• Financial health score\n\nTry asking something specific!",
            income.doubleValue(), expenses.doubleValue(), savings.doubleValue());
    }

    private String handleWhyExpensesIncreased(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate thisStart = now.withDayOfMonth(1);
        LocalDate thisEnd = now.withDayOfMonth(now.lengthOfMonth());
        LocalDate lastStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastEnd = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());

        BigDecimal thisExp = expenseRepository.getTotalExpensesByDateRange(userId, thisStart, thisEnd);
        BigDecimal lastExp = expenseRepository.getTotalExpensesByDateRange(userId, lastStart, lastEnd);
        if (thisExp == null) thisExp = BigDecimal.ZERO;
        if (lastExp == null) lastExp = BigDecimal.ZERO;

        BigDecimal diff = thisExp.subtract(lastExp);
        double pct = lastExp.compareTo(BigDecimal.ZERO) > 0 ? diff.multiply(BigDecimal.valueOf(100)).divide(lastExp, 1, RoundingMode.HALF_UP).doubleValue() : 0;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Your %s spending: ₹%,.0f\n", now.getMonth(), thisExp.doubleValue()));
        sb.append(String.format("Your %s spending: ₹%,.0f\n", now.minusMonths(1).getMonth(), lastExp.doubleValue()));
        sb.append(String.format("Difference: ₹%,.0f (%s%.1f%%)\n\n", diff.abs().doubleValue(), diff.compareTo(BigDecimal.ZERO) > 0 ? "+" : "-", Math.abs(pct)));

        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("Main reasons for the increase:\n");
            List<Object[]> cats = expenseRepository.getCategorySpending(userId, thisStart, thisEnd);
            if (cats != null && !cats.isEmpty()) {
                for (int i = 0; i < Math.min(3, cats.size()); i++) {
                    String cat = ((Expense.Category) cats.get(i)[0]).name();
                    BigDecimal amt = (BigDecimal) cats.get(i)[1];
                    sb.append(String.format("  • %s: ₹%,.0f\n", cat, amt.doubleValue()));
                }
            }
            sb.append("\nTip: Review your top categories to find areas to cut back.");
        } else {
            sb.append("Great news! Your spending decreased this month. Keep up the good work! 🎉");
        }
        return sb.toString();
    }

    private String buildUserContext(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByDateRange(userId, startOfMonth, endOfMonth);
        BigDecimal totalIncome = expenseRepository.getTotalIncomeByDateRange(userId, startOfMonth, endOfMonth);
        Expense.Category topCategory = expenseRepository.getTopSpendingCategory(userId, startOfMonth, endOfMonth);

        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        return String.format(
            "- Current month expenses: ₹%,.2f\n" +
            "- Current month income: ₹%,.2f\n" +
            "- Top spending category: %s\n" +
            "- Savings: ₹%,.2f",
            totalExpenses, totalIncome,
            topCategory != null ? topCategory.name() : "N/A",
            totalIncome.subtract(totalExpenses)
        );
    }

    public List<ChatMessage> getChatHistory(Long userId) {
        return chatMessageRepository.findTop20ByUserIdOrderByTimestampDesc(userId);
    }
}
