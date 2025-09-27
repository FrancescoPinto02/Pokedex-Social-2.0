package com.pokedexsocial.backend.repository;

import com.pokedexsocial.backend.model.Team;
import com.pokedexsocial.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

    Page<Team> findByUser(User user, Pageable pageable);

    List<Team> findByVisibility(String visibility);

    @Query("SELECT t FROM Team t WHERE t.visibility = 'PUBLIC' OR t.user.id = :userId")
    Page<Team> findAllVisibleForUser(Integer userId, Pageable pageable);
}
