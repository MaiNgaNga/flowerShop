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
        contactDAO.save(contact);
    }

    @Override
    public List<Contact> getAllContacts() {
        return contactDAO.findAll();
       
    }

    @Override
    public Contact getContactById(int id) {
        // TODO Auto-generated method stub
        return contactDAO.findById(id).orElse(null);
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
    public Page<Contact> findAllUnprocessed(Pageable pageable) {
        return contactDAO.findAllUnprocessed(pageable);
    }

}
