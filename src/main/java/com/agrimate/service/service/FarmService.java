package com.agrimate.service.service;

import com.agrimate.service.exception.ApiException;
import com.agrimate.service.dto.FarmDtos.FarmDto;
import com.agrimate.service.dto.FarmDtos.FarmRequest;
import com.agrimate.service.model.farm.Farm;
import com.agrimate.service.model.user.User;
import com.agrimate.service.repository.FarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FarmService {

    private final FarmRepository farmRepository;

    public FarmService(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    public List<FarmDto> list(User user) {
        return farmRepository.findByAccountId(user.getAccount().getId()).stream().map(FarmDto::from).toList();
    }

    public FarmDto get(User user, Long id) {
        return FarmDto.from(owned(user, id));
    }

    @Transactional
    public FarmDto create(User user, FarmRequest req) {
        Farm farm = new Farm();
        farm.setAccount(user.getAccount());
        apply(farm, req);
        return FarmDto.from(farmRepository.save(farm));
    }

    @Transactional
    public FarmDto update(User user, Long id, FarmRequest req) {
        Farm farm = owned(user, id);
        apply(farm, req);
        return FarmDto.from(farmRepository.save(farm));
    }

    @Transactional
    public void delete(User user, Long id) {
        farmRepository.delete(owned(user, id));
    }

    private void apply(Farm farm, FarmRequest req) {
        farm.setName(req.name());
        farm.setLatitude(req.latitude());
        farm.setLongitude(req.longitude());
        farm.setSizeAcres(req.sizeAcres());
        farm.setSoilType(req.soilType());
    }

    private Farm owned(User user, Long id) {
        return farmRepository.findByIdAndAccountId(id, user.getAccount().getId())
                .orElseThrow(() -> ApiException.notFound("Farm not found"));
    }
}
