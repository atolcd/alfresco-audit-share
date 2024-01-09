/*
 * Copyright (C) 2018 Atol Conseils et Développements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atolcd.alfresco;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Override of the HttpServletRequestWrapper.
 * 
 * Basically, the inputStream can be read only once.
 * 
 * It was therefore impossible to audit data posted by Alfresco. The
 * getInputStream() method is overrided to return a new inputStream for every
 * call. It is an object of this class, which is then sent to the doFilter.
 */
public class RequestWrapper extends HttpServletRequestWrapper {
    // Logger
    private static final Log logger = LogFactory.getLog(RequestWrapper.class);

    private String stringRequest;

    public RequestWrapper(HttpServletRequest request) {
        super(request);
        this.stringRequest = "";
    }

    /**
     * Override of the getInputStream() method.
     * 
     * Return a new inputStream created from the stringRequest rather than
     * return the current inputStream, which may have been already read.
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream inputStream;
        if (!this.stringRequest.isEmpty()) {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stringRequest.getBytes());
            inputStream = new ServletInputStream() {
                @Override
                public int read() throws IOException {
                    try {
                        return byteArrayInputStream.read();
                    } catch (Exception e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(e.getMessage(), e);
                        }
                        return 0;
                    }
                }
                @Override
                public boolean isFinished() {
                  return false;
                }

                @Override
                public boolean isReady() {
                  return false;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                }
            };
        } else
            inputStream = this.getRequest().getInputStream();

        return inputStream;
    }

    public String buildStringContent() {
        try {
            // First read of the request
            InputStream inputStream = this.getRequest().getInputStream();
            if (inputStream != null) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer);
                return writer.toString();
            }
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
        }

        return "";
    }

    public String getStringContent() {
        if (this.stringRequest.isEmpty()) {
            this.stringRequest = this.buildStringContent();
        }
        return this.stringRequest;
    }
}