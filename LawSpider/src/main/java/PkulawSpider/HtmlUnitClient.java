package PkulawSpider;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * @Author : Administrator
 * @Date : 2017/11/16 21:09
 * @Description :
 */
public class HtmlUnitClient {
    private WebClient webClient;

    private HtmlUnitClient() {
        System.out.println("load htmlUnitClient begin...");
        webClient = new WebClient(BrowserVersion.CHROME);
        // 这里是配置一下不加载css和加载javaScript
        webClient.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
        webClient.getOptions().setCssEnabled(false); // 禁用css支持
        webClient.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
        webClient.getOptions().setTimeout(50000); // 设置连接超时时间 ，这里是50S。如果为0，则无限期等待
        System.out.println("load htmlUnitClient success...");
    }

    public static WebClient getSingletonHtmlUntiClent() {
        return SingletonHelp.instance.webClient;
    }

    private static class SingletonHelp {
        private static HtmlUnitClient instance = new HtmlUnitClient();
    }

}
