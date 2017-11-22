import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

/**
 * @Author : Administrator
 * @Date : 2017/11/16 15:09
 * @Description :
 */
public class main3 {
    public static void main(String[] args){
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        // 这里是配置一下不加载css和javaScript,配置起来很简单，是不是
        webClient.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
        webClient.getOptions().setCssEnabled(false); // 禁用css支持
        webClient.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
        webClient.getOptions().setTimeout(50000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待
        System.out.println("aaaaaaa");
//            HtmlPage page1 = (HtmlPage) webClient.getPage("http://www.pkulaw.cn/doSearch.ashx?" +
//                    "Db=chl&" +
//                    "clusterwhere=%2525e6%252595%252588%2525e5%25258a%25259b%2525e7%2525ba%2525a7%2525e5%252588%2525ab%25253dXA01&" +
//                    "clust_db=chl&" +
//                    "Search_Mode=accurate&" +
//                    "range=name&" +
//                    "menu_item=law&" +
//                    "EncodingName=&" +
//                    "time=0.5651965288821565");
        try {
            HtmlPage page1 = (HtmlPage) webClient.getPage("http://www.pkulaw.cn/cluster_call_form.aspx");
            Thread.sleep(3000);
            HtmlDivision page2 = (HtmlDivision) page1.getByXPath("//*[@id=\"cluster_div\"]").get(0);
            System.out.println(page2.asXml());
//            HtmlAnchor anchor = (HtmlAnchor) page1.getByXPath("//*[@id=\"div_fid_0_0\"]/a[1]").get(0);
//            System.out.println("====== " + anchor.toString());
//            HtmlPage page = anchor.click();
//            Thread.sleep(8000);
//            System.out.println(page.asXml());
        }catch (IOException e){
            System.out.println("nnnn");
            e.printStackTrace();
        }catch (InterruptedException e){
            System.out.println("mmmm");
            e.printStackTrace();
        }catch (Exception e){
            System.out.println("zzzz");
            e.printStackTrace();
        }

    }

}
