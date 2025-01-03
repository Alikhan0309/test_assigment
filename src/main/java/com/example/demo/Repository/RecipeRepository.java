package com.example.demo.Repository;

import com.example.demo.Model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    @Query("SELECT r.name, i.name, ri.amount FROM Recipe r " +
            "JOIN r.recipeIngredients ri " +
            "JOIN ri.ingredient i")
    List<Object[]> findIngredientsForAllRecipes();


}
