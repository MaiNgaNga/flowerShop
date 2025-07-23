package com.datn.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.datn.model.Contact;

public interface ContactDAO extends JpaRepository<Contact, Integer> {
    // Define any additional query methods if needed

    @Query("SELECT c FROM Contact c WHERE c.status = true")
    Page<Contact> findAllUnprocessed(Pageable pageable);

} 
