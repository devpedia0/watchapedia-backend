package com.devpedia.watchapedia.builder;

import com.devpedia.watchapedia.domain.Participant;

public class ParticipantMother {

    public static Participant.ParticipantBuilder defaultParticipant(String job) {
        return Participant.builder()
                .name("name")
                .description("desc")
                .job(job)
                .profileImage(null);
    }
}
