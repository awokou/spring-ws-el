package com.server.spring.ws.el;

import com.server.spring.ws.el.service.BitPayService;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;


class BitPayServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private BitPayService bitPayService;

    @InjectMocks
    private BitPayService service;
}
