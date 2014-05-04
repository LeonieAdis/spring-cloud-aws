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

package org.elasticspring.jdbc.rds;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.GetUserResult;
import com.amazonaws.services.identitymanagement.model.User;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.ListTagsForResourceRequest;
import com.amazonaws.services.rds.model.ListTagsForResourceResult;
import com.amazonaws.services.rds.model.Tag;
import org.elasticspring.core.env.ResourceIdResolver;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.Map;

/**
 * @author Agim Emruli
 */
public class AmazonRdsDataSourceUserTagsFactoryBeanTest {

	@Test
	public void getObject_instanceWithTagsConfiguredWithCustomResourceResolverAndCustomRegion_mapWithTagsReturned() throws Exception {
		//Arrange
		AmazonRDS amazonRds = Mockito.mock(AmazonRDS.class);
		ResourceIdResolver resourceIdResolver = Mockito.mock(ResourceIdResolver.class);
		AmazonIdentityManagement amazonIdentityManagement = Mockito.mock(AmazonIdentityManagement.class);
		AmazonRdsDataSourceUserTagsFactoryBean factoryBean = new AmazonRdsDataSourceUserTagsFactoryBean(amazonRds, "test", amazonIdentityManagement);
		factoryBean.setResourceIdResolver(resourceIdResolver);
		factoryBean.setRegion(Region.getRegion(Regions.EU_WEST_1));

		Mockito.when(resourceIdResolver.resolveToPhysicalResourceId("test")).thenReturn("stack-test");
		Mockito.when(amazonIdentityManagement.getUser()).thenReturn(new GetUserResult().withUser(new User("/", "aemruli", "123456789012", "arn:aws:iam::1234567890:user/aemruli", new Date())));
		Mockito.when(amazonRds.listTagsForResource(new ListTagsForResourceRequest().withResourceName("arn:aws:rds:eu-west-1:1234567890:db:stack-test"))).thenReturn(new ListTagsForResourceResult().withTagList(
				new Tag().withKey("key1").withValue("value1"),
				new Tag().withKey("key2").withValue("value2")
		));

		//Act
		factoryBean.afterPropertiesSet();
		Map<String,String> userTagMap = factoryBean.getObject();

		//Assert
		Assert.assertEquals("value1", userTagMap.get("key1"));
		Assert.assertEquals("value2", userTagMap.get("key2"));
	}

	@Test
	public void getObject_instanceWithOutTags_emptyMapReturned() throws Exception {
		//Arrange
		AmazonRDS amazonRds = Mockito.mock(AmazonRDS.class);
		ResourceIdResolver resourceIdResolver = Mockito.mock(ResourceIdResolver.class);
		AmazonIdentityManagement amazonIdentityManagement = Mockito.mock(AmazonIdentityManagement.class);
		AmazonRdsDataSourceUserTagsFactoryBean factoryBean = new AmazonRdsDataSourceUserTagsFactoryBean(amazonRds, "test", amazonIdentityManagement);
		factoryBean.setResourceIdResolver(resourceIdResolver);
		factoryBean.setResourceIdResolver(resourceIdResolver);
		factoryBean.setRegion(Region.getRegion(Regions.EU_WEST_1));

		Mockito.when(resourceIdResolver.resolveToPhysicalResourceId("test")).thenReturn("stack-test");
		Mockito.when(amazonIdentityManagement.getUser()).thenReturn(new GetUserResult().withUser(new User("/", "aemruli", "123456789012", "arn:aws:iam::1234567890:user/aemruli", new Date())));
		Mockito.when(amazonRds.listTagsForResource(new ListTagsForResourceRequest().withResourceName("arn:aws:rds:eu-west-1:1234567890:db:stack-test"))).thenReturn(new ListTagsForResourceResult());

		//Act
		factoryBean.afterPropertiesSet();
		Map<String,String> userTagMap = factoryBean.getObject();

		//Assert
		Assert.assertTrue(userTagMap.isEmpty());
	}

	@Test
	public void getObject_instanceWithTagsAndNoResourceIdResolverAndDefaultRegion_mapWithTagsReturned() throws Exception {
		//Arrange
		AmazonRDS amazonRds = Mockito.mock(AmazonRDS.class);
		AmazonIdentityManagement amazonIdentityManagement = Mockito.mock(AmazonIdentityManagement.class);

		AmazonRdsDataSourceUserTagsFactoryBean factoryBean = new AmazonRdsDataSourceUserTagsFactoryBean(amazonRds, "test", amazonIdentityManagement);


		Mockito.when(amazonIdentityManagement.getUser()).thenReturn(new GetUserResult().withUser(new User("/", "aemruli", "123456789012", "arn:aws:iam::1234567890:user/aemruli", new Date())));
		Mockito.when(amazonRds.listTagsForResource(new ListTagsForResourceRequest().withResourceName("arn:aws:rds:us-west-2:1234567890:db:test"))).thenReturn(new ListTagsForResourceResult().withTagList(
				new Tag().withKey("key1").withValue("value1"),
				new Tag().withKey("key2").withValue("value2")
		));

		//Act
		factoryBean.afterPropertiesSet();
		Map<String,String> userTagMap = factoryBean.getObject();

		//Assert
		Assert.assertEquals("value1", userTagMap.get("key1"));
		Assert.assertEquals("value2", userTagMap.get("key2"));
	}
}
