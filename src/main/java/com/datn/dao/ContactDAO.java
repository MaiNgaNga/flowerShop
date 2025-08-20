package com.datn.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.Contact;

public interface ContactDAO extends JpaRepository<Contact, Integer> {
    // Define any additional query methods if needed

    @Query("SELECT c FROM Contact c ORDER BY c.createdAt DESC")
    Page<Contact> findAll(Pageable pageable);

    @Query("SELECT c FROM Contact c WHERE c.id = ?1")
    Contact findById(int id);

    @Query("SELECT c FROM Contact c where c.status =?1")
    Page<Contact> findbyStatus(boolean status, Pageable pageable);

    @Query("SELECT c FROM Contact c " +
       "WHERE (:month IS NULL OR FUNCTION('MONTH', c.createdAt) = :month) " +
       "AND (:year IS NULL OR FUNCTION('YEAR', c.createdAt) = :year)")
    Page<Contact> findByMonthAndYear(@Param("month") Integer month,
                                 @Param("year") Integer year,
                                 Pageable pageable);

    @Query("SELECT DISTINCT FUNCTION('YEAR', c.createdAt) FROM Contact c ORDER BY FUNCTION('YEAR', c.createdAt) DESC")
    List<Integer> findDistinctYears();

    

}
