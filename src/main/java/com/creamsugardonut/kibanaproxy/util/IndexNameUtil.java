package com.creamsugardonut.kibanaproxy.util;

import java.util.List;

/**
 * @author lks21c
 */
public class IndexNameUtil {
    public static String getIndexName(List<String> idl) {
        String indexName = idl.get(0).substring(0,idl.get(0).lastIndexOf("_")) + "*";
        System.out.println("indexName = " + indexName);
        return indexName;
    }
}
