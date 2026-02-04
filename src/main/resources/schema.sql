ALTER TABLE apartments MODIFY price_per_m2 DECIMAL(15,2) NULL;
ALTER TABLE apartments ADD COLUMN IF NOT EXISTS entrance VARCHAR(50) NULL;
ALTER TABLE apartments ADD COLUMN IF NOT EXISTS floor VARCHAR(50) NULL;

SET @old_apartment_idx := (
  SELECT INDEX_NAME
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'apartments'
    AND NON_UNIQUE = 0
  GROUP BY INDEX_NAME
  HAVING GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) = 'building_id,apartment_number'
  LIMIT 1
);
SET @drop_old_idx_sql := IF(@old_apartment_idx IS NOT NULL,
  CONCAT('DROP INDEX ', @old_apartment_idx, ' ON apartments'),
  'SELECT 1');
PREPARE stmt_drop_old FROM @drop_old_idx_sql;
EXECUTE stmt_drop_old;
DEALLOCATE PREPARE stmt_drop_old;

SET @new_apartment_idx := (
  SELECT INDEX_NAME
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'apartments'
    AND INDEX_NAME = 'uk_apartments_building_entrance_number'
  LIMIT 1
);
SET @add_new_idx_sql := IF(@new_apartment_idx IS NULL,
  'ALTER TABLE apartments ADD UNIQUE INDEX uk_apartments_building_entrance_number (building_id, entrance, apartment_number)',
  'SELECT 1');
PREPARE stmt_add_new FROM @add_new_idx_sql;
EXECUTE stmt_add_new;
DEALLOCATE PREPARE stmt_add_new;
