package org.petrovic.qa.tdt;

import java.util.Random;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello Random World!");

        while (true) {
            Integer r = new Random().nextInt();
            String msg = String.format("random: %d", r);
            System.out.println(msg);
            Thread.sleep(10000);
        }
    }
}
