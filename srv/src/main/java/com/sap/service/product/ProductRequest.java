package com.sap.service.product;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataRetrieveRequest;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.core.ODataClientFactory;

public abstract class ProductRequest {
	public static String ROUTE = System.getenv("PRODUCT_ROUTE");
	public static String API_KEY = System.getenv("API_KEY");

	public static final String HTTP_METHOD_PUT = "PUT";
	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_GET = "GET";

	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HTTP_HEADER_ACCEPT = "Accept";

	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_ATOM_XML = "application/atom+xml";
	public static final String APPLICATION_FORM = "application/x-www-form-urlencoded";
	public static final String METADATA = "$metadata";
	public static final String INDEX = "/index.jsp";
	public static final String SEPARATOR = "/";
	ODataClient client = null;

	public URIBuilder getURIBuilder() {
		client = ODataClientFactory.getClient();
		return client.newURIBuilder(ROUTE);
	}

	public ODataRetrieveRequest<ClientEntity> buildRequest(String segment, String key) {
		URI requestURI = getURIBuilder().appendEntitySetSegment(segment).appendKeySegment(key).build();
		ODataRetrieveRequest<ClientEntity> request = client.getRetrieveRequestFactory().getEntityRequest(requestURI);
		request.setAccept("application/json");
		request.setContentType("application/json");
		request.addCustomHeader("APIKey", API_KEY);

		return request;

	}

	private InputStream execute(String relativeUri, String contentType, String httpMethod) throws IOException {
		HttpURLConnection connection = initializeConnection(relativeUri, contentType, httpMethod);

		connection.connect();
		checkStatus(connection);
		InputStream content = connection.getInputStream();

		return content;
	}

	private HttpURLConnection initializeConnection(String absolutUri, String contentType, String httpMethod)
			throws MalformedURLException, IOException {
		URL url = new URL(absolutUri);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod(httpMethod);
		connection.setRequestProperty(HTTP_HEADER_ACCEPT, contentType);
		connection.setRequestProperty("APIKey", API_KEY);
		connection.setRequestProperty(HTTP_HEADER_CONTENT_TYPE, contentType);

		return connection;
	}

	private HttpStatusCodes checkStatus(HttpURLConnection connection) throws IOException {
		HttpStatusCodes httpStatusCode = HttpStatusCodes.fromStatusCode(connection.getResponseCode());
		if (400 <= httpStatusCode.getStatusCode() && httpStatusCode.getStatusCode() <= 599) {
			throw new RuntimeException("Http Connection failed with status " + httpStatusCode.getStatusCode() + " "
					+ httpStatusCode.toString());
		}
		return httpStatusCode;
	}

	public Edm readEdm() throws IOException, ODataException {
		InputStream content = execute(ROUTE + SEPARATOR + METADATA, APPLICATION_XML, HTTP_METHOD_GET);
		return EntityProvider.readMetadata(content, false);
	}

	public ODataFeed readFeed(Edm edm, String entitySetName, String keyValue, String expandRelationName)
			throws IllegalStateException, IOException, EdmException, EntityProviderException {
		EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
		String absolutUri = createUri(ROUTE, entitySetName, keyValue, expandRelationName);

		InputStream content = executeGet(absolutUri, APPLICATION_JSON);
		return EntityProvider.readFeed(APPLICATION_JSON, entityContainer.getEntitySet(entitySetName), content,
				EntityProviderReadProperties.init().build());
	}

	public ODataEntry readEntry(Edm edm, String entitySetName, String keyValue, String expandRelationName)
			throws IOException, ODataException {
		String absolutUri = createUri(ROUTE, entitySetName, keyValue, expandRelationName);

		EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
		InputStream content = execute(absolutUri, APPLICATION_JSON, HTTP_METHOD_GET);

		return EntityProvider.readEntry(APPLICATION_JSON, entityContainer.getEntitySet(entitySetName), content,
				EntityProviderReadProperties.init().build());
	}

	private InputStream executeGet(String absoluteUrl, String contentType) throws IllegalStateException, IOException {
		final HttpGet get = new HttpGet(absoluteUrl);
		get.setHeader(HTTP_HEADER_ACCEPT, contentType);
		get.setHeader("APIKey", API_KEY);

		HttpResponse response = HttpClientBuilder.create().build().execute(get);
		return response.getEntity().getContent();
	}

	public static String prettyPrint(Map<String, Object> properties, int level) {
		StringBuilder b = new StringBuilder();
		Set<Entry<String, Object>> entries = properties.entrySet();

		for (Entry<String, Object> entry : entries) {
			intend(b, level);
			b.append(entry.getKey()).append(": ");
			Object value = entry.getValue();
			if (value instanceof Map) {
				value = prettyPrint((Map<String, Object>) value, level + 1);
				b.append(value).append("\n");
			} else if (value instanceof Calendar) {
				Calendar cal = (Calendar) value;
				value = SimpleDateFormat.getInstance().format(cal.getTime());
				b.append(value).append("\n");
			} else if (value instanceof ODataDeltaFeed) {
				ODataDeltaFeed feed = (ODataDeltaFeed) value;
				List<ODataEntry> inlineEntries = feed.getEntries();
				b.append("{");
				for (ODataEntry oDataEntry : inlineEntries) {
					value = prettyPrint((Map<String, Object>) oDataEntry.getProperties(), level + 1);
					b.append("\n[\n").append(value).append("\n],");
				}
				b.deleteCharAt(b.length() - 1);
				intend(b, level);
				b.append("}\n");
			} else {
				b.append(value).append("\n");
			}
		}
		// remove last line break
		b.deleteCharAt(b.length() - 1);
		return b.toString();
	}

	private static void intend(StringBuilder builder, int intendLevel) {
		for (int i = 0; i < intendLevel; i++) {
			builder.append("  ");
		}
	}

	private String createUri(String serviceUri, String entitySetName, String id, String expand) {
		final StringBuilder absolutUri = new StringBuilder(serviceUri).append(SEPARATOR).append(entitySetName);
		if (id != null) {
			absolutUri.append("('").append(id).append("')");
		}
		if (expand != null) {
			absolutUri.append("/?$expand=").append(expand);
		}
		return absolutUri.toString();
	}

	public abstract Map<String, Object> getProduct(String key);

}
