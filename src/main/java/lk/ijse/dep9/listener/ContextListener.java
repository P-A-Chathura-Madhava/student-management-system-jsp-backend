package lk.ijse.dep9.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lk.ijse.dep9.db.ConnectionPool;
import org.apache.commons.dbcp2.BasicDataSource;

/* This is not usable at Line 27 */

@WebListener
public class ContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
//        ConnectionPool dbPool = new ConnectionPool(10);   /* Remove at Line 25 */
        BasicDataSource dbPool = new BasicDataSource();
        dbPool.setUrl("jdbc:mysql://localhost:3306/sms");
        dbPool.setUsername("root");
        dbPool.setPassword("123@Ctech");
        dbPool.setDriverClassName("com.mysql.cj.jdbc.Driver");

        dbPool.setInitialSize(10);
        dbPool.setMaxTotal(20);

        sce.getServletContext().setAttribute("pool", dbPool);
    }
}
