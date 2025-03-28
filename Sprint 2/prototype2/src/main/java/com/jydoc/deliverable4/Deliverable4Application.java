package com.jydoc.deliverable4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import java.util.Arrays;

/**
 * Main entry point for the Deliverable 4 Spring Boot application.
 * <p>
 * This class serves as the configuration class and application launcher. It enables:
 * <ul>
 *   <li>Spring Boot auto-configuration</li>
 *   <li>Component scanning within the base package and sub-packages</li>
 *   <li>Externalized configuration through application.properties/yml</li>
 * </ul>
 *
 * @SpringBootApplication is a convenience annotation that combines:
 * @Configuration, @EnableAutoConfiguration, and @ComponentScan
 */
@SpringBootApplication
public class Deliverable4Application {

	/**
	 * Main method that serves as the entry point for the Spring Boot application.
	 * <p>
	 * Initializes the Spring application context and starts the embedded server.
	 *
	 * @param args command line arguments passed to the application
	 *             (can include Spring profile activation, property overrides, etc.)
	 */
	public static void main(String[] args) {
		// Create and configure the Spring application
		SpringApplication application = new SpringApplication(Deliverable4Application.class);

		// Apply any additional configuration
		configureApplication(application);

		// Run the application and get the environment context
		Environment env = application.run(args).getEnvironment();

		// Log application startup information
		logApplicationStartup(env);
	}

	/**
	 * Configures additional Spring application settings.
	 * <p>
	 * Placeholder for custom application configuration that needs to execute
	 * before the application context is refreshed.
	 *
	 * @param application the SpringApplication instance being configured
	 */
	private static void configureApplication(SpringApplication application) {
		// Example custom configurations:
		// application.setBannerMode(Banner.Mode.OFF);
		// application.setAdditionalProfiles("dev");
		// application.setLogStartupInfo(false);
	}

	/**
	 * Logs application startup information including:
	 * <ul>
	 *   <li>Access URLs</li>
	 *   <li>Active profiles</li>
	 *   <li>Protocol (HTTP/HTTPS)</li>
	 * </ul>
	 *
	 * @param env the Spring Environment containing configuration properties
	 */
	private static void logApplicationStartup(Environment env) {
		// Determine protocol (HTTP/HTTPS)
		String protocol = env.getProperty("server.ssl.key-store") != null ? "https" : "http";

		// Get server configuration
		String serverPort = env.getProperty("server.port", "8080"); // Default to 8080 if not set
		String contextPath = env.getProperty("server.servlet.context-path", "");
		String appName = env.getProperty("spring.application.name", "application");

		// Format and display startup information
		System.out.printf("\n----------------------------------------------------------\n" +
						"Application '%s' is running!\n\n" +
						"Access URLs:\n" +
						"Local: \t\t%s://localhost:%s%s\n" +
						"External: \t%s://%s:%s%s\n" +
						"Profile(s): \t%s\n" +
						"----------------------------------------------------------\n",
				appName,
				protocol,
				serverPort,
				contextPath,
				protocol,
				getHostAddress(),
				serverPort,
				contextPath,
				Arrays.toString(env.getActiveProfiles()));
	}

	/**
	 * Gets the host address for external access display.
	 * <p>
	 * In a real production environment, this would get the actual host IP.
	 *
	 * @return the host address string
	 */
	private static String getHostAddress() {
		// In production, you might use:
		// return InetAddress.getLocalHost().getHostAddress();
		return "localhost";
	}
}