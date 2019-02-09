package com.syscom.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.syscom.beans.User;

public interface UserESRepository extends ElasticsearchRepository<User, String> {
	
	Page<User> findByName(String name, Pageable pageable);
	
    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": \"?0\"}}]}}")
    Page<User> findByNameUsingCustomQuery(String name, Pageable pageable);

}
