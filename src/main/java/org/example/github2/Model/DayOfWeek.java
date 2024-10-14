package org.example.github2.Model;

public enum DayOfWeek {
    MONDAY(0),
    TUESDAY(1),
    WEDNESDAY(2),
    THURSDAY(3),
    FRIDAY(4),
    SATURDAY(5),
    SUNDAY(6);

    public final int dayCode;

    DayOfWeek(int numberDayOfWeek) {
        dayCode=numberDayOfWeek;
    }

    public static DayOfWeek fromDayCode(int dayOfWeekCode) {
        return switch (dayOfWeekCode) {
            case 0 -> MONDAY;
            case 1 -> TUESDAY;
            case 2 -> WEDNESDAY;
            case 3 -> THURSDAY;
            case 4 -> FRIDAY;
            case 5 -> SATURDAY;
            case 6 -> SUNDAY;
            default -> null;
        };
    }
}