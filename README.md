# Jira Cross Instance Attachment Migrator

🚀 A Java-based solution to migrate Jira attachments across instances using REST APIs — without relying on plugins or external tools.

---

## 🧠 Problem Statement

Migrating attachments across Jira instances is not straightforward.

Most existing approaches depend on:
- Jira admins with elevated permissions  
- Paid plugins or external migration tools  
- Full instance migration instead of selective transfer  

There is no simple, flexible way to migrate attachments **selectively across instances**.

---

## 💡 Solution

This project demonstrates how to use Jira REST APIs with Java to build a custom attachment migration engine.

It enables:
- Cross-instance attachment migration  
- Selective issue mapping using CSV  
- Fully automated execution without plugins  

---

## ⚙️ Features

- 📎 Migrate attachments from source to target Jira issues  
- 📄 CSV-driven bulk migration (1000+ issues supported)  
- 🔁 Handles multiple attachments per issue  
- ⚡ Streaming-based transfer (no local file storage required)  
- 🔐 Uses Jira REST API authentication  
- 🚀 Parallel processing with controlled thread pool  
- 🔁 Retry mechanism with exponential backoff (rate-limit safe)  

---

## 🏗️ Architecture Overview

1. Read source and target issue mapping from CSV  
2. Fetch attachments from source Jira using REST API  
3. Stream attachment data using InputStream  
4. Upload attachments to target Jira using multipart API  
5. Handle failures with retry logic  

---

## 🧪 Challenges Solved

- ❗ 403 Authentication & permission issues  
- 🔀 303 Redirect handling for attachment downloads  
- ⚡ Efficient streaming using InputStream / OutputStream  
- 📦 Handling large-scale bulk migrations  
- 🚫 Avoiding memory overload (no temp file storage)  
- 🔁 Handling API rate limits with retry strategy  

---

## 📂 Project Structure
src/
 ├── JiraAttachmentCsvRunner.java
 ├── JiraIssueBulkAttachmentMigratorCsv.java
 └── README.md

sample-data/
 └── sample-mapping.csv

 ---

## 🔧 Configuration

Update the following values before running:

SOURCE_BASE_URL = "https://your-source.atlassian.net";
SOURCE_EMAIL = "your-email";
SOURCE_API_TOKEN = "your-api-token";

TARGET_BASE_URL = "https://your-target.atlassian.net";
TARGET_EMAIL = "your-email";
TARGET_API_TOKEN = "your-api-token";

⚠️ Recommended: Use environment variables instead of hardcoding credentials.

---

## ▶️ How to Run
Prepare CSV file with source and target issue keys
Update configuration in JiraIssueBulkAttachmentMigratorCsv.java
Run JiraAttachmentCsvRunner.java
Monitor console logs for progress

---

## 📊 Example CSV
Source Key,Target Key
TUD-1127,DUM-13017
TUD-1128,DUM-13018

---

## 🔗 Atlassian Community Discussion Check out the full discussion here: [View Post](https://community.atlassian.com/forums/Jira-questions/How-can-we-migrate-Jira-attachments-across-instances-using-REST/qaq-p/3213603#M1174241)

---

## 💛 Learning Outcome

This project demonstrates how Jira can be extended beyond UI limitations using engineering approaches.

It reflects:

REST API integration skills
Backend problem-solving
System design thinking
Handling real-world API challenges

---

## 🤝 Contributions

Feel free to raise issues or suggest improvements.

---

## ⭐ If you find this useful

Give it a star ⭐ and share your feedback!

---
