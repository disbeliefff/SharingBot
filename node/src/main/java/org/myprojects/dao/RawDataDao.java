package org.myprojects.dao;

import org.myprojects.entity.RawData;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RawDataDao extends JpaRepository<RawData, Long> {
    
}
