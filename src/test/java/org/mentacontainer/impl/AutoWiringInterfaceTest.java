package org.mentacontainer.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mentacontainer.Container;

public class AutoWiringInterfaceTest {

	@Test
	public void testAutoWire() {
		
		Container c = new MentaContainer();
		c.ioc(Session.class, FakeSession.class)
			.addInitValue("fake session");
		c.autowire(Session.class);
		
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
	
	public static interface Session {
		public String getSessionName();
	}
	
	public static class FakeSession implements Session {
		
		private String sessionName;
		
		public FakeSession(String name) {
			sessionName = name;
		}

		@Override
		public String getSessionName() {
			return sessionName;
		}

		public void setSessionName(String sessionName) {
			this.sessionName = sessionName;
		}
	}
	
	public static class FakeDAO {
		
		private final Session session;
		
		public FakeDAO(Session session) {
			this.session = session;
		}
		
		public Session session() {
			return session;
		}
	}
	
	public static class AnotherFakeDAO {
		
		private Session session;
		
		public Session getSession() {
			return session;
		}
		
		public void setSession(Session session) {
			this.session = session;
		}
	}
}
