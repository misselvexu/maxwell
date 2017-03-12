package com.zendesk.maxwell.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.codahale.metrics.servlets.PingServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


public class MaxwellServer {
	public MaxwellServer(int port, MetricRegistry metricRegistry, HealthCheckRegistry healthCheckRegistry) {
		MaxwellServerWorker maxwellServerWorker = new MaxwellServerWorker(port, metricRegistry, healthCheckRegistry);
		new Thread(maxwellServerWorker).start();
	}
}

class MaxwellServerWorker implements Runnable {

	private int port;
	private MetricRegistry metricRegistry;
	private HealthCheckRegistry healthCheckRegistry;
	private Server server;

	public MaxwellServerWorker(int port, MetricRegistry metricRegistry, HealthCheckRegistry healthCheckRegistry) {
		this.port = port;
		this.metricRegistry = metricRegistry;
		this.healthCheckRegistry = healthCheckRegistry;
	}

	public void startServer() throws Exception {
		this.server = new Server(this.port);

		ServletContextHandler handler = new ServletContextHandler(this.server, "/");
		// TODO: there is a way to wire these up automagically via the AdminServlet, but it escapes me right now
		handler.addServlet(new ServletHolder(new MetricsServlet(this.metricRegistry)), "/metrics");
		handler.addServlet(new ServletHolder(new HealthCheckServlet(this.healthCheckRegistry)), "/healthcheck");
		handler.addServlet(new ServletHolder(new PingServlet()), "/ping");

		this.server.start();
		this.server.join();
	}

	@Override
	public void run() {
		try {
			startServer();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}