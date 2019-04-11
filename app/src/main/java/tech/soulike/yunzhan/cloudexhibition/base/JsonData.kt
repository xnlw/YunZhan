package tech.soulike.yunzhan.cloudexhibition.base

/**
 * Created by thunder on 18-3-6.
 * This class is designed to parse json data, where the fields and attributes may have multiple meanings
 */
class JsonData {
    lateinit var data: Data
    var code = -1
    lateinit var ad: List<Advertise>

    class Advertise {
        lateinit var url: String
        var id:Int = 0
        var qrCodeUrl:String? = null
        var type:Int=0
        var time:Int=20
        var qrCodePosition:Int=-1
    }
    class Data {
        lateinit var url: String
        /**
         * Representative polling interval in the polling system
         */
        lateinit var time: String
        /**
         * In the request 'gain_json', this field represents resource file's url
         */
        lateinit var json_url: String
        /**
         * Representative self-start time in the polling system
         */
        var auto_time: String?= null


        var bind:Boolean= false

        var status:Boolean=false

        var update:Long =-1L

    }
}