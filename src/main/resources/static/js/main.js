'use strict';

// DOM Elements
const usernamePage = document.getElementById('username-page');
const chatPage = document.getElementById('chat-page');
const usernameForm = document.getElementById('usernameForm');
const messageForm = document.getElementById('messageForm');
const messageInput = document.getElementById('message');
const messageArea = document.getElementById('messageArea');
const connectingEl = document.getElementById('connectingEl');
const onlineUsers = document.getElementById('onlineUsers');
const onlineCount = document.getElementById('onlineCount');

// Global State
let stompClient = null;
let username = null;
let onlineUsersList = new Set();

// Connect to WebSocket
const connect = (event) => {
    event.preventDefault();

    username = document.getElementById('name').value.trim();
    if (!username || username.length < 1 || username.length > 30) {
        alert('Gamer tag must be 1-30 characters long.');
        return;
    }

    usernamePage.classList.add('hidden');
    chatPage.classList.remove('hidden');

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
};

const onConnected = () => {
    // Subscribe to public messages
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Send JOIN notification
    stompClient.send('/app/chat.addUser', {}, JSON.stringify({
        sender: username,
        type: 'JOIN'
    }));

    connectingEl.classList.add('hidden');
    messageInput.focus();
};

const onError = (error) => {
    console.error('Connection error:', error);
    connectingEl.textContent = 'Failed to connect. Please refresh.';
    connectingEl.style.background = 'var(--bg-secondary)';
    connectingEl.style.color = '#ef4444';
};

// Message Handling
const sendMessage = (event) => {
    event.preventDefault();
    const content = messageInput.value.trim();
    if (!content || !stompClient?.connected) return;

    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
        sender: username,
        content: content,
        type: 'CHAT'
    }));

    messageInput.value = '';
    messageInput.focus();
};

const onMessageReceived = (payload) => {
    const msg = JSON.parse(payload.body);

    if (msg.type === 'JOIN' || msg.type === 'LEAVE') {
        handleUserEvent(msg);
    } else {
        displayChatMessage(msg);
    }
};

// User Management
const handleUserEvent = (msg) => {
    if (msg.type === 'JOIN') {
        onlineUsersList.add(msg.sender);
        displayJoinMessage(msg.sender);
    } else {
        onlineUsersList.delete(msg.sender);
        displayLeaveMessage(msg.sender);
    }
    updateOnlineUsers();
};

const updateOnlineUsers = () => {
    onlineUsers.innerHTML = '';
    onlineCount.textContent = onlineUsersList.size;

    onlineUsersList.forEach(user => {
        const userElement = document.createElement('div');
        userElement.className = 'user-item';
        userElement.innerHTML = `
            <div class="user-avatar">${user.charAt(0).toUpperCase()}</div>
            <div class="user-info">
                <div class="user-name">${user}</div>
                <div class="user-status">
                    <span class="status-dot"></span>
                    Online
                </div>
            </div>
        `;
        onlineUsers.appendChild(userElement);
    });
};

// Message Display
const displayJoinMessage = (sender) => {
    const eventElement = document.createElement('div');
    eventElement.className = 'event-message';
    eventElement.innerHTML = `<div class="event-text">🎮 ${sender} joined the lobby</div>`;
    messageArea.appendChild(eventElement);
    messageArea.scrollTop = messageArea.scrollHeight;
};

const displayLeaveMessage = (sender) => {
    const eventElement = document.createElement('div');
    eventElement.className = 'event-message';
    eventElement.innerHTML = `<div class="event-text">👋 ${sender} left the lobby</div>`;
    messageArea.appendChild(eventElement);
    messageArea.scrollTop = messageArea.scrollHeight;
};

const displayChatMessage = (msg) => {
    const messageElement = document.createElement('div');
    const isSent = msg.sender === username;

    messageElement.className = `message ${isSent ? 'sent' : ''}`;
    messageElement.innerHTML = `
        <div class="message-bubble">
            ${!isSent ? `<div class="message-meta"><span class="message-sender">${msg.sender}</span></div>` : ''}
            <div class="message-content">${msg.content}</div>
            <div class="message-meta">
                <span></span>
                <span class="message-time">${new Date().toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'})}</span>
            </div>
        </div>
    `;

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
};

// Utility Functions
const disconnect = () => {
    if (stompClient && stompClient.connected) {
        stompClient.send('/app/chat.removeUser', {}, JSON.stringify({
            sender: username,
            type: 'LEAVE'
        }));
        stompClient.disconnect();
    }
};

const createGameRoom = () => {
    alert('Game room creation feature coming soon! 🎮');
};

// Event Listeners
usernameForm.addEventListener('submit', connect);
messageForm.addEventListener('submit', sendMessage);
window.addEventListener('beforeunload', disconnect);

// Enter key to send message
messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage(e);
    }
});