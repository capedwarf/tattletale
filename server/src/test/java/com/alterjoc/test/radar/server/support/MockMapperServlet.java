package com.alterjoc.test.radar.server.support;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Mock mapper servlet.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MockMapperServlet extends HttpServlet
{
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      handle(req, resp);
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      handle(req, resp);
   }

   protected void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      resp.getWriter().write("OK");
   }
}
