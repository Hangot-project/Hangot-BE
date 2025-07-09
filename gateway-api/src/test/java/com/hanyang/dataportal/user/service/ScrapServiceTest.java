package com.hanyang.dataportal.user.service;

import com.hanyang.dataportal.core.exception.ResourceExistException;
import com.hanyang.dataportal.core.exception.ResourceNotFoundException;
import com.hanyang.dataportal.core.response.ResponseMessage;
import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.repository.DatasetRepository;
import com.hanyang.dataportal.user.domain.Scrap;
import com.hanyang.dataportal.user.domain.User;
import com.hanyang.dataportal.user.repository.ScrapRepository;
import com.hanyang.dataportal.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Sql("/h2-truncate.sql")
class ScrapServiceTest {
    @Autowired
    private ScrapService scrapService;
    @Autowired
    private ScrapRepository scrapRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DatasetRepository datasetRepository;

    @Test
    @DisplayName("스크랩할 수 있다.")
    void create(){
        //Given
        User user = userRepository.save(User.builder().email("test@email.com").build());
        Dataset dataset = datasetRepository.save(Dataset.builder().build());

        //When
        Scrap scrap = scrapService.create(user.getEmail(), dataset.getDatasetId());

        //Then
        Assertions.assertThat(scrapRepository.findById(scrap.getScrapId())).isPresent();
        Assertions.assertThat(user.getUserId()).isEqualTo(scrap.getUser().getUserId());
    }

    @Test
    @DisplayName("스크랩한 데이터셋을 또 스크랩할 수 없다")
    void createDuplicateError(){
        //Given
        User user = userRepository.save(User.builder().email("test@email.com").build());
        Dataset dataset = datasetRepository.save(Dataset.builder().build());
        scrapRepository.save(Scrap.builder().user(user).dataset(dataset).build());

        //When & Then
        Assertions.assertThatThrownBy(()->{
            scrapService.create(user.getEmail(),dataset.getDatasetId());})
                .isExactlyInstanceOf(ResourceExistException.class).hasMessage(ResponseMessage.DUPLICATE_SCRAP);

    }

    @Test
    @DisplayName("스크랩을 취소할 수 있다.")
    void delete(){
        //Given
        User user = userRepository.save(User.builder().email("test@email.com").build());
        Dataset dataset = datasetRepository.save(Dataset.builder().build());
        Scrap scrap = scrapRepository.save(Scrap.builder().user(user).dataset(dataset).build());

        //When
        scrapService.delete(user.getEmail(),dataset.getDatasetId());

        //Then
        Assertions.assertThat(scrapRepository.findById(scrap.getScrapId())).isEmpty();

    }

    @Test
    @DisplayName("스크랩하지 않은 데이터셋을 스크랩 취소할 수 없다")
    void deleteError(){
        //Given
        User user = userRepository.save(User.builder().email("test@email.com").build());
        Dataset dataset = datasetRepository.save(Dataset.builder().build());

        //When & Then
        Assertions.assertThatThrownBy(()->{
                    scrapService.delete(user.getEmail(),dataset.getDatasetId());})
                .isExactlyInstanceOf(ResourceNotFoundException.class).hasMessage(ResponseMessage.NOT_EXIST_SCRAP);

    }

    @Test
    @DisplayName("특정 데이터셋을 스크랩했는지 확인할 수 있다.")
    void isUserScrap(){
        //Given
        final String email = "test@email.com";
        User user = userRepository.save(User.builder().email(email).build());
        Dataset dataset = datasetRepository.save(Dataset.builder().build());
        Scrap scrap = Scrap.builder().user(user).dataset(dataset).build();
        scrapRepository.save(scrap);

        //When
        boolean userScrap = scrapService.isUserScrap(email, dataset.getDatasetId());

        //Then
        Assertions.assertThat(userScrap).isTrue();

    }
}