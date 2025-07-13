package com.server.spring.ws.el.repository;

import com.server.spring.ws.el.model.BitPay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BitPayRepository extends JpaRepository<BitPay, UUID> {
    boolean existsByCode(String code);
}
