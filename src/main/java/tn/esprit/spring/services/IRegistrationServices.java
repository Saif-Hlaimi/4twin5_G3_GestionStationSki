package tn.esprit.spring.services;

import tn.esprit.spring.entities.*;

import java.util.List;

public interface IRegistrationServices {
	List<Registration> retrieveAllRegistration();

	Registration  addRegistration(Registration  registration);

	Registration updateRegistration(Registration registration);

	Registration retrieveRegistration(Long numRegistration);
	void removeRegistration (Long numRegistration);

	Registration addRegistrationAndAssignToSkier(Registration registration, Long numSkier);
	Registration assignRegistrationToCourse(Long numRegistration, Long numCourse);
	Registration addRegistrationAndAssignToSkierAndCourse(Registration registration, Long numSkieur, Long numCours);
	List<Integer> numWeeksCourseOfInstructorBySupport(Long numInstructor, Support support);
}

