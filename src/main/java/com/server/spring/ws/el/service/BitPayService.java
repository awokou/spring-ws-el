package com.server.spring.ws.el.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.spring.ws.el.dto.BitPayDto;
import com.server.spring.ws.el.model.BitPay;
import com.server.spring.ws.el.repository.BitPayRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;

@Service
@Slf4j
public class BitPayService {

    private final RestTemplate restTemplate;
    private final BitPayRepository bitPayRepository;

    @Value("${spring.url}")
    private String baseURL;

    @Value("${spring.path}")
    private String path;

    public BitPayService(RestTemplate restTemplate, BitPayRepository bitPayRepository) {
        this.restTemplate = restTemplate;
        this.bitPayRepository = bitPayRepository;
    }

    public List<BitPayDto> getAllBitPay() {
        return bitPayRepository.findAll()
                .stream()
                .map(x -> new BitPayDto(x.getCode(), x.getName(), x.getRate()))
                .toList();
    }

    public List<BitPayDto> getAllBitPayByCode(String code) {
        return bitPayRepository.findByCodeContaining(code)
                .stream()
                .map(x -> new BitPayDto(x.getCode(), x.getName(), x.getRate()))
                .toList();
    }

    public void getRestTemplate() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<BitPayDto>> responseType = new ParameterizedTypeReference<List<BitPayDto>>() {};
        ResponseEntity<List<BitPayDto>> response = restTemplate.exchange(baseURL, HttpMethod.GET, requestEntity, responseType);
        List<BitPayDto> bitPays = response.getBody();
        log.info("Reponse 1: " ,bitPays);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            persistFileBitPayToJson(bitPays);
        } else {
            write(Path.of(format("", baseURL)), null, UTF_8);
        }
    }

    public void getOkHttpClient() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(baseURL)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            List<BitPayDto> bitPays = mapper.readValue(responseBody, mapper.getTypeFactory().constructCollectionType(List.class, BitPayDto.class));
            persistFileBitPayToJson(bitPays);
            log.info("Reponse 2 : " ,responseBody);
        }
    }

    public void getHttpClient() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(baseURL);
        CloseableHttpResponse response = httpClient.execute(request);
        org.apache.http.HttpEntity entity = response.getEntity();
        String reponseBody = (entity != null) ? EntityUtils.toString(entity) : null;
        log.info("Reponse 3 : " ,reponseBody);
        ObjectMapper mapper = new ObjectMapper();
        List<BitPayDto> bitPays = mapper.readValue(reponseBody,mapper.getTypeFactory().constructCollectionType(List.class, BitPayDto.class));
        persistFileBitPayToJson(bitPays);

        response.close();
        httpClient.close();
    }

    public void fetchAndSaveBitPay() {
        BitPay[] bitPays = restTemplate.getForObject(baseURL, BitPay[].class);
        if (bitPays != null) {
            List<BitPay> uniqueTodos = Arrays.stream(bitPays)
                    .filter(bitPay -> !bitPayRepository.existsByCode(bitPay.getCode()))
                    .toList();
            if (!uniqueTodos.isEmpty()) {
                bitPayRepository.saveAll(uniqueTodos);
                log.info( "Nouveaux inséré.",uniqueTodos.size());
            } else {
                log.info("Aucun nouveau");
            }
        }
    }


    private void persistFileBitPayToJson(List<BitPayDto> bitPays) throws IOException {
        File file = new File( path);
        if (file.exists()) {
            file.delete();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, bitPays);
        log.info("Données sauvegardées dans le fichier : ", file.getAbsolutePath());
    }
}
