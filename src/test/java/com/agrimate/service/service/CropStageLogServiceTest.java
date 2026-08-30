package com.agrimate.service.service;

import com.agrimate.service.dto.CropStageLogDtos.CropStageLogDto;
import com.agrimate.service.dto.CropStageLogDtos.CropStageLogRequest;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.crop.Crop;
import com.agrimate.service.model.cropStageLog.CropStageLog;
import com.agrimate.service.model.farm.Farm;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.CropRepository;
import com.agrimate.service.repository.CropStageLogRepository;
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
class CropStageLogServiceTest {

    @Mock private CropStageLogRepository logRepository;
    @Mock private CropRepository cropRepository;
    private CropStageLogService service;

    @BeforeEach
    void setUp() {
        service = new CropStageLogService(logRepository, cropRepository);
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

    private CropStageLog logOn(Crop crop, long id, String stageKey, LocalDate date) {
        CropStageLog log = new CropStageLog();
        log.setId(id);
        log.setCrop(crop);
        log.setStageKey(stageKey);
        log.setReachedDate(date);
        return log;
    }

    // BE-STAGE-01
    @Test
    void list_returnsLogsForAnOwnedCropInReachedDateOrder() {
        User user = userWithAccount(10L);
        Crop crop = cropOwnedBy(10L, 1L);
        when(cropRepository.findByIdAndFarmAccountId(1L, 10L)).thenReturn(Optional.of(crop));
        when(logRepository.findByCropIdOrderByReachedDateAsc(1L))
                .thenReturn(List.of(logOn(crop, 100L, "seedling", LocalDate.of(2026, 1, 1))));

        List<CropStageLogDto> logs = service.list(user, 1L);

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).stageKey()).isEqualTo("seedling");
    }

    // BE-STAGE-02
    @Test
    void list_throwsNotFound_whenTheCropIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(cropRepository.findByIdAndFarmAccountId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.list(user, 1L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-STAGE-03
    @Test
    void create_savesTheLogAndResyncsTheCropsGrowthStageToTheLatestEntry() {
        User user = userWithAccount(10L);
        Crop crop = cropOwnedBy(10L, 1L);
        when(cropRepository.findByIdAndFarmAccountId(1L, 10L)).thenReturn(Optional.of(crop));
        when(logRepository.save(any(CropStageLog.class))).thenAnswer(inv -> {
            CropStageLog l = inv.getArgument(0);
            l.setId(200L);
            return l;
        });
        when(logRepository.findByCropIdOrderByReachedDateAsc(1L)).thenReturn(List.of(
                logOn(crop, 100L, "seedling", LocalDate.of(2026, 1, 1)),
                logOn(crop, 200L, "tillering", LocalDate.of(2026, 2, 1))
        ));
        CropStageLogRequest req = new CropStageLogRequest("tillering", LocalDate.of(2026, 2, 1));

        CropStageLogDto dto = service.create(user, 1L, req);

        assertThat(dto.stageKey()).isEqualTo("tillering");
        assertThat(crop.getGrowthStage()).isEqualTo("tillering"); // resynced to the latest log
        verify(cropRepository).save(crop);
    }

    // BE-STAGE-04
    @Test
    void delete_removesTheLogAndResyncsGrowthStageToWhateverRemains() {
        User user = userWithAccount(10L);
        Crop crop = cropOwnedBy(10L, 1L);
        CropStageLog toDelete = logOn(crop, 200L, "tillering", LocalDate.of(2026, 2, 1));
        when(logRepository.findByIdAndCropFarmAccountId(200L, 10L)).thenReturn(Optional.of(toDelete));
        when(logRepository.findByCropIdOrderByReachedDateAsc(1L))
                .thenReturn(List.of(logOn(crop, 100L, "seedling", LocalDate.of(2026, 1, 1))));

        service.delete(user, 200L);

        verify(logRepository).delete(toDelete);
        assertThat(crop.getGrowthStage()).isEqualTo("seedling");
    }

    // BE-STAGE-05
    @Test
    void delete_throwsNotFound_whenTheLogIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(logRepository.findByIdAndCropFarmAccountId(200L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(user, 200L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }
}
