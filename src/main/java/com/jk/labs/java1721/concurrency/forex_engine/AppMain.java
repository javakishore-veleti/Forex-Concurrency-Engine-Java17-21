package com.jk.labs.java1721.concurrency.forex_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppMain {

    public static void main(String[] args) {
        System.out.println("Forex Engine Application Started");
     SpringApplication.run(AppMain.class,args);   // Application logic goes here
    }
}
