package com.natived.spock.demo.nativedspocktestdemo.repository;

import com.natived.spock.demo.nativedspocktestdemo.model.CollegeStudent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentDAO extends CrudRepository<CollegeStudent, Integer> {

    public CollegeStudent findByEmailAddress(String emailAddress);
}
