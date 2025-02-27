package tn.esprit.spring;

import org.junit.jupiter.api.Test;
import tn.esprit.spring.entities.Course;
import tn.esprit.spring.entities.Support;
import tn.esprit.spring.entities.TypeCourse;

import static org.junit.jupiter.api.Assertions.*;

public class CourseTest {

    @Test
    public void testCourseCreation() {
        Course course = new Course();
        course.setNumCourse(1L);
        course.setLevel(1);
        course.setTypeCourse(TypeCourse.INDIVIDUAL);
        course.setSupport(Support.SKI);
        course.setPrice(100.0f);
        course.setTimeSlot(60);

        assertNotNull(course);
        assertEquals(1L, course.getNumCourse());
        assertEquals(1, course.getLevel());
        assertEquals(TypeCourse.INDIVIDUAL, course.getTypeCourse());
        assertEquals(Support.SKI, course.getSupport());
        assertEquals(100.0f, course.getPrice(), 0.01f);
        assertEquals(60, course.getTimeSlot());
    }

    @Test
    public void testCourseWithNullValues() {
        Course course = new Course();
        assertNull(course.getNumCourse());
        assertNull(course.getTypeCourse());
        assertNull(course.getSupport());
        assertNull(course.getPrice());
        assertEquals(0, course.getLevel());
        assertEquals(0, course.getTimeSlot());
    }
}