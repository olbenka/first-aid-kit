package ru.project.service;

import ru.project.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
