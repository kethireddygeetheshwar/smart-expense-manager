const API_BASE = '';

const api = {
    async request(endpoint, options = {}) {
        const token = localStorage.getItem('token');
        const headers = { 'Content-Type': 'application/json', ...options.headers };
        if (token) headers['Authorization'] = `Bearer ${token}`;
        const response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
        if (response.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.hash = '#login';
            throw new Error('Session expired');
        }
        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: 'Request failed' }));
            throw new Error(error.message || 'Request failed');
        }
        return response.json();
    },
    get(endpoint) { return this.request(endpoint); },
    post(endpoint, data) { return this.request(endpoint, { method: 'POST', body: JSON.stringify(data) }); },
    put(endpoint, data) { return this.request(endpoint, { method: 'PUT', body: JSON.stringify(data) }); },
    delete(endpoint) { return this.request(endpoint, { method: 'DELETE' }); }
};

function showToast(message, type = 'success') {
    const existing = document.querySelectorAll('.toast-notification');
    existing.forEach(t => t.remove());
    const toast = document.createElement('div');
    toast.className = 'toast-notification';
    const colors = { success: 'bg-emerald-500', error: 'bg-rose-500', warning: 'bg-amber-500' };
    const icons = { success: 'fa-check-circle', error: 'fa-times-circle', warning: 'fa-exclamation-circle' };
    toast.style.cssText = `position:fixed;top:24px;right:24px;z-index:9999;padding:14px 24px;border-radius:16px;color:white;font-weight:600;font-size:14px;display:flex;align-items:center;gap:10px;animation:slideInRight 0.4s ease;box-shadow:0 20px 40px rgba(0,0,0,0.3);`;
    toast.style.background = type === 'success' ? '#10b981' : type === 'error' ? '#f43f5e' : '#f59e0b';
    toast.innerHTML = `<i class="fas ${icons[type]}"></i><span>${message}</span>`;
    document.body.appendChild(toast);
    setTimeout(() => { toast.style.opacity='0'; toast.style.transition='opacity 0.3s'; setTimeout(()=>toast.remove(),300); }, 3000);
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount || 0);
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

function getCategoryIcon(category) {
    const icons = { FOOD:'fa-utensils',TRAVEL:'fa-plane',EDUCATION:'fa-graduation-cap',SHOPPING:'fa-bag-shopping',ENTERTAINMENT:'fa-gamepad',HEALTHCARE:'fa-heart-pulse',UTILITIES:'fa-bolt',RENT:'fa-house',TRANSPORTATION:'fa-car',OTHER_INCOME:'fa-sack-dollar' };
    return icons[category] || 'fa-circle';
}

function getCategoryColor(category) {
    const colors = { FOOD:'#f59e0b',TRAVEL:'#0ea5e9',EDUCATION:'#a855f7',SHOPPING:'#ec4899',ENTERTAINMENT:'#f97316',HEALTHCARE:'#ef4444',UTILITIES:'#06b6d4',RENT:'#14b8a6',TRANSPORTATION:'#6366f1',OTHER_INCOME:'#22c55e' };
    return colors[category] || '#6b7280';
}

function getCategoryGradient(category) {
    const g = { FOOD:'linear-gradient(135deg,#f59e0b,#fbbf24)',TRAVEL:'linear-gradient(135deg,#0ea5e9,#38bdf8)',EDUCATION:'linear-gradient(135deg,#a855f7,#c084fc)',SHOPPING:'linear-gradient(135deg,#ec4899,#f472b6)',ENTERTAINMENT:'linear-gradient(135deg,#f97316,#fb923c)',HEALTHCARE:'linear-gradient(135deg,#ef4444,#f87171)',UTILITIES:'linear-gradient(135deg,#06b6d4,#22d3ee)',RENT:'linear-gradient(135deg,#14b8a6,#2dd4bf)',TRANSPORTATION:'linear-gradient(135deg,#6366f1,#818cf8)',OTHER_INCOME:'linear-gradient(135deg,#22c55e,#4ade80)' };
    return g[category] || 'linear-gradient(135deg,#6b7280,#9ca3af)';
}

const style = document.createElement('style');
style.textContent = '@keyframes slideInRight{from{transform:translateX(120%);opacity:0}to{transform:translateX(0);opacity:1}}';
document.head.appendChild(style);
