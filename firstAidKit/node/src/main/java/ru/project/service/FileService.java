package ru.project.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.project.entity.AppDocument;
import ru.project.entity.AppPhoto;
import ru.project.service.enums.LinkType;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
    String generateLink(Long docId, LinkType linkType);
}
