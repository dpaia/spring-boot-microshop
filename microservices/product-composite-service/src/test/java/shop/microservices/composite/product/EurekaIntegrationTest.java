package shop.microservices.composite.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Integration test for Eureka service discovery.
 * This test verifies that the product-composite service can register with Eureka Server
 * and discover other services through Eureka.
 * 
 * Prerequisites: Eureka Server must be running on localhost:8761
 * Run: docker compose up -d eureka
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class EurekaIntegrationTest {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Test
    void testServiceRegistrationWithEureka() throws InterruptedException {
        // Wait for service to register with Eureka
        Thread.sleep(10000);

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