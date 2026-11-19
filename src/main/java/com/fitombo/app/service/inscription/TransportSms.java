package com.fitombo.app.service.inscription;

public interface TransportSms {
  void envoyer(String telephone, String code);
}
