package com.natived.spock.demo.nativedspocktestdemo;

import com.natived.spock.demo.nativedspocktestdemo.model.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class NativedSpockTestDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NativedSpockTestDemoApplication.class, args);
    }

}
