package com.usps.quickship.data.repo.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.usps.quickship.data.model.user.User;

public interface UserRepository extends MongoRepository<User, String>,
		PagingAndSortingRepository<User, String> {

	User findByUserName(String userName);
}
