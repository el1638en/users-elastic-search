## Elastic Search

Elastic Search est un moteur de recherche distribué destiné au traitement de données volumineuses.Il utilise Apache Lucene pour indexer et rechercher les données. Elastic Search fournit une API REST qu'on peut utiliser pour manipuler les données (POST/GET/PUT/DELETE, etc...).
Pour plus d'informations, consulter cette [page officielle](https://www.elastic.co/fr/products/elasticsearch).

1. Installation

    1. Environnement

    ```
    root@pl-debian:~# uname -a
    Linux pl-debian 4.9.0-8-amd64 #1 SMP Debian 4.9.110-3+deb9u6 (2018-10-08) x86_64 GNU/Linux
    root@pl-debian:~# lsb_release -a
    No LSB modules are available.
    Distributor ID: Debian
    Description:    Debian GNU/Linux 9.5 (stretch)
    Release:        9.5
    Codename:       stretch
    ```

    2. Ajout de la clé PGP d'Elastic Search

    ```
    root@pl-debian:~# wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
    ```

    3. Ajout de la source des paquets d'Elastic Search

    ```
    root@pl-debian:# echo "deb https://artifacts.elastic.co/packages/6.x/apt stable main" | sudo tee -a /etc/apt/sources.list.d/elastic-6.x.list
    ```

    4. Installation d'Elastic Search

    ```
    root@pl-debian-1:# apt update && apt install elasticsearch
    ```

    5. Configuration d'Elastic Search

    Editer le fichier de configuration `/etc/elasticsearch/elasticsearch.yml`. Bien évidemment, modifier les configurations selon vos besoins.

    ```
    cluster.name: my-application
    node.name: node-1
    node.master: true
    node.data: true
    http.port: 9200
    transport.tcp.port: 9300
    ```

    6. Démarrage d'Elastic Search

    ```
    root@pl-debian-1:# systemctl start elasticsearch.service
    root@pl-debian-1:~# systemctl status elasticsearch.service
    ● elasticsearch.service - Elasticsearch
    Loaded: loaded (/usr/lib/systemd/system/elasticsearch.service; disabled; vendor preset: enabled)
    Active: active (running)
     Docs: http://www.elastic.co
     Main PID: 10645 (java)
     Tasks: 28 (limit: 4915)
     Memory: 1.2G
      CPU: 31.138s
      CGroup: /system.slice/elasticsearch.service
           ├─10645 /usr/bin/java -Xms1g -Xmx1g -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -Des.networkaddress.cache.ttl=60 -Des.networkadd
           └─10724 /usr/share/elasticsearch/modules/x-pack-ml/platform/linux-x86_64/bin/controller
           pl-debian-1 systemd[1]: Started Elasticsearch.
     ```

2. [Spring Data Elasticsearch](https://spring.io/projects/spring-data-elasticsearch)

Spring Data Elasticsearch, intégré à Spring Data, permet d'effectuer des opérations de CRUD sur un serveur Elasticsearch. Dans l'exemple qui suit, nous allons utiliser Spring Data Elasticsearch pour créer/modifier/consulter/supprimer des utilisateurs. Les données seront exposées via une API REST et elles seront stockées dans un cluster Elasticsearch.


  1. Model `User`

      ```java
          package com.syscom.beans;
          import java.io.Serializable;
          import org.springframework.data.annotation.Id;
          import org.springframework.data.elasticsearch.annotations.Document;

          @Document(indexName = "user_index", type = "users")
          public class User implements Serializable {
            @Id
            private String login;
            private String name;
            private String firstName;
            private String password;
        }
      ```

  2. Le DAO de recherche des utilisateurs.

      ```java
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
      ```

  3. Le service métier de traitement des utilisateurs. Ce service utilisera le DAO écrit ci-dessus.

      ```java
          package com.syscom.service.impl;

          import java.util.Optional;
          import org.slf4j.Logger;
          import org.slf4j.LoggerFactory;
          import org.springframework.beans.factory.annotation.Autowired;
          import org.springframework.data.domain.Page;
          import org.springframework.data.domain.Pageable;
          import org.springframework.stereotype.Service;
          import com.syscom.beans.User;
          import com.syscom.dao.UserESRepository;
          import com.syscom.service.UserESService;

          @Service
          public class UserESServiceImpl implements UserESService {

            private final Logger logger = LoggerFactory.getLogger(UserESServiceImpl.class);

            @Autowired
            private UserESRepository userESRepository;

            @Override
            public User create(User user) {
              logger.info("Création de l'utilisateur {}.", user);
              return userESRepository.save(user);
            }

            @Override
            public User findOne(String login) {
              logger.info("Recherche un utilisateur à partir du login : {}.", login);
              Optional<User> optionalUser = userESRepository.findById(login);
              if (optionalUser.isPresent()) {
                return optionalUser.get();
              }
              return null;
            }

            @Override
            public Iterable<User> findAll() {
              logger.info("Recherche de tous les utilisateurs");
              return userESRepository.findAll();
            }

            @Override
            public Page<User> findByUserName(String name, Pageable pageable) {
              logger.info("Recherche des utilisateurs ayant pour nom {}.", name);
              return userESRepository.findByName(name, pageable);
            }

            @Override
            public Page<User> findByUserNameUsingCustomQuery(String name, Pageable pageable) {
              logger.info("Recherche des utilisateurs ayant pour nom {}.", name);
              return userESRepository.findByNameUsingCustomQuery(name, pageable);
            }

            @Override
            public long count() {
              logger.info("Nombre total d'utilisateurs.");
              return userESRepository.count();
            }

            @Override
            public void delete(String login) {
              logger.info("Suppression de l'utilisateur ayant pour login : {}.", login);
              User user = findOne(login);
              if (user != null) {
                userESRepository.delete(user);
              }
            }
          }
      ```

  4. Configuration des informations d'accès au serveur Elasticsearch (IP, Port, etc...).

      - fichier `src/main/resources/application.properties`

        ```
            spring.data.elasticsearch.repositories.enabled=true
            spring.main.allow-bean-definition-overriding=true
            elasticsearch.cluster.name=my-application
            elasticsearch.host=127.0.0.1
            elasticsearch.port=9300
        ```

      - Configuration SpringBoot de connexion à Elasticsearch

        ```java
            package com.syscom.config;

            import java.net.InetAddress;
            import java.net.UnknownHostException;
            import org.elasticsearch.client.Client;
            import org.elasticsearch.client.transport.TransportClient;
            import org.elasticsearch.common.settings.Settings;
            import org.elasticsearch.common.transport.TransportAddress;
            import org.elasticsearch.transport.client.PreBuiltTransportClient;
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
                } catch (UnknownHostException e) {
                  logger.error("Erreur de la connexion au serveur Elastic Search {}:{}",esHost, esPort, unknownHostException);
                }
                return client;
              }

            @Bean
            public ElasticsearchOperations elasticsearchTemplate() {
              return new ElasticsearchTemplate(client());
            }
          }
        ```

    5. Le controller REST de l'API

      ```java
          @RestController
          @RequestMapping(UserController.PATH)
          public class UserController {

          	public static final String PATH = "/api/user";

          	private final Logger logger = LoggerFactory.getLogger(UserController.class);

          	@Autowired
          	private UserESService userESService;

          	/**
          	 * creer un nouvel utilisateur
          	 *
          	 * @param user {@link User}
          	 */
          	@PostMapping
          	public void createUser(@RequestBody User user) {
          		logger.info("Creation de l'utilisateur : {}", user);
          		userESService.create(user);
          	}

          	/**
          	 * Recherche par login
          	 * @param login : login de l'utilisateur
          	 * @return user
          	 */
          	@GetMapping(value = "/{login}")
          	public User findByLogin(@PathVariable String login) {
          		logger.info("Recherche d'utilisateur à partir du login : : {}", login);
          		return userESService.findOne(login);
          	}

          	/**
          	 * Supprimer un utilisateur
          	 *
          	 * @param login de l'utilisateur
          	 */
          	@DeleteMapping(value = "/{login}")
          	public void deleteByLogin(@PathVariable String login) {
          		userESService.delete(login);
          	}

          	/**
          	 * Liste de tous les utilisateurs.
          	 *
          	 * @return liste des utilisateurs.
          	 */
          	@GetMapping(value = "/all")
          	public List<User> getAll() {
          		return StreamSupport.stream(userESService.findAll().spliterator(), false).collect(Collectors.toList());
          	}
          }
      ```

Le code source de l'exemple est [ici](https://github.com/el1638en/users-elastic-search).
