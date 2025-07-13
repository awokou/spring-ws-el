package com.server.spring.ws.el.controller;

import com.server.spring.ws.el.dto.BitPayDto;
import com.server.spring.ws.el.service.BitPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BitPayController {

    private final BitPayService bitPayService;

    public BitPayController(BitPayService bitPayService) {
        this.bitPayService = bitPayService;
    }

    /**
     * Endpoint to fetch all BitPay data.
     *
     * @return ResponseEntity containing a list of BitPayDto objects.
     */
    // http://localhost:8080/api/bitpay?format=json
    // http://localhost:8080/api/bitpay?format=xml
    @GetMapping("/bitpay")
    public ResponseEntity<List<BitPayDto>> getAllBitPay() {
        return ResponseEntity.ok(bitPayService.getAllBitPay());
    }
}
