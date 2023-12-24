package ru.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.project.entity.BinaryContent;

public interface BinaryContentDAO extends JpaRepository<BinaryContent, Long> {
}
