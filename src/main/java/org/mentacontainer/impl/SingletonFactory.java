package org.mentacontainer.impl;

import org.mentacontainer.Factory;

public class SingletonFactory<T> implements Factory<T> {
	
	private final Object instance;
	
	private final Class<?> type;
	
	
	public SingletonFactory(Object instance) {

		this.instance = instance;
		
		this.type = instance.getClass();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getInstance()  {
		
		return (T) instance;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends T> getType() {
		
		return (Class<? extends T>) type;
	}
	
	public static <T> Factory<T> singleton(Object instance) {
		return new SingletonFactory<T>(instance);
	}
}