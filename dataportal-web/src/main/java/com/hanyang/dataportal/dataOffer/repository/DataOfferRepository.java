package com.hanyang.dataportal.dataOffer.repository;

import com.hanyang.dataportal.dataOffer.domain.DataOffer;
import com.hanyang.dataportal.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DataOfferRepository extends JpaRepository<DataOffer, Long> {

    Page<DataOffer> findByUser(User admin, Pageable pageable);

}
