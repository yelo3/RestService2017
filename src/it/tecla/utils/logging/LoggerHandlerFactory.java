package it.tecla.utils.logging;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerHandlerFactory {
	
	public static LoggerHandler create(Class<?> targetClass, Method method, Object...args) {
		
		long startTime = System.currentTimeMillis();
		
		Logger logger;
		String methodSignature;
		boolean hasReturnValue;
		
		// ottengo il logger
		String loggerName;
		if (method != null) {
			loggerName = method.getName();
			StringBuilder sb = new StringBuilder();
			sb.append(method.getName());
			sb.append("(");
			Parameter[] parameters = method.getParameters();
			for (int i=0;i<parameters.length; i++) {
				sb.append(parameters[i].getName());
				sb.append("=");
				sb.append(String.format("%s", args[i].toString()));
				if (i < parameters.length-1) {
					sb.append(", ");
				}
			}
			sb.append(")");
			methodSignature = sb.toString();
			hasReturnValue = !method.getReturnType().equals(Void.TYPE);
		} else {
			Class<?> clazz = targetClass;
			loggerName = clazz.getName();
			methodSignature = "<unknown method>";
			hasReturnValue = false;
		}
		
		logger = LoggerFactory.getLogger(loggerName);

		LoggerHandler loggerHandler = new LoggerHandler();
		loggerHandler.setStartTime(startTime);
		loggerHandler.setLogger(logger);
		loggerHandler.setMethodSignature(methodSignature);
		if (hasReturnValue) {
			loggerHandler.setReturnFormat("{}");
		} else {
			loggerHandler.setReturnFormat("<void>");
		}
		
		return loggerHandler;
	}
}
