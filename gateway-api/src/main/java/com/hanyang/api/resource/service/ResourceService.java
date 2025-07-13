package com.hanyang.api.resource.service;

import com.hanyang.api.core.exception.ResourceNotFoundException;
import com.hanyang.api.dataset.domain.Dataset;
import com.hanyang.api.dataset.repository.DatasetRepository;
import com.hanyang.api.resource.repository.DownloadRepository;
import com.hanyang.api.user.domain.Download;
import com.hanyang.api.user.domain.User;
import com.hanyang.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class ResourceService {

    private final UserService userService;
    private final DownloadRepository downloadRepository;
    private final DatasetRepository datasetRepository;

    //유저가 다운로드를 하면
    public void download(UserDetails userDetails, Long datasetId){
        if(userDetails != null){
            User user = userService.findByEmail(userDetails.getUsername());
            Dataset dataset = datasetRepository.findByIdWithTheme(datasetId).orElseThrow(() -> new ResourceNotFoundException("해당 데이터셋은 존재하지 않습니다"));
            Download download = Download.builder().dataset(dataset).user(user).build();
            downloadRepository.save(download);
        }
    }

    public List<Dataset> getMyDownloadsList(String email) {
        User user = userService.findByEmail(email);
        List<Download> downloads = downloadRepository.findByUser(user);
        return downloads.stream().map(Download::getDataset).toList();
    }
}
