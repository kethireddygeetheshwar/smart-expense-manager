async function renderBudgets() {
    var app = document.getElementById('app');
    app.innerHTML = renderLayout('<div class="fade-in"><div class="flex items-center justify-between mb-8"><div><h1 class="text-2xl font-extrabold tracking-tight">Budgets</h1><p class="text-gray-400 text-sm mt-1">Set limits and track your spending</p></div><button onclick="openBudgetModal()" class="px-5 py-2.5 gradient-accent rounded-xl font-bold text-midnight-900 btn-glow text-sm"><i class="fas fa-plus mr-2"></i>Set Budget</button></div><div id="budgets-loading" class="flex justify-center py-20"><div class="loading-spinner"></div></div><div id="budgets-content" class="hidden"></div></div>');

    try {
        var [budgets, alerts] = await Promise.all([api.get('/api/budgets'), api.get('/api/budgets/alerts')]);
        document.getElementById('budgets-loading').classList.add('hidden');
        var content = document.getElementById('budgets-content');
        content.classList.remove('hidden');

        if (budgets.length === 0) {
            content.innerHTML = '<div class="text-center py-20 glass rounded-2xl"><i class="fas fa-wallet text-5xl text-gray-700 mb-4"></i><p class="text-gray-400 text-lg mb-4">No budgets set yet</p><button onclick="openBudgetModal()" class="px-6 py-2.5 gradient-accent rounded-xl font-bold text-midnight-900 text-sm">Set your first budget</button></div>';
            return;
        }

        var html = '';
        if (alerts.length > 0) {
            html += '<div class="mb-8 glass rounded-2xl p-5 border border-amber-500/20"><h3 class="text-sm font-bold text-amber-400 mb-3"><i class="fas fa-exclamation-triangle mr-2"></i>Budget Alerts</h3><div class="space-y-2">';
            alerts.forEach(function(a) {
                html += '<div class="flex items-center justify-between text-sm"><span class="font-medium">' + a.category + '</span><span class="' + (a.exceeded ? 'text-rose-400 font-bold' : 'text-amber-400 font-bold') + '">' + a.usagePercentage.toFixed(0) + '% used ' + (a.exceeded ? '(EXCEEDED!)' : '(near limit)') + '</span></div>';
            });
            html += '</div></div>';
        }

        html += '<div class="grid grid-cols-1 md:grid-cols-2 gap-5">';
        budgets.forEach(function(b) {
            var badge = b.exceeded ? '<span class="px-2.5 py-1 bg-rose-500/20 text-rose-400 text-xs rounded-lg font-bold">EXCEEDED</span>' : b.nearLimit ? '<span class="px-2.5 py-1 bg-amber-500/20 text-amber-400 text-xs rounded-lg font-bold">WARNING</span>' : '<span class="px-2.5 py-1 bg-emerald-500/20 text-emerald-400 text-xs rounded-lg font-bold">ON TRACK</span>';
            var barColor = b.exceeded ? 'bg-rose-500' : b.nearLimit ? 'bg-amber-500' : 'bg-emerald-500';
            var pct = Math.min(b.usagePercentage, 100);
            html += '<div class="glass rounded-2xl p-6 card-hover"><div class="flex items-center justify-between mb-4"><div class="flex items-center gap-3"><div class="w-12 h-12 rounded-xl flex items-center justify-center" style="background:' + getCategoryColor(b.category) + '20"><i class="fas ' + getCategoryIcon(b.category) + ' text-lg" style="color:' + getCategoryColor(b.category) + '"></i></div><div><p class="font-bold">' + b.category + '</p><p class="text-xs text-gray-400">' + b.monthYear + '</p></div></div>' + badge + '</div>';
            html += '<div class="mb-3"><div class="flex justify-between text-sm mb-2"><span class="text-gray-400 font-medium">' + formatCurrency(b.spentAmount) + '</span><span class="text-gray-400">of ' + formatCurrency(b.limitAmount) + '</span></div>';
            html += '<div class="w-full h-3 bg-surface-800 rounded-full overflow-hidden"><div class="h-full rounded-full progress-bar ' + barColor + '" style="width:' + pct + '%"></div></div>';
            html += '<p class="text-xs text-gray-500 mt-2 font-medium">' + b.usagePercentage.toFixed(1) + '% used</p></div>';
            html += '<button onclick="deleteBudget(' + b.id + ')" class="text-xs text-gray-500 hover:text-rose-400 transition"><i class="fas fa-trash-alt mr-1"></i>Remove</button></div>';
        });
        html += '</div>';
        content.innerHTML = html;
    } catch (err) { showToast('Failed to load budgets', 'error'); }
}

function openBudgetModal() {
    var now = new Date();
    var monthYear = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
    var modal = document.createElement('div');
    modal.className = 'fixed inset-0 z-50 flex items-center justify-center p-4 fade-in';
    modal.style.background = 'rgba(0,0,0,0.7)';
    modal.style.backdropFilter = 'blur(8px)';
    modal.id = 'budget-modal';
    modal.innerHTML = '<div class="glass-strong rounded-3xl p-7 w-full max-w-md modal-content"><div class="flex items-center justify-between mb-6"><h2 class="text-xl font-extrabold">Set Budget</h2><button onclick="closeBudgetModal()" class="w-9 h-9 rounded-xl hover:bg-white/10 flex items-center justify-center transition"><i class="fas fa-times text-gray-400"></i></button></div><form onsubmit="handleAddBudget(event)" class="space-y-5"><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Category</label><select id="budget-category" required class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm focus:border-midnight-500 transition-all"><option value="FOOD">Food</option><option value="TRAVEL">Travel</option><option value="EDUCATION">Education</option><option value="SHOPPING">Shopping</option><option value="ENTERTAINMENT">Entertainment</option><option value="HEALTHCARE">Healthcare</option><option value="UTILITIES">Utilities</option><option value="RENT">Rent</option><option value="TRANSPORTATION">Transportation</option></select></div><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Monthly Limit (₹)</label><input type="number" id="budget-limit" required min="1" placeholder="e.g. 8000" class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm placeholder-gray-500 focus:border-midnight-500 transition-all"></div><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Month</label><input type="month" id="budget-month" value="' + monthYear + '" required class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm focus:border-midnight-500 transition-all"></div><button type="submit" class="w-full py-3.5 gradient-accent rounded-xl font-bold text-midnight-900 btn-glow hover:opacity-90 transition-all text-sm"><i class="fas fa-check mr-2"></i>Set Budget</button></form></div>';
    document.body.appendChild(modal);
    modal.addEventListener('click', function(e) { if (e.target === modal) closeBudgetModal(); });
}

function closeBudgetModal() {
    var modal = document.getElementById('budget-modal');
    if (modal) modal.remove();
}

async function handleAddBudget(e) {
    e.preventDefault();
    var data = { category: document.getElementById('budget-category').value, limitAmount: parseFloat(document.getElementById('budget-limit').value), monthYear: document.getElementById('budget-month').value };
    try {
        await api.post('/api/budgets', data);
        showToast('Budget set successfully!');
        closeBudgetModal();
        renderBudgets();
    } catch (err) { showToast(err.message, 'error'); }
}

async function deleteBudget(id) {
    if (!confirm('Remove this budget?')) return;
    try { await api.delete('/api/budgets/' + id); showToast('Budget removed'); renderBudgets(); } catch (err) { showToast(err.message, 'error'); }
}
