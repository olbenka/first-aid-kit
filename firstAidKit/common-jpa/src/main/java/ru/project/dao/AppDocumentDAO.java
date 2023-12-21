package ru.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.project.entity.AppDocument;

public interface AppDocumentDAO extends JpaRepository<AppDocument, Long> {
}
