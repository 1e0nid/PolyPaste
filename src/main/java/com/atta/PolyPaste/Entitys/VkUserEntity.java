package com.atta.PolyPaste.entitys;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "VkUsers")
@Data
public class VkUserEntity {
    @Id
    private Long vkId;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String avatarUrl;
}
