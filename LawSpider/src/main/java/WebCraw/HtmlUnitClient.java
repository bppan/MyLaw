package WebCraw;

import Log.LawLogger;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

/**
 * @Author : Administrator
 * @Date : 2017/11/16 21:09
 * @Description :
 */

public class HtmlUnitClient {
    private static Logger LOGGER = LawLogger.getLawLogger(HtmlUnitClient.class);
    private WebClient webClient;

    private HtmlUnitClient() {
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        this.webClient = getNewHtmlUnitClient(0);
    }

    public static WebClient getSingletonHtmlUntiClent() {
        return SingletonHelp.instance.webClient;
    }

    public static WebClient getNewHtmlUnitClient(int webClient) {
        LOGGER.info("load htmlUnitClient begin...");
        WebClient newClient;
        if (webClient == 0) {
            newClient = new WebClient(BrowserVersion.CHROME);
        } else if (webClient == 1) {
            newClient = new WebClient(BrowserVersion.FIREFOX_45);
        } else {
            newClient = new WebClient(BrowserVersion.INTERNET_EXPLORER);
        }
        newClient.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
        newClient.getOptions().setCssEnabled(false); // 禁用css支持
        //启动cookie管理
        newClient.setCookieManager(new CookieManager());
        //启动ajax代理
        newClient.setAjaxController(new NicelyResynchronizingAjaxController());
        newClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        newClient.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
        newClient.getOptions().setTimeout(50000); // 设置连接超时时间 ，这里是50S。如果为0，则无限期等待
        LOGGER.info("load htmlUnitClient success...");
        return newClient;
    }

    private static class SingletonHelp {
        private static HtmlUnitClient instance = new HtmlUnitClient();
    }


}
