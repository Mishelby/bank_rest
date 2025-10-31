package com.example.bankcards.repository;

import com.example.bankcards.entity.CardStatusRequestEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardStatusRequestRepository extends CrudRepository<CardStatusRequestEntity, Long> {
}