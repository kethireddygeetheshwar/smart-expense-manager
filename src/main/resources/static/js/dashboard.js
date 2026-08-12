async function renderDashboard() {
    var user = JSON.parse(localStorage.getItem('user') || '{}');
    var app = document.getElementById('app');
    app.innerHTML = renderLayout('<div class="fade-in"><div class="flex items-center justify-between mb-8"><div><h1 class="text-2xl font-extrabold tracking-tight flex items-center gap-3"><span>Good evening</span><span class="neon-text">' + (user.fullName || 'User') + '</span>👋</h1><p class="text-gray-400 text-sm mt-1">Here\'s your financial overview</p></div><button onclick="openAddModal()" class="px-5 py-2.5 gradient-accent rounded-xl font-bold text-midnight-900 btn-glow text-sm hidden md:flex items-center gap-2"><i class="fas fa-plus-circle"></i>Add Transaction</button></div><div id="dashboard-loading" class="flex justify-center py-20"><div class="loading-spinner"></div></div><div id="dashboard-content" class="hidden"></div><button onclick="openAddModal()" class="md:hidden fixed bottom-6 right-6 w-14 h-14 gradient-accent rounded-full flex items-center justify-center btn-glow z-40"><i class="fas fa-plus text-xl text-midnight-900"></i></button></div>');

    try {
        var results = await Promise.allSettled([
            api.get('/api/dashboard'),
            api.get('/api/intelligence/health-score'),
            api.get('/api/intelligence/anomalies'),
            api.get('/api/intelligence/predictions')
        ]);
        var dashData = results[0].status === 'fulfilled' ? results[0].value : { totalIncome: 0, totalExpenses: 0, remaining: 0, savingsRate: 0, categorySpendings: [], monthlySpendings: [] };
        var healthScore = results[1].status === 'fulfilled' ? results[1].value : { score: 0, status: 'N/A', factors: [] };
        var anomalies = results[2].status === 'fulfilled' ? results[2].value : [];
        var prediction = results[3].status === 'fulfilled' ? results[3].value : {};

        document.getElementById('dashboard-loading').classList.add('hidden');
        var content = document.getElementById('dashboard-content');
        content.classList.remove('hidden');

        var html = '';
        html += '<div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">';
        html += '<div class="glass rounded-2xl p-5 card-hover border-t-2 border-emerald-500/50"><div class="flex items-center justify-between mb-3"><div class="w-10 h-10 rounded-xl bg-emerald-500/15 flex items-center justify-center"><i class="fas fa-arrow-down text-emerald-400"></i></div><span class="text-xs text-gray-500 bg-white/5 px-2 py-1 rounded-md">This Month</span></div><p class="text-xs text-gray-400 mb-1">Total Income</p><p class="text-2xl font-extrabold text-emerald-400">' + formatCurrency(dashData.totalIncome) + '</p></div>';
        html += '<div class="glass rounded-2xl p-5 card-hover border-t-2 border-rose-500/50"><div class="flex items-center justify-between mb-3"><div class="w-10 h-10 rounded-xl bg-rose-500/15 flex items-center justify-center"><i class="fas fa-arrow-up text-rose-400"></i></div><span class="text-xs text-gray-500 bg-white/5 px-2 py-1 rounded-md">This Month</span></div><p class="text-xs text-gray-400 mb-1">Total Expenses</p><p class="text-2xl font-extrabold text-rose-400">' + formatCurrency(dashData.totalExpenses) + '</p></div>';
        html += '<div class="glass rounded-2xl p-5 card-hover border-t-2 border-midnight-400/50"><div class="flex items-center justify-between mb-3"><div class="w-10 h-10 rounded-xl bg-midnight-500/20 flex items-center justify-center"><i class="fas fa-piggy-bank text-midnight-300"></i></div><span class="text-xs text-gray-500 bg-white/5 px-2 py-1 rounded-md">' + dashData.savingsRate + '% rate</span></div><p class="text-xs text-gray-400 mb-1">Remaining</p><p class="text-2xl font-extrabold neon-text">' + formatCurrency(dashData.remaining) + '</p></div>';
        html += '</div>';

        html += '<div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">';
        html += '<div class="lg:col-span-2 glass rounded-2xl p-6"><h3 class="font-bold mb-5 flex items-center gap-2 text-lg"><i class="fas fa-chart-pie text-midnight-400"></i>Where did my money go?</h3><div class="relative h-64 mb-4"><canvas id="categoryChart"></canvas></div><div id="category-details" class="mt-4"></div></div>';
        html += '<div class="glass rounded-2xl p-6"><div class="text-center mb-4"><div class="relative w-36 h-36 mx-auto mb-3"><svg class="w-full h-full transform -rotate-90" viewBox="0 0 36 36"><path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="#1e293b" stroke-width="3"/><path id="health-ring" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="' + getHealthColor(healthScore.score) + '" stroke-width="3" stroke-linecap="round" stroke-dasharray="' + healthScore.score + ', 100"/></svg><div class="absolute inset-0 flex flex-col items-center justify-center"><span id="health-score-value" class="text-3xl font-extrabold">' + healthScore.score + '</span><span class="text-xs text-gray-400" id="health-status">' + healthScore.status + '</span></div></div><p class="text-xs text-gray-400 font-medium">MONEY HEALTH SCORE</p></div><div id="health-factors" class="space-y-2 mt-4"></div></div>';
        html += '</div>';

        if (anomalies && anomalies.length > 0) {
            html += '<div class="mb-8"><h3 class="font-bold mb-4 flex items-center gap-2"><i class="fas fa-exclamation-triangle text-amber-400"></i>Unusual Spending Detected</h3><div class="space-y-3">';
            anomalies.forEach(function(a) {
                var borderColor = a.severity === 'HIGH' ? 'border-rose-500/40' : 'border-amber-500/30';
                html += '<div class="glass rounded-xl p-4 border ' + borderColor + ' flex items-center justify-between"><div class="flex items-center gap-3"><div class="w-10 h-10 rounded-xl ' + (a.severity === 'HIGH' ? 'bg-rose-500/20' : 'bg-amber-500/20') + ' flex items-center justify-center"><i class="fas ' + (a.severity === 'HIGH' ? 'fa-triangle-exclamation text-rose-400' : 'fa-chart-line text-amber-400') + '"></i></div><div><p class="font-semibold text-sm">' + a.category + '</p><p class="text-xs text-gray-400">' + a.message + '</p></div></div><div class="text-right"><span class="text-xs font-bold ' + (a.severity === 'HIGH' ? 'text-rose-400' : 'text-amber-400') + '">' + a.increasePercent + '% ↑</span><p class="text-xs text-gray-500">Anomaly: ' + a.anomalyProbability + '%</p></div></div>';
            });
            html += '</div></div>';
        }

        if (prediction && prediction.predictedAmount) {
            html += '<div class="mb-8 glass rounded-2xl p-6"><h3 class="font-bold mb-3 flex items-center gap-2"><i class="fas fa-wand-magic-sparkles text-purple-400"></i>Monthly Forecast</h3><div class="flex items-center gap-4"><div class="flex-1"><div class="flex justify-between text-sm mb-2"><span class="text-gray-400">Predicted Spending</span><span class="font-bold neon-text">' + formatCurrency(prediction.predictedAmount) + '</span></div><div class="w-full h-3 bg-surface-800 rounded-full overflow-hidden"><div class="h-full rounded-full gradient-accent" style="width:60%"></div></div><p class="text-xs text-gray-500 mt-2"><i class="fas fa-shield-halved mr-1 text-electric-400"></i>Confidence: ' + prediction.confidence + '% • Trend: ' + prediction.trend + '</p></div></div></div>';
        }

        html += '<div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">';
        html += '<div class="glass rounded-2xl p-6"><h3 class="font-bold mb-4 flex items-center gap-2"><i class="fas fa-chart-bar text-electric-400"></i>Monthly Trend</h3><div class="relative h-56"><canvas id="monthlyChart"></canvas></div></div>';
        html += '<div class="glass rounded-2xl p-6"><h3 class="font-bold mb-4 flex items-center gap-2"><i class="fas fa-bullseye text-neon-400"></i>Quick Actions</h3><div class="grid grid-cols-2 gap-3"><a href="#assistant" class="p-4 rounded-xl bg-midnight-600/30 hover:bg-midnight-600/50 transition text-center group"><i class="fas fa-robot text-2xl text-midnight-300 group-hover:text-neon-400 mb-2 transition"></i><p class="text-xs font-medium">Ask AI</p></a><a href="#goals" class="p-4 rounded-xl bg-midnight-600/30 hover:bg-midnight-600/50 transition text-center group"><i class="fas fa-flag text-2xl text-midnight-300 group-hover:text-neon-400 mb-2 transition"></i><p class="text-xs font-medium">Goals</p></a><a href="#budgets" class="p-4 rounded-xl bg-midnight-600/30 hover:bg-midnight-600/50 transition text-center group"><i class="fas fa-wallet text-2xl text-midnight-300 group-hover:text-neon-400 mb-2 transition"></i><p class="text-xs font-medium">Budgets</p></a><a href="#analytics" class="p-4 rounded-xl bg-midnight-600/30 hover:bg-midnight-600/50 transition text-center group"><i class="fas fa-chart-line text-2xl text-midnight-300 group-hover:text-neon-400 mb-2 transition"></i><p class="text-xs font-medium">Analytics</p></a></div></div>';
        html += '</div>';

        content.innerHTML = html;
        renderCategoryChart(dashData.categorySpendings);
        renderMonthlyChart(dashData.monthlySpendings);
        renderHealthFactors(healthScore);
    } catch (err) {
        showToast('Failed to load dashboard', 'error');
    }
}

function getHealthColor(score) {
    if (score >= 80) return '#22c55e';
    if (score >= 60) return '#a3e635';
    if (score >= 40) return '#f59e0b';
    return '#f43f5e';
}

function renderHealthFactors(healthScore) {
    var container = document.getElementById('health-factors');
    if (!container || !healthScore.factors) return;
    var html = '';
    healthScore.factors.forEach(function(f) {
        var pct = Math.min(100, (f.score / f.max) * 100);
        var color = pct >= 70 ? 'bg-emerald-500' : pct >= 40 ? 'bg-amber-500' : 'bg-rose-500';
        html += '<div class="flex items-center justify-between text-xs"><span class="text-gray-400">' + f.name + '</span><div class="flex items-center gap-2"><div class="w-16 h-1.5 bg-surface-800 rounded-full overflow-hidden"><div class="h-full rounded-full ' + color + '" style="width:' + pct + '%"></div></div><span class="text-gray-500 font-mono w-10 text-right">' + f.score + '/' + f.max + '</span></div></div>';
    });
    container.innerHTML = html;
}

function renderCategoryChart(data) {
    var ctx = document.getElementById('categoryChart');
    if (!ctx || !data || data.length === 0) return;
    charts.cat = new Chart(ctx, {
        type: 'doughnut',
        data: { labels: data.map(function(d){return d.category}), datasets: [{ data: data.map(function(d){return d.amount}), backgroundColor: data.map(function(d){return getCategoryColor(d.category)}), borderWidth: 3, borderColor: '#0f1128', hoverOffset: 12 }] },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom', labels: { color: '#94a3b8', padding: 12, font: { size: 11 }, usePointStyle: true, pointStyle: 'circle' } } },
            cutout: '70%',
            onClick: function(event, elements) {
                if (elements.length > 0) {
                    var idx = elements[0].index;
                    showCategoryDetail(data[idx].category, data[idx]);
                }
            }
        }
    });
}

function showCategoryDetail(category, data) {
    var container = document.getElementById('category-details');
    if (!container) return;
    container.innerHTML = '<div class="mt-4 p-4 bg-midnight-900/50 rounded-xl border border-midnight-600/30 fade-in"><div class="flex items-center justify-between mb-3"><div class="flex items-center gap-3"><div class="w-10 h-10 rounded-xl flex items-center justify-center" style="background:' + getCategoryColor(category) + '20"><i class="fas ' + getCategoryIcon(category) + '" style="color:' + getCategoryColor(category) + '"></i></div><div><p class="font-bold">' + category + '</p><p class="text-xs text-gray-400">' + data.percentage.toFixed(1) + '% of total spending</p></div></div><span class="text-lg font-extrabold neon-text">' + formatCurrency(data.amount) + '</span></div><div class="flex gap-2 mt-3"><a href="#expenses" class="text-xs px-3 py-1.5 bg-midnight-600/30 rounded-lg text-midnight-300 hover:text-white transition">View Transactions</a><a href="#assistant" class="text-xs px-3 py-1.5 gradient-accent rounded-lg text-midnight-900 font-medium transition">Analyze with AI</a></div></div>';
}

function renderMonthlyChart(data) {
    var ctx = document.getElementById('monthlyChart');
    if (!ctx) return;
    var months = data || [];
    charts.mon = new Chart(ctx, {
        type: 'bar',
        data: { labels: months.map(function(d){return d.monthName || d.month}), datasets: [{ label: 'Spending', data: months.map(function(d){return d.amount}), backgroundColor: 'rgba(90,117,242,0.4)', borderColor: '#5a75f2', borderWidth: 2, borderRadius: 8, borderSkipped: false }] },
        options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { x: { ticks: { color: '#94a3b8' }, grid: { display: false } }, y: { ticks: { color: '#94a3b8', callback: function(v){return '₹'+(v/1000)+'k'} }, grid: { color: 'rgba(255,255,255,0.04)' } } } }
    });
}
