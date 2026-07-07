[README.md](https://github.com/user-attachments/files/29735447/README.md)
# 🧩 圖靈解密 (Turing Machine)

> **A Java Swing implementation inspired by the board game *Turing
> Machine*, featuring logical deduction gameplay, user management, and
> administrative analytics.**

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-007396?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

------------------------------------------------------------------------

## 📖 專案介紹

**圖靈解密（Turing Machine）** 是一款以經典桌遊 **Turing Machine**
為靈感所開發的 Java Desktop 應用程式。

玩家需要透過驗證卡逐步排除錯誤組合，推理出三位數秘密密碼。

### 功能

-   使用者登入/註冊
-   管理員後台
-   金幣獎勵系統
-   遊戲紀錄
-   統計分析
-   MySQL 資料庫

採用 **MVC + DAO Pattern** 架構。

------------------------------------------------------------------------

## ✨ 系統特色

### 👤 使用者系統

-   User / Admin 雙角色
-   登入驗證
-   金幣錢包
-   Header 即時資訊同步

### 🎮 遊戲核心

1.  選擇三個數字
2.  驗證最多三張卡片
3.  邏輯推理
4.  提交答案

### 📝 推理筆記

-   3×5 數字矩陣
-   空白 / O / X 標記

### 📊 管理後台

-   會員管理
-   卡片 CRUD
-   DAU、新會員、通關率、金幣統計

------------------------------------------------------------------------

## 🏗️ 專案架構

``` text
src
├── controller
├── service
├── dao
├── dao.impl
├── model
├── util
├── exception
└── view
```

------------------------------------------------------------------------

## 💻 開發環境

  項目    版本
  ------- ------------
  Java    17+
  MySQL   8.0+
  Maven   3.x
  GUI     Java Swing

------------------------------------------------------------------------

## 🗄️ 建立資料庫

``` sql
CREATE DATABASE turing_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

------------------------------------------------------------------------

## ▶️ 執行方式

``` bash
git clone https://github.com/your-name/TuringMachine.git
```

完成資料庫設定後執行 `MainFrame.java`。

------------------------------------------------------------------------

## 📄 License

MIT License
