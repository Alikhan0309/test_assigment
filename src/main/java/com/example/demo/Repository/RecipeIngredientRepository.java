package com.example.demo.Repository;
import com.example.demo.Model.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient,Long> { }
