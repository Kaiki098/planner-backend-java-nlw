package com.rocketseat.planner.activity;

import com.rocketseat.planner.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {
    @Autowired
    private ActivityRepository repository;

    public ActivityResponse registerActivity(ActivityRequestPayload payload, Trip trip) {
        Activity newActivity = new Activity(payload.title(), payload.occurs_at(), trip);

        this.repository.save(newActivity);

        return new ActivityResponse(newActivity.getId());
    }

    public List<ActivityData> getAllActivitiesFromEvent(UUID tripId) {
        return this.repository
                .findByTripId(tripId)
                .stream()
                .map(activity ->
                        new ActivityData(
                                activity.getId(),
                                activity.getTitle(),
                                activity.getOccursAt()
                        )
                ).toList();
    }

    public List<OrganizedActivitiesData> getAllActivitiesFromEventOrganized(UUID tripId) {
        List<ActivityData> activities = getAllActivitiesFromEvent(tripId);

        List<LocalDate> dates = new ArrayList<>();

        activities.forEach((activityData) -> {
            LocalDate date = activityData.occurs_at().toLocalDate();
            if (!dates.contains(date)) {
                dates.add(date);
            }
        });

        return dates.stream().map(date ->
                new OrganizedActivitiesData(
                        date,
                        activities.stream().filter(activity -> activity.occurs_at().toLocalDate() == date).toList()
                )
        ).toList();
    }
}
