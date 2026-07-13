package com.garmentstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class StockFixTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void fixStockQuantities() {
        System.out.println("Executing SQL to seed stock quantities...");
        jdbcTemplate.execute("UPDATE product_variants SET stock_quantity = FLOOR(RAND() * 15 + 1) WHERE active = TRUE");
        jdbcTemplate.execute("UPDATE product_variants SET stock_quantity = 0 WHERE sku_code = 'OX-BLU-S'");
        System.out.println("Stock quantities seeded successfully!");
    }
}
