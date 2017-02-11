/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package legal.documentassembly.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import legal.documentassembly.EnvironmentProperties;


public class MyContextListener implements ServletContextListener {
    	@Override
	public void contextDestroyed(ServletContextEvent event) {
		System.out.println("ServletContextListener destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
            EnvironmentProperties.initialize(event.getServletContext().getRealPath("/WEB-INF/classes/"));

		System.out.println("ServletContextListener started");
	}
}
