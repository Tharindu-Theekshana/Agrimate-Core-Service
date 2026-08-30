package com.agrimate.service.service;

import com.agrimate.service.dto.CropDtos.CropDto;
import com.agrimate.service.dto.CropDtos.CropRequest;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.crop.CropStatus;
import com.agrimate.service.model.crop.Season;
import com.agrimate.service.model.farm.Farm;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.CropRepository;
import com.agrimate.service.repository.FarmRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CropServiceTest {

    @Mock private CropRepository cropRepository;
    @Mock private FarmRepository farmRepository;
    private CropService cropService;

    @BeforeEach
    void setUp() {
        cropService = new CropService(cropRepository, farmRepository);
    }

    private User userWithAccount(long accountId) {
        User user = new User();
        Account account = new Account();
        account.setId(accountId);
        user.setAccount(account);
        return user;
    }

    private Farm farmOwnedBy(long accountId, long farmId) {
        Farm farm = new Farm();
        farm.setId(farmId);
        Account account = new Account();
        account.setId(accountId);
        farm.setAccount(account);
        return farm;
    }

    private Crop cropOn(Farm farm, long cropId) {
        Crop crop = new Crop();
        crop.setId(cropId);
        crop.setFarm(farm);
        return crop;
    }

    // BE-CROP-01
    @Test
    void listByFarm_returnsCropsForAnOwnedFarm() {
        User user = userWithAccount(10L);
        Farm farm = farmOwnedBy(10L, 1L);
        when(farmRepository.findByIdAndAccountId(1L, 10L)).thenReturn(Optional.of(farm));
        when(cropRepository.findByFarmId(1L)).thenReturn(List.of(cropOn(farm, 20L)));

        List<CropDto> crops = cropService.listByFarm(user, 1L);

        assertThat(crops).hasSize(1);
        assertThat(crops.get(0).farmId()).isEqualTo(1L);
    }

    // BE-CROP-02
    @Test
    void listByFarm_throwsNotFound_whenTheFarmIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(farmRepository.findByIdAndAccountId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cropService.listByFarm(user, 1L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-CROP-03
    @Test
    void create_savesANewPaddyCropOnTheOwnedFarmWithTheRequestedFields() {
        User user = userWithAccount(10L);
        Farm farm = farmOwnedBy(10L, 1L);
        when(farmRepository.findByIdAndAccountId(1L, 10L)).thenReturn(Optional.of(farm));
        ArgumentCaptor<Crop> captor = ArgumentCaptor.forClass(Crop.class);
        when(cropRepository.save(captor.capture())).thenAnswer(inv -> {
            Crop c = inv.getArgument(0);
            c.setId(30L);
            return c;
        });
        CropRequest req = new CropRequest("BG-352", Season.YALA, 1.5, null, null, null,
                null, null, null, null, null, null);

        CropDto dto = cropService.create(user, 1L, req);

        assertThat(dto.id()).isEqualTo(30L);
        assertThat(captor.getValue().getCropType()).isEqualTo("paddy");
        assertThat(captor.getValue().getVariety()).isEqualTo("BG-352");
        assertThat(captor.getValue().getFarm()).isSameAs(farm);
    }

    // BE-CROP-04
    @Test
    void create_throwsNotFound_whenTheFarmIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(farmRepository.findByIdAndAccountId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cropService.create(user, 1L,
                new CropRequest(null, null, null, null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-CROP-05
    @Test
    void create_computesExpectedHarvestDate_asPlantingDatePlusGrowingPeriod_whenNotExplicitlyProvided() {
        User user = userWithAccount(10L);
        Farm farm = farmOwnedBy(10L, 1L);
        when(farmRepository.findByIdAndAccountId(1L, 10L)).thenReturn(Optional.of(farm));
        when(cropRepository.save(any(Crop.class))).thenAnswer(inv -> inv.getArgument(0));
        LocalDate planting = LocalDate.of(2026, 1, 1);
        CropRequest req = new CropRequest(null, Season.MAHA, null, planting, null, 105,
                null, null, null, null, null, null);

        CropDto dto = cropService.create(user, 1L, req);

        assertThat(dto.expectedHarvestDate()).isEqualTo(planting.plusDays(105));
    }

    // BE-CROP-06
    @Test
    void create_usesTheExplicitlyProvidedExpectedHarvestDate_whenGiven() {
        User user = userWithAccount(10L);
        Farm farm = farmOwnedBy(10L, 1L);
        when(farmRepository.findByIdAndAccountId(1L, 10L)).thenReturn(Optional.of(farm));
        when(cropRepository.save(any(Crop.class))).thenAnswer(inv -> inv.getArgument(0));
        LocalDate explicit = LocalDate.of(2026, 6, 1);
        CropRequest req = new CropRequest(null, Season.MAHA, null, LocalDate.of(2026, 1, 1), explicit, 105,
                null, null, null, null, null, null);

        CropDto dto = cropService.create(user, 1L, req);

        assertThat(dto.expectedHarvestDate()).isEqualTo(explicit);
    }

    // BE-CROP-07
    @Test
    void update_appliesTheRequestedFieldsIncludingStatusToAnOwnedCrop() {
        User user = userWithAccount(10L);
        Farm farm = farmOwnedBy(10L, 1L);
        Crop existing = cropOn(farm, 30L);
        when(cropRepository.findByIdAndFarmAccountId(30L, 10L)).thenReturn(Optional.of(existing));
        when(cropRepository.save(any(Crop.class))).thenAnswer(inv -> inv.getArgument(0));
        CropRequest req = new CropRequest(null, null, null, null, null, null, null,
                CropStatus.HARVESTED, LocalDate.of(2026, 5, 1), 3200.0, "A", 95000.0);

        CropDto dto = cropService.update(user, 30L, req);

        assertThat(dto.status()).isEqualTo(CropStatus.HARVESTED);
        assertThat(dto.yieldKg()).isEqualTo(3200.0);
    }

    // BE-CROP-08
    @Test
    void update_throwsNotFound_whenTheCropIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(cropRepository.findByIdAndFarmAccountId(30L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cropService.update(user, 30L,
                new CropRequest(null, null, null, null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-CROP-09
    @Test
    void delete_removesAnOwnedCrop() {
        User user = userWithAccount(10L);
        Farm farm = farmOwnedBy(10L, 1L);
        Crop existing = cropOn(farm, 30L);
        when(cropRepository.findByIdAndFarmAccountId(30L, 10L)).thenReturn(Optional.of(existing));

        cropService.delete(user, 30L);

        verify(cropRepository).delete(existing);
    }
}
