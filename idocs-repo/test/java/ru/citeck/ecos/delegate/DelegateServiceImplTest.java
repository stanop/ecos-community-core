package ru.citeck.ecos.delegate;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
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

import ru.citeck.ecos.model.DelegateModel;
import ru.citeck.ecos.orgstruct.OrgMetaService;
import ru.citeck.ecos.orgstruct.OrgStructService;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.test.ApplicationContextHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=ApplicationContextHelper.class)
@Transactional
public class DelegateServiceImplTest {

	private DelegateService delegateService;
	private AuthorityService authorityService;
	private NodeService nodeService;
	private PersonService personService;
	private OrgStructService orgStructService;
	private OrgMetaService orgMetaService;
	private AvailabilityService availabilityService;
	
	private static String ROLE_GROUP_TYPE = "role";
	private static String ROLE_GROUP_SUBTYPE = "role-type";
	
	private static String TEST_USER1 = "testDelegateUser1";
	private static String TEST_USER2 = "testDelegateUser2";
	private static String TEST_USER3 = "testDelegateUser3";
	private static String TEST_ROLE1 = "GROUP_testDelegateRole1";
	private static String TEST_ROLE2 = "GROUP_testDelegateRole2";
	private static String TEST_ROLE3 = "GROUP_testDelegateRole3";
	
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
		delegateService = (DelegateService) serviceRegistry.getService(CiteckServices.DELEGATE_SERVICE);

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
		nodeService.setProperty(role1, DelegateModel.PROP_MANAGED_BY_MEMBERS, true);
	}
	
	@Test
	public void test() {
		
		List<String> delegates = null;
		
		List<String> roles = null;
		
		
		// check user delegates manipulation
		
		delegates = delegateService.getUserDelegates(TEST_USER1);
		assertSetEquals(delegates, Collections.emptyList());
		
		delegates = delegateService.getUserDelegates(TEST_USER2);
		assertSetEquals(delegates, Collections.emptyList());
		
		delegateService.removeUserDelegates(TEST_USER1, TEST_USERS2);
		delegates = delegateService.getUserDelegates(TEST_USER1);
		assertSetEquals(delegates, Collections.emptyList());
		
		delegateService.removeUserDelegates(TEST_USER2, TEST_USERS3);
		delegates = delegateService.getUserDelegates(TEST_USER2);
		assertSetEquals(delegates, Collections.emptyList());
		
		delegateService.addUserDelegates(TEST_USER1, TEST_USERS2);
		delegates = delegateService.getUserDelegates(TEST_USER1);
		assertSetEquals(delegates, TEST_USERS2);
		
		delegateService.addUserDelegates(TEST_USER2, TEST_USERS3);
		delegates = delegateService.getUserDelegates(TEST_USER2);
		assertSetEquals(delegates, TEST_USERS3);
		
		delegateService.addUserDelegates(TEST_USER1, TEST_USERS23);
		delegates = delegateService.getUserDelegates(TEST_USER1);
		assertSetEquals(delegates, TEST_USERS23);
		
		delegateService.addUserDelegates(TEST_USER1, TEST_USERS3);
		delegates = delegateService.getUserDelegates(TEST_USER1);
		assertSetEquals(delegates, TEST_USERS23);
		
		delegateService.removeUserDelegates(TEST_USER2, TEST_USERS3);
		delegates = delegateService.getUserDelegates(TEST_USER2);
		assertSetEquals(delegates, Collections.emptyList());
		
		delegateService.removeUserDelegates(TEST_USER1, TEST_USERS3);
		delegates = delegateService.getUserDelegates(TEST_USER1);
		assertSetEquals(delegates, TEST_USERS2);
		
		delegateService.removeUserDelegates(TEST_USER1, TEST_USERS2);
		delegates = delegateService.getUserDelegates(TEST_USER1);
		assertSetEquals(delegates, Collections.emptyList());
		
		// check role delegates manipulation
		
		delegates = delegateService.getRoleDelegates(TEST_ROLE1);
		assertSetEquals(delegates, Collections.emptyList());
		
		delegateService.addRoleDelegates(TEST_ROLE1, TEST_USERS2);
		delegates = delegateService.getRoleDelegates(TEST_ROLE1);
		assertSetEquals(delegates, TEST_USERS2);
		
		delegateService.removeRoleDelegates(TEST_ROLE1, TEST_USERS3);
		delegates = delegateService.getRoleDelegates(TEST_ROLE1);
		assertSetEquals(delegates, TEST_USERS2);
		
		delegateService.removeRoleDelegates(TEST_ROLE1, TEST_USERS2);
		delegates = delegateService.getRoleDelegates(TEST_ROLE1);
		assertSetEquals(delegates, Collections.emptyList());
		
		// check isRoleDelegatedByMembers method
		// check isRoleDelegatedByUser method
		// check getRolesDelegatedByUser method
		
		assertTrue (delegateService.isRoleDelegatedByMembers(TEST_ROLE1));
		assertFalse(delegateService.isRoleDelegatedByMembers(TEST_ROLE2));
		assertFalse(delegateService.isRoleDelegatedByMembers(TEST_ROLE3));
		
		assertTrue (delegateService.isRoleDelegatedByUser(TEST_ROLE1, AuthenticationUtil.getAdminUserName()));
		assertTrue (delegateService.isRoleDelegatedByUser(TEST_ROLE1, TEST_USER1));
		assertFalse(delegateService.isRoleDelegatedByUser(TEST_ROLE1, TEST_USER2));
		assertFalse(delegateService.isRoleDelegatedByUser(TEST_ROLE1, TEST_USER3));
		
		assertTrue (delegateService.isRoleDelegatedByUser(TEST_ROLE2, AuthenticationUtil.getAdminUserName()));
		assertFalse(delegateService.isRoleDelegatedByUser(TEST_ROLE2, TEST_USER1));
		assertFalse(delegateService.isRoleDelegatedByUser(TEST_ROLE2, TEST_USER2));
		assertFalse(delegateService.isRoleDelegatedByUser(TEST_ROLE2, TEST_USER3));
		
		assertSetEquals(delegateService.getRolesDelegatedByUser(TEST_USER1), TEST_ROLES1);
		assertSetEquals(delegateService.getRolesDelegatedByUser(TEST_USER2), Collections.emptySet());
		assertSetEquals(delegateService.getRolesDelegatedByUser(TEST_USER3), Collections.emptySet());

		// add authority
		authorityService.addAuthority(TEST_ROLE1, TEST_USER2);
		assertTrue (delegateService.isRoleDelegatedByUser(TEST_ROLE1, TEST_USER2));		
		assertSetEquals(delegateService.getRolesDelegatedByUser(TEST_USER2), TEST_ROLES1);
		
		// add delegation
		delegateService.addRoleDelegates(TEST_ROLE1, TEST_USERS2);
		assertFalse(delegateService.isRoleDelegatedByUser(TEST_ROLE1, TEST_USER2));
		assertSetEquals(delegateService.getRolesDelegatedByUser(TEST_USER2), Collections.emptySet());
		
		// remove delegation
		delegateService.removeRoleDelegates(TEST_ROLE1, TEST_USERS2);
// TODO should delegate be removed from role automatically ?
//		assertTrue (delegateService.isRoleDelegatedByUser(TEST_ROLE1, TEST_USER2));
		
		// remove authority
		authorityService.removeAuthority(TEST_ROLE1, TEST_USER2);
		assertFalse(delegateService.isRoleDelegatedByUser(TEST_ROLE1, TEST_USER2));
		assertSetEquals(delegateService.getRolesDelegatedByUser(TEST_USER2), Collections.emptySet());
		
		// check isRoleDelegatedToUser method
		// check getRolesDelegatedToUser method
		
		assertFalse(delegateService.isRoleDelegatedToUser(TEST_ROLE1, TEST_USER2));
		assertSetEquals(delegateService.getRolesDelegatedToUser(TEST_USER2), Collections.emptySet());
		
		delegateService.addRoleDelegates(TEST_ROLE1, TEST_USERS2);
		assertTrue (delegateService.isRoleDelegatedToUser(TEST_ROLE1, TEST_USER2));
		assertSetEquals(delegateService.getRolesDelegatedToUser(TEST_USER2), TEST_ROLES1);
		
		delegateService.removeRoleDelegates(TEST_ROLE1, TEST_USERS2);
		assertFalse(delegateService.isRoleDelegatedToUser(TEST_ROLE1, TEST_USER2));
		assertSetEquals(delegateService.getRolesDelegatedToUser(TEST_USER2), Collections.emptySet());
		
		// check getUserRoles method
		
		roles = delegateService.getUserRoles(TEST_USER1);
		assertSetEquals(roles, TEST_ROLES1);
		
		authorityService.addAuthority(TEST_ROLE2, TEST_USER1);
		roles = delegateService.getUserRoles(TEST_USER1);
		assertSetEquals(roles, TEST_ROLES12);

		delegateService.addRoleDelegates(TEST_ROLE2, TEST_USERS1);
		roles = delegateService.getUserRoles(TEST_USER1);
		assertSetEquals(roles, TEST_ROLES1);
		
		delegateService.removeRoleDelegates(TEST_ROLE2, TEST_USERS1);
// TODO should delegate be removed from role automatically ?
//		roles = delegateService.getUserRoles(TEST_USER1);
//		assertSetEquals(roles, TEST_ROLES12);
		
		authorityService.removeAuthority(TEST_ROLE2, TEST_USER1);
		roles = delegateService.getUserRoles(TEST_USER1);
		assertSetEquals(roles, TEST_ROLES1);
		
		//
		// check interface for delegate listeners
		//
		
		// check userAvailabilityChanged method
		// check userMembershipChanged method
		// assume that RoleMembershipDelegateListener is enabled
		
		// setup: user1 is full member, user2 is delegate
		assertSetEquals(delegateService.getRoleDelegates(TEST_ROLE1), Collections.emptySet());
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS1);
		delegateService.addRoleDelegates(TEST_ROLE1, TEST_USERS2);
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS1);
		
		// step 1: user1 is unavailable, permissions should be granted to user2 
		availabilityService.setUserAvailability(TEST_USER1, false);
		delegateService.userAvailabilityChanged(TEST_USER1);
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS12);
		
		// step 2: user3 is added as a full member, permissions should be revoked from user2
		authorityService.addAuthority(TEST_ROLE1, TEST_USER3);
		delegateService.userMembershipChanged(TEST_USER3, TEST_ROLE1);
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS13);
		
		// step 3: user3 is removed from role, permissions should be granted to user2
		authorityService.removeAuthority(TEST_ROLE1, TEST_USER3);
		delegateService.userMembershipChanged(TEST_USER3, TEST_ROLE1);
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS12);
		
		// step 4: user2
		availabilityService.setUserAvailability(TEST_USER1, true);
		delegateService.userAvailabilityChanged(TEST_USER1);
		assertSetEquals(authorityService.getContainedAuthorities(AuthorityType.USER, TEST_ROLE1, false), TEST_USERS1);

		// remove setup
		delegateService.removeRoleDelegates(TEST_ROLE1, TEST_USERS2);
		assertSetEquals(delegateService.getRoleDelegates(TEST_ROLE1), Collections.emptySet());
		
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
