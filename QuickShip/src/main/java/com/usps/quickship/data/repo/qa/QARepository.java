package com.usps.quickship.data.repo.qa;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.usps.quickship.data.model.qa.Question;

public interface QARepository extends MongoRepository<Question, String>,
		PagingAndSortingRepository<Question, String> {
	Question findBySubject(String subject);

	Question findById(String id);
}
