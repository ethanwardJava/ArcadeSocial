# ArcadeSocial – Ultra-Anonymous Ephemeral Chat

**No profiles. No logs. No trace.**  
Just pure, secure, temporary conversations.

---

## Overview

**ArcadeSocial** is a minimalist, privacy-first messaging platform designed for **completely anonymous, short-lived text communication**.

- **Zero registration** – No accounts, no emails, no phone numbers.
- **No profile pictures** – Pure text only.
- **Temporary chat IDs** – One-time use, auto-generated for each session.
- **Self-destructing chats** – Everything is **wiped when users leave** or after **24 hours max**.
- **In-memory only** – No database, no persistence, no recovery.

> **Once the chat ends — it never existed.**

---

## How It Works

1. User clicks **"Open a Chat"** → System generates a **unique 8-character temp ID** (e.g., `A7K9-M2P1`).
2. User shares the ID with another person (via any secure channel).
3. Second user enters the ID → **Joins the same chat instantly**.
4. Both send and receive **plain text messages in real time**.
5. When **either user closes the session**, the chat is marked for deletion.
6. When **both users exit**, the **entire conversation is erased from memory**.
7. Any remaining data is **force-deleted after 24 hours**.

---

## Privacy & Security

| Feature | Implementation |
|-------|----------------|
| **Anonymous by Design** | No user identifiers stored |
| **No Persistent Storage** | All data held **only in RAM** |
| **Auto-Delete on Exit** | Chat removed when last participant leaves |
| **24-Hour TTL** | Hard expiration for all sessions |
| **No Logging** | Zero access logs, IP tracking, or metadata |
| **Encrypted Transport** | WebSocket over TLS 1.3 |

---

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3 (WebFlux for reactive streams)
- **Real-time**: WebSocket + STOMP
- **Data**: `ConcurrentHashMap` (in-memory, no DB)
- **Cleanup**: `@Scheduled` task for TTL enforcement
- **Security**: Input validation, rate limiting, TLS
- **Container**: Docker-ready

---
