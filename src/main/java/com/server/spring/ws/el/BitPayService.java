package com.server.spring.ws.el;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.spring.ws.el.dto.BitPay;
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
import java.util.List;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;

@Service
public class BitPayService {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @Value("${spring.url}")
    private String baseURL;

    @Value("${spring.path}")
    private String path;

    public BitPayService(RestTemplate restTemplate, WebClient webClient) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
    }

    public void getRestTemplate() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<BitPay>> responseType = new ParameterizedTypeReference<List<BitPay>>() {};
        ResponseEntity<List<BitPay>> response = restTemplate.exchange(baseURL, HttpMethod.GET, requestEntity, responseType);
        List<BitPay> bitPays = response.getBody();
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
            List<BitPay> bitPays = mapper.readValue(responseBody, mapper.getTypeFactory().constructCollectionType(List.class, BitPay.class));
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
        List<BitPay> bitPays = mapper.readValue(reponseBody,mapper.getTypeFactory().constructCollectionType(List.class, BitPay.class));
        persistFileBitPayToJson(bitPays);

        response.close();
        httpClient.close();
    }

    public void getWebClient() throws IOException {
        List<BitPay> reponseBody = webClient.get()
                .uri(baseURL)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BitPay>>() {})
                .block();
        System.out.println("Reponse 4 : " + reponseBody);
    }

    private void persistFileBitPayToJson(List<BitPay> bitPays) throws IOException {
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
