# MemeDetective — NCCU Java HW8

Spring Boot + Thymeleaf 搜尋網站。Java 17 以上（已使用 JDK 21 驗證）。

## 啟動（macOS / Linux）

在此 README 所在的資料夾開啟終端機：

```sh
chmod +x mvnw
# macOS：安全輸入全新金鑰，不會寫入 shell 歷史或顯示在螢幕
read -s 'TAVILY_API_KEY?輸入新 Tavily API key：'; echo
export TAVILY_API_KEY
./mvnw spring-boot:run
```

上述 read 語法適用 macOS 預設 zsh；Bash 請用 `read -s -p '輸入新金鑰：' TAVILY_API_KEY`。
開啟 http://127.0.0.1:8080 。若 JAVA_HOME 無效，macOS Homebrew JDK 可先設定：

```sh
export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
```

## Windows PowerShell

安裝 JDK 17 或 21 並設定 JAVA_HOME，接著：

```powershell
$secret = Read-Host '輸入新的 Tavily API key' -AsSecureString
$env:TAVILY_API_KEY = [System.Net.NetworkCredential]::new('', $secret).Password
.\mvnw.cmd spring-boot:run
```

首次執行 wrapper 需要網路下載 Maven 與依賴。沒有金鑰仍可啟動並查看首頁與缺少金鑰的提示；不會產生假結果。
環境變數必須在啟動 Java 的同一個終端機設定；此專案不會自動載入 `.env`。

## 使用方式與作業對照

1. 表單 GET `/`，查詢參數 `q`；Controller 檢查空白與 300 字限制。
2. Service 從 `TAVILY_API_KEY` 讀取金鑰；後端 POST `https://api.tavily.com/search`，Header 使用 `Authorization: Bearer …`。
3. 請求 JSON 包含 query、search_depth=basic、max_results=8、include_answer=false、include_raw_content=false。
4. 解析 results 的 title、url、content，在 Thymeleaf 畫面顯示標題、可點擊網址和摘要。
5. 無結果、無金鑰、認證錯誤、額度限制、逾時與格式異常均有友善提示。
6. 顯示內容由 Thymeleaf 跳脫，只允許 http/https 結果連結；金鑰不送到前端，不印出上游錯誤內容。

官方文件（2026-11-06 核對）：https://docs.tavily.com/documentation/api-reference/endpoint/search

## 測試與打包

```sh
./mvnw test
./mvnw clean package
java -jar target/memedetective-hw8-1.0.0.jar
```

自動測試使用本地 mock HTTP 回應，驗證 POST/Bearer、結果解析、安全網址、空結果、缺少金鑰、上游錯誤與查詢驗證；它們不代表真實 Tavily 搜尋已成功。

## 真實搜尋截圖（必做）

1. 先在 Tavily Dashboard 撤銷曾貼到聊天的舊金鑰，建立新金鑰。
2. 使用上面的私密輸入方式啟動，再搜尋 `國立政治大學` 或其他任意字詞。
3. 確認畫面顯示多筆真實標題、網址與摘要，點一筆確認連結可用。
4. 擷取瀏覽器畫面，保留關鍵字與搜尋結果；不要截取金鑰或 Dashboard。
5. 「介面預覽／缺少金鑰」截圖僅用來檢視介面，不可冒充作業要求的成功搜尋截圖。

## Moodle 繳交

上傳真實 Tavily 搜尋成功的截圖及本資料夾的公開 GitHub 網址。截止時間為 2026-11-15 23:59（台北時間）。
