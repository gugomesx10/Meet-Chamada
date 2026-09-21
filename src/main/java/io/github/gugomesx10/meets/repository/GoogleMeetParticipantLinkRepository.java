package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.GoogleMeetParticipantLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoogleMeetParticipantLinkRepository extends JpaRepository<GoogleMeetParticipantLink, UUID> {

    Optional<GoogleMeetParticipantLink> findByGoogleMeetUserName(
            String googleMeetUserName
    );

    List<GoogleMeetParticipantLink> findAllByUserId(
            UUID userId
    );

    boolean existsByGoogleMeetUserName(
            String googleMeetUserName
    );
}