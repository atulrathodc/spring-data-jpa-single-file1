package com.dailycodebuffer.spring.data.jpa.tutorial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
class SpringDataJpaTutorialApplication1 {
	public int add(int a, int b) {
		return a + b;
	}

	public int multi(int a, int b) {
		return a + b;
	}
	public static void main(String[] args){
	SpringApplication.run(SpringDataJpaTutorialApplication1.class, args);

	}
}

