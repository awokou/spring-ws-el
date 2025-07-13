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
import org.springframework.web.reactive.function.client.WebClient;

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
    private final WebClient webClient;
    private final BitPayRepository bitPayRepository;

    @Value("${spring.url}")
    private String baseURL;

    @Value("${spring.path}")
    private String path;

    public BitPayService(RestTemplate restTemplate, WebClient webClient, BitPayRepository bitPayRepository) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
        this.bitPayRepository = bitPayRepository;
    }

    /*List<CountryDTO> res = countries.stream().map(c -> new CountryDTO(c.getName(),
            c.getStates().stream().mapToInt(State::getPopulation).sum(),
            c.getStates().stream().flatMap(s -> s.getTowns().stream()).toList())).toList();*/

    public List<BitPayDto> getAllBitPay() {
        return bitPayRepository.findAll()
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
        System.out.println("Reponse 1: " + bitPays);
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
            System.out.println("Reponse 2 : " + responseBody);
        }
    }

    public void getHttpClient() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(baseURL);
        CloseableHttpResponse response = httpClient.execute(request);
        org.apache.http.HttpEntity entity = response.getEntity();
        String reponseBody = (entity != null) ? EntityUtils.toString(entity) : null;
        System.out.println("Reponse 3 : " + reponseBody);
        ObjectMapper mapper = new ObjectMapper();
        List<BitPayDto> bitPays = mapper.readValue(reponseBody,mapper.getTypeFactory().constructCollectionType(List.class, BitPayDto.class));
        persistFileBitPayToJson(bitPays);

        response.close();
        httpClient.close();
    }

    public void getWebClient() throws IOException {
        List<BitPayDto> reponseBody = webClient.get()
                .uri(baseURL)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BitPayDto>>() {})
                .block();
        System.out.println("Reponse 4 : " + reponseBody);
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
        System.out.println("Données sauvegardées dans le fichier : " + file.getAbsolutePath());
    }
}
