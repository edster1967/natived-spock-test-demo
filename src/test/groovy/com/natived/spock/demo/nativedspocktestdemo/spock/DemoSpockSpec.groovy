package com.natived.spock.demo.nativedspocktestdemo.spock

import spock.lang.Specification

class DemoSpockSpec extends Specification {

    def "two plus two should equal four"() {

        given:
        int left = 2
        int right = 2

        when:
        int result = left + right

        then:
        result == 4
    }
}
