package org.openmrs.module.addresshierarchy.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyService;

/**
 * The Class AddressHierarchyServlet which is called when the page addresshierarchyTree.jsp to feed the json data to build the tree.
 */
public class AddressHierarchyServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	/**
 	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	       throws ServletException, IOException {
	     doPost(req, resp);
	   }
	  
	/**
	 * Method writes the json data to the page which is used as the feed data to build the javascript tree
   	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   	 */
   	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	       throws ServletException, IOException {
		 resp.setHeader("Cache-Control","no-cache");
	     resp.setContentType("application/x-json");
	     PrintWriter writer = resp.getWriter();
	     String x = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).getCompleteLocations();
	     writer.println(x);
	     writer.close();
	   }

}
