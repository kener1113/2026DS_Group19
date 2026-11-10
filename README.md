# MemeDetective

國立政治大學資料結構第 19 組的期末專題。HW8 提供一個網頁搜尋介面，可用 Tavily 查詢網頁並查看標題、連結與摘要。

- [HW8 原始碼](hw8-tavily-search/)
- [完整啟動、測試與繳交說明](hw8-tavily-search/README.md)

需要 JDK 17 或以上。macOS 的 zsh 可用以下指令啟動。金鑰輸入時不會回顯。

```zsh
cd hw8-tavily-search
read -rs 'TAVILY_API_KEY?輸入 Tavily API key：'; echo
export TAVILY_API_KEY
./mvnw spring-boot:run
```

啟動後開啟 [http://127.0.0.1:8080](http://127.0.0.1:8080)。Windows、Bash 與其他操作請見上方完整說明。
