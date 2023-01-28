package com.example.clova.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@Repository
public class GardenRepository {

    private JdbcTemplate jdbc;

    @Autowired
    public GardenRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public int insert(GardenItem gardenItem) {
        var sql = "insert into " +
                "watering_item (DATE_W) " +
                "values (?)";

        return jdbc.update(sql, gardenItem.getDate());
    }

    public List<GardenItem> select(java.util.Date utilDate) {
        var sql = "select * from watering_item " +
                "where DATE_W = ?";

        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        var items = jdbc.query(sql, new BeanPropertyRowMapper<>(GardenItem.class), sqlDate);
        return items;
    }

}
