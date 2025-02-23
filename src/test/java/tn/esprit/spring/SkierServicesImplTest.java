package tn.esprit.spring;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.entities.*;
import tn.esprit.spring.repositories.*;
import tn.esprit.spring.services.*;
import java.time.LocalDate;
import java.util.*;
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class SkierServicesImplTest {
    @Autowired
    private SkierServicesImpl skierServices;

    @Autowired
    private ISkierRepository skierRepository;

    @Test
    @Order(1)
    @Transactional
    public void testRetrieveAllSkiers() {
        // Création de données de test
        Skier skier1 = new Skier();
        skier1.setFirstName("John");
        skier1.setLastName("Doe");

        Skier skier2 = new Skier();
        skier2.setFirstName("Jane");
        skier2.setLastName("Smith");

        // Sauvegarde dans la base de données
        skierRepository.save(skier1);
        skierRepository.save(skier2);

        // Exécution de la méthode à tester
        List<Skier> skiers = skierServices.retrieveAllSkiers();

        // Vérifications
        assertFalse(skiers.isEmpty(), "La liste ne devrait pas être vide");
        assertTrue(skiers.size() >= 2, "Devrait trouver au moins 2 skieurs");

        // Vérification des données persistées
        boolean foundJohn = skiers.stream()
                .anyMatch(s -> "John".equals(s.getFirstName()));
        boolean foundJane = skiers.stream()
                .anyMatch(s -> "Jane".equals(s.getFirstName()));

        assertTrue(foundJohn, "John Doe devrait être dans la liste");
        assertTrue(foundJane, "Jane Smith devrait être dans la liste");
    }

    @Test
    @Order(2)
    @Transactional
    public void testAddSkier() {
        // Création d'un skieur avec un abonnement annuel
        Skier skier = new Skier();
        skier.setFirstName("Alice");
        skier.setLastName("Johnson");

        Subscription subscription = new Subscription();
        subscription.setTypeSub(TypeSubscription.ANNUAL);
        subscription.setStartDate(LocalDate.now());
        skier.setSubscription(subscription);

        // Exécution de la méthode à tester
        Skier savedSkier = skierServices.addSkier(skier);

        // Vérifications
        assertNotNull(savedSkier, "Le skieur sauvegardé ne devrait pas être null");
        assertNotNull(savedSkier.getNumSkier(), "Le skieur sauvegardé devrait avoir un numéro de skieur");
        assertEquals("Alice", savedSkier.getFirstName(), "Le prénom du skieur devrait être Alice");
        assertEquals("Johnson", savedSkier.getLastName(), "Le nom de famille du skieur devrait être Johnson");

        // Vérification de la date de fin de l'abonnement
        LocalDate expectedEndDate = subscription.getStartDate().plusYears(1);
        assertEquals(expectedEndDate, savedSkier.getSubscription().getEndDate(), "La date de fin de l'abonnement devrait être un an après la date de début");

        // Vérification que le skieur est bien enregistré dans la base de données
        Optional<Skier> retrievedSkier = skierRepository.findById(savedSkier.getNumSkier());
        assertTrue(retrievedSkier.isPresent(), "Le skieur devrait être trouvé dans la base de données");
        assertEquals(savedSkier.getFirstName(), retrievedSkier.get().getFirstName(), "Le prénom du skieur récupéré devrait correspondre");
        assertEquals(savedSkier.getLastName(), retrievedSkier.get().getLastName(), "Le nom de famille du skieur récupéré devrait correspondre");
        assertEquals(savedSkier.getSubscription().getEndDate(), retrievedSkier.get().getSubscription().getEndDate(), "La date de fin de l'abonnement récupérée devrait correspondre");
    }


    @Test
    @Order(5)
    @Transactional
    public void testRetrieveSkier() {
        // Création d'un skieur pour le test
        Skier skier = new Skier();
        skier.setFirstName("Test");
        skier.setLastName("User");
        skier.setSubscription(new Subscription()); // Ajoutez un abonnement si nécessaire

        // Sauvegarde du skieur dans la base de données
        Skier savedSkier = skierRepository.save(skier);
        Long savedSkierId = savedSkier.getNumSkier();

        // Exécution de la méthode à tester
        Skier retrievedSkier = skierServices.retrieveSkier(savedSkierId);

        // Vérifications
        assertNotNull(retrievedSkier, "Le skieur récupéré ne devrait pas être null");
        assertEquals(savedSkierId, retrievedSkier.getNumSkier(), "L'ID du skieur récupéré devrait correspondre");
        assertEquals("Test", retrievedSkier.getFirstName(), "Le prénom du skieur récupéré devrait être 'Test'");
        assertEquals("User", retrievedSkier.getLastName(), "Le nom de famille du skieur récupéré devrait être 'User'");

        // Test avec un ID inexistant
        Skier nonExistentSkier = skierServices.retrieveSkier(999999L);
        assertNull(nonExistentSkier, "Un skieur avec un ID inexistant devrait retourner null");
    }


    @Test
    @Order(5)
    @Transactional
    public void testRetrieveSkierById() {
        // Création d'un skieur pour le test
        Skier skier = new Skier();
        skier.setFirstName("TestSkier");
        skier.setLastName("SkierLastName");
        skier.setSubscription(new Subscription()); // Ajoutez un abonnement si nécessaire

        // Sauvegarde du skieur dans la base de données
        Skier savedSkier = skierRepository.save(skier);
        Long savedSkierId = savedSkier.getNumSkier();

        // Exécution de la méthode à tester
        Skier retrievedSkier = skierServices.retrieveSkier(savedSkierId);

        // Vérifications
        assertNotNull(retrievedSkier, "Le skieur récupéré ne devrait pas être null");
        assertEquals(savedSkierId, retrievedSkier.getNumSkier(), "L'ID du skieur récupéré devrait correspondre");
        assertEquals("TestSkier", retrievedSkier.getFirstName(), "Le prénom du skieur récupéré devrait être 'TestSkier'");
        assertEquals("SkierLastName", retrievedSkier.getLastName(), "Le nom de famille du skieur récupéré devrait être 'SkierLastName'");

        // Test avec un ID inexistant
        Skier nonExistentSkier = skierServices.retrieveSkier(999999L);
        assertNull(nonExistentSkier, "Un skieur avec un ID inexistant devrait retourner null");
    }
}
