package com.sentinel.hrms.repository;

import com.sentinel.hrms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByUserName(String username);

    User findUserByEmail(String email);


}
