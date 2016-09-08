package org.mentacontainer.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mentacontainer.Container;

public class AutoWiringClassTest {

	@Test
	public void testInject() {
		
		Container c = new MentaContainer();
		c.ioc(FakeSession.class, FakeSession.class)
			.addInitValue("fake session");
		c.autowire(FakeSession.class, "session");
		
		AnotherFakeDAO dao = new AnotherFakeDAO();
		// here the method setSession must be call (not setFakeBeanSession)
		c.inject(dao);
		assertNotNull(dao.getSession());
	}
	
	@Test
	public void testAutoWire() {
		
		Container c = new MentaContainer();
		c.ioc(FakeSession.class, FakeSession.class)
			.addInitValue("fake session");
		c.autowire(FakeSession.class, "session");
		
		// by constructor
		FakeDAO dao = c.get(FakeDAO.class);
		assertNotNull(dao);
		assertNotNull(dao.session());
		assertEquals("fake session", dao.session().getSessionName());
		
		// by setter
		AnotherFakeDAO dao2 = c.get(AnotherFakeDAO.class);
		assertNotNull(dao2);
		assertNotNull(dao2.getSession());
		assertEquals("fake session", dao2.getSession().getSessionName());
		
		// now I already have an AnotherFakeDAO instance, just inject the session for me
		dao2 = new AnotherFakeDAO();
		assertNull(dao2.getSession());
		c.inject(dao2);
		assertNotNull(dao2.getSession());
		assertEquals("fake session", dao2.getSession().getSessionName());
		
		// give me another instance.. you can do it!
		StringBuilder b = c.get(StringBuilder.class);
		assertNotNull(b);
	}
	
	public static class FakeSession {
		
		private String sessionName;
		
		public FakeSession(String name) {
			sessionName = name;
		}

		public String getSessionName() {
			return sessionName;
		}

		public void setSessionName(String sessionName) {
			this.sessionName = sessionName;
		}
	}
	
	public static class FakeDAO {
		
		private final FakeSession session;
		
		public FakeDAO(FakeSession session) {
			this.session = session;
		}
		
		public FakeSession session() {
			return session;
		}
	}
	
	public static class AnotherFakeDAO {
		
		private FakeSession session;
		
		public FakeSession getSession() {
			return session;
		}
		
		public void setSession(FakeSession session) {
			this.session = session;
		}
	}
}
