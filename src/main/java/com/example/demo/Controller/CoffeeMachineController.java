package com.example.demo.Controller;

import com.example.demo.Model.*;
import com.example.demo.Repository.*;
import com.example.demo.Service.HolidayService;
import com.example.demo.Model.Ingredient;
import com.example.demo.Model.Recipe;
import com.example.demo.Model.RecipeIngredient;
import com.example.demo.Model.Sale;
import com.example.demo.Repository.IngredientRepository;
import com.example.demo.Repository.RecipeRepository;
import com.example.demo.Repository.SaleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/coffee-machine")
public class CoffeeMachineController {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private SaleRepository saleRepository;

    private RecipeRequestDTO recipeRequestDTO;

    @Autowired
    private HolidayService holidayService;

    @GetMapping("/recipes")
    @Transactional
    public List<Object[]> getAllRecipes() {
        return recipeRepository.findIngredientsForAllRecipes();
    }

    @PostMapping("/add-recipe")
    @Transactional
    public ResponseEntity<String> addRecipe(@RequestBody RecipeRequestDTO recipeRequestDTO) {
        // Создание нового рецепта
        Recipe recipe = new Recipe();
        recipe.setName(recipeRequestDTO.getRecipeName());

        // Получаем ингредиенты (кофе, вода, молоко) из базы данных или создаем новые, если они не существуют
        Ingredient coffee = ingredientRepository.findByName("Кофе").orElse(new Ingredient("Кофе"));
        Ingredient water = ingredientRepository.findByName("Вода").orElse(new Ingredient("Вода"));
        Ingredient milk = ingredientRepository.findByName("Молоко").orElse(new Ingredient("Молоко"));

        // Создание связей между рецептом и ингредиентами с их пропорциями
        RecipeIngredient recipeIngredientCoffee = new RecipeIngredient();
        recipeIngredientCoffee.setRecipe(recipe);
        recipeIngredientCoffee.setIngredient(coffee);
        recipeIngredientCoffee.setAmount(recipeRequestDTO.getCoffeeAmount());

        RecipeIngredient recipeIngredientWater = new RecipeIngredient();
        recipeIngredientWater.setRecipe(recipe);
        recipeIngredientWater.setIngredient(water);
        recipeIngredientWater.setAmount(recipeRequestDTO.getWaterAmount());

        RecipeIngredient recipeIngredientMilk = new RecipeIngredient();
        recipeIngredientMilk.setRecipe(recipe);
        recipeIngredientMilk.setIngredient(milk);
        recipeIngredientMilk.setAmount(recipeRequestDTO.getMilkAmount());

        // Добавление этих связей в рецепт
        List<RecipeIngredient> recipeIngredients = new ArrayList<>();
        recipeIngredients.add(recipeIngredientCoffee);
        recipeIngredients.add(recipeIngredientWater);
        recipeIngredients.add(recipeIngredientMilk);
        recipe.setRecipeIngredients(recipeIngredients);

        // Сохранение рецепта и ингредиентов
        recipeRepository.save(recipe);
        ingredientRepository.save(coffee);
        ingredientRepository.save(water);
        ingredientRepository.save(milk);

        return ResponseEntity.ok("Recipe added successfully");
    }


    @GetMapping("/sales")
    @Transactional
    public String getMostPopularRecipe() {
        return saleRepository.findMostSoldRecipe();
    }

    @PutMapping("/replenish-ingredient/{ingredientId}")
    @Transactional
    public ResponseEntity<String> replenishIngredient(@PathVariable Long ingredientId, @RequestParam BigDecimal amount) {
        // Получаем ингредиент по его ID
        Ingredient ingredient = ingredientRepository.findById(ingredientId).orElse(null);
        if (ingredient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ingredient not found.");
        }

        // Пополняем количество ингредиента
        ingredient.setQuantity(ingredient.getQuantity().add(amount)); // Добавляем количество
        ingredientRepository.save(ingredient); // Сохраняем обновленный ингредиент

        return ResponseEntity.ok("Ingredient replenished successfully.");
    }


    @PostMapping("/make-coffee/{recipeId}")
    @Transactional
    public ResponseEntity<String> makeCoffee(@PathVariable Long recipeId) {
        if (isWorkingTime() && isWeekday() && !isHoliday()) {
            // Получаем рецепт
            Recipe recipe = recipeRepository.findById(recipeId).orElse(null);
            if (recipe == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found.");
            }
            // Проверяем, достаточно ли ингредиентов
            for (RecipeIngredient recipeIngredient : recipe.getRecipeIngredients()) {
                Ingredient ingredient = recipeIngredient.getIngredient();
                BigDecimal requiredAmount = recipeIngredient.getAmount();
                if (ingredient.getQuantity().compareTo(requiredAmount) < 0) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not enough " + ingredient.getName() + " to make the coffee.");
                }
            }

            // Если ингредиентов достаточно, обновляем их количество и создаем продажу
            for (RecipeIngredient recipeIngredient : recipe.getRecipeIngredients()) {
                Ingredient ingredient = recipeIngredient.getIngredient();
                BigDecimal requiredAmount = recipeIngredient.getAmount();
                ingredient.setQuantity(ingredient.getQuantity().subtract(requiredAmount)); // Уменьшаем количество
                ingredientRepository.save(ingredient); // Сохраняем обновленный ингредиент
            }

            // Создаем запись о продаже
            Sale sale = new Sale();
            sale.setRecipe(recipe);
            sale.setTimestamp(LocalDateTime.now());
            saleRepository.save(sale);

            return ResponseEntity.ok("Coffee is ready!");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Machine is not working at this time.");
        }
    }

    @GetMapping("/check-api-status")
    public ResponseEntity<String> checkApiStatus() {
        return holidayService.checkApiStatus();
    }
    private boolean isWorkingTime() {
        LocalTime currentTime = LocalTime.now();
        return true;
        //return currentTime.isAfter(LocalTime.of(8, 0)) && currentTime.isBefore(LocalTime.of(17, 0));
    }
    private boolean isWeekday() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        return  true;
        //return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
    private boolean isHoliday() {
        LocalDate today = LocalDate.now();
        System.out.println(holidayService.isHoliday(today));
        return holidayService.isHoliday(today);
    }

    public RecipeRequestDTO getRecipeRequestDTO() {
        return recipeRequestDTO;
    }

    public void setRecipeRequestDTO(RecipeRequestDTO recipeRequestDTO) {
        this.recipeRequestDTO = recipeRequestDTO;
    }
}