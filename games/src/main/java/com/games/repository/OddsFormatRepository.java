package com.games.repository;

import com.games.entity.OddsFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OddsFormatRepository extends JpaRepository<OddsFormat, Long> {

}
