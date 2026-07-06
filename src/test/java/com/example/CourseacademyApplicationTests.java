package com.example;

import com.courseacademy.CourseacademyApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CourseacademyApplication.class)
@org.springframework.test.context.ActiveProfiles("test")
class CourseacademyApplicationTests {


    @Test
    void contextLoads() {
    }
}

