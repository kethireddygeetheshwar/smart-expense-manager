var chatHistory = [];

async function renderAssistant() {
    var app = document.getElementById('app');
    app.innerHTML = renderLayout('<div class="fade-in h-full flex flex-col"><div class="flex items-center justify-between mb-5"><div><h1 class="text-2xl font-extrabold tracking-tight flex items-center gap-3"><span class="neon-text">FinSight</span> AI Copilot 🤖</h1><p class="text-gray-400 text-sm mt-1">Your personal financial analyst</p></div><div class="flex items-center gap-2 glass rounded-full px-3 py-1.5"><span class="w-2 h-2 bg-emerald-400 rounded-full pulse-dot"></span><span class="text-xs text-gray-400">AI Ready</span></div></div><div id="chat-container" class="flex-1 glass rounded-2xl p-5 mb-4 overflow-y-auto" style="min-height:350px;max-height:calc(100vh - 320px)"><div id="chat-messages" class="space-y-4"></div></div><div class="flex flex-wrap gap-2 mb-4"><button onclick="sendQuick(\'Where did I spend the most this month?\')" class="px-3 py-1.5 glass hover:bg-white/5 rounded-lg text-xs text-gray-300 transition font-medium"><i class="fas fa-fire mr-1 text-rose-400"></i>Top spending</button><button onclick="sendQuick(\'Compare my spending with last month\')" class="px-3 py-1.5 glass hover:bg-white/5 rounded-lg text-xs text-gray-300 transition font-medium"><i class="fas fa-scale-balanced mr-1 text-electric-400"></i>Compare months</button><button onclick="sendQuick(\'How can I reduce my expenses?\')" class="px-3 py-1.5 glass hover:bg-white/5 rounded-lg text-xs text-gray-300 transition font-medium"><i class="fas fa-piggy-bank mr-1 text-neon-400"></i>Savings tips</button><button onclick="sendQuick(\'Show my category breakdown\')" class="px-3 py-1.5 glass hover:bg-white/5 rounded-lg text-xs text-gray-300 transition font-medium"><i class="fas fa-chart-pie mr-1 text-midnight-400"></i>Breakdown</button><button onclick="sendQuick(\'What is my financial health score?\')" class="px-3 py-1.5 glass hover:bg-white/5 rounded-lg text-xs text-gray-300 transition font-medium"><i class="fas fa-heart-pulse mr-1 text-rose-400"></i>Health check</button><button onclick="sendQuick(\'Predict my spending for this month\')" class="px-3 py-1.5 glass hover:bg-white/5 rounded-lg text-xs text-gray-300 transition font-medium"><i class="fas fa-wand-magic-sparkles mr-1 text-purple-400"></i>Forecast</button></div><form onsubmit="handleChat(event)" class="flex gap-3"><input type="text" id="chat-input" placeholder="Ask anything about your finances..." class="flex-1 px-5 py-3.5 bg-surface-900/80 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:border-midnight-500 transition-all"><button type="submit" class="px-6 py-3.5 gradient-accent rounded-xl font-bold text-midnight-900 btn-glow"><i class="fas fa-paper-plane"></i></button></form></div>');

    var container = document.getElementById('chat-messages');
    container.innerHTML = '<div class="text-center text-gray-500 py-12"><div class="w-16 h-16 rounded-2xl gradient-accent flex items-center justify-center mx-auto mb-4 floating"><i class="fas fa-robot text-2xl text-midnight-900"></i></div><p class="text-sm font-medium text-gray-300">Hi! I\'m FinSight, your AI financial copilot</p><p class="text-xs text-gray-500 mt-2">Ask me anything about your spending, budgets, or financial goals</p><p class="text-xs text-gray-600 mt-1">I analyze your actual transaction data to give personalized insights</p></div>';
}

function sendQuick(msg) {
    document.getElementById('chat-input').value = msg;
    handleChat({ preventDefault: function() {} });
}

async function handleChat(e) {
    e.preventDefault();
    var input = document.getElementById('chat-input');
    var message = input.value.trim();
    if (!message) return;
    input.value = '';

    var container = document.getElementById('chat-messages');
    if (container.querySelector('.text-center')) container.innerHTML = '';

    container.innerHTML += '<div class="flex justify-end chat-bubble"><div class="gradient-accent rounded-2xl rounded-tr-sm px-4 py-3 max-w-[80%]"><p class="text-sm text-midnight-900 font-medium">' + message + '</p></div></div>';

    var loadingId = 'loading-' + Date.now();
    container.innerHTML += '<div class="flex justify-start chat-bubble" id="' + loadingId + '"><div class="glass rounded-2xl rounded-tl-sm px-5 py-4 max-w-[80%]"><div class="flex gap-1.5"><div class="w-2 h-2 bg-midnight-400 rounded-full animate-bounce"></div><div class="w-2 h-2 bg-midnight-400 rounded-full animate-bounce" style="animation-delay:0.15s"></div><div class="w-2 h-2 bg-midnight-400 rounded-full animate-bounce" style="animation-delay:0.3s"></div></div></div></div>';
    container.scrollTop = container.scrollHeight;

    try {
        var result = await api.post('/api/assistant/chat', { message: message });
        var el = document.getElementById(loadingId);
        if (el) el.remove();
        container.innerHTML += '<div class="flex justify-start chat-bubble"><div class="glass rounded-2xl rounded-tl-sm px-4 py-3 max-w-[80%]"><p class="text-sm whitespace-pre-line leading-relaxed">' + formatAIResponse(result.response) + '</p></div></div>';
    } catch (err) {
        var el2 = document.getElementById(loadingId);
        if (el2) el2.remove();
        container.innerHTML += '<div class="flex justify-start chat-bubble"><div class="bg-rose-900/30 border border-rose-500/30 rounded-2xl rounded-tl-sm px-4 py-3 max-w-[80%]"><p class="text-sm text-rose-300">Sorry, I couldn\'t process that. Please try again.</p></div></div>';
    }
    container.scrollTop = container.scrollHeight;
}

function formatAIResponse(text) {
    text = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    text = text.replace(/\n/g, '<br>');
    return text;
}
