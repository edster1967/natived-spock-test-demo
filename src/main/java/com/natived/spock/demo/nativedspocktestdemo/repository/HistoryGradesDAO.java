package com.natived.spock.demo.nativedspocktestdemo.repository;

import com.natived.spock.demo.nativedspocktestdemo.model.HistoryGrade;
import org.springframework.data.repository.CrudRepository;

public interface HistoryGradesDAO extends CrudRepository<HistoryGrade, Integer> {

    public Iterable<HistoryGrade> findGradeByStudentId(int id);

    public void deleteByStudentId(int id);
}
