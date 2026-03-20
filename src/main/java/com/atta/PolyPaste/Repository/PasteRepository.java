package com.atta.PolyPaste.Repository;

import com.atta.PolyPaste.Entitys.PasteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasteRepository extends JpaRepository<PasteEntity, Integer> {
}
