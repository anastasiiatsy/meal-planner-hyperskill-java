package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.sql.*;

public class GUI {

    private Scanner scanner = new Scanner(System.in);

    private Pattern categoryPattern = Pattern.compile("breakfast|lunch|dinner");

    private String selectCommand() {
        System.out.println("What would you like to do (add, show, plan, save, exit)?");
        return scanner.nextLine();
    }

    public void startPlanner() {
        try {
            Database database = new Database();
            while (true) {
                String command = selectCommand();
                if ("exit".equals(command)) {
                    System.out.println("Bye!");
                    break;
                } else if ("add".equals(command)) {
                    add(database);
                } else if ("show".equals(command)) {
                    show(database);
                } else if ("plan".equals(command)) {
                    plan(database);
                } else if ("save".equals(command)) {
                    save(database);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
    }

    private void add(Database database) {
        System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
        Meal newMeal = new Meal(Category.valueOf(getCategory()), getName(), getIngredients());
        database.addMealToDB(newMeal);
        database.addIngredientsToDB(newMeal);
        System.out.println("The meal has been added!");
    }

    private void show(Database database) {
        System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
        String category = getCategory();
        List<Meal> savedMeals = database.extractMealData();
        if (containsCategory(savedMeals, category)) {
            System.out.printf("Category: %s\n", category);
            showMeals(savedMeals, category);
        } else {
            System.out.println("No meals found.");
        }
    }

    private void plan(Database database) {
        List<Plan> plannedMeals = planMeals(database);
        saveAndPrintPlan(plannedMeals, database);
    }

    private void save(Database database) {
        if (database.hasPlan()) {
            System.out.println("Input a filename:");
            String file = scanner.nextLine();
            Map<String, Integer> list = createShoppingList(database);
            storeListInFile(list, file);
            System.out.println("Saved!");
        } else {
            System.out.println("Unable to save. Plan your meals first.");
        }
    }

    private boolean isAlpha(String text) {
        return text.matches("^[a-zA-Z\\s]+$");
    }

    private String getName() {
        System.out.println("Input the meal's name:");
        while (true) {
            String name = scanner.nextLine();
            if (isAlpha(name)) {
                return name;
            } else {
                System.out.println("Wrong format. Use letters only!");
            }
        }
    }

    private String getCategory() {
        while (true) {
            String category = scanner.nextLine().replaceAll(" ", "");
            Matcher categryMatcher = categoryPattern.matcher(category);
            if (categryMatcher.matches()) {
                return category;
            } else {
                System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }
        }
    }

    private List<String> getIngredients() {
        List<String> ingredients;
        System.out.println("Input the ingredients:");

        while (true) {
            String input = scanner.nextLine();
            ingredients = Arrays.stream(input.split(","))
                    .collect(Collectors.toList());

            for (int i = 0; i < ingredients.size(); i++) {
                String ingredient = ingredients.get(i).trim();
                if (isAlpha(ingredient)) {
                    ingredients.set(i, ingredient);
                } else {
                    System.out.println("Wrong format. Use letters only!");
                }
            }
            break;
        }
        return ingredients;
    }

    private String getMeal(Database database, String category) {
        String selectedMeal = "";
        while (true) {
            selectedMeal = scanner.nextLine();
            if (database.containsMeal(selectedMeal, category)) {
                break;
            } else {
                System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
            }
        }
        return selectedMeal;
    }

    private void showMeals(List<Meal> savedMeals, String category) {
        System.out.println("");
        if (savedMeals.isEmpty()) {
            System.out.println("No meals saved. Add a meal first.");
        } else {
            for (Meal meal : savedMeals) {
                if (meal.getCategory().name().equals(category)) {
                    System.out.println(meal.toString());
                }
            }
        }
    }

    private boolean containsCategory(List<Meal> savedMeals, String category) {
        for (Meal meal : savedMeals) {
            if (meal.getCategory().name().equals(category)) {
                return true;
            }
        }
        return false;
    }

    private void saveAndPrintPlan(List<Plan> plannedMeals, Database database) {
        for (Plan plan : plannedMeals) {
            database.addPlanToDB(plan);
            System.out.println(plan.getDay());
            plan.printPlan();

        }
    }

    private List<Plan> planMeals(Database database) {
        List<Plan> plannedMeals = new ArrayList<>();

        for (DayOfWeek day : DayOfWeek.values()) {
            System.out.println(day);
            Map<Category, Map<Integer, String>> planForDay = new LinkedHashMap<>();
            for (Category category : Category.values()) {
                database.printMeals(category.name());
                System.out.printf("Choose the %s for %s from the list above:\n", category.name(), day.name());
                String selectedMeal = getMeal(database, category.name());
                Map<Integer, String> meals = new LinkedHashMap<>();
                int mealId = database.getMealId(selectedMeal);
                meals.put(mealId, selectedMeal);
                planForDay.put(category, meals);
            }
            Plan plan = new Plan.PlanBuilder()
                    .day(day)
                    .planForDay(planForDay)
                    .build();
            plannedMeals.add(plan);

            System.out.println("Yeah! We planned the meals for " + day.name() + ".\n");
        }
        return plannedMeals;
    }

    private Map<String, Integer> createShoppingList(Database database) {
        Map<String, Integer> list = new HashMap<>();
        List<String> ingredients;
        List<Integer> plannedMealIds = database.extractPlannedMealIDs();

        for (int i = 0; i < plannedMealIds.size(); i++) {
            int mealId = plannedMealIds.get(i);
            ingredients = database.extractIngredients(mealId);

            for (int j = 0; j < ingredients.size(); j++) {
                String ingredient = ingredients.get(j);
                if (list.containsKey(ingredient)) {
                    list.replace(ingredient, list.get(ingredient),list.get(ingredient) + 1);
                } else {
                    list.put(ingredient, 1);
                }
            }
        }
        return list;
    }

    private void storeListInFile(Map<String, Integer> list, String file) {
        try(FileWriter writer = new FileWriter(file)) {
            for (Map.Entry<String, Integer> entry : list.entrySet()) {
                writer.write(entry.getKey());
                if (entry.getValue() > 1) {
                    writer.write(" x" + entry.getValue() + "\n");
                } else {
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
