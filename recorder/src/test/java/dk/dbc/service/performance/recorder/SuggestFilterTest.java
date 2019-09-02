/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of performance-test-recorder
 *
 * performance-test-recorder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * performance-test-recorder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.service.performance.recorder;

import dk.dbc.jslib.Environment;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class SuggestFilterTest {

    private static final String SUGGEST_LINE = "{\"level\":\"INFO\",\"sys_nydus_destination\":\"k8s-os-externals\",\"logger\":\"dk.dbc.laesekompas.suggester.webservice.SuggestResource\",\"sys_appid\":\"os-externals/suggester-laesekompas-webservice-container\",\"thread\":\"http-thread-pool::http-listener(2)\",\"message\":\"suggestion performed with query: foo, collectcion: ALL\",\"sys_kubernetes_ns\":\"os-externals\",\"version\":\"1\",\"mdc\":{\"requestType\":\"suggest\",\"query\":\"foo\",\"collection\":\"suggest-all\"},\"sys_stream\":\"stdout\",\"sys_kubernetes_container\":\"suggester-laesekompas-webservice-container\",\"@timestamp\":\"2019-08-27T06:34:27.515+00:00\",\"sys_env\":\"kubernetes\",\"level_value\":20000,\"sys_host\":\"container-p03\",\"sys_kubernetes\":{\"container\":{\"name\":\"suggester-laesekompas-webservice-container\"},\"labels\":{\"network-policy-http-incoming\":\"yes\",\"pod-template-hash\":\"5b884c8645\",\"app\":{\"kubernetes\":{\"io/part-of\":\"laesekompas\",\"io/version\":\"46\",\"io/name\":\"suggester-laesekompas-webservice\",\"io/component\":\"pod\"},\"dbc\":{\"dk/team\":\"os-team\",\"dk/release\":\"1\"}},\"network-policy-solr7-outgoing\":\"yes\"},\"namespace\":\"os-externals\",\"pod\":{\"name\":\"suggester-laesekompas-webservice-1-deploy-5b884c8645-v9fqh\",\"uid\":\"b6d98147-aedb-11e9-9183-48df371ca910\"},\"replicaset\":{\"name\":\"suggester-laesekompas-webservice-1-deploy-5b884c8645\"}},\"sys_taskid\":\"os-externals/suggester-laesekompas-webservice-1-deploy-5b884c8645-v9fqh\",\"timestamp\":\"2019-08-27T06:34:27.515+00:00\"}";
    private static final String SEARCH_LINE  = "{\"level\":\"INFO\",\"sys_nydus_destination\":\"k8s-os-externals\",\"logger\":\"dk.dbc.laesekompas.suggester.webservice.SearchResource\",\"sys_appid\":\"os-externals/suggester-laesekompas-webservice-container\",\"thread\":\"http-thread-pool::http-listener(2)\",\"message\":\"/search performed with query: london, field: , exact: false, merge_workid: false, rows: 10\",\"sys_kubernetes_ns\":\"os-externals\",\"version\":\"1\",\"mdc\":{\"merge_workid\":\"false\",\"requestType\":\"search\",\"field\":\"\",\"query\":\"london\",\"exact\":\"false\",\"rows\":\"10\"},\"sys_stream\":\"stdout\",\"sys_kubernetes_container\":\"suggester-laesekompas-webservice-container\",\"@timestamp\":\"2019-08-27T07:03:44.692+00:00\",\"sys_env\":\"kubernetes\",\"level_value\":20000,\"sys_host\":\"container-p03\",\"sys_kubernetes\":{\"container\":{\"name\":\"suggester-laesekompas-webservice-container\"},\"labels\":{\"app\":{\"dbc\":{\"dk/release\":\"1\",\"dk/team\":\"os-team\"},\"kubernetes\":{\"io/version\":\"46\",\"io/part-of\":\"laesekompas\",\"io/name\":\"suggester-laesekompas-webservice\",\"io/component\":\"pod\"}},\"pod-template-hash\":\"5b884c8645\",\"network-policy-http-incoming\":\"yes\",\"network-policy-solr7-outgoing\":\"yes\"},\"namespace\":\"os-externals\",\"pod\":{\"name\":\"suggester-laesekompas-webservice-1-deploy-5b884c8645-v9fqh\",\"uid\":\"b6d98147-aedb-11e9-9183-48df371ca910\"},\"replicaset\":{\"name\":\"suggester-laesekompas-webservice-1-deploy-5b884c8645\"}},\"sys_taskid\":\"os-externals/suggester-laesekompas-webservice-1-deploy-5b884c8645-v9fqh\",\"timestamp\":\"2019-08-27T07:03:44.692+00:00\"}";


    private final Environment MOCK_ENVIRONMENT;
    public SuggestFilterTest() {
        try {
            MOCK_ENVIRONMENT = new Environment();
            Recorder.createModuleHandler(MOCK_ENVIRONMENT);
            final String testjs = "suggestfilter.js";
            InputStream js = getClass().getClassLoader().getResourceAsStream(testjs);
            MOCK_ENVIRONMENT.eval(new InputStreamReader(js), testjs);
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    @Test(timeout = 2_000L)
    public void testSuggest() throws Exception {
        System.out.println("testQuery");

        LogLine logLine = LogLine.mappingScript(SUGGEST_LINE, MOCK_ENVIRONMENT);
        assertThat(logLine, is(notNullValue()));
        assertThat(logLine.isValid(), is(true));
        assertThat(logLine.getQuery(), notNullValue());
        assertThat(logLine.getQuery(), comparesEqualTo("/suggest?query=foo"));
    }

    @Test(timeout = 2_000L)
    public void testSearch() throws Exception {
        System.out.println("testQuery");

        LogLine logLine = LogLine.mappingScript(SEARCH_LINE, MOCK_ENVIRONMENT);
        assertThat(logLine, is(notNullValue()));
        assertThat(logLine.isValid(), is(true));
        assertThat(logLine.getQuery(), notNullValue());
        assertThat(logLine.getQuery(), comparesEqualTo("/search?query=london&field=&rows=10"));
    }
}
