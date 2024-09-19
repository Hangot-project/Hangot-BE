package com.hanyang.dataportal.resource.repository;

import com.hanyang.dataportal.user.domain.Download;
import com.hanyang.dataportal.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DownloadRepository extends JpaRepository<Download,Long> {
    int countByUser(User user);
    List<Download> findByUser(User user);

}
