package au.org.ala.soils2sat

import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.json.JSONObject

class StudyLocationService extends ServiceBase {

    def grailsApplication
    def layerService
    def logService

    @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName}")
    List<StudyLocationTO> getStudyLocations() {
        def studyLocations = proxyServiceCall(grailsApplication, "getStudyLocations")
        def results = new ArrayList<StudyLocationTO>()
        studyLocations?.each { studyLocation ->
            if (!studyLocation.longitude && !studyLocation.latitude) {
                // print "Discarding studyLocation (no position data): " + studyLocation.studyLocationName
            }

            def map = cleanMap(studyLocation)
            results << new StudyLocationTO(map)
        }

        return results
    }

    private Map cleanMap(JSONElement elem) {
        elem.each {
            if (it.value instanceof JSONObject.Null) {
                it.value = null
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

    public getSamplingUnitDetails(String visitId, String samplingUnitTypeId) {
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

    public getSoilPhForStudyLocation(String studyLocationName) {
        // Get the most recent visit, and get its soil PH from the soil characterisation sampling unit...
        List<StudyLocationVisitTO> visits = getStudyLocationVisits(studyLocationName)
        def rows = []
        def visit = visits.max { it.visitStartDate }
        if (visit) {
            def samplingUnit = getSamplingUnitDetails(visit.studyLocationVisitId, "7")
            if (samplingUnit?.samplingUnitData) {
                rows = samplingUnit.samplingUnitData.collect { [upperDepth: it.upperDepth, lowerDepth: it.lowerDepth, pH: it.ph ] }
            }
            rows = rows?.sort { it.upperDepth }
        }
        return rows
    }

    public getSoilECForStudyLocation(String studyLocationName) {
        // Get the most recent visit, and get its soil PH from the soil characterisation sampling unit...
        List<StudyLocationVisitTO> visits = getStudyLocationVisits(studyLocationName)
        def rows = []
        def visit = visits.max { it.visitStartDate }
        if (visit) {
            def samplingUnit = getSamplingUnitDetails(visit.studyLocationVisitId, "7")
            if (samplingUnit?.samplingUnitData) {
                rows = samplingUnit.samplingUnitData.collect { [upperDepth: it.upperDepth, lowerDepth: it.lowerDepth, EC: it.ec ] }
            }
            rows = rows?.sort { it.upperDepth }
        }
        return rows
    }

    public getVoucheredTaxaForStudyLocation(String studyLocationName) {
        def vouchers = proxyServiceCall(grailsApplication, "getStudyLocationVouchers/${studyLocationName}")
        def taxa = []
        vouchers.each { voucher ->
            if (voucher.herbariumDetermination && !voucher.herbariumDetermination.toString().equalsIgnoreCase('NO ID')) {
                taxa << voucher.herbariumDetermination
            }
        }
        return taxa?.sort { it }
    }

    // @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName,#studyLocationName}")
    def getStudyLocationDetailsOld(String studyLocationName) {
        def details = proxyServiceCall(grailsApplication, "getSiteLocationDetails", [siteLocationName: studyLocationName, serviceUrl:'http://s2s-dev.ecoinformatics.org.au:8080/s2s-services/getSiteLocationDetails'])
        def results = new StudyLocationDetails(details)
        return results
    }

    // @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName,#visitId}")
    def getVisitDetails(String visitId) {
        def details = proxyServiceCall(grailsApplication, "getStudyLocationVisitDetails/${visitId}")
        def visit = new StudyLocationVisitDetailsTO(cleanMap(details))
        return visit
    }

    @Cacheable(value="S2S_StudyLocationCache", key="{#root.methodName}")
    def getSearchTraits() {
        def traits = proxyServiceCall(grailsApplication, "getSearchTraits", [])
        return traits
    }

    @CacheEvict("S2S_LayerCache")
    public void flushCache() {
        logService.log("Flushing Layer Cache")
    }


    @Override
    protected String getServiceRootUrl() {
        return "${grailsApplication.config.aekosServiceRoot}"
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
        def visitDetails = getVisitDetails(visitId)
        return visitDetails?.studyLocationName
    }

}

