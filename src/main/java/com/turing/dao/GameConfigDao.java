package com.turing.dao;

import java.sql.SQLException;
import java.util.List;


import com.turing.model.GameConfig;

/**
 * Data Access Object interface for system game configurations.
 */
public interface GameConfigDao {
    
	String getValue(String key);
    
    void updateValue(String key, String value);
    
    
    
    public List<GameConfig> getAllConfigs() throws SQLException;
    
    public int getConfigValue(char diffChar, String columnName);
    
    public void updateConfig(char diffChar, int roundsLimit, int tokens) throws SQLException;
}
