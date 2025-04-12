package tn.esprit.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.entities.*;
import tn.esprit.spring.repositories.*;
import tn.esprit.spring.services.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkierServicesImplUnitTest {

    @Mock
    private ISkierRepository skierRepository;

    @Mock
    private ISubscriptionRepository subscriptionRepository;

    @Mock
    private IPisteRepository pisteRepository;

    @Mock
    private ICourseRepository courseRepository;

    @Mock
    private IRegistrationRepository registrationRepository;

    @InjectMocks
    private SkierServicesImpl skierServices;

    private Skier skier;
    private Subscription subscription;
    private Piste piste;
    private Course course;
    private Registration registration;

    @BeforeEach
    void setUp() {
        skier = new Skier();
        skier.setNumSkier(1L);
        skier.setFirstName("Karim");
        skier.setLastName("Khrouf");
        skier.setDateOfBirth(LocalDate.of(1990, 1, 1));
        skier.setCity("Tunis");
        skier.setPistes(new HashSet<>());
        skier.setRegistrations(new HashSet<>());

        subscription = new Subscription();
        subscription.setNumSub(1L);
        subscription.setTypeSub(TypeSubscription.MONTHLY);
        subscription.setStartDate(LocalDate.now());
        subscription.setPrice(50.0f);

        piste = new Piste();
        piste.setNumPiste(1L);
        piste.setNamePiste("Piste 1");
        piste.setColor(Color.BLUE);
        piste.setLength(1000);
        piste.setSlope(15);
        piste.setSkiers(new HashSet<>());

        course = new Course();
        course.setNumCourse(1L);
        course.setLevel(1);
        course.setTypeCourse(TypeCourse.COLLECTIVE_ADULT);
        course.setSupport(Support.SKI);
        course.setPrice(200.0f);
        course.setTimeSlot(2);
        course.setRegistrations(new HashSet<>());

        registration = new Registration();
        registration.setNumRegistration(1L);
        registration.setNumWeek(1);
    }

    @Test
    void testAssignSkierToSubscription() {
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(skierRepository.save(any(Skier.class))).thenReturn(skier);

        Skier updatedSkier = skierServices.assignSkierToSubscription(1L, 1L);

        assertNotNull(updatedSkier, "Updated skier should not be null");
        assertNotNull(updatedSkier.getSubscription(), "Skier should have a subscription");
        assertEquals(subscription.getNumSub(), updatedSkier.getSubscription().getNumSub(), "Subscription ID should match");
        assertEquals(TypeSubscription.MONTHLY, updatedSkier.getSubscription().getTypeSub(), "Subscription type should be MONTHLY");

        verify(skierRepository, times(1)).findById(1L);
        verify(subscriptionRepository, times(1)).findById(1L);
        verify(skierRepository, times(1)).save(skier);
    }

    @Test
    void testAddSkierAndAssignToCourse() {
        registration.setSkier(skier);
        registration.setCourse(course);
        skier.getRegistrations().add(registration);

        when(skierRepository.save(any(Skier.class))).thenReturn(skier);
        when(courseRepository.getById(1L)).thenReturn(course);
        when(registrationRepository.save(any(Registration.class))).thenReturn(registration);

        Skier savedSkier = skierServices.addSkierAndAssignToCourse(skier, 1L);

        assertNotNull(savedSkier, "Saved skier should not be null");
        assertNotNull(savedSkier.getNumSkier(), "Skier should have an ID");
        assertFalse(savedSkier.getRegistrations().isEmpty(), "Skier should have registrations");
        assertTrue(savedSkier.getRegistrations().stream().allMatch(r -> r.getSkier() != null && r.getSkier().getNumSkier().equals(savedSkier.getNumSkier())),
                "Registrations should be linked to the skier");
        assertTrue(savedSkier.getRegistrations().stream().allMatch(r -> r.getCourse() != null && r.getCourse().getNumCourse().equals(course.getNumCourse())),
                "Registrations should be linked to the course");

        verify(skierRepository, times(1)).save(any(Skier.class));
        verify(courseRepository, times(1)).getById(1L);
        verify(registrationRepository, times(1)).save(any(Registration.class));
    }

    @Test
    void testAssignSkierToPiste() {
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));
        when(pisteRepository.findById(1L)).thenReturn(Optional.of(piste));
        when(skierRepository.save(any(Skier.class))).thenReturn(skier);

        Skier updatedSkier = skierServices.assignSkierToPiste(1L, 1L);

        assertNotNull(updatedSkier, "Updated skier should not be null");
        assertFalse(updatedSkier.getPistes().isEmpty(), "Skier should have at least one piste");
        assertTrue(updatedSkier.getPistes().stream().anyMatch(p -> p.getNumPiste().equals(piste.getNumPiste())),
                "Assigned piste should be in skier’s pistes");

        verify(skierRepository, times(1)).findById(1L);
        verify(pisteRepository, times(1)).findById(1L);
        verify(skierRepository, times(1)).save(any(Skier.class));
    }

    @Test
    void testRetrieveSkiersBySubscriptionType() {
        List<Skier> skiers = Arrays.asList(skier);
        when(skierRepository.findBySubscription_TypeSub(TypeSubscription.SEMESTRIEL)).thenReturn(skiers);
        skier.setSubscription(subscription);
        subscription.setTypeSub(TypeSubscription.SEMESTRIEL);

        List<Skier> result = skierServices.retrieveSkiersBySubscriptionType(TypeSubscription.SEMESTRIEL);

        assertFalse(result.isEmpty(), "The list should not be empty");
        assertTrue(result.stream().anyMatch(s -> "Karim".equals(s.getFirstName())), "Karim Khrouf should be in the list");
        assertTrue(result.stream().allMatch(s -> s.getSubscription() != null && s.getSubscription().getTypeSub() == TypeSubscription.SEMESTRIEL),
                "All skiers should have SEMESTRIEL subscriptions");

        verify(skierRepository, times(1)).findBySubscription_TypeSub(TypeSubscription.SEMESTRIEL);
    }
}