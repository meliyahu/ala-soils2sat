<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<!doctype html>
<html>
    <head>
        <r:require module='jqueryui' />
        <r:require module='bootstrap_responsive' />
        <meta name="layout" content="detail"/>
        <title>Questions</title>
    </head>
    <body>
        <div class="container">

            <legend>
                <table style="width:100%">
                    <tr>
                        <td>Questions</td>
                        <td></td>
                    </tr>
                </table>
            </legend>

            <div class="alert alert-info">
                <strong>Selected study locations:</strong>
                ${userInstance.applicationState.selectedPlots.collect({ it.name }).join(", ")}
            </div>

            <div class="well well-small">
                <h4>Select a question:</h4>
                <g:if test="${!questions}">
                    <span class="alert alert-danger">No questions have been defined yet</span>
                </g:if>
                <g:else>
                    <ul class="nav nav-pills nav-stacked">
                        <g:each in="${questions}" var="question">
                            <li><a href="#">${question.text}</a></li>
                        </g:each>
                    </ul>
                </g:else>

            </div>

        </div>
        <script type="text/javascript">
        </script>
    </body>
</html>