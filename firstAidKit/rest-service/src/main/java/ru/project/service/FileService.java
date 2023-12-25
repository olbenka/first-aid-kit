package ru.project.service;

import org.springframework.core.io.FileSystemResource;
import ru.project.entity.AppDocument;
import ru.project.entity.AppPhoto;
import ru.project.entity.BinaryContent;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
}
