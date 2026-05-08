package com.atta.PolyPaste.repository;

import com.atta.PolyPaste.entitys.VkUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VkUserRepository extends JpaRepository<VkUserEntity, Integer> {
    Optional<VkUserEntity> findByVkId(Long vkId);
    boolean existsByVkId(Long vkId);
}
