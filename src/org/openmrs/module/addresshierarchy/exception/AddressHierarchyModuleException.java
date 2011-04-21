package org.openmrs.module.addresshierarchy.exception;

import org.openmrs.api.APIException;


public class AddressHierarchyModuleException extends APIException {

	private static final long serialVersionUID = 1L;

	/**
	 * Default empty constructor. If at all possible, don't use this one, but use the
	 * {@link #AddressHierarchyModuleException(String)} constructor to specify a helpful message to the end user
	 */
	public AddressHierarchyModuleException() {
	}
	
	/**
	 * General constructor to give the end user a helpful message that relates to why this error
	 * occurred.
	 * 
	 * @param message helpful message string for the end user
	 */
	public AddressHierarchyModuleException(String message) {
		super(message);
	}
	
	/**
	 * General constructor to give the end user a helpful message and to also propagate the parent
	 * error exception message.
	 * 
	 * @param message helpful message string for the end user
	 * @param cause the parent exception cause that this AddressHierarchyModuleException is wrapping around
	 */
	public AddressHierarchyModuleException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor used to simply chain a parent exception cause to an AddressHierarchyModuleException. Preference
	 * should be given to the {@link #AddressHierarchyModuleException(String, Throwable)} constructor if at all
	 * possible instead of this one.
	 * 
	 * @param cause the parent exception cause that this AddressHierarchyModuleException is wrapping around
	 */
	public AddressHierarchyModuleException(Throwable cause) {
		super(cause);
	}
}
