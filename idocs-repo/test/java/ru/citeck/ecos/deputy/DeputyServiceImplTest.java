package ru.citeck.ecos.deputy;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import ru.citeck.ecos.model.DeputyModel;
import ru.citeck.ecos.orgstruct.OrgMetaService;
import ru.citeck.ecos.orgstruct.OrgStructService;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.test.ApplicationContextHelper;

import java.io.Serializable;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=ApplicationContextHelper.class)
@Transactional
public class DeputyServiceImplTest {

	private DeputyService deputyService;
	private AuthorityService authorityService;
	private NodeService nodeService;
	private PersonService personService;
	private OrgStructService orgStructService;
	private OrgMetaService orgMetaService;
	private AvailabilityService availabilityService;
	
	private static String ROLE_GROUP_TYPE = "role";
	private static String ROLE_GROUP_SUBTYPE = "role-type";
	
	private static String TEST_USER1 = "testDeputyUser1";
	private static String TEST_USER2 = "testDeputyUser2";
	private static String TEST_USER3 = "testDeputyUser3";
	private static String TEST_ROLE1 = "GROUP_testDeputyRole1";
	private static String TEST_ROLE2 = "GROUP_testDeputyRole2";
	private static String TEST_ROLE3 = "GROUP_testDeputyRole3";
	
	private static List<String> TEST_USERS1 = Collections.singletonList(TEST_USER1);
	private static List<String> TEST_USERS2 = Collections.singletonList(TEST_USER2);
	private static List<String> TEST_USERS3 = Collections.singletonList(TEST_USER3);
	private static List<String> TEST_USERS12 = addLists(TEST_USERS1, TEST_USERS2);
	private static List<String> TEST_USERS13 = addLists(TEST_USERS1, TEST_USERS3);
	private static List<String> TEST_USERS23 = addLists(TEST_USERS2, TEST_USERS3);
	private static List<String> TEST_USERS123 = addLists(TEST_USERS1, TEST_USERS23);
	
	private static List<String> TEST_ROLES1 = Collections.singletonList(TEST_ROLE1);
	private static List<String> TEST_ROLES2 = Collections.singletonList(TEST_ROLE2);
	private static List<String> TEST_ROLES3 = Collections.singletonList(TEST_ROLE3);
	private static List<String> TEST_ROLES12 = addLists(TEST_ROLES1, TEST_ROLES2);
	private static List<String> TEST_ROLES13 = addLists(TEST_ROLES1, TEST_ROLES3);
	private static List<String> TEST_ROLES23 = addLists(TEST_ROLES2, TEST_ROLES3);
	private static List<String> TEST_ROLES123 = addLists(TEST_ROLES1, TEST_ROLES23);
	
	@Before
	public void beforeTest() {
		
		ApplicationContext context = ApplicationContextHelper.getApplicationContext();
		
		ServiceRegistry serviceRegistry = context.getBean("ServiceRegistry", ServiceRegistry.class);
		
		authorityService = serviceRegistry.getAuthorityService();
		nodeService = serviceRegistry.getNodeService();
		personService = serviceRegistry.getPersonService();
		orgStructService = (OrgStructService) serviceRegistry.getService(CiteckServices.ORG_STRUCT_SERVICE);
		orgMetaService = (OrgMetaService) serviceRegistry.getService(CiteckServices.ORG_META_SERVICE);
		availabilityService = (AvailabilityService) serviceRegistry.getService(CiteckServices.AVAILABILITY_SERVICE);
		deputyService = (DeputyService) serviceRegistry.getService(CiteckServices.DEPUTY_SERVICE);

		AuthenticationUtil.setRunAsUserSystem();
		
		// create test org.structure
		orgMetaService.createSubType(ROLE_GROUP_TYPE, ROLE_GROUP_SUBTYPE);
		int shortNameIndex = AuthorityType.GROUP.getPrefixString().length();
		authorityService.createAuthority(AuthorityType.GROUP, TEST_ROLE1.substring(shortNameIndex));
		authorityService.createAuthority(AuthorityType.GROUP, TEST_ROLE2.substring(shortNameIndex));
		authorityService.createAuthority(AuthorityType.GROUP, TEST_ROLE3.substring(shortNameIndex));
		orgStructService.createTypedGroup(ROLE_GROUP_TYPE, ROLE_GROUP_SUBTYPE, TEST_ROLE1);
		orgStructService.createTypedGroup(ROLE_GROUP_TYPE, ROLE_GROUP_SUBTYPE, TEST_ROLE2);
		orgStructService.createTypedGroup(ROLE_GROUP_TYPE, ROLE_GROUP_SUBTYPE, TEST_ROLE3);
		Map<QName, Serializable> personProperties = new HashMap<QName, Serializable>(1);
		personProperties.put(ContentModel.PROP_USERNAME, TEST_USER1);
		personService.createPerson(personProperties);
		personProperties.put(ContentModel.PROP_USERNAME, TEST_USER2);
		personService.createPerson(personProperties);
		personProperties.put(ContentModel.PROP_USERNAME, TEST_USER3);
		personService.createPerson(personProperties);
		authorityService.addAuthority(TEST_ROLE1, TEST_USER1);
		authorityService.addAuthority(TEST_ROLE2, TEST_USER2);
		authorityService.addAuthority(TEST_ROLE3, TEST_USER3);
		
		NodeRef role1 = authorityService.getAuthorityNodeRef(TEST_ROLE1);
		nodeService.setProperty(role1, DeputyModel.PROP_MANAGED_BY_MEMBERS, true);
	}
	
	@Test
	public void test() {
		
		List<String> deputies = null;
		
		List<String> roles = null;
		
		
		// check user deputies manipulation
		
		deputies = deputyService.getUserDeputies(TEST_USER1);
		assertSetEquals(deputies, Collections.emptyList());
		
		deputies = deputyService.getUserDeputies(TEST_USER2);
		assertSetEquals(deputies, Collections.emptyList());
		
		deputyService.removeUserDeputies(TEST_USER1, TEST_USERS2);
		deputies = deputyService.getUserDeputies(TEST_USER1);
		assertSetEquals(deputies, Collections.emptyList());
		
		deputyService.removeUserDeputies(TEST_USER2, TEST_USERS3);
		deputies = deputyService.getUserDeputies(TEST_USER2);
		assertSetEquals(deputies, Collections.emptyList());
		
		deputyService.addUserDeputies(TEST_USER1, TEST_USERS2);
		deputies = deputyService.getUserDeputies(TEST_USER1);
		assertSetEquals(deputies, TEST_USERS2);
		
		deputyService.addUserDeputies(TEST_USER2, TEST_USERS3);
		deputies = deputyService.getUserDeputies(TEST_USER2);
		assertSetEquals(deputies, TEST_USERS3);
		
		deputyService.addUserDeputies(TEST_USER1, TEST_USERS23);
		deputies = deputyService.getUserDeputies(TEST_USER1);
		assertSetEquals(deputies, TEST_USERS23);
		
		deputyService.addUserDeputies(TEST_USER1, TEST_USERS3);
		deputies = deputyService.getUserDeputies(TEST_USER1);
		assertSetEquals(deputies, TEST_USERS23);
		
		deputyService.removeUserDeputies(TEST_USER2, TEST_USERS3);
		deputies = deputyService.getUserDeputies(TEST_USER2);
		assertSetEquals(deputies, Collections.emptyList());
		
		deputyService.removeUserDeputies(TEST_USER1, TEST_USERS3);
		deputies = deputyService.getUserDeputies(TEST_USER1);
		assertSetEquals(deputies, TEST_USERS2);
		
		deputyService.removeUserDeputies(TEST_USER1, TEST_USERS2);
		deputies = deputyService.getUserDeputies(TEST_USER1);
		assertSetEquals(deputies, Collections.emptyList());
		
		// check role deputies manipulation
		
		deputies = deputyService.getRoleDeputies(TEST_ROLE1);
		assertSetEquals(deputies, Collections.emptyList());
		
		deputyService.addRoleDeputies(TEST_ROLE1, TEST_USERS2);
		deputies = deputyService.getRoleDeputies(TEST_ROLE1);
		assertSetEquals(deputies, TEST_USERS2);
		
		deputyService.removeRoleDeputies(TEST_ROLE1, TEST_USERS3);
		deputies = deputyService.getRoleDeputies(TEST_ROLE1);
		assertSetEquals(deputies, TEST_USERS2);
		
		deputyService.removeRoleDeputies(TEST_ROLE1, TEST_USERS2);
		deputies = deputyService.getRoleDeputies(TEST_ROLE1);
		assertSetEquals(deputies, Collections.emptyList());
		
		// check isRoleDeputiedByMembers method
		// check isRoleDeputiedByUser method
		// check getRolesDeputiedByUser method
		
		assertTrue (deputyService.isRoleDeputiedByMembers(TEST_ROLE1));
		assertFalse(deputyService.isRoleDeputiedByMembers(TEST_ROLE2));
		assertFalse(deputyService.isRoleDeputiedByMembers(TEST_ROLE3));
		
		assertTrue (deputyService.isRoleDeputiedByUser(TEST_ROLE1, AuthenticationUtil.getAdminUserName()));
		assertTrue (deputyService.isRoleDeputiedByUser(TEST_ROLE1, TEST_USER1));
		assertFalse(deputyService.isRoleDeputiedByUser(TEST_ROLE1, TEST_USER2));
		assertFalse(deputyService.isRoleDeputiedByUser(TEST_ROLE1, TEST_USER3));
		
		assertTrue (deputyService.isRoleDeputiedByUser(TEST_ROLE2, AuthenticationUtil.getAdminUserName()));
		assertFalse(deputyService.isRoleDeputiedByUser(TEST_ROLE2, TEST_USER1));
		assertFalse(deputyService.isRoleDeputiedByUser(TEST_ROLE2, TEST_USER2));
		assertFalse(deputyService.isRoleDeputiedByUser(TEST_ROLE2, TEST_USER3));
		
		assertSetEquals(deputyService.getRolesDeputiedByUser(TEST_USER1), TEST_ROLES1);
		assertSetEquals(deputyService.getRolesDeputiedByUser(TEST_USER2), Collections.emptySet());
		assertSetEquals(deputyService.getRolesDeputiedByUser(TEST_USER3), Collections.emptySet());

		// add authority
		authorityService.addAuthority(TEST_ROLE1, TEST_USER2);
		assertTrue (deputyService.isRoleDeputiedByUser(TEST_ROLE1, TEST_USER2));		
		assertSetEquals(deputyService.getRolesDeputiedByUser(TEST_USER2), TEST_ROLES1);
		
		// add deputation
		deputyService.addRoleDeputies(TEST_ROLE1, TEST_USERS2);
		assertFalse(deputyService.isRoleDeputiedByUser(TEST_ROLE1, TEST_USER2));
		assertSetEquals(deputyService.getRolesDeputiedByUser(TEST_USER2), Collections.emptySet());
		
		// remove deputation
		deputyService.removeRoleDeputies(TEST_ROLE1, TEST_USERS2);
// TODO should deputy be removed from role automatically ?
//		assertTrue (deputyService.isRoleDeputiedByUser(TEST_ROLE1, TEST_USER2));
		
		// remove authority
		authorityService.removeAuthority(TEST_ROLE1, TEST_USER2);
		assertFalse(deputyService.isRoleDeputiedByUser(TEST_ROLE1, TEST_USER2));
		assertSetEquals(deputyService.getRolesDeputiedByUser(TEST_USER2), Collections.emptySet());
		
		// check isRoleDeputiedToUser method
		// check getRolesDeputiedToUser method
		
		assertFalse(deputyService.isRoleDeputiedToUser(TEST_ROLE1, TEST_USER2));
		assertSetEquals(deputyService.getRolesDeputiedToUser(TEST_USER2), Collections.emptySet());
		
		deputyService.addRoleDeputies(TEST_ROLE1, TEST_USERS2);
		assertTrue (deputyService.isRoleDeputiedToUser(TEST_ROLE1, TEST_USER2));
		assertSetEquals(deputyService.getRolesDeputiedToUser(TEST_USER2), TEST_ROLES1);
		
		deputyService.removeRoleDeputies(TEST_ROLE1, TEST_USERS2);
		assertFalse(deputyService.isRoleDeputiedToUser(TEST_ROLE1, TEST_USER2));
		assertSetEquals(deputyService.getRolesDeputiedToUser(TEST_USER2), Collections.emptySet());
		
		// check getUserRoles method
		
		roles = deputyService.getUserRoles(TEST_USER1);
		assertSetEquals(roles, TEST_ROLES1);
		
		authorityService.addAuthority(TEST_ROLE2, TEST_USER1);
		roles = deputyService.getUserRoles(TEST_USER1);
		assertSetEquals(roles, TEST_ROLES12);

		deputyService.addRoleDeputies(TEST_ROLE2, TEST_USERS1);
		roles = deputyService.getUserRoles(TEST_USER1);
		assertSetEquals(roles, TEST_ROLES1);
		
		deputyService.removeRoleDeputies(TEST_ROLE2, TEST_USERS1);
// TODO should deputy be removed from role automatically ?
//		roles = deputyService.getUserRoles(TEST_USER1);
//		assertSetEquals(roles, TEST_ROLES12);
		
		authorityService.removeAuthority(TEST_ROLE2, TEST_USER1);
		roles = deputyService.getUserRoles(TEST_USER1);
		assertSetEquals(roles, TEST_ROLES1);
		
		//
		// check interface for deputy listeners
		//
		
		// check userAvailabilityChanged method
		// check userMembershipChanged method
		// assume that RoleMembershipDeputyListener is enabled
		
		// setup: user1 is full member, user2 is deputy
		assertSetEquals(deputyService.getRoleDeputies(TEST_ROLE1), Collections.emptySet());
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS1);
		deputyService.addRoleDeputies(TEST_ROLE1, TEST_USERS2);
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS1);
		
		// step 1: user1 is unavailable, permissions should be granted to user2 
		availabilityService.setUserAvailability(TEST_USER1, false);
		deputyService.userAvailabilityChanged(TEST_USER1);
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS12);
		
		// step 2: user3 is added as a full member, permissions should be revoked from user2
		authorityService.addAuthority(TEST_ROLE1, TEST_USER3);
		deputyService.userMembershipChanged(TEST_USER3, TEST_ROLE1);
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS13);
		
		// step 3: user3 is removed from role, permissions should be granted to user2
		authorityService.removeAuthority(TEST_ROLE1, TEST_USER3);
		deputyService.userMembershipChanged(TEST_USER3, TEST_ROLE1);
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS12);
		
		// step 4: user2
		availabilityService.setUserAvailability(TEST_USER1, true);
		deputyService.userAvailabilityChanged(TEST_USER1);
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS1);

		// remove setup
		deputyService.removeRoleDeputies(TEST_ROLE1, TEST_USERS2);
		assertSetEquals(deputyService.getRoleDeputies(TEST_ROLE1), Collections.emptySet());
		
	}
	
	@After
	public void afterTest() {
		
		try { authorityService.deleteAuthority(TEST_ROLE1); } catch(Exception e) { /* ignore */ }
		try { authorityService.deleteAuthority(TEST_ROLE2); } catch(Exception e) { /* ignore */ }
		try { authorityService.deleteAuthority(TEST_ROLE3); } catch(Exception e) { /* ignore */ }
		try { personService.deletePerson(TEST_USER1); } catch(Exception e) { /* ignore */ }
		try { personService.deletePerson(TEST_USER2); } catch(Exception e) { /* ignore */ }
		try { personService.deletePerson(TEST_USER3); } catch(Exception e) { /* ignore */ }
	
		AuthenticationUtil.clearCurrentSecurityContext();

	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void assertSetEquals(Collection a, Collection b) {
		Set sa = convertToSet(a);
		Set sb = convertToSet(b);
		assertEquals(sa, sb);
	}
	
	private static <T> List<T> addLists(List<T> a, List<T> b) {
		List<T> c = new ArrayList<T>(a.size() + b.size());
		c.addAll(a);
		c.addAll(b);
		return c;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T> Set<T> convertToSet(Collection<T> c) {
		Set<T> s = null;
		if(c instanceof Set) {
			s = (Set) c;
		} else {
			s = new HashSet<T>(c.size());
			s.addAll(c);
		}
		return s;
	}
	
}
