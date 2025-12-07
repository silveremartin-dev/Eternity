/**
 * Eternity II Web Client
 * JavaScript application for solving visualization
 */

class EternityApp {
    constructor() {
        this.boardSize = 16;
        this.cells = [];
        this.isRunning = false;
        this.startTime = null;
        this.stats = {
            placedCount: 0,
            candidatesChecked: 0,
            maxDepth: 0
        };

        this.init();
    }

    init() {
        this.createBoard();
        this.bindEvents();
        this.checkServerStatus();
        this.log('Application initialized');
    }

    createBoard() {
        const board = document.getElementById('board');
        board.innerHTML = '';

        for (let y = 0; y < this.boardSize; y++) {
            for (let x = 0; x < this.boardSize; x++) {
                const cell = document.createElement('div');
                cell.className = 'cell';
                cell.dataset.x = x;
                cell.dataset.y = y;

                // Mark corners and edges
                const isCorner = (x === 0 || x === this.boardSize - 1) &&
                    (y === 0 || y === this.boardSize - 1);
                const isEdge = x === 0 || x === this.boardSize - 1 ||
                    y === 0 || y === this.boardSize - 1;

                if (isCorner) {
                    cell.classList.add('corner');
                } else if (isEdge) {
                    cell.classList.add('edge-piece');
                }

                board.appendChild(cell);
                this.cells.push(cell);
            }
        }
    }

    bindEvents() {
        // Navigation
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.switchView(e.target.dataset.view));
        });

        // Controls
        document.getElementById('startBtn').addEventListener('click', () => this.start());
        document.getElementById('pauseBtn').addEventListener('click', () => this.pause());
        document.getElementById('resetBtn').addEventListener('click', () => this.reset());
    }

    switchView(viewName) {
        // Update nav buttons
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.view === viewName);
        });

        // Update views
        document.querySelectorAll('.view').forEach(view => {
            view.classList.toggle('active', view.id === viewName + 'View');
        });
    }

    async checkServerStatus() {
        const statusDot = document.getElementById('serverStatus');
        const statusText = document.getElementById('serverStatusText');

        try {
            const response = await fetch('http://localhost:9090/health');
            if (response.ok) {
                statusDot.className = 'status-dot connected';
                statusText.textContent = 'Connected';
                this.log('Connected to server');
            } else {
                throw new Error('Server error');
            }
        } catch (error) {
            statusDot.className = 'status-dot error';
            statusText.textContent = 'Disconnected';
            this.log('Cannot connect to server - running in demo mode');

            // Retry in 5 seconds
            setTimeout(() => this.checkServerStatus(), 5000);
        }
    }

    start() {
        if (this.isRunning) return;

        this.isRunning = true;
        this.startTime = Date.now();
        this.log('Solver started');

        // Demo: simulate solving
        this.simulateSolving();
    }

    simulateSolving() {
        if (!this.isRunning) return;

        // Randomly fill a cell
        const emptyCells = this.cells.filter(c => !c.classList.contains('filled'));
        if (emptyCells.length > 0) {
            const randomCell = emptyCells[Math.floor(Math.random() * emptyCells.length)];
            randomCell.classList.add('filled');
            this.stats.placedCount++;
            this.stats.candidatesChecked += Math.floor(Math.random() * 1000) + 100;
            this.stats.maxDepth = Math.max(this.stats.maxDepth, this.stats.placedCount);

            this.updateStats();
        }

        if (this.stats.placedCount < this.boardSize * this.boardSize) {
            setTimeout(() => this.simulateSolving(), 50);
        } else {
            this.isRunning = false;
            this.log('Puzzle solved!');
        }
    }

    pause() {
        this.isRunning = false;
        this.log('Solver paused');
    }

    reset() {
        this.isRunning = false;
        this.stats = { placedCount: 0, candidatesChecked: 0, maxDepth: 0 };

        this.cells.forEach(cell => {
            cell.classList.remove('filled');
        });

        this.updateStats();
        this.log('Board reset');
    }

    updateStats() {
        const total = this.boardSize * this.boardSize;
        const percent = Math.round((this.stats.placedCount / total) * 100);
        const elapsed = this.startTime ? (Date.now() - this.startTime) / 1000 : 0;

        // Placed count
        document.getElementById('placedCount').textContent =
            `${this.stats.placedCount}/${total}`;

        // Pieces per second
        const pps = elapsed > 0 ? Math.round(this.stats.placedCount / elapsed) : 0;
        document.getElementById('pps').textContent = this.formatNumber(pps);

        // Candidates per second
        const cps = elapsed > 0 ? Math.round(this.stats.candidatesChecked / elapsed) : 0;
        document.getElementById('cps').textContent = this.formatNumber(cps);

        // Elapsed time
        document.getElementById('elapsed').textContent = this.formatTime(elapsed);

        // Max depth
        document.getElementById('depth').textContent = this.stats.maxDepth;

        // Progress bar
        document.getElementById('progressFill').style.width = `${percent}%`;
        document.getElementById('progressPercent').textContent = `${percent}%`;

        // ETA
        if (pps > 0 && this.stats.placedCount < total) {
            const remaining = (total - this.stats.placedCount) / pps;
            document.getElementById('progressEta').textContent = `ETA: ${this.formatTime(remaining)}`;
        } else if (this.stats.placedCount >= total) {
            document.getElementById('progressEta').textContent = 'Complete!';
        }

        // Global stats
        document.getElementById('totalCandidates').textContent =
            this.formatNumber(this.stats.candidatesChecked);
        document.getElementById('totalPieces').textContent =
            this.formatNumber(this.stats.placedCount);
        document.getElementById('avgSpeed').textContent =
            (cps / 1000000).toFixed(2);
    }

    formatNumber(n) {
        if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M';
        if (n >= 1000) return (n / 1000).toFixed(1) + 'K';
        return n.toString();
    }

    formatTime(seconds) {
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    log(message) {
        const logEl = document.getElementById('log');
        const time = new Date().toLocaleTimeString();
        const entry = document.createElement('div');
        entry.className = 'log-entry';
        entry.innerHTML = `<span class="time">[${time}]</span> ${message}`;
        logEl.appendChild(entry);
        logEl.scrollTop = logEl.scrollHeight;
    }
}

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    window.app = new EternityApp();
});
