# Jira Cross Instance Attachment Migrator

🚀 A Java-based solution to migrate Jira attachments across instances using REST APIs — without relying on plugins or external tools.

---

## 🧠 Problem Statement

Migrating attachments across Jira instances is not straightforward.

Most approaches depend on:
- Jira admins with elevated permissions
- Paid plugins or external migration tools

There is no simple, flexible way to migrate attachments selectively across instances.

---

## 💡 Solution

This project demonstrates how to use Jira REST APIs with Java to build a custom attachment migration solution.

It enables:
- Cross-instance attachment migration
- Selective issue mapping using CSV
- Fully automated execution

---

## ⚙️ Features

- 📎 Migrate attachments from source to target Jira issues  
- 📄 CSV-driven bulk migration (1000+ issues supported)  
- 🔁 Handles multiple attachments per issue  
- ⚡ Streaming-based transfer (no local file storage required)  
- 🔐 Uses Jira REST API authentication  

---

## 🏗️ Architecture Overview

1. Read source and target issue mapping from CSV  
2. Fetch attachments from source Jira using REST API  
3. Stream attachment data using InputStream  
4. Upload attachments to target Jira using multipart API  

---

## 🧪 Challenges Solved

- ❗ 403 Authentication issues  
- 🔀 303 Redirect handling for attachment downloads  
- ⚡ Efficient streaming using InputStream / OutputStream  
- 📦 Handling large-scale bulk migrations  

---


## 🔗 Atlassian Community Discussion

Check out the full discussion here: [View Post]([PASTE_YOUR_LINK_HERE](https://community.atlassian.com/forums/Jira-questions/How-can-we-migrate-Jira-attachments-across-instances-using-REST/qaq-p/3213603#M1174241))

