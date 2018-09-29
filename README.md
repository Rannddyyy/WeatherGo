 Weather Go 
===============================

<img src="https://i.imgur.com/sd2pFS4.png" width="100">

Introduction
---

Weather Go，一款 Android App 結合了 `氣象查詢` 以及 `路徑設定` 的功能，讓使用者只需要透過 Weather Go 即可取得路徑間的天氣資訊，而不再需要切換兩個 APP 造成使用體驗不佳，在低規格裝置上也可以輕鬆使用 Weather Go。

Prerequisites
---

- Android 6.0 (API 23) 含以上。
- GPS 定位服務
- WiFi 或行動網路
- 台灣地區限定使用

使用方法
---

### 主頁面

抓取裝置所在地理位置，畫面會顯示當前鄉鎮的天氣資訊，包括

- 顯示當前氣候圖示 (降雨或晴天...)
- 氣溫與體感溫度
- 風向與風速
- 降雨機率與濕度
- 未來 3 天的預測氣溫 (折線圖) 與降雨機率 (長條圖)
> <img src="https://i.imgur.com/AgR1OZj.png" width="200">

### 路徑規劃 
1. 使用 <img src="https://i.imgur.com/2lIfgcz.png" width="30"> 在地圖上選擇起點
2. 使用 <img src="https://i.imgur.com/zZpSCGd.png" width="30"> 在地圖上選擇終點
3. 繪製出路徑後，點擊路徑上任何位置，即可顯示該地的天氣資訊

> <img src="https://i.imgur.com/eNjGFgC.png" width="200">


系統架構設計
---

+ 基於 Android Studio 環境開發
+ buildToolsVersion = 27.0.3
+ 使用套件
    - [Google Maps Geocoding API_v11.6.2](https://developers.google.com/maps/documentation/geocoding/start) 透過經取得縣市鄉鎮資料
    - [Google Android Maps API_v11.6.2](https://developers.google.com/maps/documentation/android-sdk/intro) 支持 Weather Go  的 Map 設定
    - [Google Maps Direction API_v11.6.2](https://developers.google.com/maps/documentation/directions/intro) 用於路線規劃
    - [Google Places SDK for Android_v11.6.2](https://developers.google.com/places/android-sdk/start) 用於在 Map 上
選擇特定地點以及搜尋地點 Autocomplete
    - [Apache HttpClient API_v4.5.3](https://www.google.com/search?q=Apache+HttpClient+API&ie=utf-8&oe=utf-8&client=firefox-b-ab) 用於建立HTTP連線取得opendata
    - [MPAndroidChart_v2.1.6]() 用於建立統計圖表
    - [交通部中央氣象局-開放資料平台](http://opendata.cwb.gov.tw/index;jsessionid=9D8537279F7CFA78A97AA16889CE1784)

Screenshots
---
 <img src="https://i.imgur.com/4irHmJj.png" width="200"> <img src="https://i.imgur.com/c55BXbg.png" width="200"> <img src="https://i.imgur.com/bm3kdOm.png" width="200">
 <img src="https://i.imgur.com/8NUAyn4.png" width="200"> <img src="https://i.imgur.com/f0aZlOh.png" width="200">








