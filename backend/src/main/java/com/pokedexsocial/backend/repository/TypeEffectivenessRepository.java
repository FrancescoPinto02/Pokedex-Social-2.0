package com.pokedexsocial.backend.repository;

import com.pokedexsocial.backend.model.Type;
import com.pokedexsocial.backend.model.TypeEffectiveness;
import com.pokedexsocial.backend.model.TypeEffectivenessId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeEffectivenessRepository extends JpaRepository<TypeEffectiveness, TypeEffectivenessId> {

    // Trova tutte le relazioni per un tipo attaccante
    List<TypeEffectiveness> findByAttackerType(Type attackerType);

    // Trova tutte le relazioni per un tipo difensore
    List<TypeEffectiveness> findByDefenderType(Type defenderType);

    // Trova il moltiplicatore specifico tra attaccante e difensore
    TypeEffectiveness findByAttackerTypeAndDefenderType(Type attackerType, Type defenderType);
}
