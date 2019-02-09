package com.syscom.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.syscom.dao")
@ComponentScan(basePackages = { "com.syscom.service" })
public class ESConfig {

	private final Logger logger = LoggerFactory.getLogger(ESConfig.class);
	
	@Value("${elasticsearch.host}")
	private String esHost;

	@Value("${elasticsearch.port}")
	private int esPort;

	@Value("${elasticsearch.cluster.name}")
	private String esClusterName;

	@Bean
	public Client client() {
		TransportClient client = null;
		try {
			final Settings elasticSearchSettings = Settings.builder()
					.put("client.transport.sniff", true)
					.put("cluster.name", esClusterName)
					.build();
			client = new PreBuiltTransportClient(elasticSearchSettings);
			client.addTransportAddress(new TransportAddress(InetAddress.getByName(esHost), esPort));
		} catch (UnknownHostException unknownHostException) {
			logger.error("Erreur de la connexion au serveur Elastic Search {}:{}",esHost, esPort, unknownHostException);
		}
		return client;
	}

	@Bean
	public ElasticsearchOperations elasticsearchTemplate() {
		return new ElasticsearchTemplate(client());
	}

}
