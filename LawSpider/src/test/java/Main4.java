import PkulawSpider.HtmlUnitClient;
import PkulawSpider.LawSpider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;

/**
 * @Author : Administrator
 * @Date : 2017/11/16 21:45
 * @Description :
 */
public class Main4 {
    public static void main(String[] args){
        crawSoureceUrlField("//*[@id=\"div_fid_0_0\"]");
    }
    public static void crawSoureceUrlField(String xpath) {
        WebClient client = HtmlUnitClient.getSingletonHtmlUntiClent();
        try {
            HtmlPage page = (HtmlPage) client.getPage("http://www.pkulaw.cn");
            //等待5秒后获取页面
            Thread.sleep(3000);
            //获取局部source url
            HtmlDivision sourceFiled = (HtmlDivision) page.getByXPath(xpath).get(0);
            DomNodeList<HtmlElement> anchoresNodes = sourceFiled.getElementsByTagName("a");
            System.out.println(anchoresNodes.size());
            for (int m = 0; m < anchoresNodes.size(); m++) {
                HtmlAnchor anchor = (HtmlAnchor) anchoresNodes.get(m);
                System.out.println(anchor.getAttribute("aaaaa"));
//                HtmlPage clickPage = (HtmlPage) anchor.click();
//                Thread.sleep(3000);
//
//                System.out.println(clickPage.asXml());
//                //响应事件
//                //Event event=new Event();
//                //event.setEventType(Event.TYPE_KEY_DOWN);
//                //HtmlPage hpm=pic.click(event); //bpic.fireEvent(event);
//                //执行js
//                String hrefValue = "javascript:add(1,1,'+');";
//                ScriptResult s = page.executeJavaScript(hrefValue);//执行js方法
//                HtmlPage hpm=(HtmlPage) s.getNewPage();//获得执行后的新page对象
//                webClient.waitForBackgroundJavaScript(10000);
//                HtmlElement lpic=hpm.getHtmlElementById("viEnlargeImgLayer_img_ctr");
//                System.out.println(lpic.asXml());
            }
//            System.out

        } catch (IOException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

