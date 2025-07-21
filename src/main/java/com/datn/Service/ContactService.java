package com.datn.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.datn.model.Contact;

public interface ContactService {
    void saveContact(Contact contact);
    List<Contact> getAllContacts();
    Contact getContactById(int id);
    void updateContact(int id, Contact contact);
    void deleteContact(int id);

    Page<Contact> findAllUnprocessed(Pageable pageable);
}
