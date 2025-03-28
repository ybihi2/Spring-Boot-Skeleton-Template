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
 *   <li>Embedded server startup</li>
 * </ul>
 *
 * <p>The {@code @SpringBootApplication} annotation is a convenience annotation that combines:
 * <ul>
 *   <li>{@code @Configuration} - Tags the class as a source of bean definitions</li>
 *   <li>{@code @EnableAutoConfiguration} - Enables Spring Boot's autoconfiguration</li>
 *   <li>{@code @ComponentScan} - Enables component scanning within the package</li>
 * </ul>
 */
@SpringBootApplication
public class Deliverable4Application {

	/**
	 * Main method that serves as the entry point for the Spring Boot application.
	 * <p>
	 * Initializes the Spring application context and starts the embedded server.
	 * Performs the following operations:
	 * <ol>
	 *   <li>Creates a new SpringApplication instance</li>
	 *   <li>Applies custom configuration</li>
	 *   <li>Runs the application</li>
	 *   <li>Logs startup information</li>
	 * </ol>
	 *
	 * @param args command line arguments passed to the application. These can include:
	 *             <ul>
	 *               <li>Spring profile activation (--spring.profiles.active=dev)</li>
	 *               <li>Property overrides (--server.port=9090)</li>
	 *               <li>Other Spring Boot configuration options</li>
	 *             </ul>
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
	 * Configures additional Spring application settings before startup.
	 * <p>
	 * This method provides a hook for custom application configuration that needs to execute
	 * before the application context is refreshed. Current implementation serves as a
	 * placeholder for potential customizations.
	 *
	 * @param application the SpringApplication instance being configured
	 * @see org.springframework.boot.SpringApplication
	 *
	 * <p>Example customizations that could be added:</p>
	 * <pre>{@code
	 * // Disable Spring banner
	 * application.setBannerMode(Banner.Mode.OFF);
	 *
	 * // Set additional profiles
	 * application.setAdditionalProfiles("dev");
	 *
	 * // Disable startup info logging
	 * application.setLogStartupInfo(false);
	 * }</pre>
	 */
	private static void configureApplication(SpringApplication application) {
		// Configuration placeholder - see method documentation for examples
	}

	/**
	 * Logs comprehensive application startup information including:
	 * <ul>
	 *   <li>Application name</li>
	 *   <li>Access URLs (local and external)</li>
	 *   <li>Active profiles</li>
	 *   <li>Protocol (HTTP/HTTPS)</li>
	 * </ul>
	 *
	 * @param env the Spring Environment containing configuration properties
	 * @see org.springframework.core.env.Environment
	 *
	 * <p>The method detects SSL configuration to determine the protocol
	 * and formats a visual startup banner with key information.</p>
	 */
	private static void logApplicationStartup(Environment env) {
		// Determine protocol (HTTP/HTTPS) based on SSL configuration
		String protocol = env.getProperty("server.ssl.key-store") != null ? "https" : "http";

		// Get server configuration with defaults
		String serverPort = env.getProperty("server.port", "8080");
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
	 * Currently returns "localhost" but in a production environment could be extended to:
	 * <ul>
	 *   <li>Detect actual host IP address</li>
	 *   <li>Handle network interface enumeration</li>
	 *   <li>Support containerized environments</li>
	 * </ul>
	 *
	 * @return the host address string, currently hardcoded to "localhost"
	 *
	 * @implNote For production use, consider implementing with:
	 * {@code InetAddress.getLocalHost().getHostAddress()}
	 */
	private static String getHostAddress() {
		return "localhost";
	}
}