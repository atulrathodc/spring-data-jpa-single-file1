package com.dailycodebuffer.spring.data.jpa.tutorial;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpringDataJpasTutorialApplicationTest {
    @Test
    void contextLoads() {
        // This test ensures the Spring application context loads successfully
    }
    @Test
    void testMainMethod() throws JsonProcessingException {
        // Call the main method to ensure it runs without issues
        SpringDataJpaTutorialApplication1.main(new String[] {});
    }
    @Test
     void testAdd() {
        SpringDataJpaTutorialApplication1 app = new SpringDataJpaTutorialApplication1();
        assert(app.add(1, 2) == 3);
    }
    @Test
     void multi() {
        SpringDataJpaTutorialApplication1 app = new SpringDataJpaTutorialApplication1();
        assert(app.multi(1, 2) == 3);
    }

}
