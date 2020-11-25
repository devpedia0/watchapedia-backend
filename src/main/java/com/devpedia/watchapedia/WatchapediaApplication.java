package com.devpedia.watchapedia;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class WatchapediaApplication {
	private static final String PROPERTIES = "spring.config.location=" +
			"classpath:/application.yml" +
			",classpath:/secret.yml";

	public static void main(String[] args) {
		new SpringApplicationBuilder(WatchapediaApplication.class)
				.properties(PROPERTIES)
				.run(args);
	}
}
