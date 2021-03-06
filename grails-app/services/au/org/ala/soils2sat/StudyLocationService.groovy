/*
 * ﻿Copyright (C) 2013 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.soils2sat

import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.json.JSONObject

class StudyLocationService extends ServiceBase {

    def grailsApplication
    def logService
    def settingService

    @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName}")
    List<StudyLocationTO> getStudyLocations() {
        def studyLocations = proxyServiceCall(grailsApplication, "getStudyLocations")
        def results = new ArrayList<StudyLocationTO>()
        studyLocations?.each { studyLocation ->
            if (!studyLocation.longitude && !studyLocation.latitude) {
                // print "Discarding studyLocation (no position data): " + studyLocation.studyLocationName
            }

            def map = cleanMap(studyLocation)
            try {
                results << new StudyLocationTO(map)
            } catch (Exception ex) {
                log.error("Failed to marshall Study Location - ", ex)
            }
        }

        return results
    }

    private static Map cleanMap(JSONElement elem) {
        elem.each {
            if (it.value instanceof JSONObject.Null) {
                it.value = null
            }

            if (it.value == "NaN") {
                it.value = 0
            }
        }
        return elem
    }

    @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName,#studyLocationName}")
    public StudyLocationDetailsTO getStudyLocationDetails(String studyLocationName) {
        def details = proxyServiceCall(grailsApplication, "getStudyLocationDetails/${studyLocationName}", [:])
        if (!details) {
            return null
        }
        def to = new StudyLocationDetailsTO(cleanMap(details))
        return to
    }

    public List<StudyLocationVisitTO> getStudyLocationVisits(String studyLocationName) {
        def visits = proxyServiceCall(grailsApplication, "getStudyLocationVisits/${studyLocationName}", [:])
        def results = []
        visits?.each {
            def visit = new StudyLocationVisitTO(studyLocationVisitId: it.studyLocationVisitId, visitStartDate: it.visitStartDate, visitEndDate: it.visitStartDate, studyLocationName:  studyLocationName )
            visit.observers = []
            it.observers?.each {
                def observer = new VisitObserverTO(cleanMap(it))
                visit.observers << observer
            }

            results << visit
        }
        return results
    }

    public getSamplingUnitDetails(String visitId, SamplingUnitType samplingUnitType) {
        if (samplingUnitType == null) {
            println "Break!"
            return null;
        }
        return getSamplingUnitDetails(visitId, (int) samplingUnitType?.value)
    }

    @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName,#visitId,#samplingUnitTypeId}")
    public getSamplingUnitDetails(String visitId, int samplingUnitTypeId) {
        def details = proxyServiceCall(grailsApplication, "getSamplingUnits/${visitId}/getDetails/${samplingUnitTypeId}")
        if (details) {

            details.samplingUnitData?.each {
                cleanMap(it)
            }

            details.samplingUnitData = details?.samplingUnitData?.sort {
                it.id
            }

        }

        return details
    }

    public getSoilPhForStudyLocationVisit(String studyLocationVisitId) {
        def samplingUnit = getSamplingUnitDetails(studyLocationVisitId, SamplingUnitType.SoilCharacter)
        def rows = []
        if (samplingUnit?.samplingUnitData) {
            rows = samplingUnit.samplingUnitData.collect { [upperDepth: it.upperDepth, lowerDepth: it.lowerDepth, pH: it.ph ] }
        }
        rows = rows?.sort { it.upperDepth }
    }

    public String getLastVisitIdForStudyLocation(String studyLocationName) {
        return getLastVisitForStudyLocation(studyLocationName)?.studyLocationVisitId
    }

    public StudyLocationVisitTO getLastVisitForStudyLocation(String studyLocationName) {
        List<StudyLocationVisitTO> visits = getStudyLocationVisits(studyLocationName)
        def visit = visits.max { it.visitStartDate }
        return visit
    }


    public getSoilPhForStudyLocation(String studyLocationName) {
        def visit = getLastVisitForStudyLocation(studyLocationName)
        def rows = []
        if (visit) {
            rows = getSoilPhForStudyLocationVisit(visit.studyLocationVisitId)
        }
        return rows
    }

    public getSoilECForStudyLocationVisit(String studyLocationVisitId) {
        // Get the most recent visit, and get its soil PH from the soil characterisation sampling unit...
        def samplingUnit = getSamplingUnitDetails(studyLocationVisitId, SamplingUnitType.SoilCharacter)
        def rows = []
        if (samplingUnit?.samplingUnitData) {
            rows = samplingUnit.samplingUnitData.collect { [upperDepth: it.upperDepth, lowerDepth: it.lowerDepth, EC: it.ec ] }
        }
        rows = rows?.sort { it.upperDepth }
        return rows
    }

    public getSoilECForStudyLocation(String studyLocationName) {
        // Get the most recent visit, and get its soil PH from the soil characterisation sampling unit...
        def rows = []
        def visit = getLastVisitForStudyLocation(studyLocationName)
        if (visit) {
            rows = getSoilECForStudyLocationVisit(visit.studyLocationVisitId)
        }
        return rows
    }

    public getVoucheredTaxaForStudyLocation(String studyLocationName) {
        if (!studyLocationName) {
            return []
        }
        def vouchers = proxyServiceCall(grailsApplication, "getStudyLocationVouchers/${studyLocationName}")
        def taxa = []
        vouchers.each { voucher ->
            if (voucher.herbariumDetermination && !voucher.herbariumDetermination.toString().equalsIgnoreCase('NO ID')) {
                taxa << voucher.herbariumDetermination
            }
        }

        return taxa?.sort { it }
    }

    public getVoucheredTaxaForStudyLocationVisit(String studyLocationVisitId) {
        def vouchers = proxyServiceCall(grailsApplication, "getStudyLocationVisitVouchers/${studyLocationVisitId}")
        def taxa = []
        vouchers.each { voucher ->
            if (voucher.herbariumDetermination && !voucher.herbariumDetermination.toString().equalsIgnoreCase('NO ID')) {
                taxa << voucher.herbariumDetermination
            }
        }

        return taxa?.sort { it }
    }

    public getPointInterceptTaxaForVisit(String studyLocationVisitId) {
        def samplingUnit = getSamplingUnitDetails(studyLocationVisitId, SamplingUnitType.PointIntercept)
        def taxaMap = [:]
        samplingUnit?.samplingUnitData?.each {
            def name = StringUtils.collapseSpaces(it.herbariumDetermination)
            if (name) {
                if (taxaMap.containsKey(name)) {
                    taxaMap[name] ++
                } else {
                    taxaMap[name] = 1
                }
            }
        }
        return taxaMap
    }

    @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName,#studyLocationName}")
    def getStudyLocationDetailsOld(String studyLocationName) {
        def details = proxyServiceCall(grailsApplication, "getSiteLocationDetails", [siteLocationName: studyLocationName, serviceUrl:"${serviceRootUrl}/getSiteLocationDetails"])
        def results = new StudyLocationDetails(details)
        return results
    }

    @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName,#visitId}")
    def getStudyLocationVisitDetails(String visitId) {
        def details = proxyServiceCall(grailsApplication, "getStudyLocationVisitDetails/${visitId}")
        if (details) {
            return new StudyLocationVisitDetailsTO(cleanMap(details))
        }
        return null
    }

    public List<StudyLocationVisitSearchResult> fiqlSearch(String fiql) {

        def json = proxyServiceCall(grailsApplication, "search", ['_s': fiql])
        def results = []
        json.each {
            def result = new StudyLocationVisitSearchResult(cleanMap(it))
            results << result
        }

        return results
    }

    @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName}")
    def getSearchTraits() {
        def traits = proxyServiceCall(grailsApplication, "getSearchTraits", [:])
        return traits
    }

    @CacheEvict(value = "S2S_StudyLocationCache", allEntries = true)
    public void flushCache() {
        logService.log("Flushing study location Cache")
    }

    @Override
    protected String getServiceRootUrl() {
        return settingService.soils2SatServiceUrl
    }

    @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName,#siteId}")
    public getStudyLocationNameForId(String siteId) {
        def locations = getStudyLocations()
        def names = []
        locations.each { location ->
            names << location.siteName
        }

        def results = proxyServiceCall(grailsApplication, "getStudyLocationSummary", [siteNames: names.join(",")])?.studyLocationSummaryList
        for (def location : results) {
            if (location.siteLocSysId?.toString() == siteId) {
                return location.siteLocationName
            }
        }

        return null
    }

    @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName,#visitId}")
    public String getStudyLocationNameForVisitId(String visitId) {
        def visitDetails = getStudyLocationVisitDetails(visitId)
        return visitDetails?.studyLocationName
    }

    def getSearchTraitData(int searchTraitId) {
        def results = proxyServiceCall(grailsApplication, "getSearchTraitData/${searchTraitId}", [:])
    }

    def getAllowedValuesForTrait(String searchTrait) {
        // first have to find the id for the searchTrait

        def traits = getSearchTraits()
        int id = -1
        def results = []
        traits.each {
            if (it.criteria == searchTrait) {
                id = it.id
            }
        }
        if (id >= 0) {
            // now get the search trait data...
            results = getSearchTraitData(id)

        }

        return results
    }

    def getAvailableSamplingUnits(List visitIds) {
        def samplingUnits = []
        visitIds?.each { visitId ->
            def tmp = getStudyLocationVisitDetails(visitId)
            tmp?.samplingUnits?.each { unit ->
                if (!samplingUnits.find{it.id?.equalsIgnoreCase(unit.id?.toString()) }) {
                    samplingUnits << new SamplingUnitTO(unit)
                }
            }
        }
        return samplingUnits
    }

}

