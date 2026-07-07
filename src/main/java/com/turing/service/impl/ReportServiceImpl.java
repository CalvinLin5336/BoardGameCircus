package com.turing.service.impl;

import com.turing.dao.impl.ReportDaoImpl;

import java.sql.Date;
import java.time.LocalDate;

import com.turing.dao.ReportDao;
import com.turing.service.ReportService;

public class ReportServiceImpl implements ReportService{

	private final ReportDao reportDao = new ReportDaoImpl();
	@Override
	public void checkAndCatchUpReports() {
		try {
            // 1. 取得資料庫裡最後一次做報表是哪一天
            Date latestDateInDb = reportDao.getLatestReportDate();
            
            // 2. 定義「昨天」是哪一天（因為日報表通常只能結算到昨天）
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            LocalDate startDate;
            
            if (latestDateInDb == null) {
                // 如果全新的系統，表裡完全沒資料，我們就預設從「一週前」開始補算（或者你可以指定平台上線日）
                startDate = LocalDate.now().minusDays(365);
                System.out.println("ℹ️ [報表系統] 偵測到首次運行，將初始化過去 7 天的報表...");
            } else {
                // 如果有舊資料，補跑的起點就是「資料庫最後日期的隔一天」
                startDate = latestDateInDb.toLocalDate().plusDays(1);
            }

            // 3. 🧠 核心 For 迴圈：只要起點日期小於等於昨天，就代表有漏跑，進去補算！
            while (!startDate.isAfter(yesterday)) {
                Date targetSqlDate = Date.valueOf(startDate);
                
                // 呼叫 DAO 去對那一天做大數據加總並寫入
                reportDao.generateDailySummary(targetSqlDate);
                
                // 算完一天，指標日期加一天，繼續往今天推進
                startDate = startDate.plusDays(1);
            }
            
            System.out.println("✅ [報表系統] 全服日報表檢查完畢，目前數據已完美同步至最新狀態。");

        } catch (Exception e) {
            System.err.println("❌ [報表系統] 自動補跑報表時發生異常: " + e.getMessage());
            e.printStackTrace();
        }
    }
		
}


