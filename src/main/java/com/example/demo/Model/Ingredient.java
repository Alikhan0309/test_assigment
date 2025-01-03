package com.example.demo.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name = "ingredients")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private BigDecimal quantity;

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal subtract) {
        this.quantity = subtract;
    }

    public String getName() {
        return name;
    }
    public Ingredient() {
    }

    // Конструктор с параметром для name
    public Ingredient(String name) {
        this.name = name;
    }
}
