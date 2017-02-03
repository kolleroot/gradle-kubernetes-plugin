/**
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
