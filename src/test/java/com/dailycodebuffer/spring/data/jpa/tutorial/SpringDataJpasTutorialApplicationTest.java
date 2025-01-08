package com.dailycodebuffer.spring.data.jpa.tutorial;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SpringDataJpasTutorialApplicationTest {
    @Test
    void contextLoads() {
        // This test ensures the Spring application context loads successfully
    }
    @Test
    void testMainMethod(){
        // Call the main method to ensure it runs without issues
        SpringDataJpaTutorialApplication1 app = new SpringDataJpaTutorialApplication1();
        SpringDataJpaTutorialApplication1.main(new String[] {});
        assertEquals(3, app.add(1, 2), "The addition result should be 5");
    }
    @Test
     void testAdd() {
        SpringDataJpaTutorialApplication1 app = new SpringDataJpaTutorialApplication1();
        assertEquals(3, app.add(1, 2), "The addition result should be 5");
    }
    @Test
     void multi() {
        SpringDataJpaTutorialApplication1 app = new SpringDataJpaTutorialApplication1();
        assertEquals(3, app.multi(1, 2), "The addition result should be 5");
    }

}
