package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.entity.enums.converter.CardOperationConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "card_status_request")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardStatusRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "card_id", nullable = false, unique = true)
    private Long cardID;

    @Column(name = "owner_id", nullable = false)
    private Long ownerID;

    @Convert(converter = CardOperationConverter.class)
    private CardOperation status;
}
