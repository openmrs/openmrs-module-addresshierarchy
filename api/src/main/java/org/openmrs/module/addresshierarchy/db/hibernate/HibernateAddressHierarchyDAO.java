/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.db.hibernate;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.AddressToEntryMap;
import org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;

/**
 * The Class HibernateAddressHierarchyDAO which links to the tables address_hierarchy,
 * address_hierarchy_type and person_address. This class does the functions of storing and
 * retrieving addresses.
 */
public class HibernateAddressHierarchyDAO implements AddressHierarchyDAO {
	
	protected final Log log = LogFactory.getLog(getClass());

	// how many rows are sent to the database per round trip when bulk loading a hierarchy
	protected static final int ENTRY_BATCH_SIZE = 500;

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@SuppressWarnings("unchecked")
	public int getAddressHierarchyEntryCount() {
		int x = 0;
		Session session = getCurrentSession();
		List<Long> rows = session.createQuery("select count(*) from AddressHierarchyEntry", Long.class).list();
		if (rows.size() > 0) {
			x = rows.get(0).intValue();
		}
		return x;
	}
	
	@SuppressWarnings("unchecked")
	public int getAddressHierarchyEntryCountByLevel(AddressHierarchyLevel level) {
		int x = 0;
		Session session = getCurrentSession();
		List<Long> rows = session
		        .createQuery("select count(*) from AddressHierarchyEntry e where e.level.levelId = :levelId", Long.class)
		        .setParameter("levelId", level.getId()).list();
		if (rows.size() > 0) {
			x = rows.get(0).intValue();
		}
		return x;
	}
	
	public AddressHierarchyEntry getAddressHierarchyEntry(int addressHierarchyEntryId) {
		Session session = getCurrentSession();
		AddressHierarchyEntry ah = session.getReference(AddressHierarchyEntry.class, addressHierarchyEntryId);
		return ah;
	}
	
	@SuppressWarnings("unchecked")
	public AddressHierarchyEntry getAddressHierarchyEntryByUserGenId(String userGeneratedId) {
		AddressHierarchyEntry ah = null;
		Session session = getCurrentSession();
		List<AddressHierarchyEntry> list = session
		        .createQuery("from AddressHierarchyEntry e where e.userGeneratedId = :userGeneratedId",
		            AddressHierarchyEntry.class)
		        .setParameter("userGeneratedId", userGeneratedId).list();
		if (list != null && list.size() > 0) {
			ah = list.get(0);
		}
		return ah;
	}
	
	@SuppressWarnings("unchecked")
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevel(AddressHierarchyLevel addressHierarchyLevel) {
		Session session = getCurrentSession();
		return session.createQuery("from AddressHierarchyEntry e where e.level.levelId = :levelId", AddressHierarchyEntry.class)
		        .setParameter("levelId", addressHierarchyLevel.getId()).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndName(AddressHierarchyLevel addressHierarchyLevel,
	        String name) {
		Session session = getCurrentSession();
		Query<AddressHierarchyEntry> query = session.createQuery(
		    "from AddressHierarchyEntry e where e.level.levelId = :levelId and " + getNameCriteria(name),
		    AddressHierarchyEntry.class);
		query.setParameter("levelId", addressHierarchyLevel.getId());
		setNameParameter(query, name);
		return query.list();
	}
	
	/**
	 * HQL equivalent of the former Restrictions.isNull("name") / Restrictions.eq("name", name).ignoreCase()
	 */
	private String getNameCriteria(String name) {
		return name == null ? "e.name is null" : "lower(e.name) = :name";
	}
	
	private void setNameParameter(Query<?> query, String name) {
		if (name != null) {
			query.setParameter("name", name.toLowerCase());
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndNameAndParent(
	        AddressHierarchyLevel addressHierarchyLevel, String name, AddressHierarchyEntry parent) {
		Session session = getCurrentSession();
		Query<AddressHierarchyEntry> query = session.createQuery(
		    "from AddressHierarchyEntry e where e.level.levelId = :levelId and e.parent.addressHierarchyEntryId = :parentId and "
		            + getNameCriteria(name),
		    AddressHierarchyEntry.class);
		query.setParameter("levelId", addressHierarchyLevel.getId());
		query.setParameter("parentId", parent.getId());
		setNameParameter(query, name);
		return query.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndLikeNameAndParent(
	        AddressHierarchyLevel addressHierarchyLevel, String name, AddressHierarchyEntry parent) {
		Session session = getCurrentSession();
		return session.createQuery(
		    "from AddressHierarchyEntry e where e.level.levelId = :levelId and e.parent.addressHierarchyEntryId = :parentId and lower(e.name) like :name",
		    AddressHierarchyEntry.class).setParameter("levelId", addressHierarchyLevel.getId())
		        .setParameter("parentId", parent.getId()).setParameter("name", getAnywhereLikeValue(name)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(AddressHierarchyEntry entry) {
		Session session = getCurrentSession();
		List<AddressHierarchyEntry> list = session
		        .createQuery("from AddressHierarchyEntry e where e.parent.addressHierarchyEntryId = :parentId",
		            AddressHierarchyEntry.class)
		        .setParameter("parentId", entry.getId()).list();
		return list;
	}
	
	public AddressHierarchyEntry getChildAddressHierarchyEntryByName(AddressHierarchyEntry entry, String childName) {
		Session session = getCurrentSession();
		Query<AddressHierarchyEntry> query = session.createQuery(
		    "from AddressHierarchyEntry e where e.parent.addressHierarchyEntryId = :parentId and " + getNameCriteria(childName),
		    AddressHierarchyEntry.class);
		query.setParameter("parentId", entry.getId());
		setNameParameter(query, childName); // do a case-insensitive match
		
		List<AddressHierarchyEntry> entries = query.list();
		
		if (entries == null || entries.size() == 0) {
			return null;
		}
		
		// if there are multiple entries with the same name, log this as an error, but just return the first result
		if (entries.size() > 1) {
			log.error("Duplicate address hierarchy entries: " + entries.get(0).getName());
		}
		
		return entries.get(0);
	}
	
	public void saveAddressHierarchyEntry(AddressHierarchyEntry ah) {
		try {
			Session session = getCurrentSession();
			HibernateUtil.saveOrUpdate(session, ah);
		}
		catch (Throwable t) {
			throw new DAOException(t);
		}
	}

	@SuppressWarnings("unchecked")
	public List<AddressHierarchyEntry> getDetachedAddressHierarchyEntries() {
		Session session = getCurrentSession();

		// selecting the columns rather than the entity keeps the persistence context empty. Loading these as
		// entities instead would leave every row managed, and Hibernate walks the whole persistence context on
		// each flush, which is what made importing a large hierarchy scale quadratically.
		List<Object[]> rows = session.createQuery(
		    "select e.addressHierarchyEntryId, e.name, e.userGeneratedId, e.latitude, e.longitude, e.elevation, e.uuid, e.level.levelId, e.parent.addressHierarchyEntryId from AddressHierarchyEntry e",
		    Object[].class).list();

		Map<Integer, AddressHierarchyLevel> levelsById = new HashMap<Integer, AddressHierarchyLevel>();
		for (AddressHierarchyLevel level : getAddressHierarchyLevels()) {
			levelsById.put(level.getId(), level);
		}

		// build every entry first, then link the parents, so that the graph does not depend on the order the
		// rows come back in
		Map<Integer, AddressHierarchyEntry> entriesById = new HashMap<Integer, AddressHierarchyEntry>(rows.size() * 2);
		List<AddressHierarchyEntry> entries = new ArrayList<AddressHierarchyEntry>(rows.size());
		for (Object[] row : rows) {
			AddressHierarchyEntry entry = new AddressHierarchyEntry();
			entry.setAddressHierarchyEntryId((Integer) row[0]);
			entry.setName((String) row[1]);
			entry.setUserGeneratedId((String) row[2]);
			entry.setLatitude((Double) row[3]);
			entry.setLongitude((Double) row[4]);
			entry.setElevation((Double) row[5]);
			entry.setUuid((String) row[6]);
			entry.setLevel(levelsById.get(row[7]));
			entriesById.put(entry.getId(), entry);
			entries.add(entry);
		}

		int i = 0;
		for (Object[] row : rows) {
			Integer parentId = (Integer) row[8];
			if (parentId != null) {
				entries.get(i).setParent(entriesById.get(parentId));
			}
			i++;
		}

		return entries;
	}

	public void insertAddressHierarchyEntries(final List<AddressHierarchyEntry> entries) {
		if (entries == null || entries.isEmpty()) {
			return;
		}

		// An entry whose parent is also being inserted cannot be written until that parent has been given an
		// id, so entries are grouped by how far they sit below the nearest already-persisted ancestor and each
		// group is written in turn. An IdentityHashMap is used deliberately: AddressHierarchyEntry.equals()
		// reports false for anything without an id, so entries that have not been written yet cannot be told
		// apart by a normal map.
		final List<List<AddressHierarchyEntry>> generations = new ArrayList<List<AddressHierarchyEntry>>();
		Map<AddressHierarchyEntry, Integer> depths = new IdentityHashMap<AddressHierarchyEntry, Integer>(entries.size());
		for (AddressHierarchyEntry entry : entries) {
			AddressHierarchyEntry parent = entry.getParent();
			Integer parentDepth = parent == null ? null : depths.get(parent);
			int depth = parentDepth == null ? 0 : parentDepth + 1;
			depths.put(entry, depth);
			while (generations.size() <= depth) {
				generations.add(new ArrayList<AddressHierarchyEntry>());
			}
			generations.get(depth).add(entry);
		}

		try {
			// doWork borrows the session's own connection, so these statements join whatever transaction the
			// caller is running in and commit or roll back along with it
			getCurrentSession().doWork(connection -> {
				for (List<AddressHierarchyEntry> generation : generations) {
					insertGeneration(connection, generation);
				}
			});
		}
		catch (Throwable t) {
			throw new DAOException(t);
		}
	}

	/**
	 * Writes one generation of entries, every one of which is guaranteed to have a parent that already carries
	 * an id, and copies the ids the database assigns back onto the entries so the next generation can refer to
	 * them.
	 */
	private void insertGeneration(Connection connection, List<AddressHierarchyEntry> generation) throws SQLException {
		String sql = "insert into address_hierarchy_entry (name, level_id, parent_id, user_generated_id, latitude, longitude, elevation, uuid) values (?, ?, ?, ?, ?, ?, ?, ?)";

		// naming the key column rather than asking for RETURN_GENERATED_KEYS keeps this portable: PostgreSQL
		// turns the generic form into "returning *" and hands back every column, so reading the first one would
		// only find the id for as long as it stays the first column in the table
		try (PreparedStatement statement = connection.prepareStatement(sql,
		    new String[] { "address_hierarchy_entry_id" })) {
			int batchStart = 0;
			for (int i = 0; i < generation.size(); i++) {
				AddressHierarchyEntry entry = generation.get(i);
				statement.setString(1, entry.getName());
				setIntOrNull(statement, 2, entry.getLevel() == null ? null : entry.getLevel().getId());
				setIntOrNull(statement, 3, entry.getParent() == null ? null : entry.getParent().getId());
				statement.setString(4, entry.getUserGeneratedId());
				setDoubleOrNull(statement, 5, entry.getLatitude());
				setDoubleOrNull(statement, 6, entry.getLongitude());
				setDoubleOrNull(statement, 7, entry.getElevation());
				statement.setString(8, entry.getUuid());
				statement.addBatch();

				if ((i + 1) % ENTRY_BATCH_SIZE == 0 || i == generation.size() - 1) {
					statement.executeBatch();
					assignGeneratedIds(statement, generation.subList(batchStart, i + 1));
					batchStart = i + 1;
				}
			}
		}
	}

	/**
	 * Copies the keys the database generated onto the entries they were generated for. The driver returns them
	 * in the order the rows were added to the batch, so they are matched up positionally; a short or missing
	 * result means the entries would silently keep null ids and their children would be written with a null
	 * parent, so it is treated as a failure instead.
	 */
	private void assignGeneratedIds(Statement statement, List<AddressHierarchyEntry> batch) throws SQLException {
		int assigned = 0;
		try (ResultSet keys = statement.getGeneratedKeys()) {
			while (keys.next() && assigned < batch.size()) {
				batch.get(assigned).setAddressHierarchyEntryId(keys.getInt(1));
				assigned++;
			}
		}

		if (assigned != batch.size()) {
			throw new SQLException("Expected " + batch.size() + " generated address hierarchy entry ids but received "
			        + assigned);
		}
	}

	public void updateAddressHierarchyEntryUserGeneratedIds(final Map<Integer, String> userGeneratedIdsByEntryId) {
		if (userGeneratedIdsByEntryId == null || userGeneratedIdsByEntryId.isEmpty()) {
			return;
		}

		try {
			Session session = getCurrentSession();

			// these rows are rewritten behind Hibernate's back, so anything the session already holds for them
			// would otherwise keep serving the old value. Flushing first means pending work is written rather
			// than thrown away by the clear that follows.
			session.flush();

			session.doWork(connection -> {
				try (PreparedStatement statement = connection
				        .prepareStatement("update address_hierarchy_entry set user_generated_id = ? where address_hierarchy_entry_id = ?")) {
					int i = 0;
					for (Map.Entry<Integer, String> update : userGeneratedIdsByEntryId.entrySet()) {
						statement.setString(1, update.getValue());
						statement.setInt(2, update.getKey());
						statement.addBatch();

						if (++i % ENTRY_BATCH_SIZE == 0) {
							statement.executeBatch();
						}
					}
					statement.executeBatch();
				}
			});

			session.clear();
		}
		catch (Throwable t) {
			throw new DAOException(t);
		}
	}

	private static void setIntOrNull(PreparedStatement statement, int index, Integer value) throws SQLException {
		if (value == null) {
			statement.setNull(index, Types.INTEGER);
		} else {
			statement.setInt(index, value);
		}
	}

	private static void setDoubleOrNull(PreparedStatement statement, int index, Double value) throws SQLException {
		if (value == null) {
			statement.setNull(index, Types.DOUBLE);
		} else {
			statement.setDouble(index, value);
		}
	}
	
	public void deleteAllAddressHierarchyEntries() {
		Session session = getCurrentSession();
		
		// cycle through all the top-level entries and delete them; the rest should be deleted via cascade
		// note that I haven't been able to figure out how to have this cascade work on the hibernate level,
		// so I have defined it at the database level in mysql; therefore, the unit test for this doesn't work
		
		AddressHierarchyLevel top = getTopAddressHierarchyLevel();
		
		if (top != null) {
			for (AddressHierarchyEntry entry : getAddressHierarchyEntriesByLevel(top)) {
				session.remove(entry);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<AddressHierarchyLevel> getAddressHierarchyLevels() {
		Session session = getCurrentSession();
		return session.createQuery("from AddressHierarchyLevel", AddressHierarchyLevel.class).list();
	}
	
	public AddressHierarchyLevel getTopAddressHierarchyLevel() {
		Session session = getCurrentSession();
		Query<AddressHierarchyLevel> query = session.createQuery("from AddressHierarchyLevel l where l.parent is null",
		    AddressHierarchyLevel.class);
		
		AddressHierarchyLevel topLevel = null;
		
		try {
			topLevel = query.uniqueResult();
		}
		catch (Exception e) {
			throw new AddressHierarchyModuleException("Unable to fetch top level address hierarchy type", e);
		}
		
		return topLevel;
	}
	
	public AddressHierarchyLevel getAddressHierarchyLevel(int levelId) {
		Session session = getCurrentSession();
		AddressHierarchyLevel type = session.getReference(AddressHierarchyLevel.class, levelId);
		return type;
	}
	
	public AddressHierarchyLevel getAddressHierarchyLevelByParent(AddressHierarchyLevel parent) {
		Session session = getCurrentSession();
		Query<AddressHierarchyLevel> query = session
		        .createQuery("from AddressHierarchyLevel l where l.parent = :parent", AddressHierarchyLevel.class)
		        .setParameter("parent", parent);
		
		AddressHierarchyLevel child = null;
		
		try {
			child = query.uniqueResult();
		}
		catch (Exception e) {
			throw new AddressHierarchyModuleException("Unable to fetch child address hierarchy type", e);
		}
		
		return child;
	}
	
	public void saveAddressHierarchyLevel(AddressHierarchyLevel level) {
		try {
			Session session = getCurrentSession();
			HibernateUtil.saveOrUpdate(session, level);
		}
		catch (Throwable t) {
			throw new DAOException(t);
		}
	}
	
	public void deleteAddressHierarchyLevel(AddressHierarchyLevel level) {
		try {
			Session session = getCurrentSession();
			session.remove(level);
		}
		catch (Throwable t) {
			throw new DAOException(t);
		}
	}
	
	public AddressToEntryMap getAddressToEntryMap(int id) {
		Session session = getCurrentSession();
		AddressToEntryMap result = session.getReference(AddressToEntryMap.class, id);
		return result;
	}
	
	public void saveAddressToEntryMap(AddressToEntryMap addressToEntryMap) {
		try {
			Session session = getCurrentSession();
			HibernateUtil.saveOrUpdate(session, addressToEntryMap);
		}
		catch (Throwable t) {
			throw new DAOException(t);
		}
	}
	
	public void deleteAddressToEntryMap(AddressToEntryMap addressToEntryMap) {
		try {
			Session session = getCurrentSession();
			session.remove(addressToEntryMap);
		}
		catch (Throwable t) {
			throw new DAOException(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<AddressToEntryMap> getAddressToEntryMapByPersonAddress(PersonAddress address) {
		Session session = getCurrentSession();
		return session
		        .createQuery("from AddressToEntryMap m where m.address.personAddressId = :addressId", AddressToEntryMap.class)
		        .setParameter("addressId", address.getId()).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Patient> findAllPatientsWithDateChangedAfter(Date date) {
		Session session = getCurrentSession();
		return session.createQuery("from Patient p where p.dateChanged >= :date and p.voided = false", Patient.class)
		        .setParameter("date", date).list();
	}
	
	/**
	 * The following methods are deprecated and just exist to provide backwards compatibility to Rwanda
	 * Address Hierarchy module
	 */
	
	@Deprecated
	public void associateCoordinates(AddressHierarchyEntry ah, double latitude, double longitude) {
		ah.setLatitude(latitude);
		ah.setLongitude(longitude);
		Session session = getCurrentSession();
		HibernateUtil.saveOrUpdate(session, ah);
	}
	
	@Deprecated
	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah) {
		List<AddressHierarchyEntry> leafList = new ArrayList<AddressHierarchyEntry>();
		getLowestLevel(ah, leafList);
		return leafList;
	}
	
	// Recursively finds leaf nodes of ah 
	@Deprecated
	private List<AddressHierarchyEntry> getLowestLevel(AddressHierarchyEntry ah, List<AddressHierarchyEntry> leafList) {
		List<AddressHierarchyEntry> children = getChildAddressHierarchyEntries(ah);
		if (children.size() > 0) {
			for (AddressHierarchyEntry addressHierarchy : children) {
				getLowestLevel(addressHierarchy, leafList);
			}
		} else {
			leafList.add(ah);
		}
		return children;
	}
	
	@Deprecated
	public void initializeRwandaHierarchyTables() {
		
		// TODO: make this generic...
		// ie, change this function to initializeRwandaHierarchyTables, and make it deprecated
		
		Session session = getCurrentSession();
		
		AddressHierarchyLevel country = new AddressHierarchyLevel();
		country.setName("Country");
		
		AddressHierarchyLevel province = new AddressHierarchyLevel();
		province.setName("Province");
		
		AddressHierarchyLevel district = new AddressHierarchyLevel();
		district.setName("District");
		
		AddressHierarchyLevel sector = new AddressHierarchyLevel();
		sector.setName("Sector");
		
		AddressHierarchyLevel cell = new AddressHierarchyLevel();
		cell.setName("Cell");
		
		AddressHierarchyLevel umudugudu = new AddressHierarchyLevel();
		umudugudu.setName("Umudugudu");
		
		session.persist(country);
		session.persist(province);
		session.persist(country);
		session.persist(district);
		session.persist(sector);
		session.persist(cell);
		session.persist(umudugudu);
		
		province.setParent(country);
		district.setParent(province);
		sector.setParent(district);
		cell.setParent(sector);
		umudugudu.setParent(cell);
		
	}
	
	// TODO: remove "page" parameter?
	// TODO: deprecate this whole method, or redo it so that it doesn't rely on custom query/hierarchy level\
	// TODO: or, just change the SQL statement so that it dynamically maps based on the AddressHierarchyLevel field
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public int getUnstructuredCount(int page) {
		
		String INVALID_ADDRESS_COUNT = "select count(*) " + " from person_address "
		        + " left join patient_identifier on patient_identifier.patient_id = person_address.person_id "
		        + " left join patient_program on patient_program.patient_id = person_address.person_id "
		        + " left join patient_state on patient_program.patient_program_id = patient_state.patient_program_id "
		        + " left join program_workflow_state on patient_state.state = program_workflow_state.program_workflow_state_id "
		        + " left join concept_name on concept_name.concept_id = program_workflow_state.concept_id "
		        + " left join person_name on person_name.person_id = person_address.person_id "
		        + " where person_address.voided = 0 AND " + " patient_identifier.preferred = 1 AND "
		        + " person_name.preferred = 1 AND " + " patient_program.voided = 0 AND "
		        + " patient_program.date_completed is null AND "
		        + " (person_address.country not in (select name from address_hierarchy where type_id = 1) "
		        + " OR person_address.state_province not in (select name from address_hierarchy where type_id = 2 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.country and type_id = 1)) "
		        + " OR person_address.county_district not in (select name from address_hierarchy where type_id = 3 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.state_province and type_id = 2))"
		        + " OR person_address.city_village not in (select name from address_hierarchy where type_id = 4 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.county_district and type_id = 3))"
		        + " OR person_address.neighborhood_cell not in (select name from address_hierarchy where type_id = 5 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.city_village and type_id = 4))"
		        
		        + " OR person_address.address1 not in (select name from address_hierarchy where type_id = 6 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.neighborhood_cell and type_id = 5)))";
		
		NativeQuery<Integer> sqlQuery = getCurrentSession().createNativeQuery(INVALID_ADDRESS_COUNT);
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
		
		NativeQuery<Object[]> sqlQuery = getCurrentSession().createNativeQuery(CELL_UMU);
		sqlQuery.addScalar("patient_id", StandardBasicTypes.INTEGER).addScalar("identifier", StandardBasicTypes.STRING)
		        .addScalar("name", StandardBasicTypes.STRING).addScalar("state_province", StandardBasicTypes.STRING)
		        .addScalar("county_district", StandardBasicTypes.STRING).addScalar("city_village", StandardBasicTypes.STRING)
		        .addScalar("neighborhood_cell", StandardBasicTypes.STRING).addScalar("address1", StandardBasicTypes.STRING);
		sqlQuery.setParameter(1, locationId);
		
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
		
		NativeQuery<Object[]> sqlQuery = getCurrentSession().createNativeQuery(LOCATION_BREAKDOWN);
		sqlQuery.addScalar("city_village", StandardBasicTypes.STRING).addScalar("count(*)", StandardBasicTypes.INTEGER)
		        .setParameter(1, locationId);
		
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
		
		NativeQuery<Object[]> sqlQuery = getCurrentSession().createNativeQuery(ALL_ADDRESSES);
		sqlQuery.addScalar("patient_id", StandardBasicTypes.INTEGER).addScalar("country", StandardBasicTypes.STRING)
		        .addScalar("person_address.state_province", StandardBasicTypes.STRING)
		        .addScalar("person_address.county_district", StandardBasicTypes.STRING)
		        .addScalar("person_address.city_village", StandardBasicTypes.STRING)
		        .addScalar("person_address.neighborhood_cell", StandardBasicTypes.STRING)
		        .addScalar("person_address.address1", StandardBasicTypes.STRING);
		
		sqlQuery.setMaxResults(100);
		sqlQuery.setFirstResult(startIndex);
		
		List<Object[]> allAddresses = sqlQuery.list();
		//List<PersonAddress> pas = convertToPersonAddresses(allAddresseses);
		
		return allAddresses;
	}
	
	@Override
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndLikeName(AddressHierarchyLevel level, String name,
	        int limit) {
		Session session = getCurrentSession();
		return session
		        .createQuery("from AddressHierarchyEntry e where e.level.levelId = :levelId and lower(e.name) like :name",
		            AddressHierarchyEntry.class)
		        .setParameter("levelId", level.getId()).setParameter("name", getAnywhereLikeValue(name))
		        .setMaxResults(limit).list();
	}
	
	/**
	 * HQL equivalent of the former Restrictions.ilike(property, name, MatchMode.ANYWHERE)
	 */
	private String getAnywhereLikeValue(String name) {
		return ("%" + name + "%").toLowerCase();
	}
	
	@Override
	public AddressHierarchyEntry getAddressHierarchyEntryByUuid(String uuid) {
		return getCurrentSession().createQuery("from AddressHierarchyEntry where uuid = :uuid", AddressHierarchyEntry.class)
		        .setParameter("uuid", uuid).uniqueResult();
	}
	
	/**
	 * Gets the current hibernate session while taking care of the hibernate 3 and 4 differences.
	 * 
	 * @return the current hibernate session.
	 */
	private org.hibernate.Session getCurrentSession() {
		try {
			return sessionFactory.getCurrentSession();
		}
		catch (NoSuchMethodError ex) {
			try {
				Method method = sessionFactory.getClass().getMethod("getCurrentSession", (Class<?>) null);
				return (org.hibernate.Session) method.invoke(sessionFactory, null);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to get the current hibernate session", e);
			}
		}
	}
}
