package com.natived.spock.demo.nativedspocktestdemo.repository;

import com.natived.spock.demo.nativedspocktestdemo.model.ScienceGrade;
import org.springframework.data.repository.CrudRepository;

public interface ScienceGradesDAO extends CrudRepository<ScienceGrade, Integer> {

    public Iterable<ScienceGrade> findGradeByStudentId(int id);

    public void deleteByStudentId(int id);
}
