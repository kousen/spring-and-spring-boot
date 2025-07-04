package com.kousenit.demo.aspects;

import com.kousenit.demo.controllers.HelloController;
import com.kousenit.demo.controllers.HelloRestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.ui.Model;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Test to verify that AOP aspects are properly applied to controller methods.
 * Demonstrates testing strategies for aspect-oriented code.
 */
@SpringBootTest
class LoggingAspectTest {

    @Autowired
    private HelloController helloController;

    @Autowired
    private HelloRestController helloRestController;

    @MockitoSpyBean
    private LoggingAspect loggingAspect;

    @Test
    void testAspectBeansExist() {
        // Verify that both the controllers and aspect are properly wired
        assertNotNull(helloController);
        assertNotNull(helloRestController);
        assertNotNull(loggingAspect);
    }

    @Test
    void testTimedAnnotationAspectIsApplied() throws Throwable {
        // Given
        Model model = new ConcurrentModel();
        
        // When - call a method annotated with @Timed
        helloController.sayHello("TestUser", model);
        
        // Then - verify the @Timed aspect was called
        verify(loggingAspect, atLeastOnce()).timeAnnotatedMethods(any(), any());
    }

    @Test
    void testPerformanceMonitoringAspectIsApplied() throws Throwable {
        // When - call any controller method
        helloRestController.greet("AOP");
        
        // Then - verify the performance monitoring aspect was called
        verify(loggingAspect, atLeastOnce()).measureExecutionTime(any());
    }

    @Test
    void testLoggingAspectIsApplied() {
        // When - call any controller method
        helloRestController.greet("Logging");
        
        // Then - verify the logging aspect was called
        verify(loggingAspect, atLeastOnce()).logMethodCalls(any());
    }

    @Test
    void testAfterReturningAspectIsApplied() {
        // When - call any controller method that returns successfully
        helloRestController.greet("AfterReturning");
        
        // Then - verify the after returning aspect was called
        verify(loggingAspect, atLeastOnce()).logMethodReturn(any(), any());
    }
}