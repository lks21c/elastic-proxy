package com.creamsugardonut.kibanaproxy.service;

import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ParsingService {

    public Map<String,Object> parseXContent(String str){
        return XContentHelper.convertToMap(XContentType.JSON.xContent(), str, true);
    }
}
