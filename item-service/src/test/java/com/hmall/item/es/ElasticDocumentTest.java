package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.utils.CollUtils;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.domain.po.Item;
import com.hmall.item.service.IItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@Slf4j
@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticDocumentTest {

    private RestHighLevelClient client;

    @Autowired
    private IItemService itemService;

    @BeforeEach
    public void setUp() {
        this.client = new RestHighLevelClient(
                RestClient.builder(
                        HttpHost.create("http://192.168.197.101:9200")
                )
        );
    }

    @Test
    public void testIndexDocument() throws IOException {

        // 1.根据id查询商品数据Item
        Item item = itemService.getById(100002644680L);
        // 2.将item封装为ItemDoc
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        // 3.将ItemDoc转为JSON
        String doc = JSONUtil.toJsonStr(itemDoc);
        // 4.创建request对象
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        // 5.准备请求参数
        request.source(doc,XContentType.JSON);
        // 6.发送请求
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    public void testDeleteDocument() throws IOException {

        // 1.准备Request，两个参数，第一个是索引库名，第二个是文档id
        DeleteRequest deleteRequest = new DeleteRequest("items", "100002644680");
        // 6.发送请求
        client.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("items", "100002644680");
        updateRequest.doc(
          "price",58000,
        "commentCount",1
        );
        client.update(updateRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void testBulkDocument() throws IOException {

        // 分页查询商品
        int pageNo = 1;
        int pageSize = 1000;

        while (true) {
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(new Page<Item>(pageNo, pageSize));
            // 非空校验
            List<Item> items = page.getRecords();
            if(CollUtil.isEmpty(items)){
                return;
            }
            log.info("加载第{}页数据，共{}条", pageNo, items.size());
            BulkRequest request = new BulkRequest("items");
            for(Item item : items){
                ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
                String jsonStr = JSONUtil.toJsonStr(itemDoc);
                request.add(
                        new IndexRequest().id(itemDoc.getId()).source(jsonStr, XContentType.JSON)
                );
            }
            // 发送请求
            client.bulk(request, RequestOptions.DEFAULT);
            pageNo++;
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
         if (client != null) {
             client.close();
         }
    }
}
