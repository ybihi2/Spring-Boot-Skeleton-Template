package com.jydoc.deliverable3.Repository;

import com.jydoc.deliverable3.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
