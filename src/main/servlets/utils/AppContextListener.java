package servlets.utils;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import repositories.DBConnector;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBConnector.close();
    }


}
