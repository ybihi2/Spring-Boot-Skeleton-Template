package com.jydoc.deliverable3.Repository;
import com.jydoc.deliverable3.Model.UserModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserModel, Long> {

    public List<UserModel> findById(int id);
    public List<UserModel> findByEmail(String email);
    public List<UserModel> findByFirstName(String firstName);
    public List<UserModel> findByLastName(String lastName);




}
