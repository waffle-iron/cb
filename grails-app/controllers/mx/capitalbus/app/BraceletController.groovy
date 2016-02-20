package mx.capitalbus.app

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import mx.capitalbus.app.bracelet.Bracelet
import mx.capitalbus.app.bracelet.BraceletState
import mx.capitalbus.app.bracelet.CostBracelet

import java.text.SimpleDateFormat

@Secured(value = ["hasAnyRole('ROLE_SUPER_ADMIN','ROLE_SALESMAN', 'ROLE_ADMIN_CONTROL_BRACELET')"])
class BraceletController {


    def braceletService
    def braceletRepository

    def create (){
        render ( new Bracelet() as JSON)
    }

    def index (){
        def rs
        def p = params.list("sort")

        String r = getPrincipal().authorities.authority
        r = r.substring(0, r.length()-1).substring(1)

        def id = getPrincipal().id

        switch (r){
            case "ROLE_SALESMAN":
                rs = braceletRepository.getBySalesmanOrderAndGroupBy(1)

                break
        }
        render ( ["id" : rs] as JSON)
    }

    def save (){
        String json = params.list("json");
        if (json != null) {
            def jsonSlurper = new JsonSlurper()
            def object = jsonSlurper.parseText(json)
            def map  = [:]
            for (String a : object) {
                def cc = JSON.parse(a)
                Integer ic = cc.idCost
                Integer ca = cc.amount
                map.put(ic,ca)
            }
            def mapCVS =  braceletService.generatingBracelets(map)
            render( [text : mapCVS + ""] as JSON )
        }else {
            response.status = 404
            render([message: message(code: "vendedor.notFound")] as JSON)
        }
    }

    def getListOfCreations(){
        def b = Bracelet.createCriteria()
        def results = b.list {
            projections {
                count("creationDate")
                groupProperty('creationDate')
            }
            order("creationDate", "asc")
        }
        render(results as JSON)
    }

    def getCSV(){
        String date = params.d
        if ( date != null) {
            def s = braceletService.getStringOfCSV(date)
            if (s != null){
                response.setHeader("Content-disposition", "attachment; filename=brazaletes.csv")
                render(contentType: "text/csv", text:s )
            }else{
                response.sendError(404)
                render([message: "error"] as JSON)
            }
        } else {
            response.sendError(404)
            render([message: "error"] as JSON)
        }

    }
    @Secured('ROLE_ADMIN_CONTROL_BRACELET')
    def getListOfCreationsByCost(){
        def b = Bracelet.createCriteria()
        def results = b.list {
            projections {
                groupProperty('costBracelet')
                count("costBracelet")
                count("deliveryDate")
            }
            order("costBracelet", "asc")
        }
        render(results as JSON)
    }

    @Secured(value=["hasRole('ROLE_ADMIN_CONTROL_BRACELET')"], httpMethod='POST')
    def toAssignForSalesman(){
        def jsonText = params.json
        def salesman = params.int("salesman")
        if (jsonText != null || !jsonText.empty || salesman > 0)
        {
            def res = braceletService.updateBraceletsWithSalesman(jsonText, salesman)
            render (res as JSON)
        }
        else
            render( ["message":"hubo un error"] as JSON)
    }
}
