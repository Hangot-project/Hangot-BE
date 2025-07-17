package com.hanyang.api.user.repository;

import com.hanyang.api.dataset.domain.Dataset;
import com.hanyang.api.user.domain.Scrap;
import com.hanyang.api.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    @Query("select s from Scrap s join fetch s.user u join fetch s.dataset d where s.dataset=:dataset and s.user=:user")
    Optional<Scrap> findByDatasetAndUser(Dataset dataset, User user);
    @Query("select s from Scrap s join fetch s.user u join fetch s.dataset d  where u.providerId=:providerId")
    List<Scrap> findAllByProviderId(String providerId);
    Long countByDataset(Dataset dataset);
    int countByUser(User user);
}
