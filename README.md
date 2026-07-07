[README.md](https://github.com/user-attachments/files/29735447/README.md)
# 🧩 桌遊馬戲團 (Board Game Circus)



![Java](https://img.shields.io/badge/Java-11+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-007396?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

------------------------------------------------------------------------

## 📖 專案介紹

**桌遊馬戲團 (Board Game Circus)** 想把經典桌遊開發成 Java Desktop 的應用程式平台。

目前尚只推出**圖靈解密**

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

### 📊 管理後台

-   會員管理
-   卡片 CRUD
-   DAU、新會員、通關率、金幣統計

#### 🎮 圖靈解密遊戲核心

1.  選擇三個數字
2.  驗證最多三張卡片
3.  邏輯推理
4.  提交答案

#### 📝 推理筆記

-   3×5 數字矩陣
-   空白 / O / X 標記



------------------------------------------------------------------------

## 🏗️ 專案架構

``` text
turing-machine-java/
├── pom.xml                               # Maven 專案設定檔 
├── schema.sql                            # MySQL 8.0 資料庫結構與測試資料初始化腳本 (含會員、配置、報表結構)
└── src/
    └── main/
        └── java/
            └── com/
                └── turing/
                    ├── model/            # 實體模型
                    │   ├── Code.java           -- 3位數提案代碼 (Blue, Yellow, Purple 1~5)
                    │   ├── VerifierCard.java   -- 圖靈數學條件驗證卡與驗證演算法
                    │   ├── Puzzle.java         -- 謎題組合 (題目難度、真密碼、作用中的驗證卡)
                    │   ├── GameRecord.java     -- 儲存至資料庫的遊戲戰績
                    │   ├── User.java           -- 會員帳戶實體 (ID, 帳號, 密碼, 角色, 代幣, 封鎖狀態)
                    │   └── GameResultDto.java  -- 賽局最終破譯結算傳輸物件 (勝負結局、動態文字組裝)
                    │
                    ├── dao/              # 資料存取層
                    │   ├── RecordDao.java      -- 戰績存取介面
                    │   ├── UserDao.java        -- 會員帳戶與財富榜 SQL 存取介面
                    │   ├── GameConfigDao.java  -- A/B/C 三大難度規則代幣發放閾值存取介面
                    │   ├── ReportDao.java      -- 營運戰情報表數據統計介面
                    │   └── impl/
                    │       ├── RecordDaoImpl.java    -- 實作 JDBC (戰績新增/查詢)，支援 MySQL 與本機記憶體雙重容錯
                    │       ├── UserDaoImpl.java      -- 實作會員 CRUD、快速封鎖、扣款發放、本地富豪統計
                    │       ├── GameConfigDaoImpl.java -- 實作動態讀取/儲存管理員調整的關卡代幣發放規則
                    │       └── ReportDaoImpl.java    -- 實作全服不重複活躍用戶(DAU)、拿幣率複雜大數據 SQL
                    │
                    ├── service/          # 業務邏輯層
                    │   ├── GameService.java    -- 圖靈解密核心服務介面 (生成、驗證、破譯、紀錄)
                    │   ├── UserService.java    -- 會員身分驗證與錢包代幣交易增減邏輯介面
                    │   ├── ReportService.java  -- 背景執行緒日誌統計、自動斷電漏跑漏算修補服務介面
                    │   └── impl/
                    │       ├── GameServiceImpl.java  -- 實作 Easy/Standard/Hard 題庫與隨機產生機制
                    │       ├── UserServiceImpl.java  -- 實作安全登入判斷、防封鎖阻斷、破譯成功動態代幣結算邏輯
                    │       └── ReportServiceImpl.java --實作 Thread 背景非同步定時自動檢查與 Catch-up 報表同步
                    │
                    ├── controller/       # 流程控制層
                    │   ├── GameController.java   -- 協調主畫面 Swing 介面操作與遊戲服務之資料轉換
                    │   └── UserController.java   -- 控制登入/註冊流向，協調管理員後台對會員權限進行高動態控制
                    │
                    ├── util/             # 工具程式
                    │   ├── DbUtil.java           -- MySQL 8.0 驅動載入與 Connection 取得，具備 Offline Mode 自動降級
                    │   ├── PuzzleGenerator.java  -- 核心智慧謎題自動機，負責生成不重複隨機圖靈序號
                    │   ├── TuringCardRegistry.java-- 全服圖靈卡片註冊表，定義所有核心數學篩選判定電路
                    │   └── CardAssetManager.java -- 卡片區域坐標管理，精準控制 JToggleButton 覆蓋位置
                    │
                    ├── exception/        # 例外處理
                    │   └── GameException.java    -- 自訂執行期例外
                    │
                    └── view/             # 視圖層（Pixel-Perfect Flat UI 極簡扁平化介面組）
                        ├── LoginFrame.java       -- 高質感系統登入與帳號註冊跳轉視窗
                        ├── LobbyFrame.java       -- 遊戲大廳主畫面 (圓角卡片排版、剩餘代幣餘額動態更新)
                        ├── MainFrame.java        -- 核心遊戲操作全螢幕視窗 (內建加減微調 Stepper、90% 劇院高清圖片說明書)
                        └── AdminConsoleDialog.java-- 核心後台管理彈窗 (支援雙向機動多載建構子、無豆腐塊營運報表)
```

------------------------------------------------------------------------

## 💻 開發環境

  項目    版本
  ------- ------------
  Java    11+
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
