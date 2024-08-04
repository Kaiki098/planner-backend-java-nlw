package com.rocketseat.planner.link;

import com.rocketseat.planner.trip.Trip;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LinkService {
    private final LinkRepository repository;

    public LinkService(LinkRepository linkRepository) {
        this.repository = linkRepository;
    }

    public LinkResponse registerLink(LinkRequestPayload payload, Trip trip) {
        Link newLink = new Link(payload.title(), payload.url(), trip);

        this.repository.save(newLink);

        return new LinkResponse(newLink.getId());
    }

    public List<LinkData> getAllLinksFromEvent(UUID tripId) {
        return this.repository
                .findByTripId(tripId)
                .stream()
                .map(link -> new LinkData(
                        link.getId(),
                        link.getTitle(),
                        link.getUrl()
                )).toList();
    }
}
