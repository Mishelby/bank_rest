package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends CrudRepository<CardEntity, Long>, JpaSpecificationExecutor<CardEntity> {

    @Query("""
            SELECT ce
            FROM CardEntity ce
            WHERE ce.id = :cardID AND ce.cardStatus = :status
            """)
    Optional<CardEntity> findCardEntityByIDAndStatus(@Param("cardID")Long cardID, CardStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CardEntity c WHERE c.id = :cardID")
    Optional<CardEntity> findCardForUpdate(@Param("cardID") Long id);

    @Query("""
            SELECT ce
            FROM CardEntity ce
            WHERE ce.id = :cardID AND ce.owner.id = :userID
            """)
    Optional<CardEntity> findCardEntityByCardAndUserID(Long cardID, Long userID);
}