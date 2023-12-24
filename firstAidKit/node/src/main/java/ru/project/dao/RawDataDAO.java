package ru.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.project.entity.RawData;


public interface RawDataDAO extends JpaRepository<RawData, Long> {
}
