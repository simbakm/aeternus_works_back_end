package com.example.aeternus_back_end.repository;

import com.example.aeternus_back_end.model.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, String> {
    List<Inquiry> findByStatus(String status);
    long countByStatus(String status);
}
