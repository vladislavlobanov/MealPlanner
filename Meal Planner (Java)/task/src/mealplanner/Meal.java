package mealplanner;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static mealplanner.Main.scanner;

public class Meal {
    private String category;
    private String name;
    private String[] ingredients;

    private String readValues(String message) {
        System.out.println(message);
        return scanner.nextLine();
    }

    private boolean validateInput(String input) {
        return input.matches("[a-zA-Z ]+");
    }

    private boolean validateCategory(String category) {
        return category.equals("breakfast") || category.equals("lunch") || category.equals("dinner");
    }

    public void setCategory() {
        boolean trigger = false;
        boolean valid = true;
        String category;

        while (!trigger) {

            if (valid) {
                category = readValues("Which meal do you want to add (breakfast, lunch, dinner)?");
            } else {
                category = readValues("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }

            if (category.equals("exit")) {
                trigger = true;
            }

            if (validateCategory(category)) {
                this.category = category;
                trigger = true;
            } else {
                valid = false;
            }
        }
    }

    public void setName() {
        boolean trigger = false;
        boolean valid = true;
        String name;

        while (!trigger) {
            if (valid) {
                name = readValues("Input the meal's name:");
            } else {
                name = readValues("Wrong format. Use letters only!");
            }

            if (name.equals("exit")) {
                trigger = true;
            }

            if (validateInput(name)) {
                this.name = name;
                trigger = true;
            } else {
                valid = false;
            }
        }
    }

    public void setIngredients() {
        boolean trigger = false;
        boolean valid = true;
        String[] ingredients;

        while (!trigger) {
            if (valid) {
                ingredients = readValues("Input the ingredients:").split(",");
            }
            else {
                ingredients = readValues("Wrong format. Use letters only!").split(",");
            }

            for (int i = 0; i < ingredients.length; i++) {
                ingredients[i] = ingredients[i].trim();
            }

            if (ingredients.length == 1 && ingredients[0].equals("exit")) {
                trigger = true;
            }

            for (String ingredient : ingredients) {
                if (!validateInput(ingredient)) {
                    valid = false;
                    break;
                } else {
                    valid = true;
                }
            }

            if (valid) {
                this.ingredients = ingredients;
                trigger = true;
            }
        }
    }

    public boolean statusOk() {
        return this.category != null && name != null && ingredients != null;
    }

//    public void getMeal() {
//        System.out.println("\nCategory: " + this.category);
//        System.out.println("Name: " + this.name);
//        System.out.println("Ingredients: ");
//
//
//        for (int i = 0; i < ingredients.length; i++) {
//            if (i == ingredients.length - 1) {
//                System.out.println(ingredients[i]+"\n");
//            } else {
//                System.out.println(ingredients[i]);
//            }
//
//        }
//    }

    public String getCategory() {
        return this.category;
    }

    public String getName() {
        return this.name;
    }


    public String[] getIngredients() {
        return this.ingredients;
    }


    public Meal() {
        setCategory();

        if (category != null) {
            setName();
        }

        if (name != null) {
            setIngredients();
        }
    }

}