package com.datn.utils.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.datn.utils.SessionService;
import jakarta.servlet.http.HttpSession;

@Service
public class SessionServiceImpl implements SessionService {
  @Autowired
  HttpSession session;

  @Override
  public <T> T get(String name) {
    Object value = session.getAttribute(name);
    return (value != null) ? (T) value : null;
  }

  @Override
  public void set(String name, Object value) {
    session.setAttribute(name, value);

  }

  @Override
  public void remove(String name) {
    session.removeAttribute(name);
  }

}
