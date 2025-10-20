# 💰 SmartSpend — Personal Expense Tracker  

A **lightweight Android app** built with **Kotlin** that helps users **track income and expenses**, **set budgets**, and **analyze spending patterns** with a simple and elegant interface.  

SmartSpend allows you to **add, edit, and delete transactions**, view **summaries**, and even **back up your financial data locally** — making it a perfect personal finance companion for daily use.

---

## 🛠 Tech Stack
- **Kotlin** 💜 – Core programming language  
- **AndroidX** 📱 – Modern Android framework  
- **RecyclerView** 🧩 – Dynamic transaction list display  
- **SharedPreferences** 💾 – Local data persistence  
- **JSON I/O** 📂 – Data backup and restore  
- **Gradle (KTS)** ⚙️ – Build automation system  

---

## ✨ Features

**💸 Transaction Management**  
- Add income or expense entries with title, category, and date  
- Edit or delete existing transactions effortlessly  
- Automatic color coding for income (green) and expenses (red)

**📊 Transaction Summary**  
- View total income, total expenses, and remaining balance  
- Budget progress bar to visualize your spending limit  
- Summary screen built with clean UI for quick insights  

**💾 Data Backup & Restore**  
- Export all transactions as a JSON backup file  
- Restore data anytime to recover previous entries  
- Ensures safe and reliable offline data management  

**⚙️ Budget Management**  
- Set and update monthly or custom budget limits  
- Visual progress bar showing budget utilization  
- Alerts when expenses exceed set limits  

**🏠 Navigation Flow**  
- Splash screen → Landing pages → Login → Home → Transaction Dashboard  
- Simple user flow for quick onboarding and ease of use  

---

## 💡 Use Cases
- **Students** – Manage daily expenses and savings  
- **Professionals** – Track monthly income and spending habits  
- **Small Business Owners** – Record business transactions on the go  
- **Families** – Maintain shared household budgets  

---

## 🚀 How to Run Locally

1. **Clone this repository**
   ```bash
   git clone https://github.com/yourusername/SmartSpend.git
   cd SmartSpend-main/SmartSpend-main

2. **Open in Android Studio**
   ```bash
   File → Open → Select the SmartSpend-main/SmartSpend-main directory
   Let Gradle sync automatically

3. **Run the App**
   ```bash
   Select an emulator or connected Android device
   Click ▶️ Run in Android Studio

3. **Build APK via CLI**
   ```bash
   ./gradlew assembleDebug
   Output APK: app/build/outputs/apk/debug/app-debug.apk

---

## 📱 App Highlights
- **Intuitive UI with Material design components**
- **Offline functionality (no internet required)**
- **Lightweight and optimized for performance**
- **Minimal permissions — secure and private**

---

## 📜 License
- **This project is licensed under the MIT License — feel free to use, modify, and share.**
