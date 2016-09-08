package org.mentacontainer.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mentacontainer.ConfigurableFactory;
import org.mentacontainer.Container;
import org.mentacontainer.Factory;
import org.mentacontainer.Interceptor;
import org.mentacontainer.Scope;
import org.mentacontainer.util.InjectionUtils;
import org.mentacontainer.util.InjectionUtils.Provider;

/**
 * The implementation of the IoC container.
 * 
 * @author sergio.oliveira.jr@gmail.com
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MentaContainer implements Container {

	private Map<String, Factory> factoriesByName = new Hashtable<String, Factory>();
	
	private Map<String, Scope> scopes = new Hashtable<String, Scope>();
	
	private Map<String, Object> singletonsCache = new Hashtable<String, Object>();
	
	private Map<String, ThreadLocal<Object>> threadLocalsCache = new Hashtable<String, ThreadLocal<Object>>();
	
	private Set<SetterDependency> setterDependencies = Collections.synchronizedSet(new HashSet<SetterDependency>());
	
	private Set<ConstructorDependency> constructorDependencies = Collections.synchronizedSet(new HashSet<ConstructorDependency>());
	
	private Set<ConstructorDependency> forConstructMethod = Collections.synchronizedSet(new HashSet<ConstructorDependency>());
	
	@Override
	public Class<?> getType(Object key) {
		
		String k = InjectionUtils.getKeyName(key);
		
		Factory factory = factoriesByName.get(k);
		
		if (factory == null) return null;
		
		return factory.getType();
	}
	
	@Override
	public void clear(Scope scope) {
		
		if (scope == Scope.SINGLETON) {
			
			List<ClearableHolder> listToClear = new LinkedList<ClearableHolder>();
			
			synchronized(this) {
			
    			for(String key : singletonsCache.keySet()) {
    				
    				Factory factory = factoriesByName.get(key);
    				
    				if (factory instanceof Interceptor) {
    					
    					Interceptor c = (Interceptor) factory;
    				
    					Object value = singletonsCache.get(key);
    					
    					listToClear.add(new ClearableHolder(c, value));
    				}
    			}
    			
    			singletonsCache.clear();
			}
			
			// clear everything inside a non-synchronized block...
			
			for(ClearableHolder cp : listToClear) cp.clear();
			
		} else if (scope == Scope.THREAD) {
			
			List<ClearableHolder> listToClear = new LinkedList<ClearableHolder>();
			
			synchronized(this) {
			
    			for(String key : threadLocalsCache.keySet()) {
    				
    				Factory factory = factoriesByName.get(key);
    				
    				if (factory instanceof Interceptor) {
    					
    					Interceptor c = (Interceptor) factory;
    				
    					ThreadLocal<Object> t = threadLocalsCache.get(key);
    					
    					Object value = t.get();
    					
    					// we are ONLY clearing if this thread has something in the threadlocal, in other words,
    					// if the thread has previously requested this key...
    					if (value != null) listToClear.add(new ClearableHolder(c, value));
    				}
    			}

				
				// and now we clear all thread locals belonging to this thread...
				// this will only clear the instances related to this thread...
    			for(ThreadLocal<Object> t : threadLocalsCache.values()) {
    				t.remove();
    			}
			}
			
			// clear everything inside a non-synchronized block...
			
			for(ClearableHolder cp : listToClear) cp.clear();
		}
	}
	
	@Override
	public <T> T clear(Object key) {
		
		String keyString = InjectionUtils.getKeyName(key);
		
		if (!factoriesByName.containsKey(keyString)) return null;
		
		Scope scope = scopes.get(keyString);
		
		if (scope == Scope.SINGLETON) {
			
			ClearableHolder cp = null;
			
			Object value = null;
			
			synchronized(this) {
			
    			value = singletonsCache.remove(keyString);
    			
    			if (value != null) {
    				
    				Factory factory = factoriesByName.get(keyString);
    				
    				if (factory instanceof Interceptor) {
    					
    					Interceptor c = (Interceptor) factory;
    					
    					cp = new ClearableHolder(c, value);
    				}
    			}
			}
			
			if (cp != null) cp.clear();
			
			return (T) value;
			
		} else if (scope == Scope.THREAD) {
			
			ClearableHolder cp = null;
			
			Object retVal = null;
			
			synchronized(this) {
			
    			ThreadLocal<Object> t = threadLocalsCache.get(keyString);
    			
    			if (t != null) {
    				
    				Object o = t.get();
    				
    				if (o != null) {
    					
    					Factory factory = factoriesByName.get(keyString);
    					
    					if (factory instanceof Interceptor) {
    						
    						Interceptor c = (Interceptor) factory;
    						
    						cp = new ClearableHolder(c, o);
    					}
    					
    					t.remove();
    					
    					retVal = o;
    				}
    			}
			}
			
			if (cp != null) cp.clear();
			
			return (T) retVal;
		
		} else if (scope == Scope.NONE) {
			
			return null; // always...
			
		} else {
			
			throw new UnsupportedOperationException("Scope not supported: " + scope);
		}
	}

	@Override
	public <T> T get(Object key) {
		
		String keyString = InjectionUtils.getKeyName(key);

		if (!factoriesByName.containsKey(keyString)) return null;

		Factory c = factoriesByName.get(keyString);
		
		Scope scope = scopes.get(keyString);
		
		Object target = null;

		try {

			if (scope == Scope.SINGLETON) {
				
				boolean needsToCreate = false;
				
				synchronized(this) {

    				if (singletonsCache.containsKey(keyString)) {
    
    					target = singletonsCache.get(keyString);
    
    					return (T) target; // no need to wire again...
    
    				} else {
    					
    					needsToCreate = true;
    				}
				}
				
				if (needsToCreate) {
					
					// getInstance needs to be in a non-synchronized block
					
					target = c.getInstance();
					
					checkInterceptable(c, target);
					
					synchronized(this) {

						singletonsCache.put(keyString, target);
					}
				}
				
			} else if (scope == Scope.THREAD) {
				
				boolean needsToCreate = false;
				
				boolean needsToAddToCache = false;
				
				ThreadLocal<Object> t = null;
				
				synchronized(this) {
				
    				if (threadLocalsCache.containsKey(keyString)) {
    					
    					t = threadLocalsCache.get(keyString);
    					
    					target = t.get();
    					
    					if (target == null) { // different thread...
    						
    						needsToCreate = true;
    						
    						// don't return... let it be wired...
    						
    					} else {
    					
    						return (T) target; // no need to wire again...
    						
    					}
    					
    				} else {
    					
    					t = new ThreadLocal<Object>();
    					
    					needsToCreate = true;
    					
						needsToAddToCache = true;
    					
    					// let it be wired...
    				}
				}
				
				if (needsToCreate) {
					
					// getInstance needs to be in a non-synchronized block
					
					target = c.getInstance();
					
					checkInterceptable(c, target);
					
					t.set(target);
				}
				
				if (needsToAddToCache) {
					
					synchronized(this) {
					
						threadLocalsCache.put(keyString, t);
					}
				}
				
			} else if (scope == Scope.NONE) {

				target = c.getInstance();
				
				checkInterceptable(c, target);
				
			} else {
				
				throw new UnsupportedOperationException("Don't know how to handle scope: " + scope);
			}

			if (target != null) {

				for (SetterDependency d : setterDependencies) {

					// has dependency ?
					Method m = d.check(target.getClass());

					if (m != null) {

						String sourceKey = d.getSource();

						if (sourceKey.equals(keyString)) {

							// cannot depend on itself... also avoid recursive StackOverflow...

							continue;

						}

						Object source = get(sourceKey);

						try {

							// inject
							m.invoke(target, source);

						} catch (Exception e) {

							throw new RuntimeException("Cannot inject dependency: method = " + (m != null ? m.getName() : "NULL") + " / source = "
							        + (source != null ? source : "NULL") + " / target = " + target, e);

						}
					}
				}
			}

			return (T) target; // return target nicely with all the dependencies

		} catch (Exception e) {

			throw new RuntimeException(e);
		}
	}
	
	private final void checkInterceptable(Factory f, Object value) {
		
		if (f instanceof Interceptor) {
			
			((Interceptor) f).onCreated(value);
		}
	}
	
	@Override
	public Factory ioc(Object key, Factory factory, Scope scope) {
		
		String keyString = InjectionUtils.getKeyName(key);
		
		factoriesByName.put(keyString, factory);
		
		singletonsCache.remove(keyString); // just in case we are overriding a previous singleton bean...
		
		ThreadLocal<Object> threadLocal = threadLocalsCache.remove(keyString); // just in case we are overriding a previous thread local...
		if (threadLocal != null) {
			threadLocal.remove();
		}
		
		scopes.put(keyString, scope);
		
		forConstructMethod.add(new ConstructorDependency(keyString, factory.getType()));
		
		return factory;
	}
	
	@Override
	public Factory ioc(Object key, Factory factory) {
		
		return ioc(key, factory, Scope.NONE);
	}
	
	@Override
	public ConfigurableFactory ioc(Object key, Class<?> klass) {
		
		ConfigurableFactory cc = new ClassFactory(this, klass);
		
		ioc(key, cc);
		
		return cc;
	}
	
	@Override
	public ConfigurableFactory ioc(Class<?> klass) {
		
		return ioc(klass, klass);
	}
	
	@Override
	public ConfigurableFactory ioc(Object key, Class<?> klass, Scope scope) {
		
		ConfigurableFactory cc = new ClassFactory(this, klass);
		
		ioc(key, cc, scope);
		
		return cc;
	}
	
	@Override
	public void autowire(Object sourceFromContainer) {
		
		// autowire by constructor and setter...
		
		String s = InjectionUtils.getKeyName(sourceFromContainer);
		
		// ATTENTION: targetProperty needs to be adjusted when sourceFromContainer is a class
		
		String targetProperty = InjectionUtils.getTargetPropertyName(sourceFromContainer);
		
		autowireBySetter(targetProperty, s);
		
		autowireByConstructor(s);
	}
	
	@Override
	public void autowire(Object sourceFromContainer, String beanProperty) {
		
		// autowire by constructor and setter...
		
		String s = InjectionUtils.getKeyName(sourceFromContainer);
		
		autowireBySetter(beanProperty, s);
		
		autowireByConstructor(s);
	}

	private void autowireBySetter(String targetProperty, String sourceFromContainer) {
		
		Class<?> sourceType = getType(sourceFromContainer);

		SetterDependency d = new SetterDependency(targetProperty, sourceFromContainer, sourceType);
		
		setterDependencies.add(d);
	}
	
	private void autowireByConstructor(String sourceFromContainer) {
		
		Class<?> sourceType = getType(sourceFromContainer);
		
		ConstructorDependency d = new ConstructorDependency(sourceFromContainer, sourceType);
		
		constructorDependencies.add(d);
	}
	
	Set<ConstructorDependency> getConstructorDependencies() {
		
		return constructorDependencies;
	}
	
	@Override
	public <T> T construct(Class<?> klass) {
		
		ClassFactory f = new ClassFactory(this, klass, forConstructMethod);
		
		return (T) f.getInstance();
	}

	@Override
	public void inject(Object bean) {
		
		Provider p = new Provider() {
			
			@Override
			public Object get(String key) {
				
				return MentaContainer.this.get(key);
			}
			
			@Override
			public boolean hasValue(String key) {
				
				return MentaContainer.this.check(key);
			}
			
		};
		
		try {

			InjectionUtils.getObject(bean, p, false, null, true, false, true);
			
		} catch(Exception e) {
			
			throw new RuntimeException("Error populating bean: " + bean, e);
		}
	}

	@Override
	public synchronized boolean check(Object obj) {
		
		String key = InjectionUtils.getKeyName(obj);
		
		if (!factoriesByName.containsKey(key)) return false;
		
		Scope scope = scopes.get(key);
		
		if (scope == Scope.NONE) {
			
			return false; // always...
			
		} else if (scope == Scope.SINGLETON) {
			
			return singletonsCache.containsKey(key);
			
		} else if (scope == Scope.THREAD) {
			
			ThreadLocal<Object> t = threadLocalsCache.get(key);
			
			if (t != null) return t.get() != null;
			
			return false;
			
		} else {
			
			throw new UnsupportedOperationException("This scope is not supported: " + scope);
		}
	}
	
	private static class ClearableHolder {

		private Interceptor c;
		private Object value;
		
		public ClearableHolder(Interceptor c, Object value) {
			this.c = c;
			this.value = value;
		}
		
		public void clear() {
			c.onCleared(value);
		}
		
	}
}