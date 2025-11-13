'use strict';

const $ = (s, p = document) => p.querySelector(s);

const usernamePage   = $('#username-page');
const chatPage       = $('#chat-page');
const usernameForm   = $('#usernameForm');
const messageForm    = $('#messageForm');
const messageInput   = $('#message');
const messageArea    = $('#messageArea');
const connectingEl   = $('.connecting');

let stompClient = null;
let username    = null;

const avatarColors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

const hashCode = str => {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = (31 * hash + str.charCodeAt(i)) | 0;
    }
    return Math.abs(hash);
};

const getAvatarColor = sender => avatarColors[hashCode(sender) % avatarColors.length];

const createAvatar = sender => {
    const i = document.createElement('i');
    i.textContent = sender[0].toUpperCase();
    i.style.backgroundColor = getAvatarColor(sender);
    return i;
};

/* ---------- Connection ---------- */
const connect = event => {
    event.preventDefault();

    username = $('#name').value.trim();
    if (!username || username.length < 1 || username.length > 30) {
        alert('Username must be 1–30 characters.');
        return;
    }

    usernamePage.classList.add('hidden');
    chatPage.classList.remove('hidden');

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
};

const onConnected = () => {
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Send JOIN
    stompClient.send('/app/chat.addUser', {}, JSON.stringify({
        sender: username,
        type: 'JOIN'
    }));

    connectingEl.classList.add('hidden');
};

const onError = () => {
    connectingEl.textContent = 'Connection failed. Please refresh.';
    connectingEl.style.color = 'red';
};

/* ---------- Messaging ---------- */
const sendMessage = event => {
    event.preventDefault();
    const content = messageInput.value.trim();
    if (!content || !stompClient?.connected) return;

    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
        sender: username,
        content: content,
        type: 'CHAT'
    }));

    messageInput.value = '';
};

const onMessageReceived = payload => {
    const msg = JSON.parse(payload.body);
    const li = document.createElement('li');

    if (msg.type === 'JOIN' || msg.type === 'LEAVE') {
        li.className = 'event-message';
        li.textContent = msg.type === 'JOIN'
            ? `${msg.sender} joined the chat`
            : `${msg.sender} left the chat`;
    } else {
        li.className = `chat-message ${msg.sender === username ? 'sent' : ''}`;

        const avatar = document.createElement('div');
        avatar.className = 'avatar';
        avatar.textContent = msg.sender[0].toUpperCase();
        li.appendChild(avatar);

        const bubble = document.createElement('div');
        bubble.className = 'message-bubble';

        // Optional: Add voice wave (simulate)
        if (msg.content.includes('[voice]')) {
            const wave = document.createElement('div');
            wave.className = 'voice-wave';
            bubble.appendChild(wave);
            bubble.innerHTML += msg.content.replace('[voice]', '');
        } else {
            bubble.textContent = msg.content;
        }

        const meta = document.createElement('div');
        meta.className = 'message-meta';
        meta.textContent = new Date().toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
        bubble.appendChild(meta);

        li.appendChild(bubble);
    }

    messageArea.appendChild(li);
    messageArea.scrollTop = messageArea.scrollHeight;
};

/* ---------- Disconnect ---------- */
const disconnect = () => {
    if (stompClient && stompClient.connected) {
        stompClient.send('/app/chat.removeUser', {}, JSON.stringify({
            sender: username,
            type: 'LEAVE'
        }));
        stompClient.disconnect();
    }
};

window.addEventListener('beforeunload', disconnect);

/* ---------- Listeners ---------- */
usernameForm.addEventListener('submit', connect);
messageForm.addEventListener('submit', sendMessage);

