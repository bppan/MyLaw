package WanfangdataSpider;

import Interface.LawClean;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/4 19:21
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class WanfangdataClean extends LawClean {
    public WanfangdataClean(String crawJobCollection, String lawCollection, String cleanCollection) {
        super(crawJobCollection, lawCollection, cleanCollection);
    }

    public String getContentHtmlBySelect(String html) {
        return html;
    }
}
