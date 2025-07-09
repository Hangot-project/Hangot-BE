package com.hanyang.dataportal.user.repository;

import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.user.domain.Scrap;
import com.hanyang.dataportal.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    @Query("select s from Scrap s join fetch s.user u join fetch s.dataset d left join fetch d.resource where s.dataset=:dataset and s.user=:user")
    Optional<Scrap> findByDatasetAndUser(Dataset dataset, User user);
    @Query("select s from Scrap s join fetch s.user u join fetch s.dataset d left join fetch d.resource where u.email=:email")
    List<Scrap> findAllByUserEmail(String email);
    Long countByDataset(Dataset dataset);
    int countByUser(User user);
}
