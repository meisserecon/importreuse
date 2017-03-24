package com.meissereconomics.trade.util;

import java.io.Closeable;
import java.util.Properties;

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;

public class Neo4JWrapper implements Closeable {

	private IDBAccess remote;

	public Neo4JWrapper() {
		Properties props = new Properties();
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		remote = DBAccessFactory.createDBAccess(DBType.REMOTE, props, "neo4j", "asdasd");
		System.out.println("Opened");
	}

	public void test() {
		System.out.println(remote.isDatabaseEmpty());
	}

	public void close() {
		remote.close();
		System.out.println("Closed");
	}

	public static void main(String[] args) {
		 try (Neo4JWrapper wrapper = new Neo4JWrapper()) { 
			wrapper.test();
		 }
	}

}
