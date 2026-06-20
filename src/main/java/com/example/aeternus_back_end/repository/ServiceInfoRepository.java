package com.example.aeternus_back_end.repository;

import com.example.aeternus_back_end.model.ServiceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceInfoRepository extends JpaRepository<ServiceInfo, String> {
}
