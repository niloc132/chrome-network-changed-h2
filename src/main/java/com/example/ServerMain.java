package com.example;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.CrossOriginHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.Set;

/**
 * Starts a webserver on three ports, serving a simple static app that attempts to load a streaming payload
 */
public class ServerMain {
    public static void main(String[] args) throws Exception {
        Server server = new Server();

        // Host on both http 1.1 and 2, both with ssl, but different ports
        addH1ConnectorCleartext(server);
        addH1ConnectorSsl(server);
        addH2Connector(server);

        // Apply cors to everything
        CrossOriginHandler crossOriginHandler = new CrossOriginHandler();
        crossOriginHandler.setAllowedOriginPatterns(Set.of("*"));

        ServletContextHandler ctx = new ServletContextHandler();
        ctx.setServer(server);
        ctx.setContextPath("/");

        // Serve a simple stream
        ServletHolder streamHolder = ctx.addServlet(StreamingServlet.class, "/stream");
        streamHolder.setAsyncSupported(true);

        // Serve plain text files
        ServletHolder defaultServletHolder = ctx.addServlet(DefaultServlet.class, "/");
        defaultServletHolder.setInitParameter("resourceBase", "src/main/resources");

        JakartaWebSocketServletContainerInitializer.configure(ctx, (servletContext, serverContainer) -> {
            serverContainer.addEndpoint(StreamingWebsocket.class);
        });

        crossOriginHandler.setHandler(ctx);
        server.setHandler(crossOriginHandler);

        server.start();

        server.join();
    }

    private static void addH2Connector(Server server) {
        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer(false));
        final HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpConfig);
        final ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol(h2.getProtocol());
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(System.getProperty("h2certpath"));
        sslContextFactory.setKeyStorePassword("secret");
        final SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

        ServerConnector h2Connector = new ServerConnector(server, tls, alpn, h2);
        h2Connector.setPort(8082);
        server.addConnector(h2Connector);
    }

    private static void addH1ConnectorSsl(Server server) {
        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer(false));
        HttpConnectionFactory h1 = new HttpConnectionFactory(httpConfig);
        final ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol(h1.getProtocol());
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(System.getProperty("h1certpath"));
        sslContextFactory.setKeyStorePassword("secret");
        final SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

        ServerConnector h1Connector = new ServerConnector(server, tls, alpn, h1);
        h1Connector.setPort(8081);
        server.addConnector(h1Connector);
    }

    private static void addH1ConnectorCleartext(Server server) {
        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer(false));
        HttpConnectionFactory h1 = new HttpConnectionFactory(httpConfig);
        final ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol(h1.getProtocol());

        ServerConnector h1Connector = new ServerConnector(server, h1);
        h1Connector.setPort(8080);
        server.addConnector(h1Connector);
    }

}
