package de.vonderbeck.bpm.identity.keycloak;

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;

import java.util.List;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;

/**
 * Tests group queries.
 */
public class KeycloakGroupQueryTest extends KeycloakIdentityProviderTest {

	public void testQueryNoFilter() {
		List<Group> groupList = identityService.createGroupQuery().list();

		assertEquals(4, groupList.size());
	}

	public void testFilterByGroupId() {
		Group group = identityService.createGroupQuery().groupId(GROUP_ID_ADMIN).singleResult();
		assertNotNull(group);

		// validate result
		assertEquals(GROUP_ID_ADMIN, group.getId());
		assertEquals("camunda-admin", group.getName());
		assertEquals("SYSTEM", group.getType());

		group = identityService.createGroupQuery().groupId("whatever").singleResult();
		assertNull(group);
	}

	public void testFilterByUserId() {
		List<Group> result = identityService.createGroupQuery().groupMember("camunda@accso.de").list();
		assertEquals(1, result.size());
	}
	
	/* The REST API of Keycloak does not deliver group attributes :-(
	public void testFilterByGroupType() {
		List<Group> result = identityService.createGroupQuery().groupType("SYSTEM").list();
		assertEquals(2, result.size());
	}
	*/
	
	public void testFilterByGroupIdIn() {
		List<Group> groups = identityService.createGroupQuery()
				.groupIdIn(GROUP_ID_ADMIN, GROUP_ID_MANAGER)
				.list();

		assertEquals(2, groups.size());
		for (Group group : groups) {
			if (!group.getName().equals("camunda-admin") && !group.getName().equals("manager")) {
				fail();
			}
		}
	}

	public void testFilterByGroupIdInAndType() {
		Group group = identityService.createGroupQuery()
				.groupIdIn(GROUP_ID_ADMIN, GROUP_ID_MANAGER)
				.groupType("WORKFLOW")
				.singleResult();
		assertNotNull(group);
		assertEquals("manager", group.getName());
		
		group = identityService.createGroupQuery()
				.groupIdIn(GROUP_ID_ADMIN, GROUP_ID_MANAGER)
				.groupType("SYSTEM")
				.singleResult();
		assertNotNull(group);
		assertEquals("camunda-admin", group.getName());
	}

	public void testFilterByGroupIdInAndUserId() {
		Group group = identityService.createGroupQuery()
				.groupIdIn(GROUP_ID_ADMIN, GROUP_ID_MANAGER)
				.groupMember("camunda@accso.de")
				.singleResult();
		assertNotNull(group);
		assertEquals("camunda-admin", group.getName());
	}
	
	public void testFilterByGroupName() {
		Group group = identityService.createGroupQuery().groupName("manager").singleResult();
		assertNotNull(group);

		// validate result
		assertEquals(GROUP_ID_MANAGER, group.getId());
		assertEquals("manager", group.getName());

		group = identityService.createGroupQuery().groupName("whatever").singleResult();
		assertNull(group);
	}

	public void testFilterByGroupNameLike() {
		Group group = identityService.createGroupQuery().groupNameLike("manage*").singleResult();
		assertNotNull(group);

		// validate result
		assertEquals(GROUP_ID_MANAGER, group.getId());
		assertEquals("manager", group.getName());

		group = identityService.createGroupQuery().groupNameLike("what*").singleResult();
		assertNull(group);
	}

	public void testFilterByGroupMember() {
		List<Group> list = identityService.createGroupQuery().groupMember("camunda@accso.de").list();
		assertEquals(1, list.size());
		list = identityService.createGroupQuery().groupMember("Gunnar.von-der-Beck@accso.de").list();
		assertEquals(2, list.size());
		list = identityService.createGroupQuery().groupMember("hans.mustermann@tradermail.info").list();
		assertEquals(1, list.size());
		list = identityService.createGroupQuery().groupMember("non-existing").list();
		assertEquals(0, list.size());
	}

	protected void createGrantAuthorization(Resource resource, String resourceId, String userId, Permission... permissions) {
		Authorization authorization = createAuthorization(AUTH_TYPE_GRANT, resource, resourceId);
		authorization.setUserId(userId);
		for (Permission permission : permissions) {
			authorization.addPermission(permission);
		}
		authorizationService.saveAuthorization(authorization);
	}

	protected Authorization createAuthorization(int type, Resource resource, String resourceId) {
		Authorization authorization = authorizationService.createNewAuthorization(type);

		authorization.setResource(resource);
		if (resourceId != null) {
			authorization.setResourceId(resourceId);
		}

		return authorization;
	}

}
