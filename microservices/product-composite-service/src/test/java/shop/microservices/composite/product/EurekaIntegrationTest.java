package shop.microservices.composite.product;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Integration test for Eureka service discovery.
 * This test verifies that the product-composite service can register with Eureka Server
 * and discover other services through Eureka.
 * <p>
 * The test automatically builds and starts the Eureka server using Gradle if not already running.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/",
        "app.eureka-server=localhost"
})
public class EurekaIntegrationTest {

    @BeforeAll
    static void startEurekaServer() throws IOException, InterruptedException {
        // Check if Eureka is already running
        if (isEurekaRunning()) {
            return;
        }

        // Start Eureka server using Gradle
        startEurekaWithGradle();

        // Verify it's running
        if (!isEurekaRunning()) {
            throw new RuntimeException("Eureka server failed to start properly");
        }
    }

    private static void startEurekaWithGradle() throws IOException, InterruptedException {
        File workingDir = new File("../../");
        File gradlewFile = new File(workingDir, "gradlew");
        File eurekaServerDir = new File(workingDir, "spring-cloud/eureka-server");

        // Build the Eureka server JAR
        ProcessBuilder buildProcess = new ProcessBuilder();
        buildProcess.directory(workingDir);
        
        // Try system Gradle first, then fallback to gradlew
        try {
            ProcessBuilder gradleCheck = new ProcessBuilder("gradle", "--version");
            gradleCheck.directory(workingDir);
            Process checkProcess = gradleCheck.start();
            int checkExitCode = checkProcess.waitFor();
            
            if (checkExitCode == 0) {
                buildProcess.command("gradle", ":spring-cloud:eureka-server:bootJar");
            } else {
                throw new IOException("System gradle not available");
            }
        } catch (Exception e) {
            buildProcess.command(gradlewFile.getAbsolutePath(), ":spring-cloud:eureka-server:bootJar");
            buildProcess.environment().put("GRADLE_USER_HOME", System.getProperty("user.home") + "/.gradle");
        }
        
        buildProcess.redirectErrorStream(true);
        Process build = buildProcess.start();
        
        String buildOutput = captureProcessOutput(build);
        int buildExitCode = build.waitFor();

        if (buildExitCode != 0) {
            throw new RuntimeException("Failed to build Eureka server JAR. Exit code: " + buildExitCode + "\nBuild output:\n" + buildOutput);
        }

        // Start the Eureka server
        ProcessBuilder runProcess = new ProcessBuilder();
        runProcess.directory(eurekaServerDir);
        runProcess.command("java", "-jar", "build/libs/eureka-server-1.0.0-SNAPSHOT.jar");
        runProcess.redirectErrorStream(true);

        Process eurekaProcess = runProcess.start();

        // Add shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (eurekaProcess.isAlive()) {
                eurekaProcess.destroyForcibly();
            }
        }));

        // Wait for Eureka server to start
        Thread.sleep(20000);
    }

    private static boolean isEurekaRunning() {
        try {
            URL url = URI.create("http://localhost:8761/eureka/apps").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static String captureProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    @Autowired
    private DiscoveryClient discoveryClient;

    @Test
    void testServiceRegistrationWithEureka() throws InterruptedException {
        // Wait for service to register with Eureka
        Thread.sleep(15000);

        // Verify that this service (product-composite) is registered
        List<String> services = discoveryClient.getServices();
        assertNotNull(services);
        assertFalse(services.isEmpty(), "No services registered with Eureka");

        // Check if product-composite service is registered
        assertTrue(services.contains("product-composite"),
                "Product-composite service should be registered with Eureka. Found services: " + services);

        // Verify service instances
        List<ServiceInstance> instances = discoveryClient.getInstances("product-composite");
        assertNotNull(instances);
        assertFalse(instances.isEmpty(), "No instances found for product-composite service");

        ServiceInstance instance = instances.get(0);
        assertNotNull(instance.getHost());
        assertTrue(instance.getPort() > 0);
        assertEquals("product-composite", instance.getServiceId().toLowerCase());
    }

    @Test
    void testDiscoveryClientAvailability() {
        // Verify that DiscoveryClient is properly configured
        assertNotNull(discoveryClient, "DiscoveryClient should be autowired");

        // This test verifies that the Eureka client is properly initialized
        List<String> services = discoveryClient.getServices();
        assertNotNull(services, "Services list should not be null");
    }
}
