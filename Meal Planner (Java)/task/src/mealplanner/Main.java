package mealplanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.sql.*;

public class Main {
    public static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws SQLException {

        boolean trigger = false;

        List<Map<String, Object>> cookBook = new ArrayList<>();
        List<Map<String, Object>> plan = new ArrayList<>();
        List<Map<String, Object>> filteredCookBook;
        List<Map<String, Object>> sortedMeals;

        List<String> ingredients;
        Map<String, Object> recipe, planItem, beforeSorting;


        //DB settings
        String DB_URL = "jdbc:postgresql:meals_db";
        String USER = "postgres";
        String PASS = "1111";

        Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
        connection.setAutoCommit(true);

        Statement statement = connection.createStatement();

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS meals (" +
                "meal_id INTEGER," +
                "category VARCHAR," +
                "meal VARCHAR" +
                ")");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS ingredients (" +
                "ingredient_id INTEGER," +
                "meal_id INTEGER," +
                "ingredient VARCHAR" +
                ")");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS plan (" +
                "meal_option VARCHAR," +
                "meal_id INTEGER," +
                "meal_category VARCHAR" +
                ")");

        ResultSet dbMeals = statement.executeQuery(
                "SELECT " +
                        "    m.meal_id, " +
                        "    m.category, " +
                        "    m.meal, " +
                        "    ARRAY_AGG(i.ingredient) AS ingredients " +
                        "FROM meals m " +
                        "JOIN ingredients i ON m.meal_id = i.meal_id " +
                        "GROUP BY m.meal_id, m.category, m.meal " +
                        "ORDER BY m.meal_id;"
        );


        // Read the result set
        while (dbMeals.next()) {
            recipe = new HashMap<>();

            recipe.put("category", dbMeals.getString("category"));
            recipe.put("name", dbMeals.getString("meal"));
            recipe.put("ingredients", (String[]) dbMeals.getArray("ingredients").getArray());

            cookBook.add(recipe);
        }

        ResultSet dbPlan = statement.executeQuery(
                "SELECT " +
                        "    p.meal_option, " +           // Weekday from the 'plan' table
                        "    m.meal_id, " +               // Meal ID from the 'meals' table
                        "    m.category, " +              // Category from the 'meals' table
                        "    m.meal " +                   // Meal name from the 'meals' table
                        "FROM plan p " +
                        "JOIN meals m ON p.meal_id = m.meal_id " + // Join on meal_id
                        "ORDER BY p.meal_option, m.category, m.meal;"  // Order by weekday, category, and meal
        );

        while (dbPlan.next()) {
            planItem = new HashMap<>();

            planItem.put("weekDay", dbPlan.getString("meal_option"));
            planItem.put("name", dbPlan.getString("meal"));
            planItem.put("category", dbPlan.getString("category"));
            planItem.put("id", dbPlan.getString("meal_id"));

            plan.add(planItem);
        }


        while (!trigger) {
            System.out.println("What would you like to do (add, show, plan, list plan, save, exit)?");
            String input = scanner.nextLine();

            if (input.equals("save")) {
                if (plan.isEmpty()) {
                    System.out.println("Unable to save. Plan your meals first.");
                }
                else {
                    ShoppingList list = new ShoppingList();


                    for (Map<String, Object> singlePlanOption : plan) {
                        int index = Integer.parseInt((String) singlePlanOption.get("id"));

                        for (int i = 0; i < cookBook.size(); i++) {
                            if (i == index - 1) {
                                String[] single = (String[]) cookBook.get(i).get("ingredients");
                                list.setIngredients(single);
                            }
                        }
                    }


                    System.out.println("Input a filename:");
                    input = scanner.nextLine();
                    list.printSummarizedIngredients(input);
                }
            }

            if (input.equals("add")) {
                Meal meal = new Meal();

                if (!meal.statusOk()) {
                    System.out.print("Bye!");
                    trigger = true;
                }
                else {
                    recipe = new HashMap<>();


                    recipe.put("category", meal.getCategory());
                    recipe.put("name", meal.getName());
                    recipe.put("ingredients", meal.getIngredients());


                    cookBook.add(recipe);

                    statement.executeUpdate(String.format(
                            "INSERT INTO meals (meal_id, category, meal) VALUES ('%s', '%s', '%s')",
                            cookBook.size(),
                            meal.getCategory(),
                            meal.getName()
                    ));


                    for (String ingredient : meal.getIngredients()) {
                        statement.executeUpdate(String.format(
                                "INSERT INTO ingredients (meal_id, ingredient) VALUES ('%s', '%s')",
                                cookBook.size(),
                                ingredient
                        ));
                    }

                    System.out.println("The meal has been added!");
                }
            }


            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            String[] mealType = {"breakfast", "lunch", "dinner"};


            if (input.equals("list plan")) {
                showPlan(days, mealType, plan);
            }

            if (input.equals("plan")) {
                int dayWeekCounter = 0;
                int mealTypeCounter = 0;
                boolean validMeal = true;

                statement.executeUpdate("DELETE from plan");
                plan = new ArrayList<>();

                while (dayWeekCounter < 7) {
                    System.out.println(days[dayWeekCounter]);

                    while (mealTypeCounter < 3) {
                        sortedMeals = new ArrayList<>();

                        for (int i = 0; i < cookBook.size(); i++) {
                            Map<String, Object> meal = cookBook.get(i);

                            if (mealType[mealTypeCounter].equals(meal.get("category"))) {
                                beforeSorting = new HashMap<>();
                                beforeSorting.put("id", i + 1);
                                beforeSorting.put("name", meal.get("name"));
                                sortedMeals.add(beforeSorting);
                            }
                        }

                        sortedMeals.sort(Comparator.comparing(meal -> meal.get("name").toString()));

                        for (Map<String, Object> meal : sortedMeals) {
                            System.out.println(meal.get("name"));
                        }

                        while (true) {
                            if (validMeal) {
                                System.out.println("Choose the " + mealType[mealTypeCounter] + " for " + days[dayWeekCounter] + " from the list above:");
                            }
                            else {
                                System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
                            }

                            input = scanner.nextLine();


                            validMeal = false;

                            int index = -1;
                            int id = -1;

                            for (int i = 0; i < sortedMeals.size(); i++) {
                                Map<String, Object> meal = sortedMeals.get(i);
                                if (meal.get("name").equals(input)) {
                                    validMeal = true;
                                    index = i;
                                    id = (int) meal.get("id");
                                    break;
                                }
                            }

                            if (validMeal) {
                                statement.executeUpdate(String.format(
                                        "INSERT INTO plan (meal_option, meal_category, meal_id) VALUES ('%s', '%s', '%s')",
                                        days[dayWeekCounter],
                                        mealType[mealTypeCounter],
                                        id
                                ));
                                planItem = new HashMap<>();
                                planItem.put("weekDay", days[dayWeekCounter]);
                                planItem.put("name", sortedMeals.get(index).get("name"));
                                planItem.put("category", mealType[mealTypeCounter]);

                                plan.add(planItem);
                                break;
                            }
                        }

                        mealTypeCounter++;
                    }

                    System.out.println("Yeah! We planned the meals for " + days[dayWeekCounter] + ".\n");

                    dayWeekCounter++;
                    mealTypeCounter = 0;
                }
                showPlan(days, mealType, plan);
            }

            if (input.equals("show")) {
                boolean showTrigger = false;
                boolean valid = true;


                while (!showTrigger) {

                    if (valid) {
                        System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
                    }
                    else {
                        System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
                    }
                    input = scanner.nextLine();

                    if (input.equals("breakfast") || input.equals("lunch") || input.equals("dinner")) {
                        PreparedStatement preparedStatement = connection.prepareStatement(
                                "SELECT m.category, m.meal, ARRAY_AGG(i.ingredient) AS ingredients " +
                                        "FROM meals m " +
                                        "JOIN ingredients i ON m.meal_id = i.meal_id " +
                                        "WHERE m.category = ? " +
                                        "GROUP BY m.category, m.meal"
                        );
                        preparedStatement.setString(1, input);

                        ResultSet dbReqWithCategory = preparedStatement.executeQuery();

                        filteredCookBook = new ArrayList<>();

                        while (dbReqWithCategory.next()) {
                            recipe = new HashMap<>();

                            recipe.put("category", dbReqWithCategory.getString("category"));
                            recipe.put("name", dbReqWithCategory.getString("meal"));
                            recipe.put("ingredients", (String[]) dbReqWithCategory.getArray("ingredients").getArray());

                            filteredCookBook.add(recipe);
                        }

                        if (!filteredCookBook.isEmpty()) {
                            System.out.println("Category: " + input + "\n");

                            for (Map<String, Object> meal : filteredCookBook) {
                                System.out.println("Name: " + meal.get("name"));
                                System.out.println("Ingredients:");

                                ingredients = new ArrayList<>(Arrays.asList((String[]) meal.get("ingredients")));

                                for (int i = 0; i < ingredients.size(); i++) {
                                    if (i == ingredients.size() - 1) {
                                        System.out.println(ingredients.get(i) + "\n");
                                    }
                                    else {
                                        System.out.println(ingredients.get(i));
                                    }
                                }
                            }
                        }
                        else {
                            System.out.println("No meals found.");
                        }

                        showTrigger = true;
                    }
                    else {
                        valid = false;
                    }
                }
            }

            if (input.equals("exit")) {
                System.out.print("Bye!");
                trigger = true;
            }
        }

        scanner.close();
        connection.close();
    }

    private static void showPlan(String[] days, String[] mealType, List<Map<String, Object>> plan) {
        for (String day : days) {
            System.out.println(day);
            for (String meal : mealType) {

                for (Map<String, Object> singlePlanOption : plan) {
                    if (day.equals(singlePlanOption.get("weekDay").toString()) && meal.equals(singlePlanOption.get(
                            "category").toString())) {
                        System.out.println(meal + ": " + singlePlanOption.get("name"));
                    }
                }
            }
            System.out.println();
        }
    }
}