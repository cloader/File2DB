package com.sirchen.file2db;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sirchen.file2db.inds.ExcelInDataSource;

public class ExcelDataSourceTest {
	private static CommonInDataSource cds;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cds=new ExcelInDataSource(ExcelDataSourceTest.class.getResource("datasource.xls").getFile());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public final void testGetValue() {
		System.out.println(cds.getValue(0, 0));
		assertEquals(cds.getValue(0, 0),"任务");
	}

	@Test
	public final void testGetLines() {
	}

	@Test
	public final void testGetColumns() {
	}

	@Test
	public final void testGetNames() {
	}

}
