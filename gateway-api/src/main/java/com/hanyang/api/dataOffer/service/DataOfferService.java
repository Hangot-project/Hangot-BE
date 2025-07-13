package com.hanyang.api.dataOffer.service;

import com.hanyang.api.core.exception.ResourceNotFoundException;
import com.hanyang.api.dataOffer.domain.DataOffer;
import com.hanyang.api.dataOffer.dto.ReqDataOfferDto;
import com.hanyang.api.dataOffer.repository.DataOfferRepository;
import com.hanyang.api.user.domain.User;
import com.hanyang.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class DataOfferService {
    private static final int PAGE_SIZE = 10 ;
    private final DataOfferRepository dataOfferRepository;
    private final UserService userService;

    public DataOffer create(ReqDataOfferDto reqDataOfferDto, String email) {

        if (email == null) {
          throw new ResourceNotFoundException("로그인후 쓰세요~");
        }

        DataOffer dataOffer = reqDataOfferDto.toEntity();
        User user = userService.findByEmail(email);
        dataOffer.setAdmin(user);
        return dataOfferRepository.save(dataOffer);

    }

    public DataOffer getDataOffer(Long id) {
        return dataOfferRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("해당하는 요청글은 존재하지 않습니다."));
    }

    public Page<DataOffer> getDataOfferList(String userName, Integer page){
        User user = userService.findByEmail(userName);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        return dataOfferRepository.findByUser(user, pageable);
    }
}



