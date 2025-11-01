package com.example.bankcards.repository;

import com.example.bankcards.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"cards"})
    @Query("""
            SELECT ue
            FROM UserEntity ue
            """)
    List<UserEntity> findAllWithCards();
}