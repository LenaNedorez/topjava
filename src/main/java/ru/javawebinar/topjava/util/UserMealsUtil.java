package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.*;
import static java.util.stream.Collectors.*;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> mealDateExcess = new HashMap<>();
        List<UserMeal> userMeals = new ArrayList<>();
        //Фильтрация по часам и группировка по датам
        meals.forEach(m -> {
            if(TimeUtil.isBetweenHalfOpen(m.getTime(),startTime,endTime)){
                userMeals.add(m);
            }
            //Группировка калорий по датам
            mealDateExcess.put(m.getDate(),mealDateExcess.getOrDefault(m.getDate(),0) + m.getCalories());
        });

        //Создание результирующего списка
        ArrayList<UserMealWithExcess> userMealWithExcesses = new ArrayList<>();
        userMeals.forEach(m -> userMealWithExcesses.add(
                addExcess(m,mealDateExcess.get(m.getDate()) < caloriesPerDay )
        ));

        return userMealWithExcesses;
    }


    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate,Integer> sumMealByDate = meals.stream().collect(
                groupingBy(UserMeal::getDate,summingInt(UserMeal::getCalories))
        );

        return meals.stream()
                .filter(m -> TimeUtil.isBetweenHalfOpen(m.getTime(),startTime,endTime))
                .map(m -> addExcess(m,sumMealByDate.get(m.getDate()) < caloriesPerDay))
                .collect(toList());
    }

    public static UserMealWithExcess addExcess(UserMeal meal, boolean excess ){
        return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess);
    }
}
