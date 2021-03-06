package org.mentacontainer.impl;


/**
 * A simple implementation of the Dependency interface.
 * 
 * @author sergio.oliveira.jr@gmail.com
 */
class ConstructorDependency {
	
	private final String sourceFromContainer;
	
	private final Class<?> sourceType;
	
	public ConstructorDependency(String sourceFromContainer, Class<?> sourceType) {
		
		this.sourceFromContainer = sourceFromContainer;
		
		this.sourceType = sourceType;
	}
	
	public String getSource() {
		
		return sourceFromContainer;
	}
	
	public Class<?> getSourceType() {
		
		return sourceType;
	}
	
	@Override
	public int hashCode() {
		
		return sourceFromContainer.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof ConstructorDependency)) return false;
		
		ConstructorDependency d = (ConstructorDependency) obj;
		
		if (!d.sourceFromContainer.equals(this.sourceFromContainer)) return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		return "[ConstructorDependency: sourceFromContainer=" + sourceFromContainer + "]";
	}
}
