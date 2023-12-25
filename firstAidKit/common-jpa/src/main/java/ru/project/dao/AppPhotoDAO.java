package ru.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.project.entity.AppPhoto;

public interface AppPhotoDAO extends JpaRepository<AppPhoto, Long> {
}
