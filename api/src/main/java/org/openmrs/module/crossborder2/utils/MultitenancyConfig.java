package org.openmrs.module.crossborder2.utils;

import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class MultitenancyConfig {
	
	private static final Logger log = LoggerFactory.getLogger(MultitenancyConfig.class);
	
	private static final String DEFAULT_SCHEMA = "openmrs";
	
	private static final String PREFIX = "openmrs_";
	
	private static String cachedSchema;
	
	public static String getCurrentOpenmrsSchema() {
		if (cachedSchema != null) {
			return cachedSchema;
		}
		
		try {
			Properties props = Context.getRuntimeProperties();
			String connectionUrl = props.getProperty("connection.url");
			
			if (connectionUrl != null && connectionUrl.contains("/")) {
				String schema = connectionUrl.substring(connectionUrl.lastIndexOf("/") + 1);
				if (schema.contains("?")) {
					schema = schema.substring(0, schema.indexOf("?"));
				}
				
				if (schema != null && !schema.trim().isEmpty()) {
					cachedSchema = schema.trim();
					log.info("KenyaEMR detected schema from runtime properties: " + cachedSchema);
					return cachedSchema;
				}
			}
		}
		catch (Exception e) {
			log.warn("Unable to parse schema from connection URL, falling back to default", e);
		}
		
		cachedSchema = DEFAULT_SCHEMA;
		return cachedSchema;
	}
	
	public static String getTenantSuffix() {
		String schema = getCurrentOpenmrsSchema();
		
		if (DEFAULT_SCHEMA.equals(schema)) {
			return "";
		}
		
		if (schema.startsWith(PREFIX)) {
			return schema.substring(PREFIX.length());
		}
		
		return "";
	}
	
	public static String getEtlDatabaseName() {
		String suffix = getTenantSuffix();
		String etlSchema = suffix.isEmpty() ? "kenyaemr_etl" : "kenyaemr_etl_" + suffix;
		System.out.println("[KenyaEMR] Active ETL Schema: " + etlSchema);
		log.info("[KenyaEMR] Active ETL Schema: " + etlSchema);
		return etlSchema;
	}
	
	public static final class EmrDatabaseEtl {
		
		public static String getEtlDatabase() {
			return MultitenancyConfig.getEtlDatabaseName();
		}
		
	}
}
