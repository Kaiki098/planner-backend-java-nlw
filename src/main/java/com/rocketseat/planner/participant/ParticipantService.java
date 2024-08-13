package com.rocketseat.planner.participant;

import com.rocketseat.planner.EmailService;
import com.rocketseat.planner.trip.Trip;
import org.springframework.stereotype.Service;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ParticipantService {
    private final ParticipantRepository repository;
    private final EmailService emailService;

    public ParticipantService(ParticipantRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public void registerParticipantsToEvent(List<String> participantsToInvite, Trip trip) {
        List<Participant> participants = participantsToInvite.stream().map(email -> new Participant(email, trip)).toList();

        this.repository.saveAll(participants);
    }

    public ParticipantCreateResponse registerParticipantToEvent(String email, Trip trip) {
        Participant newParticipant = new Participant(email, trip);
        this.repository.save(newParticipant);

        return new ParticipantCreateResponse(newParticipant.getId());
    }

    public void triggerConfirmationEmailToParticipants(Trip trip) {
        String date = String.format("<strong>%d a %d de %s de %d</strong>",
                trip.getStartsAt().getDayOfMonth(),
                trip.getEndsAt().getDayOfMonth(),
                trip.getEndsAt().getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.of("pt", "BR")),
                trip.getEndsAt().getYear());

        List<Participant> participants = repository.findByTripId(trip.getId());

        participants.forEach(participant -> {
                    String confirmationLink = String.format("https://localhost:8080/trips/%s/invite", participant.getId()); //TODO Esse link vai ser para a própria página
                    String message = String.format("""
                                     <div style="font-family: sans-serif; font-size: 16px; line-height: 1.6;">
                                         <p>Você foi convidado(a) para participar de uma viagem para <strong>%s</strong> nas datas de <strong>%s</strong>.</p>
                                         <p></p>
                                         <p>Para confirmar sua presença na viagem, clique no link abaixo:</p>
                                         <p></p>
                                         <p>
                                         <a href="%s">Confirmar viagem</a>
                                         </p>
                                         <p></p>
                                         <p>Caso você não saiba do que se trata esse e-mail, apenas ignore esse e-mail.</p>
                                    </div>
                                    """,
                            trip.getDestination(), date, confirmationLink);
                    System.out.println(message);
                    emailService.sendEmail(participant.getEmail(), "Venha viajar comigo (Esse email é um teste)", message);
                }
        );

    }

    public void triggerConfirmationEmailToParticipant(String email, Trip trip) {
        String confirmationLink = String.format("https://localhost:8080/trips/%s/confirm", trip.getId());

        String date = String.format("<strong>%d a %d de %s de %d</strong>",
                trip.getStartsAt().getDayOfMonth(),
                trip.getEndsAt().getDayOfMonth(),
                trip.getEndsAt().getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.of("pt", "BR")),
                trip.getEndsAt().getYear());

        String message = String.format("""
                <div style="font-family: sans-serif; font-size: 16px; line-height: 1.6;">
                      <p>Você foi convidado(a) para participar de uma viagem para <strong>%s</strong> nas datas de <strong>%s</strong>.</p>
                      <p></p>
                      <p>Para confirmar sua presença na viagem, clique no link abaixo:</p>
                      <p></p>
                      <p>
                        <a href="%s">Confirmar viagem</a>
                      </p>
                      <p></p>
                      <p>Caso você não saiba do que se trata esse e-mail, apenas ignore esse e-mail.</p>
                </div>
                """, trip.getDestination(), date, confirmationLink);

        emailService.sendEmail(email, "Venha viajar comigo (Esse email é um teste)", message);
    }

    public List<ParticipantData> getAllParticipantsFromEvent(UUID id) {
        return this.repository
                .findByTripId(id)
                .stream()
                .map(participant -> new ParticipantData(
                        participant.getId(),
                        participant.getName(),
                        participant.getEmail(),
                        participant.getIsConfirmed()))
                .toList();
    }
}
