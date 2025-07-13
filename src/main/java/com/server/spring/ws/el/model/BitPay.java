package com.server.spring.ws.el.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bit_pay")
public class BitPay {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    private String code;
    private String name;
    private double rate;
}
