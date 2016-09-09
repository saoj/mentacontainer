package org.mentacontainer;

/**
 * An IoC factory that knows how to create instances.
 * 
 * @author sergio.oliveira.jr@gmail.com
 */
public interface Factory<T> {
	
	/**
	 * Returns an instance. Creates one if necessary.
	 * 
	 * @return an instance
	 */
	public T getInstance();
	
	
	/**
	 * Return the type of objects that this factory disposes.
	 * 
	 * @return the type of objects returned by this factory.
	 */
	public Class<? extends T> getType();
}