package org.mentacontainer.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.mentacontainer.Factory;
import org.mentacontainer.Container;

public class WrapperFactoryTest {
	
	@Test
	public void testInstanceFactory() throws Exception {
		
		String s = new String("saoj");
		
		Factory<Object> ic = new SingletonFactory<Object>(s);
		
		Container c = new MentaContainer();
		
		c.ioc("myString", ic);
		
		String s1 = c.get("myString");
		
		String s2 = c.get("myString");
		
		Assert.assertNotNull(s1);
		Assert.assertNotNull(s2);
		Assert.assertTrue(s == s1);
		Assert.assertTrue(s1 == s2);
	}
}