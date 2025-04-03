package tn.esprit.spring.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.entities.Subscription;
import tn.esprit.spring.entities.TypeSubscription;
import tn.esprit.spring.repositories.ISubscriptionRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServicesImplTest {

    @Mock
    ISubscriptionRepository subscriptionRepository;

    @InjectMocks
    SubscriptionServicesImpl subscriptionServices;

    @Test
    void addSubscriptionTest() {
        Subscription subscription = new Subscription();
        subscription.setNumSub(1L);
        subscription.setTypeSub(TypeSubscription.ANNUAL);
        subscription.setStartDate(LocalDate.now());

        when(subscriptionRepository.save(subscription)).thenReturn(subscription);
        Subscription addedSubscription = subscriptionServices.addSubscription(subscription);

        assertNotNull(addedSubscription);
        assertEquals(subscription.getNumSub(), addedSubscription.getNumSub());
        assertEquals(subscription.getTypeSub(), addedSubscription.getTypeSub());
        assertEquals(subscription.getStartDate().plusYears(1), addedSubscription.getEndDate());
    }

    @Test
    void updateSubscriptionTest() {
        Subscription subscription = new Subscription();
        subscription.setNumSub(1L);
        subscription.setTypeSub(TypeSubscription.MONTHLY);

        when(subscriptionRepository.save(subscription)).thenReturn(subscription);
        Subscription updatedSubscription = subscriptionServices.updateSubscription(subscription);

        assertNotNull(updatedSubscription);
        assertEquals(subscription.getNumSub(), updatedSubscription.getNumSub());
        assertEquals(subscription.getTypeSub(), updatedSubscription.getTypeSub());
    }

    @Test
    void retrieveSubscriptionByIdTest() {
        Subscription subscription = new Subscription();
        subscription.setNumSub(1L);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        Subscription retrievedSubscription = subscriptionServices.retrieveSubscriptionById(1L);

        assertNotNull(retrievedSubscription);
        assertEquals(subscription.getNumSub(), retrievedSubscription.getNumSub());
    }
}
