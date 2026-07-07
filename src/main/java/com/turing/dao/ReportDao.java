package com.turing.dao;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface ReportDao {
    /**
     * 獲取統計表中最新的一筆報表日期
     * @return 如果表中完全沒資料，回傳 null
     */
    Date getLatestReportDate() throws SQLException;

    /**
     * 核心：計算並生成特定某一天的日報表，並寫入 daily_platform_summary
     * @param targetDate 要統計的那一天
     */
    void generateDailySummary(Date targetDate) throws SQLException;
    
    public List<Object[]> getDailyReports() throws SQLException;
    
    public List<Object[]> getMonthlyReports() throws SQLException;
}