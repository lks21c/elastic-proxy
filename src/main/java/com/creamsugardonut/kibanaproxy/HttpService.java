package com.creamsugardonut.kibanaproxy;

import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HttpService {

    private CloseableHttpClient client = HttpClientBuilder.create().build();

    public HttpResponse executeHttpRequest(HttpMethod requestType, String url, StringEntity entity) throws IOException, MethodNotSupportedException {
        HttpResponse httpResponse = null;

        if (entity != null) {
            entity.setContentType("application/json");
        }
        switch (requestType) {
            case POST:

                HttpPost post = new HttpPost(url);

                post.setEntity(entity);
                httpResponse = client.execute(post);
                break;
            case GET:

                HttpGet httpGet = new HttpGet(url);

                httpResponse = client.execute(httpGet);
                break;
            case DELETE:

                HttpDelete httpDelete = new HttpDelete(url);

                httpResponse = client.execute(httpDelete);
                break;
            case PUT:

                HttpPut httpput = new HttpPut(url);

                httpput.setEntity(entity);
                httpResponse = client.execute(httpput);
                break;
            default:
                throw new MethodNotSupportedException(requestType.toString());
        }

        return httpResponse;
    }
}
