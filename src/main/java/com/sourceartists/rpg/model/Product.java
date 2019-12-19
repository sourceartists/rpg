package com.sourceartists.rpg.model;

public enum Product {

    ARMOR("Armor", Double.valueOf(10)),
    EMPTY("Armor", Double.valueOf(0));

    private String name;
    private Double price;

    Product(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }
}
