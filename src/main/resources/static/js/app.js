var charts = {};

function renderLayout(content) {
    var user = JSON.parse(localStorage.getItem('user') || '{}');
    return '<div class="flex h-screen overflow-hidden"><aside class="w-72 glass-strong border-r border-white/5 flex flex-col hidden lg:flex"><div class="p-6 border-b border-white/5"><div class="flex items-center gap-3"><div class="w-11 h-11 rounded-xl gradient-accent flex items-center justify-center"><i class="fas fa-wallet text-midnight-900"></i></div><div><p class="font-bold text-sm tracking-tight">ExpenseAI</p><p class="text-[10px] text-gray-500 uppercase tracking-widest">Smart Finance</p></div></div></div><nav class="flex-1 p-4 space-y-1"><a href="#dashboard" class="sidebar-link flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-gray-300" data-page="dashboard"><i class="fas fa-th-large w-5 text-center"></i>Dashboard</a><a href="#expenses" class="sidebar-link flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-gray-300" data-page="expenses"><i class="fas fa-receipt w-5 text-center"></i>Transactions</a><a href="#budgets" class="sidebar-link flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-gray-300" data-page="budgets"><i class="fas fa-wallet w-5 text-center"></i>Budgets</a><a href="#analytics" class="sidebar-link flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-gray-300" data-page="analytics"><i class="fas fa-chart-line w-5 text-center"></i>Analytics</a><a href="#goals" class="sidebar-link flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-gray-300" data-page="goals"><i class="fas fa-bullseye w-5 text-center"></i>Goals</a><a href="#assistant" class="sidebar-link flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-gray-300" data-page="assistant"><i class="fas fa-robot w-5 text-center"></i>AI Assistant</a></nav><div class="p-4 border-t border-white/5"><div class="glass rounded-xl p-3 mb-3 flex items-center gap-3"><div class="w-9 h-9 rounded-full gradient-electric flex items-center justify-center"><i class="fas fa-user text-xs text-white"></i></div><div class="flex-1 min-w-0"><p class="text-xs text-gray-400">Signed in</p><p class="text-sm font-medium truncate">' + (user.email || 'user') + '</p></div></div><button onclick="logout()" class="w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm text-coral-400 hover:bg-coral-500/10 transition font-medium"><i class="fas fa-sign-out-alt w-5 text-center"></i>Sign Out</button></div></aside><main class="flex-1 overflow-y-auto"><div class="lg:hidden flex items-center justify-between p-4 border-b border-white/5 glass-strong"><div class="flex items-center gap-2"><div class="w-9 h-9 rounded-lg gradient-accent flex items-center justify-center"><i class="fas fa-wallet text-xs text-midnight-900"></i></div><span class="font-bold text-sm">ExpenseAI</span></div><button onclick="document.getElementById(\'mobile-menu\').classList.toggle(\'hidden\')" class="w-9 h-9 rounded-lg glass flex items-center justify-center"><i class="fas fa-bars text-sm"></i></button></div><div id="mobile-menu" class="hidden lg:hidden p-4 border-b border-white/5 glass-strong space-y-1"><a href="#dashboard" class="block px-4 py-2.5 rounded-xl text-sm hover:bg-white/5" onclick="document.getElementById(\'mobile-menu\').classList.add(\'hidden\')"><i class="fas fa-th-large mr-3 w-5 text-center"></i>Dashboard</a><a href="#expenses" class="block px-4 py-2.5 rounded-xl text-sm hover:bg-white/5" onclick="document.getElementById(\'mobile-menu\').classList.add(\'hidden\')"><i class="fas fa-receipt mr-3 w-5 text-center"></i>Transactions</a><a href="#budgets" class="block px-4 py-2.5 rounded-xl text-sm hover:bg-white/5" onclick="document.getElementById(\'mobile-menu\').classList.add(\'hidden\')"><i class="fas fa-wallet mr-3 w-5 text-center"></i>Budgets</a><a href="#analytics" class="block px-4 py-2.5 rounded-xl text-sm hover:bg-white/5" onclick="document.getElementById(\'mobile-menu\').classList.add(\'hidden\')"><i class="fas fa-chart-line mr-3 w-5 text-center"></i>Analytics</a><a href="#goals" class="block px-4 py-2.5 rounded-xl text-sm hover:bg-white/5" onclick="document.getElementById(\'mobile-menu\').classList.add(\'hidden\')"><i class="fas fa-bullseye mr-3 w-5 text-center"></i>Goals</a><a href="#assistant" class="block px-4 py-2.5 rounded-xl text-sm hover:bg-white/5" onclick="document.getElementById(\'mobile-menu\').classList.add(\'hidden\')"><i class="fas fa-robot mr-3 w-5 text-center"></i>AI Assistant</a><button onclick="logout()" class="block w-full text-left px-4 py-2.5 rounded-xl text-sm text-coral-400 hover:bg-coral-500/10"><i class="fas fa-sign-out-alt mr-3 w-5 text-center"></i>Sign Out</button></div><div class="p-6 lg:p-8">' + content + '</div></main></div>';
}

function updateActiveNav(page) {
    document.querySelectorAll('.sidebar-link').forEach(function(el) {
        el.classList.remove('active');
        if (el.dataset.page === page) el.classList.add('active');
    });
}

function destroyCharts() {
    Object.values(charts).forEach(function(c) { if (c) c.destroy(); });
    charts = {};
}

function router() {
    var hash = window.location.hash || '#dashboard';
    var page = hash.replace('#', '');
    if (!localStorage.getItem('token')) {
        if (page !== 'login' && page !== 'register') {
            window.location.hash = '#login';
        }
        renderAuthPage();
        return;
    }
    destroyCharts();
    switch (page) {
        case 'dashboard': updateActiveNav('dashboard'); renderDashboard(); break;
        case 'expenses': updateActiveNav('expenses'); renderExpenses(); break;
        case 'budgets': updateActiveNav('budgets'); renderBudgets(); break;
        case 'analytics': updateActiveNav('analytics'); renderAnalytics(); break;
        case 'assistant': updateActiveNav('assistant'); renderAssistant(); break;
        case 'goals': updateActiveNav('goals'); renderGoals(); break;
        case 'login': case 'register': renderAuthPage(); break;
        default: window.location.hash = '#dashboard';
    }
}

window.addEventListener('hashchange', router);
window.addEventListener('load', router);
