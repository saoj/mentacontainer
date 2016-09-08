package org.mentacontainer.example;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.mentacontainer.Container;
import org.mentacontainer.impl.MentaContainer;

public class CoreExamplesTest {
	
	@Test
	public void testBeanInitialization() {
		
		Container c = new MentaContainer();
		
		c.ioc("myString1", String.class);
		
		String myString1 = c.get("myString1");
		
		Assert.assertEquals("", myString1); // default constructor...
		
		c.ioc("myString2", String.class).addInitValue("saoj");
		
		String myString2 = c.get("myString2"); // using constructor....
		
		Assert.assertEquals("saoj", myString2);
		
		Assert.assertNotSame(c.get("myString1"), c.get("myString1")); // most be different instances...
		
		// test setters...
		
		c.ioc("myDate1", Date.class).addPropertyValue("hours", 15) // setHours(15)
									.addPropertyValue("minutes", 10) // setMinutes(10)
									.addPropertyValue("seconds", 45); // setSeconds(45)
		
		Date myDate1 = c.get("myDate1");
		
		Assert.assertTrue(myDate1.toString().indexOf("15:10:45") > 0);
		
		// test setter together with constructor...
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1976);
		
		c.ioc("myDate2", Date.class).addInitValue(cal.getTimeInMillis()).addPropertyValue("seconds", 59);
		
		Date myDate2 = c.get("myDate2");
		
		Assert.assertTrue(myDate2.toString().indexOf(":59") > 0);
		Assert.assertTrue(myDate2.toString().indexOf("1976") > 0);
	}
	
	@Test
	public void testDependencies() {
		
		// constructor dependency...
		
		Container c = new MentaContainer();
		
		c.ioc("username", String.class).addInitValue("saoj");

		c.ioc("myString", String.class).addConstructorDependency("username");
		
		String myString = c.get("myString");
		
		Assert.assertEquals("saoj", myString);
		
		// setter dependency...
		
		c.ioc("myBirthdayYear", Integer.class).addInitValue(76);
		
		c.ioc("myBirthday", Date.class).addPropertyDependency("year", "myBirthdayYear").addPropertyValue("month", 0).addPropertyValue("date", 20);
		
		Date myBirthday = c.get("myBirthday");
		
		Assert.assertTrue(myBirthday.toString().indexOf("Jan 20") > 0);
		Assert.assertTrue(myBirthday.toString().indexOf("1976") > 0);
		
		// both...
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1976);

		c.ioc("timeInMillis", Long.class).addInitValue(cal.getTimeInMillis());
		c.ioc("myBirthdayMonth", Integer.class).addInitValue(0);
		
		c.ioc("myBirthday2", Date.class).addConstructorDependency("timeInMillis").addPropertyDependency("month", "myBirthdayMonth").addPropertyValue("date", 20);
		
		myBirthday = c.get("myBirthday2");
		
		Assert.assertTrue(myBirthday.toString().indexOf("Jan 20") > 0);
		Assert.assertTrue(myBirthday.toString().indexOf("1976") > 0);
	}
	
	@Test
	public void testAutoWiring() {
		
		// constructor dependency...
		
		Container c = new MentaContainer();
		
		c.ioc("username", String.class).addInitValue("saoj");

		c.ioc("myString", String.class);
		
		c.autowire("username");
		
		String myString = c.get("myString");
		
		Assert.assertEquals("saoj", myString);
		
		// setter dependency...
		
		c = new MentaContainer();
		
		c.ioc("myBirthdayYear", Integer.class).addInitValue(76);
		
		c.ioc("myBirthday", Date.class).addPropertyValue("month", 0).addPropertyValue("date", 20);
		
		c.autowire("myBirthdayYear", "year");
		
		Date myBirthday = c.get("myBirthday");
		
		Assert.assertTrue(myBirthday.toString().indexOf("Jan 20") > 0);
		Assert.assertTrue(myBirthday.toString().indexOf("1976") > 0);
		
		// both...
		
		c = new MentaContainer();
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1976);

		c.ioc("timeInMillis", Long.class).addInitValue(cal.getTimeInMillis());
		c.ioc("myBirthdayMonth", Integer.class).addInitValue(0);
		
		c.ioc("myBirthday", Date.class).addPropertyValue("date", 20);
		
		c.autowire("timeInMillis");
		c.autowire("myBirthdayMonth", "month");
		
		myBirthday = c.get("myBirthday");
		
		Assert.assertTrue(myBirthday.toString().indexOf("Jan 20") > 0);
		Assert.assertTrue(myBirthday.toString().indexOf("1976") > 0);
		
		// bypass autowireByConstructor by specifying...
		
		c = new MentaContainer();
		
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1976);

		Calendar cal2 = Calendar.getInstance();
		cal2.set(Calendar.YEAR, 1977);
		
		c.ioc("timeInMillis", Long.class).addInitValue(cal.getTimeInMillis());
		c.ioc("timeInMillis2", Long.class).addInitValue(cal2.getTimeInMillis());
		c.ioc("myBirthdayMonth", Integer.class).addInitValue(0);
		
		c.ioc("myBirthday", Date.class).addPropertyValue("date", 20).addConstructorDependency("timeInMillis2");
		
		c.autowire("timeInMillis");
		c.autowire("myBirthdayMonth", "month");
		
		myBirthday = c.get("myBirthday");
		
		Assert.assertTrue(myBirthday.toString().indexOf("Jan 20") > 0);
		Assert.assertTrue(myBirthday.toString().indexOf("1977") > 0);
		
		// bypass autowireBySetter by specifying...
		
		// not supported yet...
		
		// force zero arguments constructor...
		
		c = new MentaContainer();
		
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1976);

		c.ioc("timeInMillis", Long.class).addInitValue(cal.getTimeInMillis());
		c.ioc("myBirthdayMonth", Integer.class).addInitValue(0);
		
		c.ioc("myBirthday", Date.class).addPropertyValue("date", 20).useZeroArgumentConstructor();
		
		c.autowire("timeInMillis");
		c.autowire("myBirthdayMonth", "month");
		
		myBirthday = c.get("myBirthday");
		
		Assert.assertTrue(myBirthday.toString().indexOf("Jan 20") > 0);
		Assert.assertTrue(myBirthday.toString().indexOf("1976") == -1);
	}
}