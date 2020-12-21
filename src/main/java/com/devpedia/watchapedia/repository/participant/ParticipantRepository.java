package com.devpedia.watchapedia.repository.participant;

import com.devpedia.watchapedia.domain.Participant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long>, ParticipantCustomRepository {

    Participant findFirstByName(String name);

    List<Participant> findByNameContaining(String name, Pageable pageable);
}
