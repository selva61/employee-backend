package com.sentinel.hrms.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String email;
    private String phone;
    private String profileImgUrl;
    private Date lastLoginDate;
    private Date lastLoginDateDisplay;
    private Date joinDate;
    private String[] roles;//ROLE_USER{read,edit},ROLE_ADMIN{create,edit,delete},ROLE_HR{create,edit,delete}
    private String[] authorities;//read,delete,create,edit
    private boolean isActive;
    private boolean isNotLocked;

}
