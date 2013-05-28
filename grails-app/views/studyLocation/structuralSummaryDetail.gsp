<%@ page import="org.apache.commons.lang.WordUtils" %>
<!doctype html>
<html>
    <head>
        <r:require module='jqueryui'/>
        <r:require module='bootstrap_responsive'/>
        <meta name="layout" content="detail"/>
        <title>Sampling Unit details - ${studyLocationDetail?.studyLocationName} - Visit <sts:formatDateStr date="${visitDetail.visitStartDate}" />  - ${samplingUnitName}</title>
    </head>

    <body>
        <div class="container-fluid">
            <legend>
                <table style="width:100%">
                    <tr>
                        <td>
                            <a href="${createLink(controller:'map', action:'index')}">Map</a><sts:navSeperator/>
                            <a href="${createLink(controller: 'studyLocation', action: 'studyLocationSummary', params: [studyLocationName: studyLocationDetail.studyLocationName])}">${studyLocationDetail.studyLocationName}</a><sts:navSeperator/>
                            <a href="${createLink(controller: 'studyLocation', action: 'studyLocationVisitSummary', params: [studyLocationVisitId: visitDetail.studyLocationVisitId])}">Visit ${visitDetail.studyLocationVisitId}</a><sts:navSeperator/>
                            ${samplingUnitName}</td>
                        <td></td>
                    </tr>
                </table>
            </legend>

            <g:set var="detail" value="${dataList[0]}" />
            <g:set var="fields" value="['upper1Dominant','upper2Dominant','upper3Dominant','mid1Dominant','mid2Dominant','mid3Dominant','ground1Dominant','ground2Dominant','ground3Dominant']" />

            <h4>Sampling Unit - ${samplingUnitName}</h4>
            <div>
                <table class="table table-bordered">
                    <tr>
                        <td>
                            <strong>Description:</strong><br/>
                            ${detail?.description}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <strong>Phenology Comment:</strong><br/>
                            ${detail?.phenologyComment}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <strong>Mass Flowing Event:</strong><br/>
                            ${detail?.massFloweringEvent}
                        </td>
                    </tr>
                </table>
            </div>

            <table class="table table-bordered table-striped">
                <g:each in="${fields}" var="field">
                    <tr>
                        <td>${field}</td>
                        <td><sts:taxaHomePageLink name="${detail[field]}" /></td>
                    </tr>
                </g:each>
            </table>

        </div>
    </body>
</html>