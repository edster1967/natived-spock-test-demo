package com.natived.spock.demo.nativedspocktestdemo.junit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource("/application-test.properties")
public class SampleJUnitTest {

    @Test
    public void isPolygonGood() {
        System.out.println("good we ran fine");
// x coordinates of vertices
        int[] x = { 10, 30, 40, 50, 110, 140 };

        // y coordinates of vertices
        int[] y = { 140, 110, 50, 40, 30, 10 };

        // number of vertices
        int numberofpoints = 6;

        // create a polygon with given x, y coordinates
        Polygon p = new Polygon(x, y, numberofpoints);
    }
}
