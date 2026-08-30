package com.agrimate.service.service;

import com.agrimate.service.client.MlClient;
import com.agrimate.service.dto.PredictionDto;
import com.agrimate.service.dto.ScanDto;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.disease.Disease;
import com.agrimate.service.model.farm.Farm;
import com.agrimate.service.model.scan.Scan;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.CropRepository;
import com.agrimate.service.repository.DiseaseRepository;
import com.agrimate.service.repository.FarmRepository;
import com.agrimate.service.repository.ScanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanServiceTest {

    @Mock private ScanRepository scanRepository;
    @Mock private FarmRepository farmRepository;
    @Mock private CropRepository cropRepository;
    @Mock private DiseaseRepository diseaseRepository;
    @Mock private StorageService storageService;
    @Mock private MlClient mlClient;
    @Mock private ObjectMapper objectMapper;

    private ScanService scanService;

    @BeforeEach
    void setUp() {
        scanService = new ScanService(scanRepository, farmRepository, cropRepository, diseaseRepository,
                storageService, mlClient, objectMapper, 0.6);
    }

    private User userWithAccount(long accountId) {
        User user = new User();
        Account account = new Account();
        account.setId(accountId);
        user.setAccount(account);
        return user;
    }

    private MultipartFile someImage() {
        return new MockMultipartFile("image", "leaf.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    private void stubHighConfidencePrediction() {
        MlClient.MlResult result = new MlClient.MlResult(
                List.of(new MlClient.MlPrediction("rice_blast", 0.95), new MlClient.MlPrediction("healthy", 0.03)),
                true);
        when(mlClient.predict(any(), any(), any())).thenReturn(result);
        when(storageService.upload(any())).thenReturn("https://cdn/leaf.jpg");
        when(scanRepository.save(any(Scan.class))).thenAnswer(inv -> {
            Scan s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });
    }

    // BE-SCAN-01
    @Test
    void scan_throwsBadRequest_whenNoImageIsProvided() {
        User user = userWithAccount(10L);

        assertThatThrownBy(() -> scanService.scan(user, null, null, null, null, null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // BE-SCAN-02
    @Test
    void scan_success_uploadsImageAndPersistsTheTopPrediction() {
        User user = userWithAccount(10L);
        stubHighConfidencePrediction();

        ScanDto dto = scanService.scan(user, someImage(), null, null, null, null);

        assertThat(dto.predictedDisease()).isEqualTo("rice_blast");
        assertThat(dto.confidence()).isEqualTo(0.95);
        assertThat(dto.imageUrl()).isEqualTo("https://cdn/leaf.jpg");
    }

    // BE-SCAN-03
    @Test
    void scan_throwsNotFound_whenTheGivenFarmIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(farmRepository.findByIdAndAccountId(5L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scanService.scan(user, someImage(), 5L, null, null, null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-SCAN-04
    @Test
    void scan_throwsNotFound_whenTheGivenCropIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(cropRepository.findByIdAndFarmAccountId(7L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scanService.scan(user, someImage(), null, 7L, null, null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-SCAN-05
    @Test
    void scan_throwsBadGateway_whenTheMlServiceReturnsNoPredictions() {
        User user = userWithAccount(10L);
        when(storageService.upload(any())).thenReturn("https://cdn/leaf.jpg");
        when(mlClient.predict(any(), any(), any())).thenReturn(new MlClient.MlResult(List.of(), false));

        assertThatThrownBy(() -> scanService.scan(user, someImage(), null, null, null, null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    // BE-SCAN-06
    @Test
    void scan_marksLowConfidence_whenTopPredictionIsBelowTheConfidenceThreshold() {
        User user = userWithAccount(10L);
        MlClient.MlResult result = new MlClient.MlResult(
                List.of(new MlClient.MlPrediction("rice_blast", 0.4), new MlClient.MlPrediction("healthy", 0.3)), true);
        when(mlClient.predict(any(), any(), any())).thenReturn(result);
        when(storageService.upload(any())).thenReturn("url");
        when(scanRepository.save(any(Scan.class))).thenAnswer(inv -> inv.getArgument(0));

        ScanDto dto = scanService.scan(user, someImage(), null, null, null, null);

        assertThat(dto.lowConfidence()).isTrue();
    }

    // BE-SCAN-07
    @Test
    void scan_marksLowConfidence_whenTopTwoPredictionsAreTooClose() {
        User user = userWithAccount(10L);
        MlClient.MlResult result = new MlClient.MlResult(
                List.of(new MlClient.MlPrediction("rice_blast", 0.7), new MlClient.MlPrediction("brown_spot", 0.65)), true);
        when(mlClient.predict(any(), any(), any())).thenReturn(result);
        when(storageService.upload(any())).thenReturn("url");
        when(scanRepository.save(any(Scan.class))).thenAnswer(inv -> inv.getArgument(0));

        ScanDto dto = scanService.scan(user, someImage(), null, null, null, null);

        assertThat(dto.lowConfidence()).isTrue();
    }

    // BE-SCAN-08
    @Test
    void scan_isNotLowConfidence_whenTopPredictionIsHighAndClearlyAheadOfSecond() {
        User user = userWithAccount(10L);
        stubHighConfidencePrediction();

        ScanDto dto = scanService.scan(user, someImage(), null, null, null, null);

        assertThat(dto.lowConfidence()).isFalse();
    }

    // BE-SCAN-09
    @Test
    void scan_fallsBackToTheFarmsCoordinates_whenNoCoordinatesAreProvidedDirectly() {
        User user = userWithAccount(10L);
        Farm farm = new Farm();
        farm.setId(5L);
        Account account = new Account();
        account.setId(10L);
        farm.setAccount(account);
        farm.setLatitude(7.29);
        farm.setLongitude(80.63);
        when(farmRepository.findByIdAndAccountId(5L, 10L)).thenReturn(Optional.of(farm));
        stubHighConfidencePrediction();

        ScanDto dto = scanService.scan(user, someImage(), 5L, null, null, null);

        assertThat(dto.latitude()).isEqualTo(7.29);
        assertThat(dto.longitude()).isEqualTo(80.63);
    }

    // BE-SCAN-10
    @Test
    void scanGuest_throwsBadRequest_whenNoImageIsProvided() {
        assertThatThrownBy(() -> scanService.scanGuest(null))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // BE-SCAN-11
    @Test
    void scanGuest_success_returnsAnUnpersistedResultAndReportsModelMockedFlag() {
        MlClient.MlResult result = new MlClient.MlResult(
                List.of(new MlClient.MlPrediction("healthy", 0.9)), false); // model_loaded=false -> mocked
        when(mlClient.predict(any(), any(), any())).thenReturn(result);

        ScanDto dto = scanService.scanGuest(someImage());

        assertThat(dto.id()).isNull();
        assertThat(dto.modelMocked()).isTrue();
        org.mockito.Mockito.verify(scanRepository, org.mockito.Mockito.never()).save(any());
    }

    // BE-SCAN-12
    @Test
    void scanGuest_throwsBadGateway_whenTheMlServiceReturnsNoPredictions() {
        when(mlClient.predict(any(), any(), any())).thenReturn(new MlClient.MlResult(List.of(), false));

        assertThatThrownBy(() -> scanService.scanGuest(someImage()))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    // BE-SCAN-13
    @Test
    void get_throwsNotFound_whenTheScanBelongsToAnotherAccount() {
        User user = userWithAccount(10L);
        Scan scan = new Scan();
        scan.setId(1L);
        Account otherAccount = new Account();
        otherAccount.setId(99L);
        scan.setAccount(otherAccount);
        when(scanRepository.findById(1L)).thenReturn(Optional.of(scan));

        assertThatThrownBy(() -> scanService.get(user, 1L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }
}
