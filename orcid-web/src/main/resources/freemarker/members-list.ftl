<#--

    =============================================================================

    ORCID (R) Open Source
    http://orcid.org

    Copyright (c) 2012-2014 ORCID, Inc.
    Licensed under an MIT-Style License (MIT)
    http://orcid.org/open-source-license

    This copyright and license information (including a link to the full license)
    shall be included in its entirety in all copies or substantial portion of
    the software.

    =============================================================================

-->
<@public classes=['home'] nav="members-list">	
    <div class="member-list row">        
        <div class="col-md-9 col-md-offset-3 col-sm-12 col-xs-12">
        	<div class="row">
        		<div class="col-md-12 col-sm-12 col-xs-12">
		            <h1>ORCID Member Organizations</h1>
		            <p>ORCID is a non-profit organization supported by a global community of organizational members, including research organizations, publishers, funders, professional associations, and other stakeholders in the research ecosystem. Interested in becoming a member? <a href="<@orcid.rootPath '/about/membership'/>">Learn more about membership</a></p>
	            </div>            
	            <div ng-controller="MembersListController">
	                <div ng-hide="membersListSrvc.membersList != null" class="text-center" ng-cloak>
	                    <i class="glyphicon glyphicon-refresh spin x4 green" id="spinner"></i>
	                    <!--[if lt IE 8]>
	                        <img src="${staticCdn}/img/spin-big.gif" width="85" height ="85"/>
	                    <![endif]-->
	                </div>
	                
	                <div ng-show="membersListSrvc.membersList">
	                	<div class="col-md-12 col-sm-12 col-xs-12">
	                    	<p ng-cloak>There are currently {{membersListSrvc.membersList.length}} ORCID member organizations.</p>
	                    </div>
	                    <div class="member" ng-repeat="member in membersListSrvc.membersList | orderBy : 'name'">
	                    	<hr />
	                    	<div class="col-md-12 col-sm-12 col-xs-12">
	                        	<h2 ng-bind="member.name" ng-cloak></h2>	                        
	                        	<p ng-cloak>{{member.researchCommunity}} | {{member.country}}</p>
	                        </div>
	                        <div class="col-md-10 col-sm-10 col-xs-12">
	                        	<p>
		                        	<img class="member-logo" src="{{member.logoUrl}}"  ng-cloak ng-if="member.logoUrl">
		                        	<span class="member-decsription" ng-bind-html="renderHtml(member.description)" ng-if="member.description" ng-cloak></span>
	                        	</p>
	                        	<p class="clear-fix">
	                        		<a ng-href="{{membersListSrvc.getMemberPageUrl(member.slug)}}" ng-cloak>Member details <i class="glyphicon x075 glyphicon-chevron-right"></i></a>
	                        	</p>
	                        </div>
	                    </div>
	                </div>
	            </div>
	    	</div>
        </div>	    
    </div>
</@public>