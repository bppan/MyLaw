package CleanContent;

import Interface.LawClean;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/11 17:06
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class CleanRedundancy extends LawClean {
    public CleanRedundancy(String lawCollection, String cleanCollection) {
        super("", lawCollection, cleanCollection);
    }

    public static void main(String[] args) {
        CleanRedundancy cleanRedundancy = new CleanRedundancy("chinacourt_clean", "cleanLaw");
        cleanRedundancy.doCleanRepeat();
    }

    public String getContentHtmlBySelect(String html) {
        return "";
    }
}
