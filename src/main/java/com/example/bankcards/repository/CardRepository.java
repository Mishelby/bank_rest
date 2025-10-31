package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends CrudRepository<CardEntity, Long>, JpaSpecificationExecutor<CardEntity> {

    @Query("""
            SELECT ce
            FROM CardEntity ce
            WHERE ce.id = :cardID AND ce.cardStatus = :status
            """)
    Optional<CardEntity> findCardEntityByIDAndStatus(Long cardID, CardStatus status);
}