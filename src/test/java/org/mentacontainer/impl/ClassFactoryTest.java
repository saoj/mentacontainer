package org.mentacontainer.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.mentacontainer.Factory;

@SuppressWarnings("unused")
public class ClassFactoryTest {
	
	private static class User {
		
		private String username;
		
		public User() { }
		
		public User(String username) {
			this.username = username;
		}
		
		public String getUsername() {
			return username;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}
	}
	
	@Test
	public void testFactory() {
		
		MentaContainer container = new MentaContainer();
		
		Factory<User> c1 = new ClassFactory<User>(container, User.class).addInitValue("saoj");
		
		Factory<User> c2 = new ClassFactory<User>(container, User.class).addPropertyValue("username", "soliveira");
		
		User u1 = c1.getInstance();
		
		User u2 = c2.getInstance();
		
		Assert.assertTrue(u1 != u2);
		
		Assert.assertTrue(!u1.getUsername().equals(u2.getUsername()));
	}
	
	private static class TestObj1 {
		
		String s;
		int i;
		long l;
		User u;
		
		public TestObj1(String s, int i, long l, User u) {
			this.s = s;
			this.i = i;
			this.l = l;
			this.u = u;
		}
	}
	
	@Test
	public void testDependable1() {
		
		MentaContainer c = new MentaContainer();
		
		c.ioc("myString", String.class).addInitValue("hello");
	
		Factory<TestObj1> c1 = new ClassFactory<TestObj1>(c, TestObj1.class).addConstructorDependency("myString").addInitValue(20).addInitValue(30000L)
				.addInitValue(new User("saoj"));
		
		TestObj1 o = c1.getInstance();
		
		Assert.assertEquals("hello", o.s);
		Assert.assertEquals(20, o.i);
		Assert.assertEquals(30000L, o.l);
		Assert.assertEquals("saoj", o.u.getUsername());
	}
	
	private static class TestObj1_1 {
		
		String s;
		int i;
		
		public TestObj1_1() { }
		
		public void setS(String s) { this.s = s; }
		public void setI(int i) { this.i = i; }
	}

	@Test
	public void testDependable2() {
		
		MentaContainer c = new MentaContainer();
		
		c.ioc("myString", String.class).addInitValue("hello");
	
		Factory<TestObj1_1> c1 = new ClassFactory<TestObj1_1>(c, TestObj1_1.class).addPropertyDependency("s", "myString").addPropertyValue("i", 30);
		
		TestObj1_1 o = c1.getInstance();
		
		Assert.assertEquals("hello", o.s);
		Assert.assertEquals(30, o.i);
	}
	
	private static class TestObj2 {
		
		int x;
		
		public TestObj2() { }
		
		public void setInteger(int x) {
			this.x = x;
		}
	}
	
	@Test
	public void testPrimitivePropertyValue1() {
		
		MentaContainer container = new MentaContainer();
		
		Factory<TestObj2> f = new ClassFactory<TestObj2>(container, TestObj2.class).addPropertyValue("integer", 20);
		
		TestObj2 o = f.getInstance();
		
		Assert.assertEquals(20, o.x);
	}
	
	@Test
	public void testPrimitivePropertyValue2() {
		
		MentaContainer container = new MentaContainer();
		
		Factory<TestObj2> f = new ClassFactory<TestObj2>(container, TestObj2.class).addPropertyValue("integer", new Integer(20));
		
		TestObj2 o = f.getInstance();
		
		Assert.assertEquals(20, o.x);
	}
	
	private static class TestObj2_1 {
		
		int x;
		
		public TestObj2_1() { }
		
		public void setInteger(Integer x) {
			this.x = x;
		}
	}	
	
	@Test
	public void testPrimitivePropertyValue3() {
		
		MentaContainer container = new MentaContainer();
		
		Factory<TestObj2_1> f = new ClassFactory<TestObj2_1>(container, TestObj2_1.class).addPropertyValue("integer", 20);
		
		TestObj2_1 o = f.getInstance();
		
		Assert.assertEquals(20, o.x);
	}
	
	@Test
	public void testPrimitivePropertyValue4() {
		
		MentaContainer container = new MentaContainer();
		
		Factory<TestObj2_1> f = new ClassFactory<TestObj2_1>(container, TestObj2_1.class).addPropertyValue("integer", new Integer(20));
		
		TestObj2_1 o = f.getInstance();
		
		Assert.assertEquals(20, o.x);
	}	
	
	
	private static class TestObj3 {
		
		int x;
		
		public TestObj3(int x) {
			this.x = x;
		}
	}
	
	@Test
	public void testPrimitiveInitValue1() {
		
		MentaContainer container = new MentaContainer();
		
		Factory<TestObj3> f = new ClassFactory<TestObj3>(container, TestObj3.class).addInitValue(20);
		
		TestObj3 o = f.getInstance();
		
		Assert.assertEquals(20, o.x);
	}
	
	private static class TestObj3_1 {
		
		int x;
		
		public TestObj3_1(Integer x) {
			this.x = x;
		}
	}
	
	@Test
	public void testPrimitiveInitValue2() {
				
		MentaContainer container = new MentaContainer();
		
		Factory<TestObj3_1> f = new ClassFactory<TestObj3_1>(container, TestObj3_1.class).addInitValue(20);
		
		TestObj3_1 o = f.getInstance();
		
		Assert.assertEquals(20, o.x);
	}
	
	private static class TestObj3_2 {
		
		int x;
		long l;
		
		public TestObj3_2(Integer x, long l) {
			this.x = x;
			this.l = l;
		}
	}
	
	@Test
	public void testPrimitiveInitValue3() {
		
		MentaContainer container = new MentaContainer();
		
		Factory<TestObj3_2> f = new ClassFactory<TestObj3_2>(container, TestObj3_2.class).addInitValue(20).addInitPrimitive(30L);
		
		TestObj3_2 o = f.getInstance();
		
		Assert.assertEquals(20, o.x);
		Assert.assertEquals(30L, o.l);
	}	
	
	private static class TestObj4 {
		
		int x;
		String s;
		Long l1;
		long l2;
		
		public TestObj4(int x, String s, Long l1, long l2) {
			this.x = x;
			this.s = s;
			this.l1 = l1;
			this.l2 = l2;
		}
	}
	
	@Test
	public void testMixInitValues() {
		
		MentaContainer container = new MentaContainer();
		
		Factory<TestObj4> f = new ClassFactory<TestObj4>(container, TestObj4.class).addInitPrimitive(20).addInitValue("hello").addInitValue(20L).addInitPrimitive(20L);
		
		TestObj4 o = f.getInstance();
		
		Assert.assertEquals(20, o.x);
		Assert.assertEquals("hello", o.s);
		Assert.assertEquals(new Long(20), o.l1);
		Assert.assertEquals(20, o.l2);
	}
	
	private static class TestObj22 {
		
		public TestObj22() { }
	}
	
	@Test
	public void testOnlyOneZeroArgConstructor() {
		
		MentaContainer container = new MentaContainer();
		
		Factory<TestObj22> f = new ClassFactory<TestObj22>(container, TestObj22.class);
		
		TestObj22 o = f.getInstance();
		
		Assert.assertNotNull(o);
		
	}
}