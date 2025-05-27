package com.cathayunitedbank.currencyconverter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * A Spring component that acts as a Servlet Filter to log incoming HTTP requests and outgoing HTTP responses.
 * This filter is crucial for debugging, auditing, and monitoring API interactions by capturing
 * request methods, URIs, headers, and the full request and response bodies.
 *
 * It utilizes `ContentCachingRequestWrapper` and `ContentCachingResponseWrapper` to allow
 * repeated reading of the request and response streams, which is necessary for logging
 * their content without interfering with downstream processing.
 *
 * Key Functionality:
 * - **Request Logging**: Captures and logs the HTTP method, URI (including query parameters),
 * all request headers, and the complete request body (payload).
 * - **Response Logging**: Captures and logs the HTTP status code, all response headers,
 * and the complete response body (payload).
 * - **Non-intrusive**: Wraps the original `HttpServletRequest` and `HttpServletResponse`
 * to buffer their content, ensuring that other filters or controllers can still
 * read the streams after this filter has processed them.
 * - **Ordered Execution**: Configured with `@Order(1)` to ensure it runs early in the
 * Servlet filter chain, typically before other filters that might consume the request body.
 *
 * This filter provides valuable insight into the data flowing in and out of the application,
 * making it an essential tool for troubleshooting and understanding API behavior.
 */
@Component
@Order(1)
public class LoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);

        try {
            // Log Request
            logRequest(requestWrapper);

            chain.doFilter(requestWrapper, responseWrapper);

            // Log Response
            logResponse(responseWrapper);

        } finally {
            // Important: copy content to actual response stream before it's sent to the client
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * Logs the details of an incoming HTTP request, including its method, URI, headers, and payload.
     *
     * @param request The wrapped HTTP servlet request.
     * @throws UnsupportedEncodingException if the character encoding is not supported.
     */
    private void logRequest(ContentCachingRequestWrapper request) throws UnsupportedEncodingException {
        StringBuilder msg = new StringBuilder();
        msg.append("Request: ");
        msg.append("method=").append(request.getMethod());
        msg.append("; uri=").append(request.getRequestURI());
        if (request.getQueryString() != null) {
            msg.append("?").append(request.getQueryString());
        }
        msg.append("; headers=").append(getRequestHeaders(request));
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String requestBody = new String(content, request.getCharacterEncoding());
            msg.append("; payload=").append(requestBody);
        }
        LOGGER.info(msg.toString());
    }

    /**
     * Logs the details of an outgoing HTTP response, including its status, headers, and payload.
     *
     * @param response The wrapped HTTP servlet response.
     * @throws IOException if an I/O error occurs.
     */
    private void logResponse(ContentCachingResponseWrapper response) throws IOException {
        StringBuilder msg = new StringBuilder();
        msg.append("Response: ");
        msg.append("status=").append(response.getStatus());
        msg.append("; headers=").append(getResponseHeaders(response));
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String responseBody = new String(content, response.getCharacterEncoding());
            msg.append("; payload=").append(responseBody);
        }
        LOGGER.info(msg.toString());
    }

    /**
     * Extracts all header names and values from an HTTP servlet request.
     *
     * @param request The HTTP servlet request.
     * @return A map where keys are header names and values are header values.
     */
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    /**
     * Extracts all header names and values from an HTTP servlet response.
     *
     * @param response The HTTP servlet response.
     * @return A map where keys are header names and values are header values.
     */
    private Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        response.getHeaderNames().forEach(headerName -> headers.put(headerName, response.getHeader(headerName)));
        return headers;
    }
}