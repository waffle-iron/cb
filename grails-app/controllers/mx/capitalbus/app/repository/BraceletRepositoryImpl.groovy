package mx.capitalbus.app.repository

import grails.converters.JSON
import mx.capitalbus.app.bracelet.Bracelet
import mx.capitalbus.app.bracelet.CostBracelet
import mx.capitalbus.app.user.Salesman

import java.text.SimpleDateFormat

/**
 * Created by becm on 2/19/16.
 */
class BraceletRepositoryImpl implements BraceletRepository {

    def springSecurityService

    @Override
    def getBySalesmanOrderAndGroupByCostBracelet(Salesman s) {
        def b = Bracelet.createCriteria()
        def results = b.list {
            projections {
                groupProperty('costBracelet')
                count("costBracelet")
            }
            and {
                eq("salesman", s)
            }
            order("costBracelet", "asc")
        }
        results
    }

    @Override
    def getBySalesmanOrderAndGroupBySold(Salesman s) {
        def b = Bracelet.createCriteria()
        def results = b.list {
            projections {
                groupProperty('costBracelet')
                count("sold")
            }
            and {
                eq("salesman", s)
                eq("sold", true)
            }
            order("costBracelet", "asc")
        }
        results
    }

    @Override
    def getBySalesmanOrderAndGroupByDeliveryDate(Salesman s, String dateText) {

        def b = Bracelet.createCriteria()
        def results = b.list {
            projections {
                groupProperty('costBracelet')
                groupProperty('deliveryDate')
                count("costBracelet")
            }
            and {
                eq("salesman", s)
            }
            order("deliveryDate", "asc")
        }
        results
    }

    @Override
    def getBySalesmanNotSold(Salesman s, CostBracelet cb, String sd, String ed) {
        Date start, end
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-6"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");

        println(sd + "          ----------------------                   " + ed)

        Calendar now = Calendar.getInstance();
        if (sd == null && ed == null){
            now.set(Calendar.HOUR, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.HOUR_OF_DAY, 0);
            start = now.getTime()
            end = new Date()
            println( "" + start + "          ----------------------                   " + end + "")
        }else{

            now.setTime(sdf.parse(ed));
            now.set(Calendar.HOUR, 23);
            now.set(Calendar.MINUTE, 59);
            now.set(Calendar.SECOND, 59);
            now.set(Calendar.HOUR_OF_DAY, 23);

            end = now.getTime()

            now.setTime(sdf.parse(sd));
            now.set(Calendar.HOUR, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.HOUR_OF_DAY, 0);
            start = now.getTime()
            println( "" + start + "          ----------------------                   " + end + "")
        }



        def results
        def query = Bracelet.where {
            (salesman == s) && (costBracelet == cb) && (sold == false) && (deliveryDate >= start && deliveryDate <= end)
        }
        results = query.order( 'id', 'asc' ).list()
        println("-------------------------------------->"  + results.size())
        results
    }
    @Override
    def getHistoryBySalesmanYesSold(String sd, String ed, long id, String ss) {
        Date start, end
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-6"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");

        Calendar now = Calendar.getInstance();
        if (sd == null && ed == null){
            now.set(Calendar.HOUR, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.HOUR_OF_DAY, 0);
            end = new Date()
            now.set(Calendar.DAY_OF_MONTH, -29);
            start = now.getTime()
        }else{

            now.setTime(sdf.parse(ed));
            now.set(Calendar.HOUR, 23);
            now.set(Calendar.MINUTE, 59);
            now.set(Calendar.SECOND, 59);
            now.set(Calendar.HOUR_OF_DAY, 23);

            end = now.getTime()

            now.setTime(sdf.parse(sd));
            now.set(Calendar.HOUR, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.HOUR_OF_DAY, 0);
            start = now.getTime()
        }

        def results
        def query = Bracelet.createCriteria()
        results = query.list {
            projections {
                groupProperty('soldDate')
                count("soldDate")
                if(ss){
                    groupProperty('costBracelet')
                    count("costBracelet")
                }
            }
            and {
                eq("salesman", Salesman.findById(id))
                eq("sold", true)
                if(ss){
                    def a = sdf.parse(ss)
                    now.setTime(a);
                    now.add(Calendar.SECOND, 1);
                    def e = now.getTime()
                    println(" -- " + a + " -- " + e)
                    between("soldDate", a, e)
                }else{
                    between("soldDate", start, end)
                }
            }
            order("soldDate", "desc")
        }
        results
    }
}
