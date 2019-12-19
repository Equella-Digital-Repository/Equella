/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.dispatcher;

import com.tle.common.URLUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public final class RemappedRequest extends HttpServletRequestWrapper {
  private String contextPath;
  private String servletPath;
  private String pathInfo;
  private String requestURI;
  private final HttpServletRequest wrapped;
  private ServletInputStream servletInputStream;

  private RemappedRequest(
      HttpServletRequest request,
      String context,
      String servletPath,
      String pathInfo,
      ServletInputStream servletInputStream) {
    super(request);
    this.wrapped = request;
    this.contextPath = context;
    this.servletPath = servletPath;
    this.pathInfo = pathInfo;
    this.servletInputStream = servletInputStream;
    setupURI();
  }

  @SuppressWarnings("nls")
  private void setupURI() {
    // Note that this will not give us an exact match for the original
    // Request URI since plus symbols will be instead be reconstructed as
    // %20's. Generating correct URLs isn't a bad thing though, right?
    this.requestURI =
        URLUtils.urlEncode(contextPath + servletPath + (pathInfo != null ? pathInfo : ""), false);
  }

  @Override
  public String getContextPath() {
    return contextPath;
  }

  @Override
  public String getPathInfo() {
    return pathInfo;
  }

  @Override
  public String getServletPath() {
    return servletPath;
  }

  @Override
  public String getRequestURI() {
    return requestURI;
  }

  @Override
  public ServletInputStream getInputStream() {
    return servletInputStream;
  }

  @Override
  public void setAttribute(String name, Object o) {
    if (name.equals("org.apache.catalina.core.DISPATCHER_REQUEST_PATH")) // $NON-NLS-1$
    {
      try {
        servletPath = URLDecoder.decode((String) o, "UTF-8"); // $NON-NLS-1$
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException();
      }
      pathInfo = null;
      setupURI();
    }
    super.setAttribute(name, o);
  }

  public HttpServletRequest getWrapped() {
    return wrapped;
  }

  public static HttpServletRequest wrap(
      HttpServletRequest request, String context, String servletPath, String pathInfo)
      throws IOException {
    if (request instanceof RemappedRequest) {
      RemappedRequest orig = (RemappedRequest) request;
      orig.servletInputStream = request.getInputStream();
      orig.contextPath = context;
      orig.pathInfo = pathInfo;
      orig.servletPath = servletPath;
      orig.setupURI();
      return orig;
    } else {
      return new RemappedRequest(request, context, servletPath, pathInfo, request.getInputStream());
    }
  }
}
