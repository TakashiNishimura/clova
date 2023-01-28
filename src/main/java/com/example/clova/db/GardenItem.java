package com.example.clova.db;

import lombok.Data;

@Data
public class GardenItem {

    private java.sql.Date date;

    public GardenItem() {
        java.util.Date now = new java.util.Date();
        date = new java.sql.Date(now.getTime());
    }

}
