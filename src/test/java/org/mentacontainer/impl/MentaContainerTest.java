package org.mentacontainer.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.mentacontainer.Interceptor;
import org.mentacontainer.Factory;
import org.mentacontainer.Container;
import org.mentacontainer.Scope;

public class MentaContainerTest {

	@Test
	public void testSimpleGet() {

		Container c = new MentaContainer();

		c.ioc("myStr", String.class);

		Assert.assertEquals("", c.get("myStr"));

		String s1 = c.get("myStr");

		String s2 = c.get("myStr");

		Assert.assertTrue(s1 != s2);

		Assert.assertTrue(s1.equals(s2));
	}

	@Test
	public void testConstructorInit() {

		Container c = new MentaContainer();

		c.ioc("myStr", String.class).addInitValue("hello");

		Assert.assertEquals("hello", c.get("myStr"));

		String s1 = c.get("myStr");

		String s2 = c.get("myStr");

		Assert.assertTrue(s1 != s2);

		Assert.assertTrue(s1.equals(s2));

		c.ioc("anotherStr", String.class).addInitValue("hi");

		String s3 = c.get("anotherStr");

		Assert.assertTrue(s1 != s3);

		Assert.assertFalse(s1.equals(s3));
	}

	@Test
	public void testSingleton() {

		Container c = new MentaContainer();

		c.ioc("myStr", String.class, Scope.SINGLETON).addInitValue("hello");

		Assert.assertEquals("hello", c.get("myStr"));

		String s1 = c.get("myStr");

		String s2 = c.get("myStr");

		Assert.assertTrue(s1 == s2);

		Assert.assertTrue(s1.equals(s2));
	}
	
	@Test
	public void testCheckAndClear() {
		
		Container c = new MentaContainer();
		
		c.ioc("myStr", String.class, Scope.SINGLETON).addInitValue("hello");

		Assert.assertEquals(false, c.check("myStr"));
		
		String s1 = c.get("myStr");
		
		Assert.assertEquals(true, c.check("myStr"));
		
		String s2 = c.get("myStr");
		
		Assert.assertTrue(s1 == s2);
		
		c.clear(Scope.SINGLETON);
		
		Assert.assertEquals(false, c.check("myStr"));
		
		String s3 = c.get("myStr");
		
		Assert.assertTrue(s3 != s2);
	}
	
	private static class MyThread extends Thread {
		
		private final Container c;
		private final String key;
		private String value = null;
		
		public MyThread(Container c, String key) {
			super();
			this.c = c;
			this.key = key;
		}
		
		@Override
		public void run() {
			
			for(int i = 0; i < 50; i++) {
			
				String v = c.get(key);
				
				if (this.value != null) {
					
					Assert.assertTrue(this.value == v);
					
				} 
					
				this.value = v;
			}
		}
		
		public String getValue() { return value; }
	}
	
	@Test
	public void testScopes() {
		
		Container c = new MentaContainer();
		
		// if you don't provide a scope the Scope is NONE
		// meaning a new instance is created on every
		// request for the bean
		
		c.ioc("myString1", String.class).addInitValue("saoj");
		
		String s1 = c.get("myString1");
		String s2 = c.get("myString1");
		
		Assert.assertTrue(s1 != s2);
		
		// then you can use SINGLETON
		// always get the same instance no matter what
		
		c.ioc("myString2", String.class, Scope.SINGLETON).addInitValue("saoj");
		
		s1 = c.get("myString2");
		s2 = c.get("myString2");
		
		Assert.assertTrue(s1 == s2);
		
		// then you can use THREAD
		// each thread will get a different instance
		
		c.ioc("myString3", String.class, Scope.THREAD).addInitValue("saoj");
		
		s1 = c.get("myString3");
		s2 = c.get("myString3");
		
		Assert.assertTrue(s1 == s2); // same thread
	}
	
	@Test
	public void testThreadLocal() throws Exception {
		
		final Container c = new MentaContainer();
		
		c.ioc("myStr", String.class, Scope.THREAD).addInitValue("saoj");
		
		String s1 = c.get("myStr");
		
		MyThread t1 = new MyThread(c, "myStr");
		MyThread t2 = new MyThread(c, "myStr");
		
		t1.start();
		t2.start();
		
		t1.join();
		t2.join();
		
		String s2 = t1.getValue();
		String s3 = t2.getValue();
		
		Assert.assertTrue(s1 != s2);
		Assert.assertTrue(s2 != s3);
	}

	public static interface MyDAO {
		
		public Object getSomething();
		
	}
	
	public static class SomeDependency {
		
		private String name;
		
		public SomeDependency() { }
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public static class Connection {
		
		private final String name;
		
		private SomeDependency dep;
		
		public Connection(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return getClass().getName() + ": " + name;
		}
		
		public String getName() { return name; }
		
		public void setMyDep(SomeDependency dep) { this.dep = dep; }
		
		public SomeDependency getMyDep() { return dep; }
	}
	
	
	
	public static class JdbcMyDAO implements MyDAO {
		
		private Connection conn;
		
		public void setConnection(Connection conn) { this.conn = conn; }
		
		public Connection getConnection() { return conn; }
		
		@Override
		public Object getSomething() {
			
			// use the connection to do something...
			
			Assert.assertNotNull(conn); // it cannot be null!
			
			// also test if the connection also received the myDep dependency...
			
			Assert.assertNotNull(conn.getMyDep());
			
			return conn.toString();
		}
	}
	
	private Container getConfiguredContainer() {
		
		Container c = new MentaContainer();
		
		c.ioc("myDAO", JdbcMyDAO.class);
		
		c.ioc("aDependency", SomeDependency.class, Scope.SINGLETON).addPropertyValue("name", "A super dependency!");
		
		c.ioc("connection", Connection.class).addInitValue("A super JDBC connection!");
		
		c.autowire("connection");
		
		c.autowire("aDependency", "myDep");
		
		return c;
	}
	
	@Test
	public void testAutoWiring() {
		
		Container c = getConfiguredContainer();
		
		MyDAO myDAO = c.get("myDAO");
		
		// check implementation...
		
		Assert.assertEquals(JdbcMyDAO.class, myDAO.getClass());
		
		// check dependency...
		
		Connection conn = ((JdbcMyDAO) myDAO).getConnection();
		
		Assert.assertNotNull(conn);
		
		Assert.assertEquals("A super JDBC connection!", conn.getName());
		
		// check dependency of dependency...
		
		Assert.assertEquals(c.get("aDependency"), conn.getMyDep());
		
		Assert.assertTrue(c.get("aDependency") == conn.getMyDep()); // singleton!
		
		// check DAO can do its job...
		
		Assert.assertNotNull(myDAO.getSomething());
	}
	
	public static class MyObject1 {
		
	}
	
	public static class MyObject2 {
		
	}
	
	public static class MyObject3 {
		
		public MyObject3(MyObject2 o2, MyObject1 o1) {

		}
	}
	
	private Container getConfiguredContainer2() {
		
		Container c = new MentaContainer();
		
		c.ioc("myObj1", MyObject1.class);
		c.ioc("myObj2", MyObject2.class);
		c.ioc("myObj3", MyObject3.class);
		
		c.autowire("myObj1");
		c.autowire("myObj2");
		
		return c;
	}
	
	@Test
	public void testAutoWiringWithTwoConstructors() {
		
		Container c = getConfiguredContainer2();
		
		MyObject3 obj3 = c.get("myObj3");
		
		Assert.assertNotNull(obj3);
	}
	
	public static class SomeAction {
		
		private MyDAO myDAO = null;
		
		public void setMyDAO(MyDAO myDAO) { 
			
			this.myDAO = myDAO;
		}
		
		public MyDAO getMyDAO() { return myDAO; }
	}
	
	@Test
	public void testPopulate() {
		
		Container c = getConfiguredContainer();
		
		SomeAction a = new SomeAction();
		
		c.inject(a); // great... properties of SomeAction were populated by container...
		
		// let's check if myDAO was injected...
		
		Assert.assertNotNull(a.getMyDAO());
		
		// also check if myDAO was corrected wired...
		
		Connection conn = ((JdbcMyDAO) a.getMyDAO()).getConnection();
		
		Assert.assertNotNull(conn);
		
		// check if conn was also wired...
		
		Assert.assertNotNull(conn.getMyDep());
	}
	
	public static class SomeAction2 {
		
		private final MyDAO myDAO;
		
		public SomeAction2(MyDAO myDAO) {
			this.myDAO = myDAO;
		}
		
		public MyDAO getMyDAO() { return myDAO; }
		
	}
	
	@Test
	public void testConstructorDependency() {
		
		Container c = new MentaContainer();
		
		c.ioc("myDAO", JdbcMyDAO.class);
		
		c.ioc("aDependency", SomeDependency.class, Scope.SINGLETON).addPropertyValue("name", "A super dependency!");
		
		c.ioc("connection", Connection.class).addInitValue("A super JDBC connection!");
		
		c.autowire("connection");
		
		c.autowire("aDependency", "myDep");
		
		c.ioc("someAction2", SomeAction2.class).addConstructorDependency("myDAO");
		
		SomeAction2 action = c.get("someAction2");
		
		Assert.assertNotNull(action.getMyDAO());
	}
	
	@Test
	public void testConstruct() {
		
		Container c = getConfiguredContainer();
		
		SomeAction2 a =  c.construct(SomeAction2.class);
		// let's check if myDAO was injected...
		
		Assert.assertNotNull(a.getMyDAO());
		
		// also check if myDAO was corrected wired...
		
		Connection conn = ((JdbcMyDAO) a.getMyDAO()).getConnection();
		
		Assert.assertNotNull(conn);
		
		// check if conn was also wired...
		
		Assert.assertNotNull(conn.getMyDep());
	}
	
	private static class SomeObject {
		
		private boolean destroyed = false;
		private boolean created = false;
		
		public void destroyed() { this.destroyed = true; }
		
		public boolean isDestroyed() { return destroyed; }
		
		public void created() { this.created = true; }
		
		public boolean isCreated() { return created; }
	}
	
	private static class SomeFactory implements Factory, Interceptor<SomeObject> {
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getInstance() {
			
			return (T) new SomeObject();
		}
		
		@Override
		public Class<?> getType() {
			
			return SomeObject.class;
		}
		
		@Override
		public void onCleared(SomeObject obj) {
			
			obj.destroyed();
		}
		
		@Override
		public void onCreated(SomeObject obj) {
			
			obj.created();
		}
	}
	
	@Test
	public void testInterceptor() {
		
		Container c = new MentaContainer();
		
		c.ioc("o1", new SomeFactory(), Scope.SINGLETON);
		
		SomeObject o = c.get("o1");
		
		Assert.assertTrue(o.isCreated());
		
		c.clear(Scope.SINGLETON);
		
		Assert.assertEquals(true, o.isDestroyed());
		
		c.ioc("o2", new SomeFactory(), Scope.THREAD);
		
		o = c.get("o2");
		
		Assert.assertTrue(o.isCreated());
		
		c.clear(Scope.SINGLETON);
		
		Assert.assertEquals(false, o.isDestroyed());
		
		c.clear(Scope.THREAD);
		
		Assert.assertEquals(true, o.isDestroyed());
		
		c.ioc("o3", new SomeFactory(), Scope.NONE);
		
		o = c.get("o3");
		
		Assert.assertTrue(o.isCreated());
		
		o = c.clear("o3");
		
		Assert.assertNull(o);
		
		c.ioc("o4", new SomeFactory(), Scope.THREAD);
		
		o = c.clear("o4");
		
		Assert.assertNull(o);
		
		o = c.get("o4");
		
		Assert.assertTrue(o.isCreated());
		
		o = c.clear("o4");
		
		Assert.assertEquals(true, o.isDestroyed());
	}
	
}