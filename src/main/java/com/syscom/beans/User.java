package com.syscom.beans;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString(exclude = { "password" })
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "password" })
@Document(indexName = "user_index", type = "users")
public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private String login;
	
	private String name;

	private String firstName;
	
	private String password;

}
