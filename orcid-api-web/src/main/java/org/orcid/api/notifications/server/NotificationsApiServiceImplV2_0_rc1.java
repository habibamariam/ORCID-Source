/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.api.notifications.server;

import javax.ws.rs.Path;

import org.orcid.core.api.OrcidApiConstants;

import io.swagger.annotations.Api;

/**
 * 
 * @author Will Simpson
 * 
 */
@Api("Notifications API v2.0_rc1")
@Path("/v2.0_rc1" + OrcidApiConstants.NOTIFICATIONS_PATH)
public class NotificationsApiServiceImplV2_0_rc1 extends NotificationsApiServiceImplBase {

}
