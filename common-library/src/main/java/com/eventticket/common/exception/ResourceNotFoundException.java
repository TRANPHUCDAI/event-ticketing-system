package com.eventticket.common.exception;

public class ResourceNotFoundException extends ApiException {
     public ResourceNotFoundException(String resourceType, String resourceId) {
          super(
                    resourceType + " with ID " + resourceId + " not found",
                    "RESOURCE_NOT_FOUND",
                    404);
     }
}
