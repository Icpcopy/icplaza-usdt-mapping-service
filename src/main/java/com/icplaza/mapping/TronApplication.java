package com.icplaza.mapping;

import com.icplaza.mapping.common.Constant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TronApplication {

    public static void main(String[] args) {
        System.out.println("++++++++++++++++++++++++++++++");
        System.out.println("icplaza mapping service v" + Constant.VERSION);
        System.out.println("++++++++++++++++++++++++++++++");
        SpringApplication.run(TronApplication.class, args);
    }

}
