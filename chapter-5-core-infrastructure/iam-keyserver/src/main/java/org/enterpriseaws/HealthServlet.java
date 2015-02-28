package org.enterpriseaws;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class HealthServlet extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    response.getWriter().write("Key Service is healthy!");
  }
}