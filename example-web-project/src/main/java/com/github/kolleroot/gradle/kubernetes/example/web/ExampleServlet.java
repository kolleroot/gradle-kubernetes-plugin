package com.github.kolleroot.gradle.kubernetes.example.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A simple servlet example application
 *
 * This example application is used to test the web container deployment
 */
@WebServlet(name = "example-servlet", urlPatterns = {"/*"})
public class ExampleServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.println("<!DOCTYPE html>");
        writer.println("<html>");
        writer.println("    <head>");
        writer.println("        <title>Hallo Welt</title>");
        writer.println("    </head>");
        writer.println("    <body>");
        writer.println("        <h1>Hallo Welt</h1>");
        writer.println("        <p>Ich bin eine Testseite</p>");
        writer.println("    </body>");
        writer.println("</html>");
    }
}
