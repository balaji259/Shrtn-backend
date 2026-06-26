package com.shrtn;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer {

    @Bean
    public CommandLineRunner initSchema(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                System.out.println("Running custom database schema alterations...");
                // Add password column if it does not exist
                jdbcTemplate.execute("ALTER TABLE url_mapping ADD COLUMN IF NOT EXISTS password VARCHAR(255)");
                // Add one_time column if it does not exist
                jdbcTemplate.execute("ALTER TABLE url_mapping ADD COLUMN IF NOT EXISTS one_time BOOLEAN DEFAULT FALSE");
                // Set any existing null one_time values to false
                jdbcTemplate.execute("UPDATE url_mapping SET one_time = FALSE WHERE one_time IS NULL");
                // Add index on url_mapping_id if not exists
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_click_event_url_mapping ON click_event (url_mapping_id)");
                // Add index on click_date if not exists
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_click_event_click_date ON click_event (click_date)");
                System.out.println("Database schema alterations completed successfully.");
            } catch (Exception e) {
                System.err.println("Schema alteration failed: " + e.getMessage());
            }
        };
    }
}
