package com.agrimate.service.service;

import com.agrimate.service.dto.TreatmentDtos.TreatmentDto;
import com.agrimate.service.dto.TreatmentDtos.TreatmentRequest;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.farm.Farm;
import com.agrimate.service.model.treatmentLog.TreatmentLog;
import com.agrimate.service.model.treatmentLog.TreatmentType;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.CropRepository;
import com.agrimate.service.repository.TreatmentLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceTest {

    @Mock private TreatmentLogRepository treatmentRepository;
    @Mock private CropRepository cropRepository;
    private TreatmentService service;

    @BeforeEach
    void setUp() {
        service = new TreatmentService(treatmentRepository, cropRepository);
    }

    private User userWithAccount(long accountId) {
        User user = new User();
        Account account = new Account();
        account.setId(accountId);
        user.setAccount(account);
        return user;
    }

    private Crop cropOwnedBy(long accountId, long cropId) {
        Crop crop = new Crop();
        crop.setId(cropId);
        Farm farm = new Farm();
        Account account = new Account();
        account.setId(accountId);
        farm.setAccount(account);
        crop.setFarm(farm);
        return crop;
    }

    // BE-TRT-01
    @Test
    void list_returnsTreatmentsForAnOwnedCrop() {
        User user = userWithAccount(10L);
        Crop crop = cropOwnedBy(10L, 1L);
        when(cropRepository.findByIdAndFarmAccountId(1L, 10L)).thenReturn(Optional.of(crop));
        TreatmentLog log = new TreatmentLog();
        log.setId(50L);
        log.setCrop(crop);
        log.setProductName("Urea");
        log.setType(TreatmentType.FERTILIZER);
        when(treatmentRepository.findByCropIdOrderByAppliedDateDesc(1L)).thenReturn(List.of(log));

        List<TreatmentDto> logs = service.list(user, 1L);

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).productName()).isEqualTo("Urea");
    }

    // BE-TRT-02
    @Test
    void list_throwsNotFound_whenTheCropIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(cropRepository.findByIdAndFarmAccountId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.list(user, 1L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-TRT-03
    @Test
    void create_defaultsToFertilizerType_whenNoTypeIsSpecified() {
        User user = userWithAccount(10L);
        Crop crop = cropOwnedBy(10L, 1L);
        when(cropRepository.findByIdAndFarmAccountId(1L, 10L)).thenReturn(Optional.of(crop));
        ArgumentCaptor<TreatmentLog> captor = ArgumentCaptor.forClass(TreatmentLog.class);
        when(treatmentRepository.save(captor.capture())).thenAnswer(inv -> {
            TreatmentLog l = inv.getArgument(0);
            l.setId(60L);
            return l;
        });
        TreatmentRequest req = new TreatmentRequest("Compost", null, "10kg", LocalDate.of(2026, 3, 1));

        service.create(user, 1L, req);

        assertThat(captor.getValue().getType()).isEqualTo(TreatmentType.FERTILIZER);
        assertThat(captor.getValue().getProductName()).isEqualTo("Compost");
    }

    // BE-TRT-04
    @Test
    void create_usesTheExplicitlyProvidedTreatmentType() {
        User user = userWithAccount(10L);
        Crop crop = cropOwnedBy(10L, 1L);
        when(cropRepository.findByIdAndFarmAccountId(1L, 10L)).thenReturn(Optional.of(crop));
        ArgumentCaptor<TreatmentLog> captor = ArgumentCaptor.forClass(TreatmentLog.class);
        when(treatmentRepository.save(captor.capture())).thenAnswer(inv -> {
            TreatmentLog l = inv.getArgument(0);
            l.setId(61L);
            return l;
        });
        TreatmentRequest req = new TreatmentRequest("Neem oil", TreatmentType.PESTICIDE, "2L", LocalDate.of(2026, 3, 1));

        service.create(user, 1L, req);

        assertThat(captor.getValue().getType()).isEqualTo(TreatmentType.PESTICIDE);
    }

    // BE-TRT-05
    @Test
    void delete_removesAnOwnedTreatmentLog() {
        User user = userWithAccount(10L);
        TreatmentLog log = new TreatmentLog();
        log.setId(50L);
        when(treatmentRepository.findByIdAndCropFarmAccountId(50L, 10L)).thenReturn(Optional.of(log));

        service.delete(user, 50L);

        verify(treatmentRepository).delete(log);
    }

    // BE-TRT-06
    @Test
    void delete_throwsNotFound_whenTheTreatmentLogIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(treatmentRepository.findByIdAndCropFarmAccountId(50L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(user, 50L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }
}
