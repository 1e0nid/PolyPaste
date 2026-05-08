package com.atta.PolyPaste.repository;

import com.atta.PolyPaste.entitys.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    UserEntity findByName(String name);
    boolean existsByName(String name);
}
