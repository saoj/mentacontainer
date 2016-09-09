package org.mentacontainer.impl;

import org.mentacontainer.Factory;

public class SingletonFactory<T> implements Factory<T> {
	
	private final T instance;
	
	private final Class<? extends T> type;
	
	@SuppressWarnings("unchecked")
	public SingletonFactory(T instance) {

		this.instance = instance;
		
		this.type = (Class<T>) instance.getClass();
	}
	
	@Override
	public T getInstance()  {
		
		return instance;
	}
	
	@Override
	public Class<? extends T> getType() {
		
		return type;
	}
	
	public static <T> Factory<T> singleton(T instance) {
		return new SingletonFactory<T>(instance);
	}
}