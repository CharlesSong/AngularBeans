package org.espritjug.angularBridge.rest;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.espritjug.angularBridge.Util;
import org.espritjug.angularBridge.context.BeanLocator;
import org.espritjug.angularBridge.remote.RemoteInvoker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Path("/invoke")
@ApplicationScoped
public class RestInvoker implements Serializable {

	@Inject
	RemoteInvoker remoteInvoker;

	@Inject
	BeanLocator locator;

	@GET
	@Path("/service/{bean}/{method}/json")
	@Produces(MediaType.APPLICATION_JSON)
	public Object doSersvice(@PathParam("bean") String beanName,
			@PathParam("method") String method,
			@QueryParam("params") String params) {

		
		JsonObject paramsObj =Util.parse(params); 

		String UID = paramsObj.get("sessionUID").getAsString();

		Object o = remoteInvoker.invoke(locator.lookup(beanName, UID), method,
				paramsObj, UID);

		return o;

	}

}