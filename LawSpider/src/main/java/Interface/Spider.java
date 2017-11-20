package Interface;

/**
 * @Author : Administrator
 * @Date : 2017/11/10 17:23
 * @Description :
 */
public abstract class Spider {
    public abstract boolean addUrl(String title, String url);

    public abstract String getUrl();

    public abstract void doCraw();
}
