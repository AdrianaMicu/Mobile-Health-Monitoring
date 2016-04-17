package monitoring.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DBConfiguration {

	private @Value("${couchdb.protocol}") String protocol;
	private @Value("${couchdb.host}") String host;
	private @Value("${couchdb.port}") int port;
	private @Value("${couchdb.name}") String dbname;
	private @Value("${couchdb.username}") String username;
	private @Value("${couchdb.password}") String password;

	@Bean
	public CouchDbConnector couchDbConnector() {

		try {
			final HttpClient httpClient = new StdHttpClient.Builder()
					.url(new URL(protocol, host, port, "")).username(username)
					.password(password).build();

			final CouchDbInstance dbInstance = new StdCouchDbInstance(
					httpClient);
			
			// final ObjectMapperFactory objectMapperFactory;
			// objectMapperFactory = new CustomObjectMapperFactory();
			// objectMapperFactory = new StdObjectMapperFactory();
			
			return new StdCouchDbConnector("mqttbridgedata", dbInstance);

		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
