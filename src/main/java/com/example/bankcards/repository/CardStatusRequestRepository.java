package com.example.bankcards.repository;

import com.example.bankcards.entity.CardStatusRequestEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardStatusRequestRepository
        extends JpaRepository<CardStatusRequestEntity, Long>, JpaSpecificationExecutor<CardStatusRequestEntity> {
    Optional<CardStatusRequestEntity> findByCardID(Long cardID);
}