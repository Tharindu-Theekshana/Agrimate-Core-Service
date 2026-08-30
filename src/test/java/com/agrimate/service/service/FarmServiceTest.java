package com.agrimate.service.service;

import com.agrimate.service.dto.FarmDtos.FarmDto;
import com.agrimate.service.dto.FarmDtos.FarmRequest;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.farm.Farm;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.FarmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    @Mock private FarmRepository farmRepository;
    private FarmService farmService;

    @BeforeEach
    void setUp() {
        farmService = new FarmService(farmRepository);
    }

    private User userWithAccount(long accountId) {
        User user = new User();
        user.setId(1L);
        Account account = new Account();
        account.setId(accountId);
        account.setUser(user);
        user.setAccount(account);
        return user;
    }

    private Farm farmOwnedBy(long accountId) {
        Farm farm = new Farm();
        farm.setId(5L);
        Account account = new Account();
        account.setId(accountId);
        farm.setAccount(account);
        farm.setName("Kamal's Paddy Field");
        return farm;
    }

    // BE-FARM-01
    @Test
    void list_returnsAllFarmsBelongingToTheAccount() {
        User user = userWithAccount(10L);
        when(farmRepository.findByAccountId(10L)).thenReturn(List.of(farmOwnedBy(10L)));

        List<FarmDto> farms = farmService.list(user);

        assertThat(farms).hasSize(1);
        assertThat(farms.get(0).name()).isEqualTo("Kamal's Paddy Field");
    }

    // BE-FARM-02
    @Test
    void get_returnsTheFarm_whenOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(farmRepository.findByIdAndAccountId(5L, 10L)).thenReturn(Optional.of(farmOwnedBy(10L)));

        FarmDto dto = farmService.get(user, 5L);

        assertThat(dto.id()).isEqualTo(5L);
    }

    // BE-FARM-03
    @Test
    void get_throwsNotFound_whenFarmBelongsToAnotherAccount() {
        User user = userWithAccount(10L);
        when(farmRepository.findByIdAndAccountId(5L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmService.get(user, 5L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-FARM-04
    @Test
    void create_savesANewFarmOwnedByTheCallersAccountWithTheRequestedFields() {
        User user = userWithAccount(10L);
        FarmRequest req = new FarmRequest("New Field", 7.29, 80.63, 2.5, "clay");
        ArgumentCaptor<Farm> captor = ArgumentCaptor.forClass(Farm.class);
        when(farmRepository.save(captor.capture())).thenAnswer(inv -> {
            Farm f = inv.getArgument(0);
            f.setId(99L);
            return f;
        });

        FarmDto dto = farmService.create(user, req);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(captor.getValue().getAccount().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getName()).isEqualTo("New Field");
        assertThat(captor.getValue().getSoilType()).isEqualTo("clay");
    }

    // BE-FARM-05
    @Test
    void update_appliesTheRequestedFieldsToAnOwnedFarm() {
        User user = userWithAccount(10L);
        Farm existing = farmOwnedBy(10L);
        when(farmRepository.findByIdAndAccountId(5L, 10L)).thenReturn(Optional.of(existing));
        when(farmRepository.save(any(Farm.class))).thenAnswer(inv -> inv.getArgument(0));
        FarmRequest req = new FarmRequest("Renamed Field", 1.0, 2.0, 3.0, "loam");

        FarmDto dto = farmService.update(user, 5L, req);

        assertThat(dto.name()).isEqualTo("Renamed Field");
        assertThat(dto.soilType()).isEqualTo("loam");
    }

    // BE-FARM-06
    @Test
    void update_throwsNotFound_whenTheFarmIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(farmRepository.findByIdAndAccountId(5L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmService.update(user, 5L, new FarmRequest("x", null, null, null, null)))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-FARM-07
    @Test
    void delete_removesAnOwnedFarm() {
        User user = userWithAccount(10L);
        Farm existing = farmOwnedBy(10L);
        when(farmRepository.findByIdAndAccountId(5L, 10L)).thenReturn(Optional.of(existing));

        farmService.delete(user, 5L);

        verify(farmRepository).delete(existing);
    }

    // BE-FARM-08
    @Test
    void delete_throwsNotFound_whenTheFarmIsNotOwnedByTheCaller() {
        User user = userWithAccount(10L);
        when(farmRepository.findByIdAndAccountId(5L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmService.delete(user, 5L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }
}
