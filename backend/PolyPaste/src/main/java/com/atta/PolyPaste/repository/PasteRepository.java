package com.atta.PolyPaste.repository;

import com.atta.PolyPaste.entitys.PasteEntity;
import com.atta.PolyPaste.enums.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasteRepository extends JpaRepository<PasteEntity, Integer> {
    Optional<PasteEntity> findById(UUID id);
    Optional<PasteEntity> findByShortUrl(String shortUrl);
    Optional<List<PasteEntity>> findByCreatorId(String creatorId);
    List<PasteEntity> findAllByExpirationTimeBefore(LocalDateTime now);
    List<PasteEntity> findByVisibilityOrderByCreatedAtDesc(Visibility visibility);
}
