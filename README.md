
openmrs-module-addresshierarchy
===============================

Allows OpenMRS address fields to be constrained and provides selectors for constrained address fields.

REST API
--------

The module provides a read-only REST API for accessing address hierarchy levels,
entries, possible address values, entry-name searches, and full-address searches.

The REST endpoints are available under `/ws/rest/v1/`.

### Address hierarchy levels

`GET /ws/rest/v1/addresshierarchy-level`

Returns configured address hierarchy levels.

Supported query parameters:

- `includeUnmapped` - include levels without a mapped address field. Default: `false`.
- `includeEmpty` - include levels without entries. Default: `true`.
- `addressField` - search by the mapped OpenMRS address field.

Levels support `ref`, `default`, and `full` representations.

### Address hierarchy entries

`GET /ws/rest/v1/addresshierarchy-entry`

Returns top-level address hierarchy entries.

Supported query parameters include:

- `q` - search entry names.
- `level` - address hierarchy level UUID.
- `parent` - parent entry UUID.
- `userGeneratedId` - search by user-generated ID.
- `limit` - maximum number of results.
- `startIndex` - pagination offset.

Entries support `ref`, `default`, and `full` representations.

### Possible address values

`GET /ws/rest/v1/addresshierarchy/possiblevalue`

Returns possible values for an address field based on the supplied parent
address fields.

The `addressField` parameter identifies the field for which possible values
should be returned. Other address-field parameters may be supplied as filters.

Results are returned using a `{ "results": [...] }` envelope.

### Entry-name search

`GET /ws/rest/v1/addresshierarchy/entryname`

Searches address hierarchy entry names using the cache-backed address search.

Required parameters:

- `q` - search text.
- `addressField` - address field associated with the hierarchy level.

Results are returned using a `{ "results": [...] }` envelope.

### Full-address search

`GET /ws/rest/v1/addresshierarchy/fulladdress`

Returns possible full addresses.

Search can be performed using:

- `entry` - address hierarchy entry UUID.
- `entryName` - address hierarchy entry name, together with `addressField`.
- `q` - full-address search text.

Each result contains an `address` value and its individual `components`.

The REST API returns components directly and does not expose the legacy
`separator` parameter.

### Legacy AJAX endpoints

The existing `/module/addresshierarchy/ajax/*.form` endpoints remain available
for O2 and legacy UI compatibility. They continue to delegate to the same
address hierarchy service methods used by the REST API.
