package fr.liglab.adele.cilia.demo.servlet;

import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AddClientServlet extends HttpServlet {


    private HttpService m_httpService;

    /**
     *
     */
    private static final long serialVersionUID = 5474628886103864909L;

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }


    public void bindHttpService() {
        try {
            m_httpService.registerServlet("/suiviConso", this, null, null);
            m_httpService.registerResources("/suiviConsoRes", "/www", null);

        } catch (ServletException e) {
        } catch (NamespaceException e) {
            e.printStackTrace();
        }
    }

    public void unbindHttpService() {
        m_httpService.unregister("/suiviConso");
        m_httpService.unregister("/suiviConsoRes");

    }


    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.sendRedirect("suiviConsoRes/main.html");
    }

}
