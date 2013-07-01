<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<!doctype html>
<html xmlns="http://www.w3.org/1999/html">
    <head>
        <meta name="layout" content="detail"/>
        <title>Questions - Question Sampling Units</title>
    </head>
    <body>
        <div class="container">

            <legend>
                <table style="width:100%">
                    <tr>
                        <td>Sampling Units</td>
                        <td style="text-align: right">
                            <a class="btn btn-small" href="${createLink(controller:'question', action:'ecologicalContexts', params:[questionId:question.id])}">Go back to contexts</a>
                        </td>
                    </tr>
                </table>
            </legend>

            <div class="alert alert-info">
                <div>
                    <strong>Selected study locations:</strong>&nbsp;${userInstance.applicationState.selectedPlotNames.collect({ it }).join(", ")}
                </div>
                <div>
                    <strong>Question:</strong>&nbsp;${question.text}
                </div>
                <div>
                    <strong>Selected contexts:</strong> &nbsp;${selectedContexts.collect({it.name}).join(", ")}
                </div>
            </div>

            <g:form controller="question">

                <g:hiddenField name="questionId" value="${question.id}" />

                <div class="well well-small">
                    <h4>Sampling Units:</h4>
                    <ul>
                        <g:each in="${samplingUnits.sort { it.name }}" var="unit">
                            <li>${unit.name}</li>
                        </g:each>
                    </ul>
                </div>

                <div class="control-group">
                    <div class="controls">
                        <button id="btnBack" class="btn">Back</button>
                        <g:submitButton class="btn btn-primary" name="submit" value='Next'/>
                    </div>
                </div>

            </g:form>

        </div>
        <script type="text/javascript">

            $("#btnBack").click(function(e) {
                e.preventDefault();
                window.location = "${createLink(controller:'question', action:'ecologicalContexts', params:[questionId: question.id])}";
            });

        </script>
    </body>
</html>