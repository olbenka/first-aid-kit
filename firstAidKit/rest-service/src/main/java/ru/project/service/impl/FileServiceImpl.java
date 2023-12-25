package ru.project.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import ru.project.dao.AppDocumentDAO;
import ru.project.dao.AppPhotoDAO;
import ru.project.entity.AppDocument;
import ru.project.entity.AppPhoto;
import ru.project.service.FileService;
import ru.project.utils.CryptoTool;


@Service
@Log4j
public class FileServiceImpl implements FileService {
    private final AppDocumentDAO appDocumentDAO;
    private final AppPhotoDAO appPhotoDAO;
    private final CryptoTool cryptoTool;

    public FileServiceImpl(AppDocumentDAO appDocumentDAO, AppPhotoDAO appPhotoDAO, CryptoTool cryptoTool) {
        this.appDocumentDAO = appDocumentDAO;
        this.appPhotoDAO = appPhotoDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public AppDocument getDocument(String docId) {
        var id = cryptoTool.idOf(docId);
        if (id == null) {
            return null;
        }
        return appDocumentDAO.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String photoId) {
        var id = cryptoTool.idOf(photoId);
        if (id == null) {
            return null;
        }
        return appPhotoDAO.findById(id).orElse(null);
    }

}
