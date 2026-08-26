package com.agrimate.service.client;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.PredictionDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MlClient {

    private static final Logger log = LoggerFactory.getLogger(MlClient.class);

    private final RestClient restClient;

    public MlClient(@Value("${agrimate.ml.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public MlResult predict(byte[] imageBytes, String filename, String contentType) {
        var builder = new MultipartBodyBuilder();
        builder.part("image", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename != null ? filename : "leaf.jpg";
            }
        }).contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"));

        try {
            MlResult mlResult = restClient.post()
                                .uri("/predict")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .body(builder.build())
                                .retrieve()
                                .body(MlResult.class);
            log.info("ML service prediction response: {}", mlResult);
            return mlResult;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Disease detection service is unavailable. Please try again. (" + ex.getMessage() + ")");
        }
    }

    public record MlResult(
            List<MlPrediction> predictions,
            @JsonProperty("model_loaded") boolean modelLoaded
    ) {
        public List<PredictionDto> toDtos() {
            return predictions.stream().map(p -> new PredictionDto(p.disease(), p.confidence())).toList();
        }
    }

    public record MlPrediction(String disease, double confidence) {}
}
