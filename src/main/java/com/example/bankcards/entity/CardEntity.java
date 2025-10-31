package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.converter.CardStatusConverter;
import com.example.bankcards.entity.enums.converter.CardEncryptorConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Convert(converter = CardEncryptorConverter.class)
    private String number;

    @ManyToOne
    private UserEntity owner;

    @Convert(converter = CardStatusConverter.class)
    private CardStatus cardStatus;

    @Convert(converter = Jsr310JpaConverters.LocalDateConverter.class)
    private LocalDate expirationDate;

    private BigDecimal balance;
}
