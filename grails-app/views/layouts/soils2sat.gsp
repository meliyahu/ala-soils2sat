<!doctype html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="Grails"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="shortcut icon" href="https://www.ala.org.au/wp-content/themes/ala2011/images/favicon.ico" type="image/x-icon">
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'soils2sat.css')}" type="text/css">
		<g:layoutHead/>
    <r:require module="bootstrap" />
		<r:layoutResources />
    <Style type="text/css">

    body {
        padding-top: 60px;
        padding-bottom: 90px;
    }

    .footer {
        height: 80px;
        bottom: 0px;
        position: fixed;
        width: 100%;
        background-color: #efefef;

    }

    #buttonBar .btn {
        margin-top: 0px;
    }

    </Style>

    <script type="text/javascript">
      $(document).ready(function(e) {
        $("#btnLogout").click(function(e) {
           window.location = "${createLink(controller: 'logout', action:'index')}";
        });
      });
    </script>
	</head>
	<body style="overflow: hidden">
    <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container-fluid">
          <a class="brand">Soils2Satellite</a>
          <div class="nav-collapse collapse">
            <div class="navbar-text pull-right">
              <span id="buttonBar">
                <sec:ifLoggedIn>
                  <span><sec:username/>&nbsp;<button class="btn" id="btnLogout">Logout</button></span>
                </sec:ifLoggedIn>

                <g:pageProperty name="page.buttonBar" />
              </span>
            </div>
            <sts:navbar active="${pageProperty(name: 'page.topLevelNav')}" />
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>
		<g:layoutBody/>
		<div class="footer" role="contentinfo">
      <img src="${resource(dir:'/images', file:'S2S-banner.png')}" style="height: 80px" />
		</div>
		<div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
		<g:javascript library="application"/>
		<r:layoutResources />
	</body>
</html>