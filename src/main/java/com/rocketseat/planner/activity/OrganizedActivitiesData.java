package com.rocketseat.planner.activity;

import java.time.LocalDate;
import java.util.List;

public record OrganizedActivitiesData(
        LocalDate date,
        List<ActivityData> activities
) {
}
