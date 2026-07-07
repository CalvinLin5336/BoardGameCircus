package com.turing.dao;

import com.turing.model.GameRecord;
import java.util.List;

/**
 * Data Access Object (DAO) Interface for managing game history records in the database.
 */
public interface RecordDao {
    
    /**
     * Saves a new game record to the database.
     * @param record the record to save
     */
    void save(GameRecord record);

    /**
     * Retrieves all saved game records.
     * @return list of game records
     */
    List<GameRecord> findAll();

    /**
     * Clears all saved game records (for reset/clean up).
     */
    void deleteAll();
    
    
}
