//This is where the backend interacts with the database to retrieve or add data

package com.jydoc.deliverable3.Repository;
import com.jydoc.deliverable3.Model.UserModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserModel, Long> {


    //Custom Queries
    public List<UserModel> findById(int id);  //TODO: Switch this to Long type
    public List<UserModel> findByEmail(String email);
    public List<UserModel> findByFirstName(String firstName);
    public List<UserModel> findByLastName(String lastName);
    public List<UserModel> findByFirstNameAndLastName(String firstName, String lastName);




}
