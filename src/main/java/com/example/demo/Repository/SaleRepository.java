package com.example.demo.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.Model.Sale;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale,Long>{
    @Query(value = "SELECT r1_0.name FROM sales s1_0 JOIN recipes r1_0 ON r1_0.id = s1_0.recipe_id GROUP BY r1_0.id, r1_0.name ORDER BY COUNT(s1_0.id) DESC LIMIT 1", nativeQuery = true)
    String findMostSoldRecipe();



}