/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.metrics.MetricCollector;
import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.solr.metrics.MetricDataParser;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

/**
 * Created by bhuvnesh.kumar on 5/23/18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtils.class)
@PowerMockIgnore("javax.net.ssl.*")

public class MetricDataParserTest {

    private MonitorContextConfiguration monitorContextConfiguration = new MonitorContextConfiguration("SolrMonitor", "Custom Metrics|Solr|",Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));

    @Test
    public void TestSystemMetricsWithoutAlias() throws Exception {
        monitorContextConfiguration.setConfigYml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/conf/config.yml");
        monitorContextConfiguration.setMetricXml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/xml/system-no-alias.xml", Stat.Stats.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(new FileInputStream("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/json/system-exact.json"), JsonNode.class);
        MetricDataParser metricDataParser = new MetricDataParser(monitorContextConfiguration);
        String serverName = "Server 1";
        Map<String, Metric> result =  metricDataParser.parseNodeData(getStat(), node, serverName, getPropertiesMap() );
//        for(String s: result.keySet()){
//            System.out.println("expectedValueMap.put(\""+ result.get(s).getMetricPath()+"\", \""+result.get(s).getMetricValue()+"\");");
//        }
        Map<String, String> expectedValueMap = initExpectedSystemMetricsWithoutAlias();

        validateMetricsList(result, expectedValueMap);

    }
        @Test
    public void TestSystemMetrics() throws Exception {

            monitorContextConfiguration.setConfigYml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/conf/config.yml");
        monitorContextConfiguration.setMetricXml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/xml/system-exact.xml", Stat.Stats.class);


        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(new FileInputStream("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/json/system-exact.json"), JsonNode.class);
        MetricDataParser metricDataParser = new MetricDataParser(monitorContextConfiguration);
        String serverName = "Server 1";
        Map<String, Metric> result =  metricDataParser.parseNodeData(getStat(), node, serverName, getPropertiesMap() );
            Map<String, String> expectedValueMap = initExpectedSystemMetrics();

        validateMetricsList(result, expectedValueMap);
    }

    private void validateMetricsList(Map<String, Metric> mapOfMetrics,Map<String, String> expectedValueMap){

        for(String prefix: mapOfMetrics.keySet()){

            String actualValue = mapOfMetrics.get(prefix).getMetricValue();
            String metricPath = mapOfMetrics.get(prefix).getMetricPath();
            if(expectedValueMap.containsKey(metricPath)){
                String expectedValue = expectedValueMap.get(metricPath);
                Assert.assertEquals("The value of metric " + metricPath + " failed", expectedValue, actualValue);
                expectedValueMap.remove(metricPath);
            }
            else {
                Assert.fail("Unknown Metric " + metricPath);
            }

        }
    }

    private Map<String, String> getPropertiesMap(){
        Map<String, String> properties = new LinkedHashMap<String, String>();
        properties.put("system", "System");
        return properties;
    }

    private Stat getStat(){
        Stat.Stats metricConfiguration = (Stat.Stats) monitorContextConfiguration.getMetricsXml();

        Stat stat = metricConfiguration.getStats()[0];

        return stat;
    }

    private Map<String, String> initExpectedSystemMetrics(){
        Map<String, String> expectedValueMap = new HashMap<String, String>();

        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Committed Virtual Memory Size", "6.57563648E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Process CPU Load", "0.4583333333333333");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Total Physical Memory Size", "1.7179869184E10");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Process CPU Time", "4.52325456E11");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Total Swap Space Size", "5.36870912E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Available Processors", "8.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Free Physical Memory Size", "2.1737472E7");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Free Swap Space Size", "1.200881664E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Max File Descriptor Count", "10240.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Open File Descriptor Count", "205.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|System Load Average", "2.455078125");

        return expectedValueMap;
    }

    private Map<String, String> initExpectedSystemMetricsWithoutAlias(){
        Map<String, String> expectedValueMap = new HashMap<String, String>();

        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|openFileDescriptorCount", "205.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|totalPhysicalMemorySize", "1.7179869184E10");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|availableProcessors", "8.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|systemLoadAverage", "2.455078125");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|freePhysicalMemorySize", "2.1737472E7");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|processCpuLoad", "0.4583333333333333");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|maxFileDescriptorCount", "10240.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|committedVirtualMemorySize", "6.57563648E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|totalSwapSpaceSize", "5.36870912E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|freeSwapSpaceSize", "1.200881664E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|processCpuTime", "4.52325456E11");
        return expectedValueMap;
    }

}
