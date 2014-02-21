package com.usps.quickship.data.repo.qa;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.usps.quickship.data.model.qa.Tag;

public interface TagRepository extends MongoRepository<Tag, String>,
		PagingAndSortingRepository<Tag, String> {
	Tag findByName(String name);

	Tag findById(String id);
}
