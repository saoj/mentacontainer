package org.mentacontainer;

/**
 * Some factories can also implement this interface to perform some cleanup
 * when the instance is created or cleared. For example, a connection pool will want
 * to know when the connection instance is cleared so it can return it to
 * the pool.
 * 
 * It makes more sense to use this interface for factories that will be placed
 * in the THREAD scope, but you can also use it with other scopes as well.
 * 
 * This is particular useful for the THREAD scope for dealing with thread pools, so
 * when the thread is returned to the thread pool you will want to clear the THREAD
 * scope. That's pretty much how web containers work: one thread per request coming from
 * a thread pool.
 * 
 * @author sergio.oliveira.jr@gmail.com
 *
 * @param <E>
 */
public interface Interceptor<E> {
	
	/**
	 * This method will be called right after the getInstance() method returns a new instance from the factory.
	 * 
	 * @param createdObject The object that was just returned by the factory.
	 */
	public void onCreated(E createdObject);
	
	/**
	 * This method will be called right after the object is cleared from the scope.
	 * 
	 * @param clearedObject The object that was cleared.
	 */
	public void onCleared(E clearedObject);
}