package util;

import com.sun.net.httpserver.HttpServer;
import httpRequestHandler.*;
import httpRequestHandler.UserHttpHandler;
import httpRequestHandler.VendorHttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
public class ServerMain {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/auth", new UserHttpHandler());
        server.createContext("/auth/cart", new ShoppingCartHttpHandler());
        server.createContext("/transactions", new WalletHttpHandler());
        server.createContext("/wallet", new WalletHttpHandler());
        server.createContext("/payment/online", new WalletHttpHandler());
        server.createContext("/orders", new OrderHttpHandler());
        server.createContext("/delivery/available", new DeliveryHttpHandler());
        server.createContext("/deliveries", new DeliveryHttpHandler());
        server.createContext("/ratings/food/", new OrderHttpHandler());
        server.createContext("/vendors", new VendorHttpHandler());
        server.createContext("/deliveries/history", new DeliveryHttpHandler());
        server.createContext("/restaurants", new RestaurantHandler());
        server.createContext("/favorites", new OrderHttpHandler());
        server.createContext("/ratings", new OrderHttpHandler());
        server.createContext("/items", new VendorHttpHandler());
         server.createContext("/admin", new AdminHttpHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server started on port 8080");
    }
}