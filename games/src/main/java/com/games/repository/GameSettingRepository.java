package com.games.repository;

import com.games.entity.GameSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameSettingRepository extends JpaRepository<GameSetting, Long> {

}
