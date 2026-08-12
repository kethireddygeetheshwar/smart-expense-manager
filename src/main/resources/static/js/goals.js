async function renderGoals() {
    var app = document.getElementById('app');
    app.innerHTML = renderLayout('<div class="fade-in"><div class="flex items-center justify-between mb-8"><div><h1 class="text-2xl font-extrabold tracking-tight">Financial Goals</h1><p class="text-gray-400 text-sm mt-1">Track your savings goals</p></div><button onclick="openGoalModal()" class="px-5 py-2.5 gradient-accent rounded-xl font-bold text-midnight-900 btn-glow text-sm"><i class="fas fa-plus mr-2"></i>New Goal</button></div><div id="goals-loading" class="flex justify-center py-20"><div class="loading-spinner"></div></div><div id="goals-content" class="hidden"></div></div>');

    try {
        var goals = await api.get('/api/goals');
        document.getElementById('goals-loading').classList.add('hidden');
        var content = document.getElementById('goals-content');
        content.classList.remove('hidden');

        if (goals.length === 0) {
            content.innerHTML = '<div class="text-center py-20 glass rounded-2xl"><i class="fas fa-bullseye text-5xl text-gray-700 mb-4"></i><p class="text-gray-400 text-lg mb-4">No goals yet</p><p class="text-gray-600 text-sm mb-4">Create your first financial goal to start tracking</p><button onclick="openGoalModal()" class="px-6 py-2.5 gradient-accent rounded-xl font-bold text-midnight-900 text-sm">Create Goal</button></div>';
            return;
        }

        var html = '<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">';
        goals.forEach(function(g) {
            var pct = Math.min(g.progressPercentage, 100);
            var barColor = g.completed ? 'bg-emerald-500' : pct >= 75 ? 'bg-midnight-400' : pct >= 50 ? 'bg-amber-500' : 'bg-rose-500';
            html += '<div class="glass rounded-2xl p-6 card-hover ' + (g.completed ? 'border border-emerald-500/30' : '') + '">';
            html += '<div class="flex items-center justify-between mb-4"><div class="flex items-center gap-3"><div class="w-12 h-12 rounded-xl bg-midnight-600/30 flex items-center justify-center"><i class="fas ' + (g.icon || 'fa-bullseye') + ' text-lg text-midnight-300"></i></div><div><p class="font-bold">' + g.name + '</p>' + (g.targetDate ? '<p class="text-xs text-gray-400">Target: ' + g.targetDate + '</p>' : '') + '</div></div>' + (g.completed ? '<span class="px-2 py-1 bg-emerald-500/20 text-emerald-400 text-xs rounded-lg font-bold">DONE</span>' : '') + '</div>';
            html += '<div class="mb-3"><div class="flex justify-between text-sm mb-2"><span class="text-gray-400 font-medium">' + formatCurrency(g.savedAmount) + '</span><span class="text-gray-400">of ' + formatCurrency(g.targetAmount) + '</span></div>';
            html += '<div class="w-full h-3 bg-surface-800 rounded-full overflow-hidden"><div class="h-full rounded-full progress-bar ' + barColor + '" style="width:' + pct + '%"></div></div>';
            html += '<div class="flex justify-between mt-2"><span class="text-xs text-gray-500 font-medium">' + g.progressPercentage.toFixed(1) + '% complete</span><span class="text-xs text-gray-500">₹' + g.remainingAmount + ' left</span></div></div>';
            if (!g.completed) {
                html += '<div class="flex gap-2"><button onclick="contributeGoal(' + g.id + ')" class="flex-1 text-xs py-2 bg-midnight-600/30 hover:bg-midnight-600/50 rounded-lg text-midnight-300 transition font-medium"><i class="fas fa-plus mr-1"></i>Add Money</button><button onclick="deleteGoal(' + g.id + ')" class="text-xs py-2 px-3 bg-rose-500/10 hover:bg-rose-500/20 rounded-lg text-rose-400 transition"><i class="fas fa-trash-alt"></i></button></div>';
            }
            html += '</div>';
        });
        html += '</div>';
        content.innerHTML = html;
    } catch (err) { showToast('Failed to load goals', 'error'); }
}

function openGoalModal() {
    var modal = document.createElement('div');
    modal.className = 'fixed inset-0 z-50 flex items-center justify-center p-4 fade-in';
    modal.style.background = 'rgba(0,0,0,0.7)';
    modal.style.backdropFilter = 'blur(8px)';
    modal.id = 'goal-modal';
    modal.innerHTML = '<div class="glass-strong rounded-3xl p-7 w-full max-w-md modal-content"><div class="flex items-center justify-between mb-6"><h2 class="text-xl font-extrabold">Create Goal</h2><button onclick="closeGoalModal()" class="w-9 h-9 rounded-xl hover:bg-white/10 flex items-center justify-center transition"><i class="fas fa-times text-gray-400"></i></button></div><form onsubmit="handleCreateGoal(event)" class="space-y-5"><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Goal Name</label><input type="text" id="goal-name" required placeholder="e.g. New Laptop" class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm placeholder-gray-500 focus:border-midnight-500 transition-all"></div><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Target Amount (₹)</label><input type="number" id="goal-target" required min="1" placeholder="e.g. 80000" class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm placeholder-gray-500 focus:border-midnight-500 transition-all"></div><div><label class="text-xs text-gray-400 mb-1.5 block font-medium">Target Date</label><input type="date" id="goal-date" class="w-full px-4 py-3 bg-surface-900/80 border border-white/10 rounded-xl text-white text-sm focus:border-midnight-500 transition-all"></div><button type="submit" class="w-full py-3.5 gradient-accent rounded-xl font-bold text-midnight-900 btn-glow hover:opacity-90 transition-all text-sm"><i class="fas fa-check mr-2"></i>Create Goal</button></form></div>';
    document.body.appendChild(modal);
    modal.addEventListener('click', function(e) { if (e.target === modal) closeGoalModal(); });
}

function closeGoalModal() { var m = document.getElementById('goal-modal'); if (m) m.remove(); }

async function handleCreateGoal(e) {
    e.preventDefault();
    var data = { name: document.getElementById('goal-name').value, targetAmount: parseFloat(document.getElementById('goal-target').value), targetDate: document.getElementById('goal-date').value || null, icon: 'fa-bullseye' };
    try { await api.post('/api/goals', data); showToast('Goal created!'); closeGoalModal(); renderGoals(); } catch (err) { showToast(err.message, 'error'); }
}

async function contributeGoal(id) {
    var amount = prompt('Enter amount to add:');
    if (!amount || isNaN(amount)) return;
    try { await api.post('/api/goals/' + id + '/contribute?amount=' + parseFloat(amount)); showToast('Contribution added!'); renderGoals(); } catch (err) { showToast(err.message, 'error'); }
}

async function deleteGoal(id) {
    if (!confirm('Delete this goal?')) return;
    try { await api.delete('/api/goals/' + id); showToast('Goal deleted'); renderGoals(); } catch (err) { showToast(err.message, 'error'); }
}
