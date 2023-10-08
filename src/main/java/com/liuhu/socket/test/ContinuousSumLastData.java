package com.liuhu.socket.test;

import java.util.*;

public class ContinuousSumLastData {
    public static List<List<Integer>> findCombinations(List<Integer> numbers) {
        List<List<Integer>> combinations = new ArrayList<>();
        int n = numbers.size();
        int runningSum = 0;

        for (int i = n - 1; i >= 0; i--) {
            runningSum += numbers.get(i);
            if (runningSum > 5) {
                List<Integer> combination = new ArrayList<>();
                for (int j = i; j < n; j++) {
                    combination.add(numbers.get(j));
                }
                combinations.add(combination);
            }
        }

        return combinations;
    }

    public static void main(String[] args) {
        // Sample input: a list of integers
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(1);
        numbers.add(4);

        List<List<Integer>> result = findCombinations(numbers);

        // Print the result
        for (List<Integer> combination : result) {
            System.out.println(combination);
        }
    }
}
