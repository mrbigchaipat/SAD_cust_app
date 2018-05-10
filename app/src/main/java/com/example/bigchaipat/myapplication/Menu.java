package com.example.bigchaipat.myapplication;

/**
 * Created by bigchaipat on 1/4/2018 AD.
 */

public class Menu {
    private String name;
    private double price;

    public Menu(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
