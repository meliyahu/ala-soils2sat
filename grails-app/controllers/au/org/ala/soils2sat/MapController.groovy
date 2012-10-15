package au.org.ala.soils2sat

import grails.converters.JSON

class MapController {
    
    def springSecurityService
    def plotService
    def layerService

    def index() {
        def userInstance = springSecurityService.currentUser as User
        [userInstance: userInstance , appState: userInstance?.applicationState]
    }

    def addLayerFragment() {
    }

    def addLayer() {
        String layerName = params.layerName
        User userInstance = springSecurityService.currentUser as User
        boolean success = false

        if (layerName && userInstance) {
            def appState = userInstance.applicationState
            def existing = appState.layers.find {
                it.name == layerName
            }
            if (!existing) {
                def visible = params.boolean("addToMap")
                def layer = new EnvironmentalLayer(name: layerName, visible: visible)
                appState.addToLayers(layer)
                appState.save(flush: true, failOnError: true)
                success = true
            }
        }
        render([status: success ? 'ok' : 'failed'] as JSON)
    }

    def removeLayer() {
        String layerName = params.layerName
        User userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState

        def existing = appState.layers.find {
            it.name == layerName
        }
        boolean success = false
        if (layerName && userInstance && existing) {
            appState.removeFromLayers(existing)
            success = true
        }
        render([status:success ? 'ok' : 'failed'] as JSON)
    }

    def removeAllLayers() {
        User userInstance = springSecurityService.currentUser as User
        def success = false
        if (userInstance) {
            def appState = userInstance.applicationState
            appState?.layers?.clear()
            appState?.save(flush: true, failOnError: true)
            success = true
        }
        render([status: success ? 'ok':'failed'] as JSON)
    }

    def sideBarFragment() {
        def userInstance = springSecurityService.currentUser as User
        [userInstance: userInstance, appState: userInstance?.applicationState]
    }

    def ajaxSetLayerVisibility() {
        def layerName = params.layerName
        def visibility = params.boolean("visibility") ?: false
        def userInstance = springSecurityService.currentUser as User
        def opacity = 1.0
        if (layerName && userInstance) {
            def layer = userInstance.applicationState?.layers?.find { it.name == layerName }
            if (layer) {
                layer.visible = visibility
                layer.save(flush: true, failOnError: true)
                opacity = layer.opacity
            }
        }
        render([status:'ok', opacity: opacity] as JSON)
    }

    def ajaxPlotHover() {
        def plotName = params.plotName
        PlotSearchResult plot = null
        if (plotName) {
            plot = plotService.getPlotSummary(plotName)
        }

        [plot: plot, plotName: plotName]
    }

    def layerInfoFragment() {
        def layerName = params.layerName;
        def info = [:]
        if (layerName) {
            info = layerService.getLayerInfo(layerName)
        }
        [layerName: layerName, layerInfo: info]
    }

    def ajaxSaveCurrentExtent = {
        def top = params.double("top")
        def left = params.double("left")
        def bottom = params.double("bottom")
        def right = params.double("right")

        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState
        appState.lock()
        def success = false;

        if (appState && top && left && bottom && right) {
            if (!appState.viewExtent) {
                def extent = new Bounds(top: top, left: left, right: right, bottom:bottom)
                extent.save(flush:true, failOnError: true)
                appState.setViewExtent(extent)
            } else {
                appState.viewExtent.top = top
                appState.viewExtent.left = left
                appState.viewExtent.bottom = bottom
                appState.viewExtent.right = right
            }

            appState.save(flush: true, failOnError: true)
            success = true
        }

        render([status: success ? 'ok' : 'failed'] as JSON)
    }

    def ajaxSaveLayerOpacity = {
        def layerName = params.layerName
        def opacity = params.float("opacity")

        boolean success = false

        def userInstance = springSecurityService.currentUser as User
        if (layerName) {
            def layerInstance = userInstance.applicationState?.layers?.find { it.name == layerName }
            if (layerInstance) {
                layerInstance.opacity = opacity
                layerInstance.save(flush: true, failOnError: true)
                success= true
            }
        }
        render([status: success ? 'ok' : 'failed'] as JSON)
    }

    def browseLayersFragment = {
        def layers = layerService.getAllLayers()
        def layerMap = ['_':[]]

        layers.each {
            if (it.classification1) {
                def topLevel = layerMap[it.classification1]
                if (!topLevel) {
                    topLevel = ['_':[]]
                    layerMap[it.classification1] = topLevel
                }

                if (it.classification2) {
                    def secondLevel = topLevel[it.classification2]
                    if (!secondLevel) {
                        secondLevel = []
                        topLevel[it.classification2] = secondLevel
                    }
                    secondLevel << it
                } else {
                    topLevel['_'] << it
                }
            } else {
                layerMap['_'] << it
            }
        }

        println layerMap

        [layerMap: layerMap]
    }

    def layerSummaryFragment = {
        def layerName = params.layerName;
        def info = [:]
        LayerDefinition layerDefinition = null
        if (layerName) {
            info = layerService.getLayerInfo(layerName)
            layerDefinition = new LayerDefinition()
            info.each {
                if (it.value && layerDefinition.hasProperty(it.key)) {
                    layerDefinition[it.key] = it.value
                }
            }
        }
        [layerName: layerName, layerDefinition: layerDefinition]
    }

    def layerToolsFragment = {
        def layerName = params.layerName;
        def userInstance = springSecurityService.currentUser as User
        if (layerName) {
            def layerInstance = userInstance.applicationState?.layers?.find { it.name == layerName }
            if (layerInstance) {
                def layerInfo = layerService.getLayerInfo(layerName)
                return [layerInstance: layerInstance, layerName: layerName, layerInfo: layerInfo]
            }
        }
    }

}
