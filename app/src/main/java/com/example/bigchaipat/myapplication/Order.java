package com.example.bigchaipat.myapplication;

import java.util.List;

/**
 * Created by bigchaipat on 1/4/2018 AD.
 */

public class Order {
    private int id;
    private double totalAmount;
    private List<Menu> menus;

    public Order(int id, double totalAmount, List<Menu> menus) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.menus = menus;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    public void calculateTotalAmount() {
        for (int i = 0; i < menus.size(); i++) {
            totalAmount = totalAmount + menus.get(i).getPrice();
        }
    }
}
