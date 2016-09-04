package org.mentacontainer.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.mentacontainer.Factory;
import org.mentacontainer.Container;
import org.mentacontainer.impl.GenericFactory;
import org.mentacontainer.impl.MentaContainer;

public class GenericFactoryTest {
	
	public static class SomeFactory {
		
		public String giveMeSomething() {
			
			return String.valueOf(System.nanoTime());
		}
	}
	
	@Test
	public void testGenericFactory() throws Exception {
		
		// in real life this will be a SessionFactory, a ConnectionPool or any factory...
		SomeFactory factory = new SomeFactory();
		
		Container c = new MentaContainer();
		
		// giveMeSomething => method that will be called to return object
		@SuppressWarnings("rawtypes")
		Factory generic = new GenericFactory(factory, "giveMeSomething");
		
		c.ioc("myFactory", generic);
		
		String s1 = c.get("myFactory");
		
		Thread.sleep(5); // so strings are different... my cpu is fast! :-)
		
		String s2 = c.get("myFactory");
		
		Assert.assertNotNull(s1);
		Assert.assertNotNull(s2);
		Assert.assertTrue(s1 != s2);
		Assert.assertTrue(!s1.equals(s2));
	}
}