package com.rocketseat.planner.trip;

import com.rocketseat.planner.activity.*;
import com.rocketseat.planner.link.LinkData;
import com.rocketseat.planner.link.LinkRequestPayload;
import com.rocketseat.planner.link.LinkResponse;
import com.rocketseat.planner.link.LinkService;
import com.rocketseat.planner.participant.ParticipantCreateResponse;
import com.rocketseat.planner.participant.ParticipantData;
import com.rocketseat.planner.participant.ParticipantRequestPayload;
import com.rocketseat.planner.participant.ParticipantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final ParticipantService participantService;

    private final ActivityService activityService;

    private final LinkService linkService;

    private final TripRepository repository; // Não interagir com repository só com service

    public TripController(
            ParticipantService participantService,
            ActivityService activityService,
            LinkService linkService,
            TripRepository repository
    ) {
        this.participantService = participantService;
        this.activityService = activityService;
        this.linkService = linkService;
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {
        Trip newTrip = new Trip(payload);

        this.repository.save(newTrip);

        this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        Optional<Trip> trip = this.repository.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
        Optional<Trip> trip = this.repository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());
            this.repository.save(rawTrip);
            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id) {
        Optional<Trip> trip = this.repository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);

            this.repository.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload) {
        Optional<Trip> trip = this.repository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ParticipantCreateResponse participantCreateResponse = this.participantService.registerParticipantToEvent(payload.email(), rawTrip);

            if (rawTrip.getIsConfirmed()) {
                this.participantService.triggerConfirmationEmailToParticipant(payload.email(), rawTrip);
            }

            return ResponseEntity.ok(participantCreateResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payload) {
        Optional<Trip> trip = this.repository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ActivityResponse activityResponse = this.activityService.registerActivity(payload, rawTrip);

            return ResponseEntity.ok(activityResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id) {
        List<ActivityData> activities = this.activityService.getAllActivitiesFromEvent(id);

        return ResponseEntity.ok(activities);
    }

    @GetMapping("/{id}/activities/organized")
    public ResponseEntity<List<OrganizedActivitiesData>> getAllActivitiesOrganized(@PathVariable UUID id) {
        List<OrganizedActivitiesData> organizedActivities = this.activityService.getAllActivitiesFromEventOrganized(id);

        return ResponseEntity.ok(organizedActivities);
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id) {
        List<ParticipantData> participants = this.participantService.getAllParticipantsFromEvent(id);

        return ResponseEntity.ok(participants);
    }


    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequestPayload payload) {
        Optional<Trip> trip = this.repository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            LinkResponse linkResponse = this.linkService.registerLink(payload, rawTrip);

            return ResponseEntity.ok(linkResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/links")
    public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id) {
        List<LinkData> links = this.linkService.getAllLinksFromEvent(id);

        return ResponseEntity.ok(links);
    }
}
