/**
 * @Author : Administrator
 * @Date : 2017/11/15 17:21
 * @Description :
 */
public class SpiderMove {
    public static void main(String[] args){
        String indeUrl = "http://www.pkulaw.cn/doSearch.ashx?range=name&check_hide_xljb=1&Db=chl&check_gaojijs=1&orderby=%25E5%258F%2591%25E5%25B8%2583%25E6%2597%25A5%25E6%259C%259F&fdep_id=&pdep_id=&shixiao_id=&xiaoli_id=&sort_id=&hidtrsWhere=377EF8C056C62113E3510356CD866D062CD82F4BD0A1F26B&&nomap=&clusterwhere=%2525e6%252595%252588%2525e5%25258a%25259b%2525e7%2525ba%2525a7%2525e5%252588%2525ab%25253dXA01&aim_page=1&page_count=60&clust_db=chl&menu_item=law&EncodingName=&time=0.2992951558904524";
        String rootUrl = "http://www.pkulaw.cn";
        int crwaThreadCount = 10;
        LawSpider spider = new LawSpider(indeUrl, rootUrl, crwaThreadCount);
        spider.doCraw();
        for(int i = 0; i <= 60; i++){
            String indeUrltemp = "http://www.pkulaw.cn/doSearch.ashx?range=name&check_hide_xljb=1&Db=chl&check_gaojijs=1&orderby=%25E5%258F%2591%25E5%25B8%2583%25E6%2597%25A5%25E6%259C%259F&fdep_id=&pdep_id=&shixiao_id=&xiaoli_id=&sort_id=&hidtrsWhere=377EF8C056C62113E3510356CD866D062CD82F4BD0A1F26B&&nomap=&clusterwhere=%2525e6%252595%252588%2525e5%25258a%25259b%2525e7%2525ba%2525a7%2525e5%252588%2525ab%25253dXA01" +
                    "&aim_page=" + i +
                    "&page_count=60" +
                    "&clust_db=chl" +
                    "&menu_item=law" +
                    "&EncodingName=&time=0.2992951558904524";
            spider.parseHomePage(indeUrltemp);
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){

            }
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for(int i = 0 ; i <= 216; i++){
//                    String tempIndex = "http://www.pkulaw.cn/doSearch.ashx?range=name&Search_Mode=accurate&menu_item=law&Db=chl,protocol,lawexplanation,whitebook,workreport,introduction&hidtrsWhere=377EF8C056C621134FB3B8C2F520A66751E70D74A6607102&&nomap=&clusterwhere=%2525e6%252595%252588%2525e5%25258a%25259b%2525e7%2525ba%2525a7%2525e5%252588%2525ab%25253dXC02&" +
//                            "aim_page=" + i +
//                            "&page_count=216" +
//                            "&clust_db=chl&EncodingName=&time=0.6806777023615227";
//                    spider.parseHomePage(tempIndex);
//                    try {
//                        Thread.sleep(1000);
//                    }catch (InterruptedException e){
//
//                    }
//
//                }
//
//            }
//        }).start();
    }
}
