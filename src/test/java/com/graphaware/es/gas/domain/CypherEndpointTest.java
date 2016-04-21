package com.graphaware.es.gas.domain;

import com.graphaware.es.gas.cypher.CypherEndPoint;
import com.graphaware.es.gas.cypher.CypherEndPointBuilder;
import com.graphaware.es.gas.cypher.CypherHttpEndPoint;
import com.graphaware.es.gas.cypher.CypherResult;
import com.graphaware.es.gas.cypher.ResultRow;
import com.graphaware.es.gas.util.TestHttpClient;
import com.graphaware.integration.neo4j.test.EmbeddedGraphDatabaseServer;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.common.settings.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class CypherEndpointTest {

    private CypherHttpEndPoint cypherHttpEndPoint;

    private EmbeddedGraphDatabaseServer server;

    private TestHttpClient httpClient;

    private static final String NEO4J_CUSTOM_PASSWORD = "password";
    private static final String NEO4J_CUSTOM_USER = "neo4j";

    @Before
    public void setUp() {
        server = new EmbeddedGraphDatabaseServer();
        server.start();
        cypherHttpEndPoint = (CypherHttpEndPoint)new CypherEndPointBuilder(CypherEndPointBuilder.CypherEndPointType.HTTP)
                .settings(Settings.EMPTY)
                .neo4jHostname(server.getURL())
                .username(NEO4J_CUSTOM_USER)
                .password(NEO4J_CUSTOM_PASSWORD)
                .build();
        httpClient = new TestHttpClient();
        changePassword();
    }

    @Test
    public void testExecuteCypher() throws Exception {
        httpClient.executeCypher(server.getURL(), getHeaders(NEO4J_CUSTOM_USER, NEO4J_CUSTOM_PASSWORD), "UNWIND range(1, 10) as x CREATE (n:Test) SET n.id = x");
        String query = "MATCH (n) RETURN n.id as id";
        HashMap<String, Object> params = new HashMap<>();
        CypherResult result = cypherHttpEndPoint.executeCypher(getHeaders(NEO4J_CUSTOM_USER, NEO4J_CUSTOM_PASSWORD), query, params);
        assertEquals(10, result.getRows().size());
        int i = 0;
        for (ResultRow resultRow : result.getRows()) {
            assertTrue(resultRow.getValues().containsKey("id"));
            assertEquals(++i, resultRow.getValues().get("id"));
        }
    }

    private void changePassword() {
        String json = "{\"password\":\"" + NEO4J_CUSTOM_PASSWORD + "\"}";
        try {
            httpClient.post(server.getURL() + "/user/neo4j/password", json, getHeaders(NEO4J_CUSTOM_USER, "neo4j"), 200);
        } catch (AssertionError e) {
            // password was already changed in a previous test and the dbms auth directory is already existing
        }
    }

    private HashMap<String, String> getHeaders(String user, String password) {
        HashMap<String, String> headers = new HashMap<>();
        try {
            String credentials = user + ":" + password;
            headers.put("Authorization", "Basic " + Base64.encodeBase64String(credentials.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return headers;
    }

    private String getJsonBody(String query) throws Exception {
        HashMap<String, Object> body = new HashMap<>();
        HashMap<String, String> statement = new HashMap<>();
        List<HashMap<String, String>> statements = new ArrayList<>();
        statement.put("statement", query);
        statements.add(statement);
        body.put("statements", statements);

        return ObjectMapper.class.newInstance().writeValueAsString(body);
    }

    @After
    public void tearDown() {
        server.stop();
    }
}
