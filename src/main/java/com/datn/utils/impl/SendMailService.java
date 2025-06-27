package com.datn.utils.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.datn.model.Mail;

import jakarta.mail.internet.MimeMessage;

@Service
public class SendMailService {
@Autowired
    JavaMailSender mailSender;

    List<Mail> listMails = new ArrayList<>();

    public void push(Mail mail){
        listMails.add(mail);
    }

    @Scheduled(fixedDelay = 500)
    public void run(){
        while (!listMails.isEmpty()) {
            this.send(listMails.remove(0));
        }
    }

    public void send(Mail mail){
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            String from = "FPTpoly@gmail.com";
            helper.setFrom(from);
            helper.setReplyTo(from);
            helper.setTo(mail.getTo());
            helper.setSubject(mail.getSubject());
            helper.setText(mail.getContent());
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
