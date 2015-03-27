/*
 * AngularBeans, CDI-AngularJS bridge 
 *
 * Copyright (c) 2014, Bessem Hmidi. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 */

/**
 @author Bessem Hmidi
 */
package angularBeans.realtime;

import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.projectodd.sockjs.SockJsConnection;
import org.projectodd.sockjs.SockJsServer;
import org.projectodd.sockjs.servlet.SockJsServlet;

import angularBeans.context.NGSessionScopeContext;
import angularBeans.util.AngularBeansUtil;
import angularBeans.wsocket.annotations.WSocketReceiveEvent;
import angularBeans.wsocket.annotations.WSocketSessionCloseEvent;
import angularBeans.wsocket.annotations.WSocketSessionReadyEvent;

import com.google.gson.JsonObject;

@WebServlet(loadOnStartup = 1, asyncSupported = true, urlPatterns = "/ws-service/*")
public class RealTimeEndPoint extends SockJsServlet {

	@Inject
	@WSocketReceiveEvent
	private Event<WSocketEvent> receiveEvents;

	@Inject
	@WSocketSessionReadyEvent
	private Event<WSocketEvent> sessionOpenEvent;

	@Inject
	@WSocketSessionCloseEvent
	private Event<WSocketEvent> sessionCloseEvent;

	@Inject
	@WSocketErrorEvent
	private Event<WSocketEvent> errorEvent;

	@Inject
	GlobalConnectionHolder globalConnectionHolder;

	// @OnClose
	// public void onclose(Session session) {
	// sessionCloseEvent.fire(new WSocketEvent(session, null));
	// Logger.getLogger("AngularBeans").info("ws-channel closed");
	// }
	//
	// @OnError
	// public void onError(Session session, Throwable error) {
	// // errorEvent.fire(new WSocketEvent(session,
	// // Util.parse(Util.getJson(error))));
	// error.printStackTrace();
	// }
	//

	@Override
	public void init() throws ServletException {
		SockJsServer echoServer = new SockJsServer();
		// Various options can be set on the server, such as:
		// echoServer.options.responseLimit = 4 * 1024;

		// onConnection is the main entry point for handling SockJS connections
		echoServer.onConnection(new SockJsServer.OnConnectionHandler() {
			@Override
			public void handle(final SockJsConnection connection) {
				getServletContext().log("SockJS client connected");
				globalConnectionHolder.getAllConnections().add(connection);

				// onData gets called when a client sends data to the server
				connection.onData(new SockJsConnection.OnDataHandler() {
					@Override
					public void handle(String message) {

						JsonObject jObj = AngularBeansUtil.parse(message);
						String UID = jObj.get("session").getAsString();

						WSocketEvent ev = new WSocketEvent(connection,
								AngularBeansUtil.parse(message));

						ev.setConnection(connection);
						NGSessionScopeContext.setCurrentContext(UID);

						String service = jObj.get("service").getAsString();

						if (service.equals("ping")) {

							sessionOpenEvent.fire(ev);
							Logger.getLogger("AngularBeans").info(
									"AngularBeans-client: " + UID);

						} else {

							receiveEvents.fire(ev);
						}

						// connection.write(message);
					}
				});

				// onClose gets called when a client disconnects
				connection.onClose(new SockJsConnection.OnCloseHandler() {
					@Override
					public void handle() {
						// globalConnectionHolder.getAllConnections().add(connection);
						getServletContext().log("SockJS client disconnected");
					}
				});
			}
		});

		setServer(echoServer);
		// Don't forget to call super.init() to wire everything up
		super.init();

	}

}