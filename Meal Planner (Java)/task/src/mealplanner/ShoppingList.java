package mealplanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingList {
    private List<String> ingredients = new ArrayList<>();

    public void setIngredients(String[] newIngredients) {

        ingredients.addAll(Arrays.asList(newIngredients));
    }

    public void printIngredients() {
        System.out.println(ingredients);
    }

    public void printSummarizedIngredients(String filename) {
        Map<String, Integer> countMap = new HashMap<>();

        // Count occurrences
        for (String ingredient : ingredients) {
            countMap.put(ingredient, countMap.getOrDefault(ingredient, 0) + 1);
        }


        try {
            FileWriter myWriter = new FileWriter(filename); // overwrites existing file

            List<Map.Entry<String, Integer>> entries = new ArrayList<>(countMap.entrySet());

            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<String, Integer> entry = entries.get(i);
                String name = entry.getKey();
                int count = entry.getValue();

                if (count > 1) {
                    myWriter.write(name + " x" + count);
                }
                else {
                    myWriter.write(name);
                }

                if (i < entries.size() - 1) {
                    myWriter.write("\n");
                }
            }

            myWriter.close();
            System.out.println("Saved!");
        }
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
