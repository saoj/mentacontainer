package org.mentacontainer.impl;

import java.lang.reflect.Method;

import org.mentacontainer.Factory;
import org.mentacontainer.Interceptor;
import org.mentacontainer.util.FindMethod;

public class GenericFactory<E> implements Factory, Interceptor<E> {
	
	private final Object factory;
	
	private final Method method;
	
	private final Class<?> type;
	
	private Interceptor<E> interceptor = null;
	
	public GenericFactory(Object factory, String methodName) {
		
		this.factory = factory;
		
		try {
		
			this.method = FindMethod.getMethod(factory.getClass(), methodName, new Class[] { });
			
			this.method.setAccessible(true);
			
			this.type = method.getReturnType();
			
		} catch(Exception e) {
			
			throw new RuntimeException(e);
		}
	}
	
	public void setInterceptor(Interceptor<E> interceptor) {
		
		this.interceptor = interceptor;
	}
	
	@Override
	public void onCreated(E createdObject) {
		if (interceptor != null) {
			interceptor.onCreated(createdObject);
		}
	}
	
	@Override
	public void onCleared(E clearedObject) {
		if (interceptor != null) {
			interceptor.onCleared(clearedObject);
		}
	}
	
	@Override
	public <T> T getInstance()  {
		
		try {
			
			return (T) method.invoke(factory, (Object[]) null);
			
		} catch(Exception e) {
			
			throw new RuntimeException("Cannot invoke method: " + method, e);
			
		} 
	}
	
	@Override
	public Class<?> getType() {
		return type;
	}
}