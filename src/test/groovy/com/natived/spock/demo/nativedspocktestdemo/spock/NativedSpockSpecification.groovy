package com.natived.spock.demo.nativedspocktestdemo.spock

import com.natived.spock.demo.nativedspocktestdemo.spockmodel.Colour
import com.natived.spock.demo.nativedspocktestdemo.spockmodel.Palette
import com.natived.spock.demo.nativedspocktestdemo.spockmodel.Renderer
import com.natived.spock.demo.nativedspocktestdemo.spockmodel.ShapeFactory
import com.natived.spock.demo.nativedspocktestdemo.spockmodel.TooFewSidesException
import spock.lang.Specification
import com.natived.spock.demo.nativedspocktestdemo.spockmodel.Polygon
import spock.lang.Subject


class NativedSpockSpecification extends Specification{

    void setupSpec(){
        // setup code that needs to be run once at the start
    }


    void setup() {
        // set up code that needs to be run before each test
    }


    def "should be a simple assertion"() {
        expect:
        1==1;
    }

    def "should demonstrate given-when-then"() {
        given:
        def polygon = new Polygon(4);

        when:
        int sides = polygon.numberOfSides;

        then:
        sides == 4;
    }

    def "should demonstrate when-then"(){
        when:
        int sides = new Polygon(4).numberOfSides;

        then:
        sides == 4;
    }

    def "should throw Exception"(){
        when:
        new Polygon(0);

        then:
        def exception = thrown(TooFewSidesException.class);
        exception.numberOfSides==0;

    }

    def "should expect an excpetion to be thrown for a number of invalid input: #sides"(){
        when:
        new Polygon(sides);

        then:
        def exception = thrown(TooFewSidesException.class);
        exception.numberOfSides==sides;

        where:
        sides << [-1,0,1,2]
    }

    def "should be able to create polygon with #sides sides"(){
        when:
        def polygon = new Polygon(sides);

        then:
        polygon.numberOfSides == sides;

        where:
        sides << [3,6,9,7];

    }

    def "should use datatables for calculating max" (){
        expect:
        Math.max(a,b) == max;

        where:
        a | b || max
        1 | 3 || 3
        7 | 4 || 7
        0 | 0 || 0

    }

    def "should be able to mock concrete class" (){
        given:
        Renderer renderer = Mock();
        @Subject
        def polygon = new Polygon(4, renderer);

        when:
        polygon.draw();

        then:
        4 * renderer.drawLine();
    }

    def "should be able to create a stub"(){
        given:
        Palette pallete = Stub();
        pallete.getPrimaryColour() >> Colour.Red;

        @Subject
        def renderer = new Renderer(pallete);

        expect:
        renderer.getForegroundColour() == Colour.Red;

    }

    def "should use a helper method" () {
        given:
        Renderer renderer = Mock();
        def shapeFactory = new ShapeFactory(renderer);

        when:
        def polygon = shapeFactory.createDefaultPolygon();

        then:
        // if one test fails - first one - then the other tests are not run
//        with(polygon){
//            numberOfSides == 4;
//            renderer == renderer;
//        }

        verifyAll(polygon) {
            numberOfSides == 4;
            renderer == renderer;
        }

    }

    void cleanup() {
        // code that tears down after each test
    }

    void cleanupSpec() {
        // code that tears down once after all tests
    }

}
