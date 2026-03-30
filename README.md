# Jira Cross Instance Attachment Migrator

🚀 A Java-based solution to migrate Jira attachments across instances using REST APIs — without plugins or external tools.

---

## 🧠 Problem Statement

Migrating attachments across Jira instances is not straightforward.

Most approaches depend on:
- Jira admins with elevated permissions  
- Paid plugins or external tools  
- Full instance migration instead of selective transfer  

There is no simple way to migrate attachments selectively across instances.

## 💡 Solution

This project uses Jira REST APIs with Java to build a custom attachment migration solution.

It enables:
- Cross-instance migration  
- CSV-based issue mapping  
- Fully automated execution  

---

## ⚙️ Features

- 📎 Migrate attachments between Jira instances  
- 📄 CSV-driven bulk migration (1000+ issues)  
- 🔁 Multiple attachments per issue  
- ⚡ Streaming upload (no local storage)  
- 🔐 REST API authentication  
- 🚀 Parallel execution (thread pool)  
- 🔁 Retry mechanism for API limits

---

## 📂 Project Structure

```
src/
 ├── JiraAttachmentCsvRunner.java
 └── JiraIssueBulkAttachmentMigratorCsv.java

sample-data/
 └── sample-mapping.csv

```

## 🔧 Configuration

Update the following values before running:

```
SOURCE_BASE_URL = "https://your-source.atlassian.net";
SOURCE_EMAIL = "your-email";
SOURCE_API_TOKEN = "your-api-token";

TARGET_BASE_URL = "https://your-target.atlassian.net";
TARGET_EMAIL = "your-email";
TARGET_API_TOKEN = "your-api-token";
⚠️ Recommended: Use environment variables instead of hardcoding credentials.


```

## ▶️ How to Run
Prepare CSV file with source and target issue keys.
Update configuration in JiraIssueBulkAttachmentMigratorCsv.java - 
Run JiraAttachmentCsvRunner.java -
Monitor console logs for progress

```
 📊 Example CSV
Source Key,Target Key
TUD-1127,DUM-13017
TUD-1128,DUM-13018

```

---

## 🔗 Atlassian Community

Check out the full discussion here:  
👉 https://community.atlassian.com/forums/Jira-questions/How-can-we-migrate-Jira-attachments-across-instances-using-REST/qaq-p/3213603

---

## 💛 Learning Outcome

This project demonstrates how Jira can be extended beyond UI limitations using engineering approaches.

It reflects:
- REST API integration  
- Streaming large files efficiently  
- Handling rate limits and retries  
- Backend problem-solving mindset  

---

## ⭐ Support

If you found this useful, give it a ⭐ and share your feedback!

