package com.natived.spock.demo.nativedspocktestdemo.repository;

import com.natived.spock.demo.nativedspocktestdemo.model.CollegeStudent;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentGradeDAO {
    public CollegeStudent findByEmailAddress(String emailAddress);
}
