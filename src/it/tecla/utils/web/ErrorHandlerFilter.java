package it.tecla.utils.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.tecla.utils.properties.PropertiesFactory;

/**
 * Filtro HTTP che permette di gestire gli errori nascondendoli o mostrandoli
 * @author Nicolo' Chieffo
 *
 */
public class ErrorHandlerFilter implements Filter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlerFilter.class);
	
	private boolean printError;

	@Override
	public void init(FilterConfig config) throws ServletException {
		printError = PropertiesFactory.getInstance().getProperty("errorHandler.printErrors", "true").equalsIgnoreCase("true");
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		try {
			
			chain.doFilter(request, response);
			
		} catch (Exception ex) {
			
			int status = response.getStatus();
			
			if (status == 200) {
				// se lo status è 200 vuol dire che l'errore non è stato ancora riconosciuto, devo gestire il caso
				
				if (!response.isCommitted()) {
					response.reset();
					response.setContentType("text/plain");
					response.setCharacterEncoding(request.getCharacterEncoding());
					response.setStatus(500);
				}

				String errorUUID = UUID.randomUUID().toString();
				
				String errorMessage = getErrorMessage(request, errorUUID);
				
				LOGGER.error(errorMessage + " | ", ex);
				
				PrintWriter writer = response.getWriter();
				try {
					writer.write(new Date().toString());
					writer.write("\n");
					
					if (printError) {
						writer.write(errorMessage);
						writer.write("\n");
						writer.write("\n");
						ex.printStackTrace(writer);
					} else {
						writer.write("An unhandled error has occurred\nERROR_UUID=");
						writer.write(errorUUID);
					}
					
				} finally {
					writer.close();
				}
			}
		}
	}

	private String getErrorMessage(HttpServletRequest request, String errorUUID) {
		
		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append("An unhandled error (ERROR_UUID=");
		errorMessage.append(errorUUID);
		errorMessage.append(") has occurred and was generated by this request: ");
		
		// METHOD
		errorMessage.append(request.getMethod());
		errorMessage.append(" ");
		
		// URL
		errorMessage.append(request.getServletPath());
		errorMessage.append(request.getPathInfo());
		if (request.getQueryString() != null) {
			errorMessage.append("?");
			errorMessage.append(request.getQueryString());
		}
		
		// CONTENT TYPE
		if (request.getContentType() != null) {
			errorMessage.append(" (Content-Type: ");
			errorMessage.append(request.getContentType());
			errorMessage.append(")");
		}
		
		// REMOTE USER
		if (request.getRemoteUser() != null) {
			errorMessage.append(" (RemoteUser: ");
			errorMessage.append(request.getRemoteUser());
			errorMessage.append(")");
		}
		
		return errorMessage.toString();
	}

	@Override
	public void destroy() {
		// nothing to destroy
	}

}
