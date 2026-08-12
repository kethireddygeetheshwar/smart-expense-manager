async function renderAnalytics() {
    const app = document.getElementById('app');
    app.innerHTML = renderLayout('<div class="fade-in"><div class="flex items-center justify-between mb-6"><div><h1 class="text-2xl font-bold">Analytics</h1><p class="text-gray-400 text-sm">Deep insights into your finances</p></div></div><div id="analytics-loading" class="flex justify-center py-20"><div class="loading-spinner"></div></div><div id="analytics-content" class="hidden"></div></div>');

    try {
        const [comparison, insights, breakdown] = await Promise.all([
            api.get('/api/analytics/monthly-comparison'),
            api.get('/api/analytics/insights'),
            api.get('/api/analytics/category-breakdown?months=3')
        ]);
        document.getElementById('analytics-loading').classList.add('hidden');
        const content = document.getElementById('analytics-content');
        content.classList.remove('hidden');

        var comparisonColor = comparison.expenseChangePercent > 0 ? 'text-red-400' : 'text-green-400';
        var comparisonIcon = comparison.expenseChangePercent > 0 ? 'fa-arrow-trend-up' : 'fa-arrow-trend-down';
        var incColor = comparison.incomeChangePercent >= 0 ? 'text-green-400' : 'text-red-400';

        var html = '';

        html += '<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">';
        html += '<div class="glass rounded-2xl p-5 card-hover"><p class="text-gray-400 text-sm mb-1">This Month Expenses</p><p class="text-xl font-bold">' + formatCurrency(comparison.thisMonthExpenses) + '</p></div>';
        html += '<div class="glass rounded-2xl p-5 card-hover"><p class="text-gray-400 text-sm mb-1">Last Month Expenses</p><p class="text-xl font-bold">' + formatCurrency(comparison.lastMonthExpenses) + '</p></div>';
        html += '<div class="glass rounded-2xl p-5 card-hover"><p class="text-gray-400 text-sm mb-1">Change</p><p class="text-xl font-bold ' + comparisonColor + '"><i class="fas ' + comparisonIcon + ' mr-2"></i>' + Math.abs(comparison.expenseChangePercent).toFixed(1) + '%</p></div>';
        html += '<div class="glass rounded-2xl p-5 card-hover"><p class="text-gray-400 text-sm mb-1">Avg Daily Spending</p><p class="text-xl font-bold">' + formatCurrency(insights.averageDailySpending) + '</p></div>';
        html += '</div>';

        html += '<div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">';
        html += '<div class="glass rounded-2xl p-5"><h3 class="font-semibold mb-4"><i class="fas fa-chart-pie text-purple-400 mr-2"></i>Income vs Expenses</h3><div class="relative h-56"><canvas id="incomeExpenseChart"></canvas></div></div>';
        html += '<div class="glass rounded-2xl p-5"><h3 class="font-semibold mb-4"><i class="fas fa-layer-group text-blue-400 mr-2"></i>Category Breakdown (3 months)</h3><div class="relative h-56"><canvas id="breakdownChart"></canvas></div></div>';
        html += '</div>';

        html += '<div class="glass rounded-2xl p-5 mb-6"><h3 class="font-semibold mb-4"><i class="fas fa-lightbulb text-yellow-400 mr-2"></i>Insights</h3>';
        html += '<div class="grid grid-cols-1 md:grid-cols-3 gap-4">';
        html += '<div class="bg-dark-800 rounded-xl p-4"><p class="text-xs text-gray-400 mb-1">Top Category</p><p class="font-semibold">' + insights.topCategory + '</p><p class="text-sm text-gray-400">' + formatCurrency(insights.topCategoryAmount) + '</p></div>';
        html += '<div class="bg-dark-800 rounded-xl p-4"><p class="text-xs text-gray-400 mb-1">Projected Monthly</p><p class="font-semibold">' + formatCurrency(insights.projectedMonthlySpending) + '</p><p class="text-sm text-gray-400">Based on daily average</p></div>';
        html += '<div class="bg-dark-800 rounded-xl p-4"><p class="text-xs text-gray-400 mb-1">Income Trend</p><p class="font-semibold ' + incColor + '">' + (comparison.incomeChangePercent >= 0 ? '+' : '') + comparison.incomeChangePercent.toFixed(1) + '%</p><p class="text-sm text-gray-400">vs last month</p></div>';
        html += '</div></div>';

        if (breakdown.categories && breakdown.categories.length > 0) {
            html += '<div class="glass rounded-2xl p-5"><h3 class="font-semibold mb-4"><i class="fas fa-list-ol text-green-400 mr-2"></i>Category Ranking</h3><div class="space-y-3">';
            breakdown.categories.forEach(function(c, i) {
                html += '<div class="flex items-center justify-between"><div class="flex items-center gap-3"><span class="w-6 h-6 rounded-full bg-dark-800 flex items-center justify-center text-xs text-gray-400">' + (i + 1) + '</span><div class="w-8 h-8 rounded-lg flex items-center justify-center" style="background:' + getCategoryColor(c.category) + '20"><i class="fas ' + getCategoryIcon(c.category) + ' text-sm" style="color:' + getCategoryColor(c.category) + '"></i></div><span class="font-medium text-sm">' + c.category + '</span></div><div class="flex items-center gap-4"><span class="font-semibold text-sm">' + formatCurrency(c.amount) + '</span><span class="text-xs text-gray-400 w-12 text-right">' + c.percentage.toFixed(1) + '%</span><div class="w-24 h-2 bg-gray-700 rounded-full"><div class="h-full rounded-full" style="width:' + c.percentage + '%;background:' + getCategoryColor(c.category) + '"></div></div></div></div>';
            });
            html += '</div></div>';
        }

        content.innerHTML = html;

        new Chart(document.getElementById('incomeExpenseChart'), {
            type: 'bar',
            data: {
                labels: ['Last Month', 'This Month'],
                datasets: [
                    { label: 'Income', data: [comparison.lastMonthIncome, comparison.thisMonthIncome], backgroundColor: 'rgba(34,197,94,0.6)', borderColor: '#22c55e', borderWidth: 1, borderRadius: 6 },
                    { label: 'Expenses', data: [comparison.lastMonthExpenses, comparison.thisMonthExpenses], backgroundColor: 'rgba(239,68,68,0.6)', borderColor: '#ef4444', borderWidth: 1, borderRadius: 6 }
                ]
            },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { labels: { color: '#94a3b8' } } }, scales: { x: { ticks: { color: '#94a3b8' }, grid: { display: false } }, y: { ticks: { color: '#94a3b8', callback: function(v) { return '₹' + (v/1000) + 'k'; } }, grid: { color: 'rgba(255,255,255,0.05)' } } } }
        });

        if (breakdown.categories && breakdown.categories.length > 0) {
            new Chart(document.getElementById('breakdownChart'), {
                type: 'doughnut',
                data: {
                    labels: breakdown.categories.map(function(c) { return c.category; }),
                    datasets: [{ data: breakdown.categories.map(function(c) { return c.amount; }), backgroundColor: breakdown.categories.map(function(c) { return getCategoryColor(c.category); }), borderWidth: 0 }]
                },
                options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { color: '#94a3b8', padding: 8, font: { size: 10 } } } }, cutout: '60%' }
            });
        }
    } catch (err) {
        showToast('Failed to load analytics', 'error');
    }
}
