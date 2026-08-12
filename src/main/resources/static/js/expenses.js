async function renderExpenses() {
    var app = document.getElementById('app');
    app.innerHTML = renderLayout('<div class="fade-in"><div class="flex items-center justify-between mb-8"><div><h1 class="text-2xl font-extrabold tracking-tight">Transactions</h1><p class="text-gray-400 text-sm mt-1">Manage your income & expenses</p></div><button onclick="openAddModal()" class="px-5 py-2.5 gradient-accent rounded-xl font-bold text-midnight-900 btn-glow text-sm"><i class="fas fa-plus mr-2"></i>Add New</button></div><div class="glass rounded-xl p-4 mb-6"><div class="flex flex-wrap gap-3"><div class="flex-1 min-w-[200px]"><div class="relative"><i class="fas fa-search absolute left-3 top-3 text-gray-500 text-sm"></i><input type="text" id="expense-search" placeholder="Search transactions..." oninput="filterExpenses()" class="w-full pl-10 pr-4 py-2.5 bg-surface-900/60 border border-white/10 rounded-xl text-sm text-white placeholder-gray-500 focus:border-midnight-500 transition-all"></div></div><select id="filter-type" onchange="filterExpenses()" class="px-4 py-2.5 bg-surface-900/60 border border-white/10 rounded-xl text-sm text-white focus:border-midnight-500 transition-all"><option value="">All Types</option><option value="EXPENSE">Expense</option><option value="INCOME">Income</option></select><select id="filter-category" onchange="filterExpenses()" class="px-4 py-2.5 bg-surface-900/60 border border-white/10 rounded-xl text-sm text-white focus:border-midnight-500 transition-all"><option value="">All Categories</option><option value="FOOD">Food</option><option value="TRAVEL">Travel</option><option value="EDUCATION">Education</option><option value="SHOPPING">Shopping</option><option value="ENTERTAINMENT">Entertainment</option><option value="HEALTHCARE">Healthcare</option><option value="UTILITIES">Utilities</option><option value="RENT">Rent</option><option value="TRANSPORTATION">Transportation</option></select></div></div><div id="expenses-loading" class="flex justify-center py-20"><div class="loading-spinner"></div></div><div id="expenses-list" class="space-y-3 hidden"></div><div id="expenses-empty" class="hidden text-center py-20"><i class="fas fa-receipt text-5xl text-gray-700 mb-4"></i><p class="text-gray-400 text-lg">No transactions found</p><p class="text-gray-600 text-sm mt-1">Add your first transaction to get started</p></div></div>');

    try {
        var expenses = await api.get('/api/expenses');
        document.getElementById('expenses-loading').classList.add('hidden');
        renderExpenseList(expenses);
        window._allExpenses = expenses;
    } catch (err) {
        showToast('Failed to load transactions', 'error');
    }
}

function renderExpenseList(expenses) {
    var list = document.getElementById('expenses-list');
    var empty = document.getElementById('expenses-empty');
    list.classList.remove('hidden');
    if (expenses.length === 0) { list.classList.add('hidden'); empty.classList.remove('hidden'); return; }
    empty.classList.add('hidden');
    list.innerHTML = expenses.map(function(exp) {
        return '<div class="glass rounded-xl p-4 flex items-center justify-between card-hover fade-in"><div class="flex items-center gap-4"><div class="w-12 h-12 rounded-xl flex items-center justify-center" style="background:' + getCategoryColor(exp.category) + '20"><i class="fas ' + getCategoryIcon(exp.category) + ' text-lg" style="color:' + getCategoryColor(exp.category) + '"></i></div><div><p class="font-semibold text-sm">' + exp.description + '</p><div class="flex items-center gap-2 mt-1"><span class="text-xs px-2 py-0.5 rounded-md bg-white/5 text-gray-400">' + exp.category + '</span><span class="text-xs text-gray-500">' + formatDate(exp.date) + '</span>' + (exp.paymentMethod ? '<span class="text-xs text-gray-500">• ' + exp.paymentMethod + '</span>' : '') + '</div></div></div><div class="flex items-center gap-4"><span class="font-bold ' + (exp.type === 'INCOME' ? 'text-emerald-400' : 'text-rose-400') + '">' + (exp.type === 'INCOME' ? '+' : '-') + formatCurrency(exp.amount) + '</span><button onclick="deleteExpense(' + exp.id + ')" class="w-9 h-9 rounded-xl hover:bg-rose-500/20 flex items-center justify-center text-gray-500 hover:text-rose-400 transition"><i class="fas fa-trash-alt text-sm"></i></button></div></div>';
    }).join('');
}

function filterExpenses() {
    var search = document.getElementById('expense-search').value.toLowerCase();
    var type = document.getElementById('filter-type').value;
    var category = document.getElementById('filter-category').value;
    var filtered = window._allExpenses || [];
    if (search) filtered = filtered.filter(function(e){return e.description.toLowerCase().includes(search)});
    if (type) filtered = filtered.filter(function(e){return e.type === type});
    if (category) filtered = filtered.filter(function(e){return e.category === category});
    renderExpenseList(filtered);
}

async function deleteExpense(id) {
    if (!confirm('Delete this transaction?')) return;
    try {
        await api.delete('/api/expenses/' + id);
        showToast('Transaction deleted');
        renderExpenses();
    } catch (err) { showToast(err.message, 'error'); }
}

function openAddModal() {
    var modal = document.createElement('div');
    modal.className = 'fixed inset-0 z-50 flex items-center justify-center p-4 fade-in';
    modal.style.background = 'rgba(0,0,0,0.7)';
    modal.style.backdropFilter = 'blur(8px)';
    modal.id = 'add-modal';
    modal.innerHTML = '<div class="glass-strong rounded-3xl p-7 w-full max-w-lg modal-content max-h-[90vh] overflow-y-auto"><div class="flex items-center justify-between mb-6"><h2 class="text-xl font-extrabold">Add Transaction</h2><button onclick="closeAddModal()" class="w-9 h-9 rounded-xl hover:bg-white/10 flex items-center justify-center transition"><i class="fas fa-times text-gray-400"></i></button></div><form onsubmit="handleAddExpense(event)" class="space-y-5"><div class="flex gap-2 p-1 bg-surface-900 rounded-2xl"><button type="button" onclick="setExpType(\'EXPENSE\')" id="type-expense" class="flex-1 py-2.5 rounded-xl text-sm font-bold bg-rose-500/20 text-rose-400 transition">Expense</button><button type="button" onclick="setExpType(\'INCOME\')" id="type-income" class="flex-1 py-2.5 rounded-xl text-sm font-bold text-gray-400 transition">Income</button></div><input type="hidden" id="exp-type" value="EXPENSE"><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Description</label><input type="text" id="exp-desc" required placeholder="What is this for?" class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm placeholder-gray-500 focus:border-midnight-500 transition-all"></div><div class="grid grid-cols-2 gap-4"><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Amount (₹)</label><input type="number" id="exp-amount" required min="1" placeholder="0" class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm placeholder-gray-500 focus:border-midnight-500 transition-all"></div><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Date</label><input type="date" id="exp-date" required class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm focus:border-midnight-500 transition-all"></div></div><div class="grid grid-cols-2 gap-4"><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Category</label><select id="exp-category" required class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm focus:border-midnight-500 transition-all"><option value="FOOD">Food</option><option value="TRAVEL">Travel</option><option value="EDUCATION">Education</option><option value="SHOPPING">Shopping</option><option value="ENTERTAINMENT">Entertainment</option><option value="HEALTHCARE">Healthcare</option><option value="UTILITIES">Utilities</option><option value="RENT">Rent</option><option value="TRANSPORTATION">Transportation</option><option value="OTHER_INCOME">Other Income</option></select></div><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Payment Method</label><select id="exp-payment" class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm focus:border-midnight-500 transition-all"><option value="CASH">Cash</option><option value="CREDIT_CARD">Credit Card</option><option value="DEBIT_CARD">Debit Card</option><option value="UPI">UPI</option><option value="NET_BANKING">Net Banking</option><option value="WALLET">Wallet</option></select></div></div><div class="flex items-center gap-3 p-3 bg-surface-900/50 rounded-xl"><input type="checkbox" id="exp-recurring" class="w-4 h-4 rounded bg-surface-800 border-gray-600 accent-midnight-500"><label for="exp-recurring" class="text-sm text-gray-300">This is a recurring transaction</label></div><button type="submit" class="w-full py-3.5 gradient-accent rounded-xl font-bold text-midnight-900 btn-glow hover:opacity-90 transition-all text-sm"><i class="fas fa-check mr-2"></i>Save Transaction</button></form></div>';
    document.body.appendChild(modal);
    document.getElementById('exp-date').value = new Date().toISOString().split('T')[0];
    modal.addEventListener('click', function(e) { if (e.target === modal) closeAddModal(); });
}

function closeAddModal() {
    var modal = document.getElementById('add-modal');
    if (modal) modal.remove();
}

function setExpType(type) {
    document.getElementById('exp-type').value = type;
    document.getElementById('type-expense').className = 'flex-1 py-2.5 rounded-xl text-sm font-bold transition ' + (type === 'EXPENSE' ? 'bg-rose-500/20 text-rose-400' : 'text-gray-400');
    document.getElementById('type-income').className = 'flex-1 py-2.5 rounded-xl text-sm font-bold transition ' + (type === 'INCOME' ? 'bg-emerald-500/20 text-emerald-400' : 'text-gray-400');
}

async function handleAddExpense(e) {
    e.preventDefault();
    var data = {
        description: document.getElementById('exp-desc').value,
        amount: parseFloat(document.getElementById('exp-amount').value),
        date: document.getElementById('exp-date').value,
        category: document.getElementById('exp-category').value,
        type: document.getElementById('exp-type').value,
        paymentMethod: document.getElementById('exp-payment').value,
        recurring: document.getElementById('exp-recurring').checked
    };
    try {
        await api.post('/api/expenses', data);
        showToast('Transaction added successfully!');
        closeAddModal();
        var currentPage = window.location.hash.replace('#', '');
        if (currentPage === 'dashboard') renderDashboard();
        else if (currentPage === 'expenses') renderExpenses();
        else renderExpenses();
    } catch (err) {
        showToast(err.message, 'error');
    }
}
