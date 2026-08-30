package com.garmentstore.catalog.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        Integer totalStock = jdbcTemplate.queryForObject("SELECT SUM(stock_quantity) FROM product_variants", Integer.class);
        if (totalStock == null || totalStock == 0) {
            log.info("Seeding random stock quantities for products...");
            jdbcTemplate.execute("UPDATE product_variants SET stock_quantity = FLOOR(RAND() * 15 + 1) WHERE status = 'ACTIVE'");
            jdbcTemplate.execute("UPDATE product_variants SET stock_quantity = 0 WHERE sku = 'OX-BLU-S'");
            log.info("Stock quantities seeded successfully!");
        }
    }
}
