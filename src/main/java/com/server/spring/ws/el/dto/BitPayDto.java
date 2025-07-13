package com.server.spring.ws.el.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "bitPay")
@XmlAccessorType(XmlAccessType.FIELD)
public class BitPayDto {
    private String code;
    private String name;
    private double rate;
}
