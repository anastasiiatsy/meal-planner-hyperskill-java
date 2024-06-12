package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Meal {

    private int mealID;

    private int ingredientID;

    private Category category;

    private String name;

    private List<String> ingredients;

    private Random random;

    public Meal(Category category, String name, List<String> ingredients) {
        this.random = new Random();
        this.category = category;
        this.name = name;
        this.ingredients = ingredients;
        this.mealID = random.nextInt(900) + 100;

    }

    public int getIngredientID() {
        return random.nextInt(3000);
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public int getMealID() {
        return mealID;
    }

    public List<String> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    @Override
    public String toString() {
        return String.format("""                           
                Name: %s
                Ingredients:
                %s
                """,this.name, String.join("\n", ingredients));
    }
}
