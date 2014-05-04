/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticspring.jdbc.datasource;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

/**
 * @author Agim Emruli
 */
public class ReadOnlyRoutingDataSourceTest {

	@Test
	public void getConnection_NoReadReplicaAvailableNoTransactionActive_returnsDefaultDataSource() throws Exception {

		//Arrange
		DataSource defaultDataSource = Mockito.mock(DataSource.class);
		Connection connection = Mockito.mock(Connection.class);

		Mockito.when(defaultDataSource.getConnection()).thenReturn(connection);

		ReadOnlyRoutingDataSource readOnlyRoutingDataSource = new ReadOnlyRoutingDataSource();
		readOnlyRoutingDataSource.setTargetDataSources(Collections.emptyMap());
		readOnlyRoutingDataSource.setDefaultTargetDataSource(defaultDataSource);
		readOnlyRoutingDataSource.afterPropertiesSet();

		LazyConnectionDataSourceProxy dataSource = new LazyConnectionDataSourceProxy(readOnlyRoutingDataSource);


		//Act
		Connection connectionReturned = dataSource.getConnection();

		//Assert
		Assert.assertSame(connection, ((ConnectionProxy) connectionReturned).getTargetConnection());
	}

	@Test
	public void getConnection_NoReadReplicaAvailableReadOnlyTransactionActive_returnsDefaultDataSource() throws Exception {

		//Arrange
		DataSource defaultDataSource = Mockito.mock(DataSource.class);
		Connection connection = Mockito.mock(Connection.class);

		Mockito.when(defaultDataSource.getConnection()).thenReturn(connection);

		ReadOnlyRoutingDataSource readOnlyRoutingDataSource = new ReadOnlyRoutingDataSource();
		readOnlyRoutingDataSource.setTargetDataSources(Collections.emptyMap());
		readOnlyRoutingDataSource.setDefaultTargetDataSource(defaultDataSource);
		readOnlyRoutingDataSource.afterPropertiesSet();

		final LazyConnectionDataSourceProxy dataSource = new LazyConnectionDataSourceProxy(readOnlyRoutingDataSource);

		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setReadOnly(true);

		TransactionTemplate transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource), transactionDefinition);

		//Act
		Connection connectionReturned = transactionTemplate.execute(new TransactionCallback<Connection>() {

			@Override
			public Connection doInTransaction(TransactionStatus status) {
				try {
					return ((ConnectionProxy) dataSource.getConnection()).getTargetConnection();
				} catch (SQLException e) {
					Assert.fail(e.getMessage());
				}
				return null;
			}
		});

		//Assert
		Assert.assertSame(connection, connectionReturned);
	}

	@Test
	public void getConnection_ReadReplicaAvailableReadOnlyTransactionActive_returnsReadReplicaDataSource() throws Exception {

		//Arrange
		DataSource defaultDataSource = Mockito.mock(DataSource.class);
		Connection connection = Mockito.mock(Connection.class);

		DataSource readOnlyDataSource = Mockito.mock(DataSource.class);
		Connection readOnlyConnection = Mockito.mock(Connection.class);


		Mockito.when(readOnlyDataSource.getConnection()).thenReturn(readOnlyConnection);
		Mockito.when(defaultDataSource.getConnection()).thenReturn(connection);

		ReadOnlyRoutingDataSource readOnlyRoutingDataSource = new ReadOnlyRoutingDataSource();
		readOnlyRoutingDataSource.setTargetDataSources(Collections.<Object,Object>singletonMap("read1", readOnlyDataSource));
		readOnlyRoutingDataSource.setDefaultTargetDataSource(defaultDataSource);
		readOnlyRoutingDataSource.afterPropertiesSet();

		final LazyConnectionDataSourceProxy dataSource = new LazyConnectionDataSourceProxy(readOnlyRoutingDataSource);

		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setReadOnly(true);

		TransactionTemplate transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource), transactionDefinition);

		//Act
		Connection connectionReturned = transactionTemplate.execute(new TransactionCallback<Connection>() {

			@Override
			public Connection doInTransaction(TransactionStatus status) {
				try {
					return ((ConnectionProxy) dataSource.getConnection()).getTargetConnection();
				} catch (SQLException e) {
					Assert.fail(e.getMessage());
				}
				return null;
			}
		});

		//Assert
		Assert.assertSame(readOnlyConnection, connectionReturned);
	}

	@Test
	public void getConnection_ReadReplicaAvailableWriteTransactionActive_returnsDefaultDataSource() throws Exception {

		//Arrange
		DataSource defaultDataSource = Mockito.mock(DataSource.class);
		Connection connection = Mockito.mock(Connection.class);

		DataSource readOnlyDataSource = Mockito.mock(DataSource.class);
		Connection readOnlyConnection = Mockito.mock(Connection.class);


		Mockito.when(readOnlyDataSource.getConnection()).thenReturn(readOnlyConnection);
		Mockito.when(defaultDataSource.getConnection()).thenReturn(connection);

		ReadOnlyRoutingDataSource readOnlyRoutingDataSource = new ReadOnlyRoutingDataSource();
		readOnlyRoutingDataSource.setTargetDataSources(Collections.<Object,Object>singletonMap("read1", readOnlyDataSource));
		readOnlyRoutingDataSource.setDefaultTargetDataSource(defaultDataSource);
		readOnlyRoutingDataSource.afterPropertiesSet();

		final LazyConnectionDataSourceProxy dataSource = new LazyConnectionDataSourceProxy(readOnlyRoutingDataSource);

		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setReadOnly(false);

		TransactionTemplate transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource), transactionDefinition);

		//Act
		Connection connectionReturned = transactionTemplate.execute(new TransactionCallback<Connection>() {

			@Override
			public Connection doInTransaction(TransactionStatus status) {
				try {
					return ((ConnectionProxy) dataSource.getConnection()).getTargetConnection();
				} catch (SQLException e) {
					Assert.fail(e.getMessage());
				}
				return null;
			}
		});

		//Assert
		Assert.assertSame(connection, connectionReturned);
	}
}