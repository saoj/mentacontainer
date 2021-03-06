package org.mentacontainer.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mentacontainer.ConfigurableFactory;
import org.mentacontainer.util.FindConstructor;
import org.mentacontainer.util.FindMethod;
import org.mentacontainer.util.InjectionUtils;

/**
 * The implementation of the Configurable Factory.
 * 
 * @author sergio.oliveira.jr@gmail.com
 */
class ClassFactory<T> implements ConfigurableFactory<T> {
	
		private final MentaContainer container;
	
	    private final Class<?> klass;
	    
	    private Map<String, Object> props = null;
	    
	    private List<Object> initValues = null;
	    
	    private List<Class<?>> initTypes = null;
	    
	    private Constructor<?> constructor = null;
	    
	    private Map<String, Method> cache = null;
	    
	    private boolean useZeroArgumentsConstructor = false;
	    
	    private final Set<ConstructorDependency> constructorDependencies;
	    
	    public ClassFactory(MentaContainer container, Class<?> klass) {

	    	this(container, klass, null);
	    }
	    
	    ClassFactory(MentaContainer container, Class<?> klass, Set<ConstructorDependency> constructorDependencies) {
	    	
	    	this.container = container;
	    	
	    	this.klass = klass;
	    	
	    	this.constructorDependencies = constructorDependencies;
	    	
	    }

	    
	    @Override
	    public ConfigurableFactory<T> addPropertyValue(String name, Object value) {
	    	
	        if (props == null) {
	        	
	            props = new HashMap<String, Object>();
	            
	            cache = new HashMap<String, Method>();
	        }
	        
	        props.put(name, value);
	        
	        return this;
	    }
	    
	    @Override
	    public ConfigurableFactory<T> useZeroArgumentConstructor() {
	    	
	    	this.useZeroArgumentsConstructor = true;
	    	
	    	return this;
	    }
	    
	    @Override
	    public ConfigurableFactory<T> addPropertyDependency(String property, Object key) {
	    	
	    	String k = InjectionUtils.getKeyName(key);
	    	
	    	return addPropertyValue(property, new DependencyKey(k));
	    }
	    
	    @Override
	    public ConfigurableFactory<T> addPropertyDependency(String property) {
	    	
	    	return addPropertyDependency(property, property);
	    }
	    
	    @Override
	    public ConfigurableFactory<T> addConstructorDependency(Object key) {
	    	
	    	String k = InjectionUtils.getKeyName(key);
	    	
	    	return addInitValue(new DependencyKey(k), container.getType(k));
	    }
	    
	    private ConfigurableFactory<T> addInitValue(Object value, Class<?> type) {
	    	
	        if (initValues == null) {
	        	
	            initValues = new LinkedList<Object>();
	            
	            initTypes = new LinkedList<Class<?>>();
	        }
	        
	        initValues.add(value);
	        
	        initTypes.add(type);
	        
	        return this;
	    }
	    
	    @Override
	    public ConfigurableFactory<T> addInitValue(Object value) {
	    	
	        return addInitValue(value, value.getClass());
	    }
	    
	    @Override
	    public ConfigurableFactory<T> addInitPrimitive(Object value) {
	    	
	    	Class<?> primitive = getPrimitiveFrom(value);
	    	
	    	if (primitive == null) throw new IllegalArgumentException("Value is not a primitive: " + value);
	    	
	    	return addInitValue(value, primitive);
	    }
	    
	    private List<Class<?>> convertToPrimitives(List<Class<?>> list) {
	    	
	    	if (list == null) return null;
	    	
	    	Iterator<Class<?>> iter = list.iterator();
	    	
	    	List<Class<?>> results = new LinkedList<Class<?>>();
	    	
	    	while(iter.hasNext()) {
	    		
	    		Class<?> klass = iter.next();
	    		
	    		Class<?> primitive = getPrimitiveFrom(klass);
	    		
	    		if (primitive != null) {
	    			
	    			results.add(primitive);
	    			
	    		} else {
	    			
	    			results.add(klass);
	    		}
	    	}
	    	
	    	return results;
	    }
	    
	    private Class<?>[] getClasses(List<Class<?>> values) {
	    	
	    	if (values == null) return new Class[0];
	    	
	        Class<?>[] types = (Class<?>[]) new Class[values.size()];
	        
	        return values.toArray(types);
	    }
	    
	    private Object [] getValues(List<Object> values) throws InstantiationException {
	    	
	    	if (values == null) return null;
	    	
	        Object [] array = new Object[values.size()];
	        
	        int index = 0;
	        
	        Iterator<Object> iter = values.iterator();
	        
	        while(iter.hasNext()) {
	        
	        	Object obj = iter.next();
	        	
	        	if (obj instanceof DependencyKey) {
	        		
	        		DependencyKey dk = (DependencyKey) obj;
	        		
	        		array[index++] = container.get(dk.getKey());
	        		
	        	} else {
	            
	        		array[index++] = obj;
	        	}
	        }
	        
	        return array;
	    }
	    
		/*
		 * Use reflection to set a property in the bean
		 */
		private void setValue(Object bean, String name, Object value) {
	        
			try {
				
				StringBuffer sb = new StringBuffer(30);
				sb.append("set");
				sb.append(name.substring(0,1).toUpperCase());
				
				if (name.length() > 1) sb.append(name.substring(1));
	            
	            String methodName = sb.toString();
	            
	            if (!cache.containsKey(name)) {
	            	
	                Method m = null;
	                
	                try {
	                	
	                    m = FindMethod.getMethod(klass, methodName, new Class[] { value.getClass() });
	                    
	                } catch(Exception e) {
	                	
	                    // try primitive...
	                	
	                    Class<?> primitive = getPrimitiveFrom(value);
	                    
	                    if (primitive != null) {
	                    	
	                        try {
	                        	
	                            m = klass.getMethod(methodName, new Class[] { primitive });
	                            
	                        } catch(Exception ex) {
	                        	// not found!
	                        }
	                    }
	                    
	                    if (m == null) {
	                    	
	                        throw new InstantiationException("Cannot find method for property: " + name);
	                    }
	                }
	                
	                if (m != null) {
	                	
	                    cache.put(name, m);
	                    
	                    m.setAccessible(true);
	                }
	            }    

	            Method m = cache.get(name);
	            
	            if (m != null) {
	            	
	                m.invoke(bean, new Object[] { value });
	            }                
	            
			} catch(Exception e) {
				
				throw new RuntimeException("Error trying to set a property with reflection: " + name, e);
			}
		} 
	    
	    private static Class<?> getPrimitiveFrom(Object w) { 
	        if (w instanceof Boolean) { return Boolean.TYPE; } 
	        else if (w instanceof Byte) { return Byte.TYPE; } 
	        else if (w instanceof Short) { return Short.TYPE; } 
	        else if (w instanceof Character) { return Character.TYPE; } 
	        else if (w instanceof Integer) { return Integer.TYPE; } 
	        else if (w instanceof Long) { return Long.TYPE; } 
	        else if (w instanceof Float) { return Float.TYPE; } 
	        else if (w instanceof Double) { return Double.TYPE; } 
	        return null;
	    }
	    
	    private static Class<?> getPrimitiveFrom(Class<?> klass) {
	        if (klass.equals(Boolean.class)) { return Boolean.TYPE; } 
	        else if (klass.equals(Byte.class)) { return Byte.TYPE; } 
	        else if (klass.equals(Short.class)) { return Short.TYPE; } 
	        else if (klass.equals(Character.class)) { return Character.TYPE; } 
	        else if (klass.equals(Integer.class)) { return Integer.TYPE; } 
	        else if (klass.equals(Long.class)) { return Long.TYPE; } 
	        else if (klass.equals(Float.class)) { return Float.TYPE; } 
	        else if (klass.equals(Double.class)) { return Double.TYPE; } 
	        return null;
	    }
	    
		@SuppressWarnings("unchecked")
		@Override
	    public T getInstance()  {
	    	
	        Object obj = null;
	        
	        Object[] values = null;
	        
	        synchronized(this) {
	        
                if (constructor == null) {
                	
                	if (!useZeroArgumentsConstructor) {
                		
                		checkConstructorDependencies();
                		
                	} else {
                		
                		if (initTypes != null) initTypes = null; // just in case client did something stupid...
                		if (initValues != null) initValues = null; // just in case client did something stupid...
                	} 
                	
                    try {
                    	
                        //constructor = klass.getConstructor(getClasses(initTypes));
                    	
                    	constructor = FindConstructor.getConstructor(klass, getClasses(initTypes));
                        
                    } catch(Exception e) {
                    	
                    	// try primitives...
                    	
                    	try {
                    	
                    		//constructor = klass.getConstructor(getClasses(convertToPrimitives(initTypes)));
                    		
                    		constructor = FindConstructor.getConstructor(klass, getClasses(convertToPrimitives(initTypes)));
                    		
                    	} catch(Exception ee) {
    
                    		throw new RuntimeException("Cannot find a constructor for class: " + klass);
                    	}
                    }
                }
                
                try {
                	
                	values = getValues(initValues);
                	
                } catch(Exception e) {
                	
                	new RuntimeException("Cannot instantiate values for constructor!", e);
                }
	        }
            
            try {
            	
                obj = constructor.newInstance(values);
                
            } catch(Exception e) {
            	
                throw new RuntimeException("Cannot create instance from constructor: " + constructor, e);
            }
	        
	        if (props != null && props.size() > 0) {
	        	
	            Iterator<String> iter = props.keySet().iterator();
	            
	            while(iter.hasNext()) {
	            	
	                String name = iter.next();
	                
	                Object value = props.get(name);
	                
	                if (value instanceof DependencyKey) {
	                	
	                	DependencyKey dk = (DependencyKey) value;
	                	
	                	value = container.get(dk.getKey());
	                }
	                
	                setValue(obj, name, value);
	            }
	        }
	        
	        return (T) obj;
	    }
	    
	    private static boolean betterIsAssignableFrom(Class<?> klass1, Class<?> klass2) {
	    	
	    	// with autoboxing both ways...
	    	
	    	if (klass1.isAssignableFrom(klass2)) return true;
	    	
	    	Class<?> k1 = klass1.isPrimitive() ? klass1 : getPrimitiveFrom(klass1);
	    	Class<?> k2 = klass2.isPrimitive() ? klass2 : getPrimitiveFrom(klass2);
	    	
	    	if (k1 == null || k2 == null) return false;
	    	
	    	return k1.isAssignableFrom(k2);
	    }
	    
	    private void checkConstructorDependencies() {
	    	
	    	Constructor<?>[] constructors = klass.getConstructors();
	    	
	    	for(Constructor<?> c : constructors) {
	    		
		    	LinkedList<Class<?>> providedInitTypes = null;
		    	
		    	if (initTypes != null) {
		    		
		    		providedInitTypes = new LinkedList<Class<?>>(initTypes); 
		    		
		    	} else {
		    		
		    		providedInitTypes = new LinkedList<Class<?>>();
		    	}
		    	
		    	LinkedList<Object> providedInitValues = null;
		    	
		    	if (initValues != null) {
		    		
		    		providedInitValues = new LinkedList<Object>(initValues);
		    		
		    	} else {
		    		
		    		providedInitValues = new LinkedList<Object>();
		    	}
		    	
		    	LinkedList<Class<?>> newInitTypes = new LinkedList<Class<?>>(); 
		    	LinkedList<Object> newInitValues = new LinkedList<Object>();
		    	
		    	Set<ConstructorDependency> constructorDependencies = this.constructorDependencies != null ? this.constructorDependencies : container.getConstructorDependencies(); 
		    	
		    	Set<ConstructorDependency> dependencies = new HashSet<ConstructorDependency>(constructorDependencies);
	    		
	    		Class<?>[] constructorParams = c.getParameterTypes();
	    		
	    		if (constructorParams == null || constructorParams.length == 0) continue; // skip default constructor for now...
	    		
	    		for(Class<?> constructorParam : constructorParams) {
	    			
	    			// first see if it was provided...
	    			
	    			Class<?> provided = providedInitTypes.isEmpty() ? null : providedInitTypes.get(0);
	    			
	    			if (provided != null && constructorParam.isAssignableFrom(provided)) {
	    				
	    				newInitTypes.add(providedInitTypes.removeFirst()); // we matched this one, so remove...
	    				
	    				newInitValues.add(providedInitValues.removeFirst());
	    				
	    				continue; 
	    				
	    			} else {
	    				
	    				// check auto-wiring...
	    				
	    				Iterator<ConstructorDependency> iter = dependencies.iterator();
	    				
	    				boolean foundMatch = false;
	    				
	    				while(iter.hasNext()) {
	    					
	    					ConstructorDependency d = iter.next();
	    					
	    					if (betterIsAssignableFrom(constructorParam, d.getSourceType())) {
	    						
	    						iter.remove();
	    						
	    						newInitTypes.add(d.getSourceType());
	    						
	    						newInitValues.add(new DependencyKey(d.getSource()));
	    						
	    						foundMatch = true;
	    						
	    						break;
	    					}
	    				}
	    				
	    				if (foundMatch) {
	    					continue; // next constructor param...
	    				}
	    				
	    			}
	    			
	    			break; // no param... next constructor...
	    		}
	    		
	    		// done, check if found...
	    		
	    		if (constructorParams.length == newInitTypes.size() && providedInitTypes.isEmpty()) {
	    			
	    			this.initTypes = newInitTypes;
	    			
	    			this.initValues = newInitValues;
	    		}
	    	}
	    }
	    
	    private static class DependencyKey {
	    	
	    	private String key;
	    	
	    	public DependencyKey(String key) { this.key = key; }
	    	
	    	private String getKey() { return key; }
	    }
	    
	    @SuppressWarnings("unchecked")
		@Override
	    public Class<? extends T> getType() {
	    	return (Class<? extends T>) klass;
	    }
	}