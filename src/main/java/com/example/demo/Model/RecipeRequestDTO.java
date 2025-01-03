package com.example.demo.Model;

import java.math.BigDecimal;

public class RecipeRequestDTO {
    private String recipeName;
    private BigDecimal coffeeAmount;
    private BigDecimal waterAmount;
    private BigDecimal milkAmount;

    // Геттеры и сеттеры
    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public BigDecimal getCoffeeAmount() {
        return coffeeAmount;
    }

    public void setCoffeeAmount(BigDecimal coffeeAmount) {
        this.coffeeAmount = coffeeAmount;
    }

    public BigDecimal getWaterAmount() {
        return waterAmount;
    }

    public void setWaterAmount(BigDecimal waterAmount) {
        this.waterAmount = waterAmount;
    }

    public BigDecimal getMilkAmount() {
        return milkAmount;
    }

    public void setMilkAmount(BigDecimal milkAmount) {
        this.milkAmount = milkAmount;
    }
}


