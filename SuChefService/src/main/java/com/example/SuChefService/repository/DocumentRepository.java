package com.example.SuChefService.repository;

import com.example.SuChefService.entity.Document;
import com.example.SuChefService.entity.DocumentStatus;
import com.example.SuChefService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByUser(User user);

    long countByUser(User user);

    long countByUserAndName(User user, String name);

    Optional<Document> findFirstByUserOrderByUploadedAtDesc(User user);

    List<Document> findByUserAndStatus(User user, DocumentStatus status);

    List<Document> findByUserAndNameContainingIgnoreCase(User user, String name);
}
