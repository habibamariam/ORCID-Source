/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2013 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.core.security.visibility.aop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.orcid.core.oauth.OrcidOAuth2Authentication;
import org.orcid.core.oauth.OrcidOauth2TokenDetailService;
import org.orcid.core.security.PermissionChecker;
import org.orcid.core.security.visibility.filter.VisibilityFilter;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.ScopePathType;
import org.orcid.jaxb.model.message.Visibility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * 2011-2012 ORCID
 * 
 * @author Declan Newman (declan) Date: 16/03/2012
 */
@Aspect
@Component
public class OrcidApiAuthorizationSecurityAspect {

    public static final String CLIENT_ID = "client_id";

    @Resource
    private OrcidOauth2TokenDetailService orcidOauthTokenDetailService;

    @Resource(name = "visibilityFilter")
    private VisibilityFilter visibilityFilter;

    @Resource(name = "defaultPermissionChecker")
    private PermissionChecker permissionChecker;

    @Before("@annotation(accessControl) && (args(uriInfo ,orcid, orcidMessage))")
    public void checkPermissionsWithAll(AccessControl accessControl, UriInfo uriInfo, String orcid, OrcidMessage orcidMessage) {
        permissionChecker.checkPermissions(getAuthentication(), accessControl.requiredScope(), orcid, orcidMessage);
    }

    @Before("@annotation(accessControl) && (args(uriInfo, orcidMessage))")
    public void checkPermissionsWithOrcidMessage(AccessControl accessControl, UriInfo uriInfo, OrcidMessage orcidMessage) {
        permissionChecker.checkPermissions(getAuthentication(), accessControl.requiredScope(), orcidMessage);

    }

    @Before("@annotation(accessControl) && args(orcid)")
    public void checkPermissionsWithOrcid(AccessControl accessControl, String orcid) {
        permissionChecker.checkPermissions(getAuthentication(), accessControl.requiredScope(), orcid);

    }

    @Before("@annotation(accessControl) && args(uriInfo , orcid, webhookUri)")
    public void checkPermissionsWithOrcidAndWebhookUri(AccessControl accessControl, UriInfo uriInfo, String orcid, String webhookUri) {
        permissionChecker.checkPermissions(getAuthentication(), accessControl.requiredScope(), orcid);
    }

    @AfterReturning(pointcut = "@annotation(accessControl)", returning = "response")
    public void visibilityResponseFilter(Response response, AccessControl accessControl) {
        Object entity = response.getEntity();
        if (entity != null && OrcidMessage.class.isAssignableFrom(entity.getClass())) {
            OrcidMessage orcidMessage = (OrcidMessage) entity;
            Set<Visibility> visibilities = permissionChecker.obtainVisibilitiesForAuthentication(getAuthentication(), accessControl.requiredScope(), orcidMessage);

            ScopePathType requiredScope = accessControl.requiredScope();
            // If the required scope is */read-limited
            if (ScopePathType.ORCID_PROFILE_READ_LIMITED.equals(requiredScope) || ScopePathType.AFFILIATIONS_READ_LIMITED.equals(requiredScope)
                    || ScopePathType.FUNDING_READ_LIMITED.equals(requiredScope) || ScopePathType.ORCID_WORKS_READ_LIMITED.equals(requiredScope)) {
                // get the client id
                Object authentication = getAuthentication();
                // If the authentication contains a client_id, use it to check
                // if it should be able to
                if (authentication.getClass().isAssignableFrom(OrcidOAuth2Authentication.class)) {
                    OrcidOAuth2Authentication orcidAuth = (OrcidOAuth2Authentication) getAuthentication();

                    AuthorizationRequest authorization = orcidAuth.getAuthorizationRequest();
                    Map<String, String> params = authorization.getAuthorizationParameters();
                    String clientId = params.get(CLIENT_ID);

                    // #1: Get the user orcid
                    String userOrcid = getUserOrcidFromOrcidMessage(orcidMessage);
                    // #2: Get the update scopes the member should have to allow
                    // him see his private information
                    List<ScopePathType> requiredScopesToSeePrivateData = getRequiredScopesToGetPrivateData(requiredScope);
                    // #3: Evaluate that list of scopes
                    boolean allowWorks = false;
                    boolean allowFunding = false;
                    boolean allowAffiliations = false;

                    // If the update works scope is required
                    if (requiredScopesToSeePrivateData.contains(ScopePathType.ORCID_WORKS_UPDATE)) {
                        // Check if the member is allowed to update works on
                        // that profile
                        if (hasScopeEnabled(clientId, userOrcid, ScopePathType.ORCID_WORKS_UPDATE.getContent()))
                            // If so, allow him to see private works
                            allowWorks = true;
                    }
                    // If the update funding scope is required
                    if (requiredScopesToSeePrivateData.contains(ScopePathType.FUNDING_UPDATE)) {
                        // Check if the member is allowed to update funding on
                        // that profile
                        if (hasScopeEnabled(clientId, userOrcid, ScopePathType.FUNDING_UPDATE.getContent()))
                            // If so, allow him to see private funding
                            allowFunding = true;
                    }
                    // If the update works scope is required
                    if (requiredScopesToSeePrivateData.contains(ScopePathType.AFFILIATIONS_UPDATE)) {
                        // Check if the member is allowed to update affiliations on
                        // that profile
                        if (hasScopeEnabled(clientId, userOrcid, ScopePathType.AFFILIATIONS_UPDATE.getContent()))
                            // If so, allow him to see private affiliations
                            allowAffiliations = true;
                    }

                    visibilityFilter.filter(orcidMessage, clientId, allowWorks, allowFunding, allowAffiliations, false,
                            visibilities.toArray(new Visibility[visibilities.size()]));
                } else {
                    visibilityFilter.filter(orcidMessage, null, false, false, false, false, visibilities.toArray(new Visibility[visibilities.size()]));
                }

            } else {
                visibilityFilter.filter(orcidMessage, null, false, false, false, false, visibilities.toArray(new Visibility[visibilities.size()]));
            }
        }
    }

    private String getUserOrcidFromOrcidMessage(OrcidMessage message) {
        OrcidProfile profile = message.getOrcidProfile();
        return profile.getOrcidIdentifier().getPath();
    }

    private List<ScopePathType> getRequiredScopesToGetPrivateData(ScopePathType readLimitedRequest) {
        List<ScopePathType> requiredScopes = new ArrayList<ScopePathType>();
        switch (readLimitedRequest) {
        case FUNDING_READ_LIMITED:
            requiredScopes.add(ScopePathType.FUNDING_UPDATE);
            break;
        case AFFILIATIONS_READ_LIMITED:
            requiredScopes.add(ScopePathType.AFFILIATIONS_UPDATE);
            break;
        case ORCID_WORKS_READ_LIMITED:
            requiredScopes.add(ScopePathType.ORCID_WORKS_UPDATE);
            break;
        case ORCID_PROFILE_READ_LIMITED:
            requiredScopes.add(ScopePathType.FUNDING_UPDATE);
            requiredScopes.add(ScopePathType.AFFILIATIONS_UPDATE);
            requiredScopes.add(ScopePathType.ORCID_WORKS_UPDATE);
            break;
        default:
            break;

        }
        return requiredScopes;
    }

    private boolean hasScopeEnabled(String clientId, String userName, String scope) {
        return orcidOauthTokenDetailService.checkIfScopeIsAvailableForMember(clientId, userName, scope);
    }

    private Authentication getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            return context.getAuthentication();
        } else {
            throw new IllegalStateException("No security context found. This is bad!");
        }
    }

}
