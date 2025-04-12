package tn.esprit.spring;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.entities.*;
import tn.esprit.spring.repositories.*;
import tn.esprit.spring.services.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SkierServicesImplIntegrationTest {

   @Autowired
   private SkierServicesImpl skierServices;

   @Autowired
   private ISkierRepository skierRepository;

   @Autowired
   private ISubscriptionRepository subscriptionRepository;

   @Test
   @Order(1)
   @Transactional
   void testRetrieveAllSkiers() {
      Skier skier1 = new Skier();
      skier1.setFirstName("Walid");
      skier1.setLastName("Khrouf");
      skier1.setPistes(new HashSet<>());
      skier1.setRegistrations(new HashSet<>());
      skierRepository.save(skier1);

      Skier skier2 = new Skier();
      skier2.setFirstName("Mohamed");
      skier2.setLastName("Benamor");
      skier2.setPistes(new HashSet<>());
      skier2.setRegistrations(new HashSet<>());
      skierRepository.save(skier2);

      List<Skier> skiers = skierServices.retrieveAllSkiers();

      assertFalse(skiers.isEmpty(), "The list should not be empty");
      assertTrue(skiers.size() >= 2, "Should find at least 2 skiers");
      assertTrue(skiers.stream().anyMatch(s -> "Walid".equals(s.getFirstName())), "Walid Khrouf should be in the list");
      assertTrue(skiers.stream().anyMatch(s -> "Mohamed".equals(s.getFirstName())), "Mohamed Benamor should be in the list");
   }

   @Test
   @Order(2)
   @Transactional
   void testAddSkier() {
      Skier skier = new Skier();
      skier.setFirstName("Ali");
      skier.setLastName("Flen");
      skier.setDateOfBirth(LocalDate.of(1990, 1, 1));
      skier.setCity("Paris");
      skier.setPistes(new HashSet<>());
      skier.setRegistrations(new HashSet<>());

      Subscription subscription = new Subscription();
      subscription.setTypeSub(TypeSubscription.ANNUAL);
      subscription.setStartDate(LocalDate.now());
      subscription.setPrice(100.0f);
      skier.setSubscription(subscription);

      Skier savedSkier = skierServices.addSkier(skier);

      assertNotNull(savedSkier, "Saved skier should not be null");
      assertNotNull(savedSkier.getNumSkier(), "Skier should have an ID");
      assertEquals("Ali", savedSkier.getFirstName(), "First name should be Ali");
      assertEquals("Flen", savedSkier.getLastName(), "Last name should be Flen");
      assertNotNull(savedSkier.getSubscription(), "Subscription should not be null");
      assertEquals(TypeSubscription.ANNUAL, savedSkier.getSubscription().getTypeSub(), "Subscription type should be ANNUAL");
      assertEquals(subscription.getStartDate().plusYears(1), savedSkier.getSubscription().getEndDate(),
              "Subscription end date should be one year after start date");

      Optional<Skier> retrievedSkier = skierRepository.findById(savedSkier.getNumSkier());
      assertTrue(retrievedSkier.isPresent(), "Skier should exist in the database");
      assertEquals("Ali", retrievedSkier.get().getFirstName(), "Retrieved skier first name should match");
      assertEquals(subscription.getStartDate().plusYears(1), retrievedSkier.get().getSubscription().getEndDate(),
              "Retrieved subscription end date should match");
   }

   @Test
   @Order(3)
   @Transactional
   void testRetrieveSkier() {
      Skier skier = new Skier();
      skier.setFirstName("Omar");
      skier.setLastName("Khrouf");
      skier.setDateOfBirth(LocalDate.of(1988, 3, 15));
      skier.setCity("Nice");
      skier.setPistes(new HashSet<>());
      skier.setRegistrations(new HashSet<>());
      Subscription subscription = new Subscription();
      subscription.setTypeSub(TypeSubscription.SEMESTRIEL);
      subscription.setStartDate(LocalDate.now());
      subscription.setPrice(75.0f);
      skier.setSubscription(subscription);
      skier = skierRepository.save(skier);
      Long skierId = skier.getNumSkier();

      Skier retrievedSkier = skierServices.retrieveSkier(skierId);

      assertNotNull(retrievedSkier, "Retrieved skier should not be null");
      assertEquals(skierId, retrievedSkier.getNumSkier(), "Skier ID should match");
      assertEquals("Omar", retrievedSkier.getFirstName(), "First name should be Omar");
      assertEquals("Khrouf", retrievedSkier.getLastName(), "Last name should be Khrouf");
      assertNotNull(retrievedSkier.getSubscription(), "Subscription should not be null");
      assertEquals(TypeSubscription.SEMESTRIEL, retrievedSkier.getSubscription().getTypeSub(), "Subscription type should be SEMESTRIEL");

      Skier nonExistentSkier = skierServices.retrieveSkier(999999L);
      assertNull(nonExistentSkier, "Non-existent skier should return null");
   }

   @Test
   @Order(4)
   @Transactional
   void testRemoveSkier() {
      Skier skier = new Skier();
      skier.setFirstName("Sami");
      skier.setLastName("Flen");
      skier.setPistes(new HashSet<>());
      skier.setRegistrations(new HashSet<>());
      skier = skierRepository.save(skier);
      Long skierId = skier.getNumSkier();

      skierServices.removeSkier(skierId);

      Optional<Skier> deletedSkier = skierRepository.findById(skierId);
      assertFalse(deletedSkier.isPresent(), "Skier should be deleted from the database");
   }
}