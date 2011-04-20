package org.openmrs.module.addresshierarchy.db.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.addresshierarchy.AddressHierarchy;
import org.openmrs.module.addresshierarchy.AddressHierarchyType;
import org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO;

/**
 * The Class HibernateAddressHierarchyDAO which links to the tables address_hierarchy,
 * address_hierarchy_type and person_address. This class does the functions of storing and
 * retrieving addresses.
 */
public class HibernateAddressHierarchyDAO implements AddressHierarchyDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * Gets a count of the number of entries in the address hierarchy table
	 */
	@SuppressWarnings("unchecked")
	public int getAddressHierarchyCount() {
		int x = 0;
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria(AddressHierarchy.class);
		List<Integer> rows = c.setProjection((Projections.rowCount())).list();
		if (rows.size() > 0) {
			x = rows.get(0).intValue();
		}
		return x;
		
	}
	
	public AddressHierarchy getAddressHierarchy(int addressHierarchyId) {
		Session session = sessionFactory.getCurrentSession();
		AddressHierarchy ah = (AddressHierarchy) session.load(AddressHierarchy.class, addressHierarchyId);
		return ah;
	}
	
	public void saveAddressHierarchy(AddressHierarchy ah) {
		Session session = sessionFactory.getCurrentSession();
		session.save(ah);
		session.flush();
		session.clear();
	}
	
	/**
	 * Adds an address hierarchy Location
	 * 
	 * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#addLocation(int,
	 *      java.lang.String, int)
	 */
	public AddressHierarchy addLocation(int parentId, String name, int typeId) {
		AddressHierarchy ah = null;
		if (parentId != -1) {
			Session session = sessionFactory.getCurrentSession();
			ah = new AddressHierarchy();
			ah.setLocationName(name);
			if (typeId != -1) {
				ah.setHierarchyType(getHierarchyType(typeId));
			} else {
				ah.setHierarchyType(getAddressHierarchy(parentId).getHierarchyType().getChildType());
			}
			ah.setParent(getAddressHierarchy(parentId));
			session.save(ah);
		}
		
		return ah;
	}
	
	/**
	 * Changes the locations name to <code>newName</code>
	 * 
	 * @param locationId
	 * @param newName
	 */
	@SuppressWarnings("unchecked")
	public AddressHierarchy editLocationName(Integer locationId, String newName) {
		// begin transaction
		Session session = sessionFactory.getCurrentSession();
		
		// get the location by id
		Criteria c = session.createCriteria(AddressHierarchy.class);
		c.add(Restrictions.idEq(locationId));
		List<AddressHierarchy> hierarchyList = c.list();
		AddressHierarchy ah = null;
		
		// change the name if we have an ah
		if (hierarchyList != null && hierarchyList.size() > 0) {
			ah = hierarchyList.get(0);
			ah.setLocationName(newName);
		}
		
		// close the transaction
		return ah;
	}
	
	public AddressHierarchy getLocation(int addressHierarchyId) {
		return getAddressHierarchy(addressHierarchyId);
	}
	
	@SuppressWarnings("unchecked")
	public AddressHierarchy getLocationFromUserGenId(String userGeneratedId) {
		AddressHierarchy ah = null;
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(AddressHierarchy.class);
		
		List<AddressHierarchy> list = criteria.add(Restrictions.eq("userGeneratedId", userGeneratedId)).list();
		if (list != null && list.size() > 0) {
			ah = list.get(0);
		}
		return ah;
	}
	
	@SuppressWarnings("unchecked")
	public List<AddressHierarchyType> getAddressHierarchyTypes() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(AddressHierarchyType.class);
		return criteria.list();
	}
	
	public AddressHierarchyType getHierarchyType(int typeId) {
		Session session = sessionFactory.getCurrentSession();
		AddressHierarchyType type = (AddressHierarchyType) session.load(AddressHierarchyType.class, typeId);
		
		return type;
	}
	
	public List<AddressHierarchy> getLeafNodes(AddressHierarchy ah) {
		List<AddressHierarchy> leafList = new ArrayList<AddressHierarchy>();
		getLowestLevel(ah, leafList);
		return leafList;
	}
	
	/**
	 * Recursively finds leaf nodes of ah
	 */
	private List<AddressHierarchy> getLowestLevel(AddressHierarchy ah, List<AddressHierarchy> leafList) {
		List<AddressHierarchy> children = getNextComponent(ah.getAddressHierarchyId());
		if (children.size() > 0) {
			for (AddressHierarchy addressHierarchy : children) {
				getLowestLevel(addressHierarchy, leafList);
			}
		} else {
			leafList.add(ah);
		}
		return children;
	}
	
	/**
	 * Method used to get the child locations.
	 * 
	 * @param parent_type_Id the parent_type_ id
	 * @param location_Name the location_ name
	 * @param parent_Id the parent_ id
	 * @return the next component in an array
	 * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getNextComponent(java.lang.Integer,
	 *      java.lang.String, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<AddressHierarchy> getNextComponent(Integer locationId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(AddressHierarchy.class);
		List<AddressHierarchy> list = criteria.createCriteria("parent").add(
		    Restrictions.eq("addressHierarchyId", locationId)).list();
		return list;
	}
	
	/**
	 * Searches for locations like the <code>searchString</code> Can restrict to a certain type by
	 * specifying a type id
	 */
	@SuppressWarnings("unchecked")
	public List<AddressHierarchy> searchHierarchy(String searchString, int locationTypeId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(AddressHierarchy.class);
		criteria.add(Restrictions.like("locationName", searchString, MatchMode.ANYWHERE));
		List<AddressHierarchy> hierarchyList;
		if (locationTypeId != -1) {
			criteria.createCriteria("hierarchyType").add(Restrictions.eq("typeId", locationTypeId));
		}
		
		hierarchyList = criteria.list();
		return hierarchyList;
	}
	
	public void associateCoordinates(AddressHierarchy ah, double latitude, double longitude) {
		ah.setLatitude(latitude);
		ah.setLongitude(longitude);
		Session session = sessionFactory.getCurrentSession();
		session.update(ah);
	}
	
	@SuppressWarnings( { "unchecked" })
	public List<AddressHierarchy> getTopOfHierarchyList() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(AddressHierarchy.class);
		List list = criteria.add(Restrictions.isNull("parent")).createCriteria("hierarchyType").add(
		    Restrictions.isNull("parentType")).list();
		
		return list;
	}
	
	public void truncateHierarchyTables() {
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery("truncate table address_hierarchy").executeUpdate();
		session.createSQLQuery("truncate table address_hierarchy_type").executeUpdate();
	}
	
	/**
	 * The following methods are deprecated and just exist to provide backwards compatibility to
	 * Rwanda Address Hierarchy module
	 */
	
	@Deprecated
	public void initializeRwandaHierarchyTables() {
		
		// TODO: make this generic...
		// ie, change this function to initializeRwandaHierarchyTables, and make it deprecated
		
		Session session = sessionFactory.getCurrentSession();
		
		AddressHierarchyType country = new AddressHierarchyType();
		country.setName("Country");
		
		AddressHierarchyType province = new AddressHierarchyType();
		province.setName("Province");
		
		AddressHierarchyType district = new AddressHierarchyType();
		district.setName("District");
		
		AddressHierarchyType sector = new AddressHierarchyType();
		sector.setName("Sector");
		
		AddressHierarchyType cell = new AddressHierarchyType();
		cell.setName("Cell");
		
		AddressHierarchyType umudugudu = new AddressHierarchyType();
		umudugudu.setName("Umudugudu");
		
		session.save(country);
		session.save(province);
		session.save(country);
		session.save(district);
		session.save(sector);
		session.save(cell);
		session.save(umudugudu);
		
		country.setChildType(province);
		province.setParentType(country);
		province.setChildType(district);
		
		district.setParentType(province);
		district.setChildType(sector);
		
		sector.setParentType(district);
		sector.setChildType(cell);
		
		cell.setParentType(sector);
		cell.setChildType(umudugudu);
		umudugudu.setParentType(cell);
		
	}
	
	// TODO: remove "page" parameter?
	// TODO: deprecate this whole method, or redo it so that it doesn't rely on custom query/hierarchy level\
	// TODO: or, just change the SQL statement so that it dynamically maps based on the AddressHierarchyType field
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public int getUnstructuredCount(int page) {
		
		String INVALID_ADDRESS_COUNT = "select count(*) "
		        + " from person_address "
		        + " left join patient_identifier on patient_identifier.patient_id = person_address.person_id "
		        + " left join patient_program on patient_program.patient_id = person_address.person_id "
		        + " left join patient_state on patient_program.patient_program_id = patient_state.patient_program_id "
		        + " left join program_workflow_state on patient_state.state = program_workflow_state.program_workflow_state_id "
		        + " left join concept_name on concept_name.concept_id = program_workflow_state.concept_id "
		        + " left join person_name on person_name.person_id = person_address.person_id "
		        + " where person_address.voided = 0 AND "
		        + " patient_identifier.preferred = 1 AND "
		        + " person_name.preferred = 1 AND "
		        + " patient_program.voided = 0 AND "
		        + " patient_program.date_completed is null AND "
		        + " (person_address.country not in (select name from address_hierarchy where type_id = 1) "
		        + " OR person_address.state_province not in (select name from address_hierarchy where type_id = 2 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.country and type_id = 1)) "
		        + " OR person_address.county_district not in (select name from address_hierarchy where type_id = 3 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.state_province and type_id = 2))"
		        + " OR person_address.city_village not in (select name from address_hierarchy where type_id = 4 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.county_district and type_id = 3))"
		        + " OR person_address.neighborhood_cell not in (select name from address_hierarchy where type_id = 5 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.city_village and type_id = 4))"
		        
		        + " OR person_address.address1 not in (select name from address_hierarchy where type_id = 6 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.neighborhood_cell and type_id = 5)))";
		
		SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(INVALID_ADDRESS_COUNT);
		List<Integer> unstructuredCount = sqlQuery.list();
		int count = 0;
		if (unstructuredCount.size() > 0) {
			count = unstructuredCount.get(0);
		}
		return count;
	}
	
	// TODO: this won't work in the latest version of openmrs because of the change in table names
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public List<Object[]> findUnstructuredAddresses(int page, int locationId) {
		int startIndex = 0;
		if (page > 0) {
			startIndex = page * 100 - 100;
		}
		
		String CELL_UMU = "select x.state_province, x.county_district, x.city_village, x.neighborhood_cell, x.address1, pi.patient_id,pi.identifier, location.name from (select identifier,location_id, patient_id, patient_identifier_id from patient_identifier where preferred = 1) pi left join (select address1,state_province, county_district, city_village, neighborhood_cell, date_created,person_id,person_address_id from person_address pa left join address_hierarchy on pa.address1 = address_hierarchy.name inner join address_hierarchy ah2 on pa.neighborhood_cell = ah2.name and address_hierarchy.parent_id = ah2.address_hierarchy_id and ah2.type_id=(select location_attribute_type_id from address_hierarchy_type where name='Cell') where voided=0) x on pi.patient_id = x.person_id inner join location on location.location_id = pi.location_id where location.location_id = ? and x.person_id is null order by x.date_created desc";
		
		SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(CELL_UMU);
		sqlQuery.addScalar("patient_id", Hibernate.INTEGER).addScalar("identifier", Hibernate.STRING).addScalar("name",
		    Hibernate.STRING).addScalar("state_province", Hibernate.STRING).addScalar("county_district", Hibernate.STRING)
		        .addScalar("city_village", Hibernate.STRING).addScalar("neighborhood_cell", Hibernate.STRING).addScalar(
		            "address1", Hibernate.STRING);
		sqlQuery.setInteger(0, locationId);
		
		sqlQuery.setMaxResults(100);
		sqlQuery.setFirstResult(startIndex);
		
		List<Object[]> unstructuredPersonAddressIds = sqlQuery.list();
		
		return unstructuredPersonAddressIds;
	}
	
	// TODO: figure out where this needs to go... probably will deprecate this?
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public List<Object[]> getLocationAddressBreakdown(int locationId) {
		
		String LOCATION_BREAKDOWN = "select pa.county_district,pa.city_village, count(*) from(select identifier,location_id, patient_id, patient_identifier_id from patient_identifier where preferred = 1)pi inner join location on location.location_id = pi.location_id and location.location_id = ? inner join (select country,state_province,county_district,city_village, person_id from person_address where voided = 0 and preferred = 1) pa on pi.patient_id = pa.person_id group by pa.country, pa.state_province, pa.county_district, pa.city_village";
		
		SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(LOCATION_BREAKDOWN);
		sqlQuery.addScalar("city_village", Hibernate.STRING).addScalar("count(*)", Hibernate.INTEGER).setInteger(0,
		    locationId);
		
		return sqlQuery.list();
	}
	
	// TODO: fix this to work with the new address model? genericize this?
	@SuppressWarnings("unchecked")
	@Deprecated
	public List<Object[]> getAllAddresses(int page) {
		
		int startIndex = 0;
		if (page > 0) {
			startIndex = page * 400 - 400;
		}
		
		String ALL_ADDRESSES = "select * from (select max(date_created), patient_id from patient_program group by patient_id) pp inner join  person_address on pp.patient_id = person_address.person_id where person_address.voided = 0  order by person_address.date_created desc";
		
		SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(ALL_ADDRESSES);
		sqlQuery.addScalar("patient_id", Hibernate.INTEGER).addScalar("country", Hibernate.STRING).addScalar(
		    "person_address.state_province", Hibernate.STRING).addScalar("person_address.county_district", Hibernate.STRING)
		        .addScalar("person_address.city_village", Hibernate.STRING).addScalar("person_address.neighborhood_cell",
		            Hibernate.STRING).addScalar("person_address.address1", Hibernate.STRING);
		
		sqlQuery.setMaxResults(100);
		sqlQuery.setFirstResult(startIndex);
		
		List<Object[]> allAddresses = sqlQuery.list();
		//List<PersonAddress> pas = convertToPersonAddresses(allAddresseses);
		
		return allAddresses;
	}
	
}
