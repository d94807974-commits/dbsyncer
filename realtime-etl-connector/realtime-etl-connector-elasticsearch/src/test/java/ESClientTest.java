/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.realtimeetl.connector.elasticsearch.config.ESConfig;
import org.realtimeetl.connector.elasticsearch.util.ESUtil;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ESTestExample
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2023-11-25 23:10
 */
public class ESClientTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private RestHighLevelClient client;
    private String indexName = "test_index";
    private String type = "_doc";

    @Before
    public void init() {
        ESConfig config = new ESConfig();
        config.setUrl("https://127.0.0.1:9200");
        config.setUsername("testuser");
        config.setPassword("123456");
        client = ESUtil.getConnection(config);
        try {
            boolean ret = client.ping(RequestOptions.DEFAULT);
            logger.info("es ping ret:{}", ret);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @After
    public void close() {
        ESUtil.close(client);
    }

    @Test
    public void isExistsIndexTest() {
        try {
            GetIndexRequest request = new GetIndexRequest(indexName);
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
            logger.info("es exist index:{}", exists);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void createIndexTest() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 0));
        // ICALLYIndexField
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            // YesNoAllows a free extension of properties below objects, (Valuetrue/false/strict,Defaulttrue）
            builder.field("dynamic", "strict");
            builder.startObject("properties");
            {
                builder.startObject("id");
                {
                    builder.field("type", "integer");
                }
                builder.endObject();
            }
            {
                builder.startObject("name");
                {
                    builder.field("type", "text");
                }
                builder.endObject();
            }
            {
                builder.startObject("content");
                {
                    builder.field("type", "keyword");
                    // StringLengthLimit (forkeyword），keywordTypeIt's too long. It doesn't mean much.IndexIt will.Disable，The data is not accessible.Default value256
                    builder.field("ignore_above", 256);
                }
                builder.endObject();
            }
            {
                builder.startObject("tags");
                {
                    builder.field("type", "long");
                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
        // 7VersionStart taking it off.type
        request.mapping(builder);
        // Create HereIndexStructure
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        // InstructionsYesNoAllNodesBothConfirmRequest
        boolean acknowledged = response.isAcknowledged();
        // InstructionsYesNoBefore overtime 13IndexEach of themminSnippetsStartIt's necessary.minNumber of copies of films
        boolean shardsAcknowledged = response.isShardsAcknowledged();
        if (acknowledged || shardsAcknowledged) {
            logger.info("Creating index succeeded! Index name is {}", indexName);
        }
    }

    @Test
    public void getIndexTest() throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        GetIndexResponse indexResponse = client.indices().get(request, RequestOptions.DEFAULT);
        // AccessIndex
        String[] indices = indexResponse.getIndices();
        for (String index : indices) {
            logger.info(index);
        }

        // FieldInfo
        MappingMetadata mappingMetaData = indexResponse.getMappings().get(indexName);
        Map<String, Object> propertiesMap = mappingMetaData.getSourceAsMap();
        Map<String, Map> properties = (Map<String, Map>) propertiesMap.get(ESUtil.PROPERTIES);
        logger.info(properties.toString());
    }

    @Test
    public void deleteIndexTest() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        logger.info(response.toString());
    }

    @Test
    public void pushTest() throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 2);
        map.put("name", "Liu Bei Yu Zhang Fei");
        map.put("content", "Peach Garden Yuyi");
        map.put("tags", new Long[]{200L});
        map.put("update_time", System.currentTimeMillis());
        IndexRequest request = new IndexRequest(indexName, type, "2");
        request.source(map, XContentType.JSON);

        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        RestStatus status = indexResponse.status();
        logger.info(status.name());
    }

    @Test
    public void bulkPushTest() throws IOException {
        BulkRequest request = new BulkRequest();
        Map<String, Object> m1 = new HashMap<>();
        m1.put("id", 1);
        m1.put("name", "Liu Bei Yu Zhang Fei");
        m1.put("content", "Peach Garden Yuyi");
        m1.put("tags", new Long[]{200L});
        m1.put("update_time", System.currentTimeMillis());
        IndexRequest r1 = new IndexRequest(indexName, type, "1");
        r1.source(m1, XContentType.JSON);
        request.add(r1);

        Map<String, Object> m2 = new HashMap<>();
        m2.put("id", 2);
        m2.put("name", "Tsao!");
        m2.put("content", "Fire");
        m2.put("tags", new Long[]{200L, 300L});
        m2.put("update_time", System.currentTimeMillis());
        IndexRequest r2 = new IndexRequest(indexName, type, "2");
        r2.source(m2, XContentType.JSON);
        request.add(r2);

        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        BulkItemResponse[] items = response.getItems();
        for (BulkItemResponse r : items) {
            logger.info(r.status().name());
        }
        logger.info(response.toString());
    }

    @Test
    public void deleteTest() throws IOException {
        DeleteRequest request = new DeleteRequest(indexName, type, "2");
        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
        logger.info(delete.toString());
    }

    @Test
    public void bulkDeleteTest() throws IOException {
        BulkRequest request = new BulkRequest();
        request.add(new DeleteRequest(indexName, type, "1"));
        request.add(new DeleteRequest(indexName, type, "2"));

        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        BulkItemResponse[] items = response.getItems();
        for (BulkItemResponse r : items) {
            logger.info(r.status().name());
        }
        logger.info(response.toString());
    }

    @Test
    public void searchTest() throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery("tags", Arrays.asList(200L, 300L))));
        sourceBuilder.from(0);
        sourceBuilder.size(10);
        sourceBuilder.timeout(TimeValue.timeValueMillis(10));
        sourceBuilder.fetchSource(new String[]{"id", "name"}, null);

        SearchRequest rq = new SearchRequest(new String[]{indexName}, sourceBuilder);
        SearchResponse searchResponse = client.search(rq, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits().value;
        logger.info("result:{}", totalHits);
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            logger.info(hit.getSourceAsMap().toString());
        }
    }

    @Test
    public void getCountTest() throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // CancelLimitsBackNumber of query lines10000
        sourceBuilder.trackTotalHits(true);
        SearchRequest request = new SearchRequest(new String[]{indexName}, sourceBuilder);
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits().value;
        logger.info("result:{}", totalHits);
    }
}