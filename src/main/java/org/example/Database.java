package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Database {

    private Connection connection;

    private static final String DB_URL = "jdbc:postgresql:meals_db";

    private static final String USER = "postgres";

    private static  final String PASS = "1111";

    private static final String CREATE_MEALS_TABLE = "CREATE TABLE IF NOT EXISTS meals (" +
            "category VARCHAR(1000)," +
            "meal VARCHAR(1000)," +
            "meal_id INTEGER" +
            ")";

    private static final String CREATE_INGREDIENTS_TABLE = "CREATE TABLE IF NOT EXISTS ingredients (" +
            "ingredient VARCHAR(1000)," +
            "ingredient_id INTEGER," +
            "meal_id INTEGER" +
            ")";

    private static final String CREATE_PLAN_TABLE = "CREATE TABLE IF NOT EXISTS plan (" +
            "category VARCHAR(1000)," +
            "meal VARCHAR(1000)," +
            "meal_id INTEGER" +
            ")";

    private static String SELECT = "SELECT * FROM %s";

    private String SELECT_INGREDIENT = "SELECT ingredient, meal_id FROM ingredients";

    private static String INSERT_DATA = "INSERT INTO %s VALUES(?,?,?)";

    private static String SELECT_MEAL = "SELECT * FROM meals WHERE category = '%s' ORDER BY meal ASC";

    private static String SELECT_MEALID = "SELECT meal_id FROM meals WHERE meal = '%s'";

    public Database() throws SQLException {
        this.connection = getConnection();
        createTables();
    }

    public Connection getConnection() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, USER,PASS);
        connection.setAutoCommit(true);
        return connection;
    }

    public void run(String str) {
        try (var ps = connection.prepareStatement(str)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
    }

    private void createTables() {
        run(CREATE_MEALS_TABLE);
        run(CREATE_INGREDIENTS_TABLE);
        run(CREATE_PLAN_TABLE);
    }

    public List<Meal> extractMealData() {
        List<Meal> savedMeals = new ArrayList<>();

        try(var ps = connection.prepareStatement(String.format(SELECT, "meals"))) {
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                String category = rs.getString("category") ;
                String name = rs.getString("meal");
                int mealID = rs.getInt("meal_id");
                List<String> ingredients = extractIngredients(mealID);
                Meal meal = new Meal(Category.valueOf(category), name, ingredients);
                savedMeals.add(meal);
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
        return savedMeals;
    }

    public List<String> extractIngredients(int mealID) {
        List<String> ingredients = new ArrayList<>();

        try (var ps = connection.prepareStatement(SELECT_INGREDIENT)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("meal_id");
                if (mealID == id) {
                    String ingredient = rs.getString("ingredient");
                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
        return ingredients;
    }

    public List<Integer> extractPlannedMealIDs() {
        List<Integer> allMealIds = new ArrayList<>();

        try (var ps = connection.prepareStatement(String.format(SELECT, "plan"))) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int mealId = rs.getInt("meal_id");
                allMealIds.add(mealId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return allMealIds;
    }

    public void addMealToDB(Meal meal) {
        String name = meal.getName();
        String category = meal.getCategory().name();
        int id = meal.getMealID();

        try(PreparedStatement ps = connection.prepareStatement(String.format(INSERT_DATA, "meals"))) {
            ps.setString(1, category);
            ps.setString(2, name);
            ps.setInt(3, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
    }

    public void addIngredientsToDB(Meal meal) {
        List<String> ingredients = meal.getIngredients();
        int mealId = meal.getMealID();

        try(PreparedStatement ps = connection.prepareStatement(String.format(INSERT_DATA, "ingredients"))) {
            for (int i = 0; i < ingredients.size(); i++) {
                String ingredient = ingredients.get(i);
                int ingredientID = meal.getIngredientID();
                ps.setString(1, ingredient);
                ps.setInt(2, ingredientID);
                ps.setInt(3, mealId);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(e.getErrorCode());;
        }
    }

    public void addPlanToDB(Plan plan) {

        for (Map.Entry<Category, Map<Integer, String>> entry : plan.getPlanForDay().entrySet()) {
            String category = entry.getKey().name();
            for (Map.Entry<Integer, String> entry1 : entry.getValue().entrySet()) {

                String meal = entry1.getValue();
                int mealId = entry1.getKey();

                try (var ps = connection.prepareStatement(String.format(INSERT_DATA, "plan"))) {
                    ps.setString(1, category);
                    ps.setString(2, meal);
                    ps.setInt(3, mealId);
                    ps.executeUpdate();

                } catch (SQLException e) {
                    System.out.println(e.getErrorCode());
                }
            }
        }
    }

    public int getMealId(String meal) {
        int mealId = 0;
        try (var ps = connection.prepareStatement(String.format(SELECT_MEALID, meal))) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mealId = rs.getInt("meal_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return mealId;
    }

    public void printMeals(String category) {
        try (var ps = connection.prepareStatement(String.format(SELECT_MEAL, category))) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String mealName = rs.getString("meal");
                int mealId = rs.getInt("meal_id");
                System.out.println(mealName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean containsMeal(String meal, String category) {
        try (var ps = connection.prepareStatement(String.format(SELECT_MEAL, category))) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String mealName = rs.getString("meal");
                if (meal.equals(mealName)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean hasPlan() {
        try (var ps = connection.prepareStatement(String.format(SELECT, "plan"))) {
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            }

        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
        return false;
    }
}
