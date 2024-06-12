package org.example;

import java.util.Map;

public class Plan {

    private DayOfWeek day;

    private Map<Category, Map<Integer, String>> planForDay;

    private Plan(PlanBuilder builder) {
        this.day = builder.day;
        this.planForDay = builder.planForDay;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public Map<Category, Map<Integer, String>> getPlanForDay() {
        return planForDay;
    }

    public void printPlan() {
        for (Map.Entry<Category,Map<Integer, String>> entry : planForDay.entrySet()) {
            String category = entry.getKey().name();
            for (Map.Entry<Integer, String> entry1 : entry.getValue().entrySet()) {
                System.out.println(category + ": " + entry1.getValue());
            }
        }
        System.out.println();
    }

    public static class PlanBuilder {

        private DayOfWeek day;

        private Map<Category, Map<Integer, String>> planForDay;

        public PlanBuilder mealId() {
            return this;
        }

        public PlanBuilder day(DayOfWeek day) {
            this.day = day;
            return this;
        }

        public PlanBuilder planForDay(Map<Category, Map<Integer, String>> planForDay) {
            this.planForDay = planForDay;
            return this;
        }

        public Plan build() {
            Plan plan = new Plan(this);
            return plan;
        }
    }
}
