package tech.soulike.yunzhan.cloudexhibition.base
import org.litepal.crud.LitePalSupport

/**
 * Created by thunder on 18-3-7.
 *
 */
class ResourceData :LitePalSupport(){
    lateinit var adName: String
    var type = 0
    lateinit var adMd5: String
    lateinit var adUrl: String
    var adId:Int = 0
    var adTime:Int = 0
    lateinit var qrCodeUrl:String
    lateinit var qrCodePosition:String
}