package com.datn.Service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.datn.Service.ContactService;
import com.datn.dao.ContactDAO;
import com.datn.model.Contact;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactDAO contactDAO;
    
    @Override
    public void saveContact(Contact contact) {
        contact.setCreatedAt(java.time.LocalDateTime.now()); // Set createdAt to current time
        contactDAO.save(contact);
    }

    @Override
    public List<Contact> getAllContacts() {
        return contactDAO.findAll();
       
    }


    @Override
    public void updateContact(int id, Contact contact) {
        contactDAO.save(contact);
    }

        

    @Override
    public void deleteContact(int id) {
        contactDAO.deleteById(id);
    }


    @Override
    public Page<Contact> findbyStatus(boolean status, Pageable pageable) {
        return contactDAO.findbyStatus(status, pageable);
    }

    @Override
    public Page<Contact> findAll(Pageable pageable) {
        return contactDAO.findAll(pageable);
    }

    @Override
    public Contact findById(int id) {
        return contactDAO.findById(id);
    }

    @Override
    public Page<Contact> findByMonthAndYear(Integer month, Integer year, Pageable pageable) {
       return contactDAO.findByMonthAndYear(month, year, pageable);
    }

    @Override
    public List<Integer> findDistinctYears() {
        return contactDAO.findDistinctYears();
    }

}
