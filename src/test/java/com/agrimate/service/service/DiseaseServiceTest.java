package com.agrimate.service.service;

import com.agrimate.service.dto.DiseaseDto;
import com.agrimate.service.exception.ApiException;
import com.agrimate.service.model.disease.Disease;
import com.agrimate.service.repository.DiseaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiseaseServiceTest {

    @Mock private DiseaseRepository diseaseRepository;
    private DiseaseService diseaseService;

    @BeforeEach
    void setUp() {
        diseaseService = new DiseaseService(diseaseRepository);
    }

    private Disease disease(String key, String nameEn) {
        Disease d = new Disease();
        d.setDiseaseKey(key);
        d.setNameEn(nameEn);
        return d;
    }

    // BE-DIS-01
    @Test
    void list_returnsAllDiseasesSortedAlphabeticallyByEnglishName() {
        when(diseaseRepository.findAll()).thenReturn(List.of(
                disease("tungro", "Tungro"), disease("brown_spot", "Brown Spot")));

        List<DiseaseDto> diseases = diseaseService.list();

        assertThat(diseases).extracting(DiseaseDto::nameEn).containsExactly("Brown Spot", "Tungro");
    }

    // BE-DIS-02
    @Test
    void get_returnsTheDiseaseForAKnownKey() {
        when(diseaseRepository.findByDiseaseKey("rice_blast")).thenReturn(Optional.of(disease("rice_blast", "Rice Blast")));

        DiseaseDto dto = diseaseService.get("rice_blast");

        assertThat(dto.nameEn()).isEqualTo("Rice Blast");
    }

    // BE-DIS-03
    @Test
    void get_throwsNotFound_forAnUnknownKey() {
        when(diseaseRepository.findByDiseaseKey("not_a_disease")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diseaseService.get("not_a_disease"))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    // BE-DIS-04
    @Test
    void find_throwsNotFound_forAnUnknownKey() {
        when(diseaseRepository.findByDiseaseKey("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diseaseService.find("ghost"))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }
}
