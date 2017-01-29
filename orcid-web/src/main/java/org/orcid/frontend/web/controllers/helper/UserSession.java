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
package org.orcid.frontend.web.controllers.helper;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Will Simpson
 *
 */
public class UserSession {

    private Set<Long> suppressedNotificationAlertIds;

    public Set<Long> getSuppressedNotificationAlertIds() {
        if (suppressedNotificationAlertIds == null) {
            suppressedNotificationAlertIds = new HashSet<>();
        }
        return suppressedNotificationAlertIds;
    }

}
