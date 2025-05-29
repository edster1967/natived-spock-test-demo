package com.natived.spock.demo.nativedspocktestdemo.repository;

import com.natived.spock.demo.nativedspocktestdemo.model.MathGrade;
import org.springframework.data.repository.CrudRepository;

public interface MahtGradesDAO extends CrudRepository<MathGrade, Integer> {

    public Iterable<MathGrade> findGradeByStudentId(int id);

    public void deleteByStudentId(int id);
}
