package com.bulbul.authservice.entity;


import com.bulbul.authservice.constant.ApplicationConstant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = ApplicationConstant.TABLE_USERS)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;

//    @StrongPassword
    private String password;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = ApplicationConstant.TABLE_USER_ROLES,
            joinColumns = @JoinColumn(name = ApplicationConstant.JOIN_COL_USER_ID),
            inverseJoinColumns = @JoinColumn(name = ApplicationConstant.JOIN_COL_ROLE_ID))
    private Set<Role> roles = new HashSet<>();

}
