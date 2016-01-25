package com.infotech.isg.it.fake.jiring;

import com.infotech.isg.proxy.jiring.JiringProxy;
import com.infotech.isg.proxy.jiring.TCSResponse;
import com.infotech.isg.proxy.jiring.TCSRequest;
import com.infotech.isg.proxy.jiring.TCSConnection;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * fake web service for Jiring, used for integration tests
 * annotated as spring component so that app properties can be used
 *
 * @author Sevak Gharibian
 */
@Component
public class JiringFake {

    private static final Logger LOG = LoggerFactory.getLogger(JiringFake.class);

    private JiringProxy jiringProxy;
    private HttpServer server;

    @Value("${jiring.url}")
    private String url;

    public void setJiringProxyImpl(JiringProxy jiringProxy) {
        this.jiringProxy = jiringProxy;
    }

    public void start() {
        try {
            if (server == null) {
                int port = new URL(url).getPort();
                server = HttpServer.create(new InetSocketAddress(port), 0);
                String path = new URL(url).getPath();
                server.createContext(path, new TCSHandler());
                server.setExecutor(null);
                server.start();
                LOG.debug("[jiring fake server] started at port:{}, path: {}", port, path);
            }
        } catch (MalformedURLException e) {
            LOG.error("error while starting jiring fake server", e);
        } catch (IOException e) {
            LOG.error("error while starting jiring fake server", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
            server = null;
            LOG.debug("[jiring fake server] stopped");
        }
    }

    private class TCSHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            StringBuilder sb = new StringBuilder();
            Map<String, List<String>> headers = t.getRequestHeaders();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                sb.append(String.format("%s: %s\n", entry.getKey(), entry.getValue()));
            }
            LOG.debug("[jiring fake server] request headers: \n{}", sb.toString());
            BufferedReader reader = new BufferedReader(new InputStreamReader(t.getRequestBody()));
            String line = null;
            sb.setLength(0);
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String data = sb.toString();
            LOG.debug("[jiring fake server] request data: \n{}", data);

            TCSRequest request = TCSConnection.unmarshal(data, TCSRequest.class);
            TCSResponse response = null;
            if (request.getFunctionName().equals("SALESREQUEST")) {
                LOG.debug("[jiring fake server] SALESREQUEST detected");
                response = jiringProxy.salesRequest(request.getFunctionParam6(), Integer.parseInt(request.getFunctionParam2()), 
                                                    "brand-Id", request.getFunctionParam7());
            } else if (request.getFunctionName().equals("SALESREQUESTEXEC")) {
                LOG.debug("[jiring fake server] SALESREQUESTEXEC detected");
                response = jiringProxy.salesRequestExec(request.getFunctionParam1(), false);
            } else if (request.getFunctionName().equals("BALANCE")) {
                LOG.debug("[jiring fake server] BALANCE detected");
                response = jiringProxy.balance();
            } else {
                // function not defined
                LOG.debug("[jiring fake server] undefined function name");
            }

            data = TCSConnection.marshal(response, TCSResponse.class);
            t.sendResponseHeaders(200, data.length());
            headers = t.getResponseHeaders();
            sb.setLength(0);
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                sb.append(String.format("%s: %s\n", entry.getKey(), entry.getValue()));
            }
            LOG.debug("[jiring fake server] response headers: \n{}", sb.toString());
            LOG.debug("[jiring fake server] response data: \n{}", data);

            OutputStream writer = t.getResponseBody();
            writer.write(data.getBytes());
            writer.close();
        }
    }
}
