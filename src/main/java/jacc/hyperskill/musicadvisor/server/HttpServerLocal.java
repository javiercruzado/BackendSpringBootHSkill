package jacc.hyperskill.musicadvisor.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class HttpServerLocal {


    private static HttpServer serverLocal;

    public static void startLocalServer(IAuthSession authSession) throws IOException {

        if (serverLocal == null) {
            serverLocal = HttpServer.create();
            serverLocal.bind(new InetSocketAddress(8080), 0);
            serverLocal.createContext("/callback",
                    httpExchange -> {
                        try {
                            URI requestURI = httpExchange.getRequestURI();
                            Map<String, String> queryParams = parseQuery(requestURI.getQuery());

                            String code = queryParams.get("code");

                            String responseMessage;

                            if (code != null) {
                                responseMessage = "Got the code. Return back to your program.";
                                authSession.getAccessToken(code);
                            } else {
                                responseMessage = "Authorization code not found. Try again.";
                            }

                            httpExchange.sendResponseHeaders(200, responseMessage.length());
                            httpExchange.getResponseBody().write(responseMessage.getBytes());
                            httpExchange.getResponseBody().close();

                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    });
            serverLocal.start();
        }
    }

    public static void stopLocalServer() {
        if (serverLocal != null) {
            serverLocal.stop(1);
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                map.put(parts[0], parts[1]);
            }
        }
        return map;
    }
}
