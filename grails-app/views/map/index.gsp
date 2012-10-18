<!doctype html>
<html>
	<head>
    <r:require module='jqueryui' />
    <r:require module='openlayers' />
    <r:require module='fancybox' />

		<meta name="layout" content="soils2sat"/>
		<title>Soils to Satellites</title>
		<style type="text/css" media="screen">
      #content {
        top: 62px;
        bottom: 80px;
        position: absolute;
        width: 100%;
      }

      #mapContent {
        height: 600px;
      }

      #layersContent {
        float: right;
        display: inline-block;
        width: 200px;
      }

      .info-popup {
        position: absolute;
        bottom: 100px;
        right: 10px;
        width: 250px;
        height:100px;
        background: #393939;
        border: 3px solid #393939;
        color: white;
        border-radius: 10px;
        padding: 10px;
        z-index: 9999;
      }

      .info-popup h4 {
        margin: 2px;
      }

      .olLayerGooglePoweredBy {
        display: none;
      }

		</style>

    <script src="http://maps.google.com/maps/api/js?key=AIzaSyDAqE0sOMHNienA0zFIUMlY53ztEDiv0d8&sensor=true" type="text/javascript"></script>
    <link rel="stylesheet" href="${resource(dir: 'js/openlayers/theme/default', file: 'style.css')}" type="text/css">
    <link rel="stylesheet" href="${resource(dir:'/jqwidgets/styles', file:'jqx.base.css')}" type="text/css" />

	</head>
	<body>
    <script type="text/javascript">

      var map;

      $(window).resize(function(e) {
        resizeMap();
      });

      function resizeMap() {
        var height = $(window).height() - (63 + 90);
        $("#mapContent").height(height);
        if (map) {
          setTimeout( function() { map.updateSize();}, 200);
        }

      }

      function refreshSidebar() {
        $.ajax('${createLink(controller:'map', action:'sideBarFragment')}').done(function(html) {
          $("#sidebarContent").html(html);
        });
      }

      function selectPlot(plotName, successCallback) {
        $.ajax("${createLink(controller:'plot', action:'selectPlot')}?plotName=" + plotName).done(function(data) {
          refreshSidebar();

          if (successCallback) {
            successCallback();
          }
        });
      }

      function selectPlots(plotNames, successCallback) {
        var plotstring = plotNames.join(",");
        $.ajax("${createLink(controller:'plot', action:'selectPlots')}?plotNames=" + plotstring).done(function(data) {
          refreshSidebar();
          refreshStudyLocationPoints();
          if (successCallback) {
            successCallback();
          }
        });
      }

      function deselectPlot(plotName, successCallback) {
        $.ajax("${createLink(controller:'plot', action:'deselectPlot')}?plotName=" + plotName).done(function(data) {
          refreshSidebar();
          refreshStudyLocationPoints();
          if (successCallback) {
            successCallback();
          }
        });
      }

      function clearSelectedPlots(successCallback) {
        $.ajax("${createLink(controller:'plot', action:'clearSelectedPlots')}").done(function(data) {
          refreshSidebar();
          refreshStudyLocationPoints();
          if (successCallback) {
            successCallback();
          }
        });

      }

      function displayLayerInfo(layerName) {
        $("#layerInfoContent").attr("layerName", layerName);
        $("#layerInfoLink").click();
        return true;
      }

      function findPlot() {
        $("#findPlotLink").click();
        return true;
      }

      function compareSelectedPlots() {
        $("#comparePlotsLink").click();
        return true;
      }

      function addLayerClicked() {
        $("#addLayerLink").click();
        return true;
      }

      function unloadWMSLayer(layerName) {
        var candidates = map.getLayersByName(layerName);
        if (candidates) {
          for (var i in candidates) {
            var layer = candidates[i];
            layer.destroy();
          }
        }
      }

      function removeLayer(layerName) {
        $.ajax("${createLink(controller: 'map', action:'removeLayer')}?layerName=" + layerName).done(function(e) {
          unloadWMSLayer(layerName);
          refreshSidebar();
        });
      }

      function addPlotPointsLayer(plotList) {

        var plots = map.getLayer("Plots");
        if (plots) {
          map.removeLayer(plots);
        }

        var latLongProj = new OpenLayers.Projection("EPSG:4326");
        plots = new OpenLayers.Layer.Markers("Plots");
        var results = plotList;
        for (resultKey in results) {

          var result = results[resultKey];
          var location = new OpenLayers.LonLat(parseFloat(result.longitude),parseFloat(result.latitude));

          location.transform(latLongProj, map.getProjectionObject());

          var marker = new OpenLayers.Marker(location);
          plots.addMarker(marker);
          marker.tag = result.siteName;

          marker.events.register('mouseover', marker, function(e) {
            showPlotHover(this.tag);
          });

          marker.events.register('mouseout', marker, function(e) {
            hidePlotHover(this.tag);
          });

          marker.events.register('click', marker, function(e) {
            showPlotDetails(this.tag);
          });

        }
        plots.id = "Plots";

        map.addLayer(plots);
        hideMessagePanel();

      }

      function refreshStudyLocationPoints() {
        $.ajax("${createLink(controller: 'plot', action: 'getUserDisplayedPlots')}").done(function(data) {
          addPlotPointsLayer(data);
        });
      }

      $(document).ready( function (e) {

        $("body").css("overflow","hidden");

        resizeMap();
        initMap();
        refreshSidebar();
        refreshStudyLocationPoints();

        $("#addLayerLink").fancybox({
            beforeLoad: function() {
              $.ajax("${createLink(controller: 'map', action:'addLayerFragment')}").done(function(data) {
                $("#addLayerContent").html(data);
              });
            }
        });

        $("#plotDetailsLink").fancybox({
          beforeLoad: function() {
            var plotName = $("#plotDetailsContent").attr("plotName");
            $.ajax("${createLink(controller: 'plot', action:'detailsFragment')}?plotName=" + plotName).done(function(data) {
              $("#plotDetailsContent").html(data);
            });
          }

        });

        $("#findPlotLink").fancybox({
          beforeLoad: function() {
            $("#findPlotContent").html("");
            $.ajax("${createLink(controller: 'plot', action:'findPlotFragment')}").done(function(html) {
              $("#findPlotContent").html(html);
            });
          }
        });

        $("#comparePlotsLink").fancybox({
          beforeLoad: function() {
            $("#comparePlotsContent").html('<h5>Please wait while study location data is retrieved...<img src="${resource(dir:'/images', file:'spinner.gif')}"/></h5>');
            $.ajax("${createLink(controller: 'plot', action:'comparePlotsFragment')}").done(function(html) {
              $("#comparePlotsContent").html(html);
            });
          }
        });


        $("#layerInfoLink").fancybox({
          beforeLoad: function() {
            var layerName = $("#layerInfoContent").attr("layerName");
            $.ajax("${createLink(controller: 'map', action:'layerInfoFragment')}?layerName=" + layerName).done(function(html) {
              $("#layerInfoContent").html(html);
            });
          }
        });

        $("#btnToggleSidebar").click(function(e) {
          toggleSidebar();
        });



        <g:each in="${appState?.layers}" var="layer">
          <g:if test="${layer.visible}">
            loadWMSLayer("${layer.name}", ${layer.opacity ?: 1.0});
          </g:if>
        </g:each>
      });

      function addLayer(name, addToMap) {
        var showInMap = false;
        if (addToMap) {
          showInMap = true;
        }
        $.ajax("${createLink(controller: 'map', action: 'addLayer')}?layerName=" + name + "&addToMap=" + showInMap).done(function (data) {
          refreshSidebar();
          if (showInMap) {
            loadWMSLayer(name, 1.0);
          }
        });

      }

      function addLayerSet(layerSetId, replaceExisting) {
        $.ajax("${createLink(controller: 'map', action: 'addLayerSet')}?layerSetId=" + layerSetId + "&replaceExisting="+replaceExisting).done(function (data) {
          refreshSidebar();
        });
      }

      function clearAllLayers() {
        $.ajax("${createLink(controller: 'map', action: 'removeAllLayers')}").done(function (data) {
          refreshSidebar();
        });
      }

      function loadWMSLayer(name, opacity) {

        var wmsLayer = new OpenLayers.Layer.WMS(name, "${grailsApplication.config.spatialPortalRoot}/geoserver/gwc/service/wms/reflect", {
            layers : 'ALA:' + name,
            srs : 'EPSG:900913',
            format : 'image/png',
            transparent : true
        });

        if (opacity) {
          wmsLayer.setOpacity(opacity);
        }

        // wmsLayer.visibility = false;
        map.addLayer(wmsLayer);
        var plots = map.getLayer("Plots");
        if (plots) {
          map.removeLayer(plots);
          map.addLayer(plots);
        }
      }

      function initMap() {

        infoMessage("Initialising map...");

        var options = {
          projection: "EPSG:900913",
          maxExtent: new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508),
          units:"m",
          controls: [
            new OpenLayers.Control.Navigation({ mouseWheelOptions: {interval: 50, cumulative: false}, zoomBoxEnabled: true } ),
            new OpenLayers.Control.ArgParser(),
            new OpenLayers.Control.PanZoom(),
            new OpenLayers.Control.PanZoomBar()
          ]
        };

        map = new OpenLayers.Map('mapContent', options);

        map.addControl(new OpenLayers.Control.LayerSwitcher());

        var gphy = new OpenLayers.Layer.Google(
            "Google Physical",
            {type: google.maps.MapTypeId.TERRAIN}
        );
        var gmap = new OpenLayers.Layer.Google(
            "Google Streets", // the default
            {numZoomLevels: 20}
        );
        var ghyb = new OpenLayers.Layer.Google(
            "Google Hybrid",
            {type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20}
        );
        var gsat = new OpenLayers.Layer.Google(
            "Google Satellite",
            {type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 20}
        );

        map.addLayers([gphy,gmap,ghyb,gsat]);

        var latLongProj = new OpenLayers.Projection("EPSG:4326");

        <g:if test="${appState?.viewExtent}">
          var extent = new OpenLayers.Bounds(
            ${appState.viewExtent?.left},
            ${appState.viewExtent?.bottom},
            ${appState.viewExtent?.right},
            ${appState.viewExtent?.top}
          );
//          extent.transform(latLongProj, map.getProjectionObject());
          map.zoomToExtent(extent);
        </g:if>
        <g:else>
          var point = new OpenLayers.LonLat(133, -28);
          point.transform(latLongProj, map.getProjectionObject());
          map.setCenter(point, 5);
        </g:else>

        map.events.register("moveend", null, onMapMoved);
        map.events.register("zoomend", null, onMapMoved);

      }

      function showPlotDetails(plotName) {

        $("#plotDetailsContent").attr("plotName", plotName);
        $("#plotDetailsLink").click();
        return true;
      }

      var plotHoverFlag = false;

      function showPlotHover(plotName) {
        plotHoverFlag = true;
        var url = "${createLink(controller:'map', action:'ajaxPlotHover')}?plotName=" + plotName;
        $.ajax(url).done(function(html) {
          if (plotHoverFlag) {
            $("#plotSummary").css("display", "block").html(html);
          }
        });

      }

      function setLayerOpacity(layerName, opacity) {
        var candidates = map.getLayersByName(layerName);

        if (candidates) {
          $.ajax("${createLink(controller: 'map', action:'ajaxSaveLayerOpacity')}?layerName=" + layerName + "&opacity=" + opacity).done(function(result) {
            for (var i in candidates) {
              var layer = candidates[i];
              layer.setOpacity(opacity);
            }
          });
        }

      }

      function hidePlotHover(plotName) {
        plotHoverFlag = false;
        hideMessagePanel();
      }

      function hideMessagePanel() {
        $('#plotSummary').css("display", "none");
      }

      function infoMessage(message) {
        var html = "<h4>" + message + "</h4>"
        $("#plotSummary").css("display", "block").html(html);
      }

      function toggleSidebar() {
        if ($('#mapContainer').hasClass("span8")) {
          $('#mapContainer').removeClass("span8").addClass("span12");
          $('#sidebarContainer').removeClass("span4").addClass("span0");
          $("#btnToggleSidebar").html("Show sidebar");
        } else {
          $('#mapContainer').removeClass("span12").addClass("span8");
          $('#sidebarContainer').removeClass("span0").addClass("span4");
          $("#btnToggleSidebar").html("Hide sidebar");
        }
        resizeMap();
      }

      function onMapMoved(e) {
        if (map) {
          var extent = map.getExtent();
          var url = "${createLink(controller: 'map', action:'ajaxSaveCurrentExtent')}?top=" + extent.top + "&left=" + extent.left + "&bottom=" + extent.bottom + "&right=" + extent.right;
          $.ajax(url).done(function(e) { });
        }
      }

    </script>

    <div class="container-fluid">
      <div class="row-fluid">
        <div id="mapContainer" class="span8">
          <div id="mapContent">
          </div>
        </div>
        <div id="sidebarContainer" class="span4">
          <div id="sidebarContent"></div>
        </div>
      </div>
    </div>

    <div class="info-popup" id="plotSummary" style="display: none; ">
      <H2>Plot details</H2>
    </div>

    <a id="addLayerLink" href="#addLayerDetails" style="display: none"></a>
    <div id="addLayerDetails" style="display:none; width: 800px; height: 500px">
      <div id="addLayerContent">
      </div>
    </div>

    <a id="plotDetailsLink" href="#plotDetails" style="display: none"></a>
    <div id="plotDetails" style="display:none; width: 600px; height: 300px">
      <div id="plotDetailsContent">
      </div>
    </div>

    <a id="findPlotLink" href="#findPlot" style="display: none"></a>
    <div id="findPlot" style="display:none; width: 600px; height: 500px">
      <div id="findPlotContent">
      </div>
    </div>

    <a id="layerInfoLink" href="#layerInfo" style="display: none"></a>
    <div id="layerInfo" style="display:none; width: 600px; height: 500px">
      <div id="layerInfoContent">
      </div>
    </div>

    <a id="comparePlotsLink" href="#comparePlots" style="display: none"></a>
    <div id="comparePlots" style="display:none; width: 800px; height: 550px">
      <div id="comparePlotsContent">
      </div>
    </div>

    <content tag="buttonBar">
        <button id="btnToggleSidebar" class="btn btn-small">Hide sidebar</button>
    </content>

    <script src="${resource(dir: '/jqwidgets', file: 'jqxcore.js')}" type="text/javascript" ></script>
    <script src="${resource(dir: '/jqwidgets', file: 'jqxpanel.js')}" type="text/javascript" ></script>
    <script src="${resource(dir: '/jqwidgets', file: 'jqxscrollbar.js')}" type="text/javascript" ></script>
    <script src="${resource(dir: '/jqwidgets', file: 'jqxbuttons.js')}" type="text/javascript" ></script>
    <script src="${resource(dir: '/jqwidgets', file: 'jqxexpander.js')}" type="text/javascript" ></script>
    <script src="${resource(dir: '/jqwidgets', file: 'jqxtree.js')}" type="text/javascript" ></script>

	</body>
</html>
