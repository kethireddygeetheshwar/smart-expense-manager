function renderAuthPage() {
    const app = document.getElementById('app');
    app.innerHTML = `
        <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
            <div class="absolute top-20 left-20 w-72 h-72 bg-midnight-600/20 rounded-full blur-3xl"></div>
            <div class="absolute bottom-20 right-20 w-96 h-96 bg-neon-500/10 rounded-full blur-3xl"></div>
            <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-midnight-500/5 rounded-full blur-3xl"></div>
            <div class="glass-strong rounded-3xl p-8 w-full max-w-md fade-in relative z-10">
                <div class="text-center mb-8">
                    <div class="w-20 h-20 rounded-2xl gradient-accent flex items-center justify-center mx-auto mb-5 floating">
                        <i class="fas fa-wallet text-3xl text-midnight-900"></i>
                    </div>
                    <h1 class="text-3xl font-extrabold bg-gradient-to-r from-midnight-300 via-neon-400 to-midnight-300 bg-clip-text text-transparent">ExpenseAI</h1>
                    <p class="text-gray-400 text-sm mt-2">Smart Finance Powered by AI</p>
                </div>
                <div class="flex gap-2 mb-6 p-1 bg-surface-900/80 rounded-2xl">
                    <button onclick="switchAuthTab('login')" id="tab-login" class="flex-1 py-2.5 rounded-xl text-sm font-semibold transition-all gradient-accent text-midnight-900">Sign In</button>
                    <button onclick="switchAuthTab('register')" id="tab-register" class="flex-1 py-2.5 rounded-xl text-sm font-semibold transition-all text-gray-400">Sign Up</button>
                </div>
                <div id="auth-form-container"></div>
                <div class="mt-6 text-center">
                    <p class="text-xs text-gray-500 bg-surface-900/50 rounded-lg py-2 px-3"><i class="fas fa-info-circle mr-1 text-electric-400"></i>Demo: demo@expense.com / password123</p>
                </div>
            </div>
        </div>
    `;
    renderLoginForm();
}

function switchAuthTab(tab) {
    const loginTab = document.getElementById('tab-login');
    const regTab = document.getElementById('tab-register');
    if (tab === 'login') {
        loginTab.className = 'flex-1 py-2.5 rounded-xl text-sm font-semibold transition-all gradient-accent text-midnight-900';
        regTab.className = 'flex-1 py-2.5 rounded-xl text-sm font-semibold transition-all text-gray-400';
    } else {
        loginTab.className = 'flex-1 py-2.5 rounded-xl text-sm font-semibold transition-all text-gray-400';
        regTab.className = 'flex-1 py-2.5 rounded-xl text-sm font-semibold transition-all gradient-accent text-midnight-900';
    }
    if (tab === 'login') renderLoginForm();
    else renderRegisterForm();
}

function renderLoginForm() {
    document.getElementById('auth-form-container').innerHTML = `
        <form onsubmit="handleLogin(event)" class="space-y-5 fade-in">
            <div>
                <label class="text-xs text-gray-400 mb-1.5 block font-medium">Email Address</label>
                <div class="relative">
                    <i class="fas fa-envelope absolute left-4 top-3.5 text-gray-500"></i>
                    <input type="email" id="login-email" value="demo@expense.com" required
                        class="w-full pl-11 pr-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:border-midnight-500 transition-all">
                </div>
            </div>
            <div>
                <label class="text-xs text-gray-400 mb-1.5 block font-medium">Password</label>
                <div class="relative">
                    <i class="fas fa-lock absolute left-4 top-3.5 text-gray-500"></i>
                    <input type="password" id="login-password" value="password123" required
                        class="w-full pl-11 pr-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:border-midnight-500 transition-all">
                </div>
            </div>
            <button type="submit" class="w-full py-3.5 gradient-accent rounded-xl font-bold text-midnight-900 btn-glow hover:opacity-90 transition-all text-sm">
                <i class="fas fa-arrow-right mr-2"></i>Sign In
            </button>
        </form>
    `;
}

function renderRegisterForm() {
    document.getElementById('auth-form-container').innerHTML = `
        <form onsubmit="handleRegister(event)" class="space-y-4 fade-in">
            <div>
                <label class="text-xs text-gray-400 mb-1.5 block font-medium">Full Name</label>
                <div class="relative">
                    <i class="fas fa-user absolute left-4 top-3.5 text-gray-500"></i>
                    <input type="text" id="reg-name" required
                        class="w-full pl-11 pr-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:border-midnight-500 transition-all">
                </div>
            </div>
            <div>
                <label class="text-xs text-gray-400 mb-1.5 block font-medium">Email Address</label>
                <div class="relative">
                    <i class="fas fa-envelope absolute left-4 top-3.5 text-gray-500"></i>
                    <input type="email" id="reg-email" required
                        class="w-full pl-11 pr-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:border-midnight-500 transition-all">
                </div>
            </div>
            <div>
                <label class="text-xs text-gray-400 mb-1.5 block font-medium">Password</label>
                <div class="relative">
                    <i class="fas fa-lock absolute left-4 top-3.5 text-gray-500"></i>
                    <input type="password" id="reg-password" required minlength="6"
                        class="w-full pl-11 pr-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:border-midnight-500 transition-all">
                </div>
            </div>
            <button type="submit" class="w-full py-3.5 gradient-accent rounded-xl font-bold text-midnight-900 btn-glow hover:opacity-90 transition-all text-sm">
                <i class="fas fa-user-plus mr-2"></i>Create Account
            </button>
        </form>
    `;
}

async function handleLogin(e) {
    e.preventDefault();
    try {
        const result = await api.post('/api/auth/login', {
            email: document.getElementById('login-email').value,
            password: document.getElementById('login-password').value
        });
        localStorage.setItem('token', result.token);
        localStorage.setItem('user', JSON.stringify(result));
        showToast('Welcome back, ' + result.fullName + '!');
        window.location.hash = '#dashboard';
        router();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    try {
        const result = await api.post('/api/auth/register', {
            fullName: document.getElementById('reg-name').value,
            email: document.getElementById('reg-email').value,
            password: document.getElementById('reg-password').value
        });
        localStorage.setItem('token', result.token);
        localStorage.setItem('user', JSON.stringify(result));
        showToast('Account created! Welcome!');
        window.location.hash = '#dashboard';
        router();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.hash = '#login';
    router();
}
