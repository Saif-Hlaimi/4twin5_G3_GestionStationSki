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
import tn.esprit.spring.entities.Skier;
import tn.esprit.spring.repositories.ISkierRepository;
import tn.esprit.spring.services.SkierServicesImpl;
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
}
