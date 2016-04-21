package com.graphaware.es.gas.domain;

import com.graphaware.es.gas.cypher.CypherEndPoint;
import com.graphaware.es.gas.cypher.CypherEndPointBuilder;
import com.graphaware.es.gas.cypher.CypherHttpEndPoint;
import org.elasticsearch.common.settings.Settings;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CypherEndpointUnitTest {

    private CypherHttpEndPoint cypherHttpEndPoint;
    
    private static final String NEO4J_SERVER_URL = "http://localhost:7474";
    private static final String NEO4J_CUSTOM_PASSWORD = "password";
    private static final String NEO4J_CUSTOM_USER = "neo4j";

    @Before
    public void setUp() {
        cypherHttpEndPoint = (CypherHttpEndPoint)new CypherEndPointBuilder(CypherEndPointBuilder.CypherEndPointType.HTTP)
                .settings(Settings.EMPTY)
                .neo4jHostname(NEO4J_SERVER_URL)
                .username(NEO4J_CUSTOM_USER)
                .password(NEO4J_CUSTOM_PASSWORD)
                .build();
    }

    @Test
    public void testBuildCypherQuery() {
        String query = "MATCH (n) RETURN n";
        String json = cypherHttpEndPoint.buildCypherQuery(query);
        assertEquals("{\"statements\" : [{\"statement\" : \"MATCH (n) RETURN n\"}]}", json);
    }

    @Test
    public void testBuildQueryWithParameters() {
        String query = "MATCH (n) WHERE id(n) IN {ids}";
        HashMap<String, Object> parameters = new HashMap<>();
        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        ids.add(2);
        ids.add(3);
        parameters.put("ids", ids);
        String json = cypherHttpEndPoint.buildCypherQuery(query, parameters);
        assertEquals("{\"statements\" : [{\"statement\" : \"MATCH (n) WHERE id(n) IN {ids}\",\"parameters\":{\"ids\":[1,2,3]}}]}", json);
    }
}
