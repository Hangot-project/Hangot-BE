package com.hanyang.api.user.service;

import com.hanyang.api.core.exception.ResourceExistException;
import com.hanyang.api.core.exception.ResourceNotFoundException;
import com.hanyang.api.core.response.ResponseMessage;
import com.hanyang.api.dataset.domain.Dataset;
import com.hanyang.api.dataset.repository.DatasetRepository;
import com.hanyang.api.user.domain.Scrap;
import com.hanyang.api.user.domain.User;
import com.hanyang.api.user.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ScrapService {
    private final ScrapRepository scrapRepository;
    private final DatasetRepository datasetRepository;
    private final UserService userService;

    public Scrap scrap(String providerId, Long datasetId) {
        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow(() -> new ResourceNotFoundException("해당 데이터셋은 존재하지 않습니다"));
        dataset.updateScrap();
        User user = userService.findByProviderId(providerId);
        checkDuplicateByDatasetAndUser(dataset, user);

        Scrap scrap = Scrap.builder()
                .user(user)
                .dataset(dataset)
                .build();

        return scrapRepository.save(scrap);
    }

    public void delete(String providerId, Long datasetId) {
        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow(() -> new ResourceNotFoundException("해당 데이터셋은 존재하지 않습니다"));
        User user = userService.findByProviderId(providerId);
        Scrap scrap = findByDatasetAndUser(dataset, user);

        scrapRepository.delete(scrap);
    }

    public List<Scrap> findAllByProviderId(String providerId) {
        return scrapRepository.findAllByProviderId(providerId);
    }


    public Scrap findByScrapId(Long scrapId) {
        return scrapRepository.findById(scrapId)
                .orElseThrow(() -> new ResourceNotFoundException(ResponseMessage.NOT_EXIST_SCRAP));
    }

    public boolean isUserScrap(String providerId,Long datasetId) {
        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow(() -> new ResourceNotFoundException("해당 데이터셋은 존재하지 않습니다"));
        User user = userService.findByProviderId(providerId);
        return scrapRepository.findByDatasetAndUser(dataset, user).isPresent();
    }

    private Scrap findByDatasetAndUser(Dataset dataset, User user) {
        return scrapRepository.findByDatasetAndUser(dataset, user)
                .orElseThrow(() -> new ResourceNotFoundException(ResponseMessage.NOT_EXIST_SCRAP));
    }

    private void checkDuplicateByDatasetAndUser(Dataset dataset, User user) {
        scrapRepository.findByDatasetAndUser(dataset, user)
                .ifPresent(scrap -> {
                    throw new ResourceExistException(ResponseMessage.DUPLICATE_SCRAP);
                });
    }
}
