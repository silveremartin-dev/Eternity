const connectBtn = document.getElementById('connectBtn');
const disconnectBtn = document.getElementById('disconnectBtn');
const usernameInput = document.getElementById('usernameInput');
const logArea = document.getElementById('logArea');
const connectionStatus = document.getElementById('connectionStatus');
const jobStatus = document.getElementById('jobStatus');

let socket;
let isConnected = false;

connectBtn.addEventListener('click', connect);
disconnectBtn.addEventListener('click', disconnect);

function connect() {
    const username = usernameInput.value.trim();
    if (!username) {
        log('Please enter a username');
        return;
    }

    // Connect to WebSocket server on port 12346 (Server port + 1)
    socket = new WebSocket('ws://localhost:12346');

    socket.onopen = () => {
        isConnected = true;
        updateUIState(true);
        log('Connected to server');
        
        // Login
        send({
            command: 'LOGIN',
            username: username
        });
    };

    socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        handleMessage(data);
    };

    socket.onclose = () => {
        isConnected = false;
        updateUIState(false);
        log('Disconnected from server');
    };

    socket.onerror = (error) => {
        log('WebSocket error: ' + error);
    };
}

function disconnect() {
    if (socket) {
        socket.close();
    }
}

function send(data) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(data));
    }
}

function handleMessage(data) {
    switch (data.command) {
        case 'LOGIN_SUCCESS':
            log('Server: ' + data.message);
            requestJob();
            break;
        case 'NO_JOB':
            log('Server: ' + data.message);
            jobStatus.textContent = 'Idle (No Job)';
            setTimeout(requestJob, 5000); // Retry after 5s
            break;
        case 'ERROR':
            log('Error: ' + data.message);
            break;
        default:
            log('Unknown command: ' + data.command);
    }
}

function requestJob() {
    if (!isConnected) return;
    
    jobStatus.textContent = 'Requesting Job...';
    send({
        command: 'JOB_REQUEST'
    });
}

function updateUIState(connected) {
    connectBtn.disabled = connected;
    disconnectBtn.disabled = !connected;
    usernameInput.disabled = connected;
    
    if (connected) {
        connectionStatus.textContent = '● Connected';
        connectionStatus.style.color = 'green';
    } else {
        connectionStatus.textContent = '● Disconnected';
        connectionStatus.style.color = 'red';
        jobStatus.textContent = 'Idle';
    }
}

function log(message) {
    const entry = document.createElement('div');
    entry.className = 'log-entry';
    
    const time = new Date().toLocaleTimeString();
    const timeSpan = document.createElement('span');
    timeSpan.className = 'log-time';
    timeSpan.textContent = `[${time}]`;
    
    entry.appendChild(timeSpan);
    entry.appendChild(document.createTextNode(message));
    
    logArea.appendChild(entry);
    logArea.scrollTop = logArea.scrollHeight;
}
