/**
 * @Author : Administrator
 * @Date : 2017/11/20 15:32
 * @Description :
 */
public class Main6 {
    public void getFwp(int i, int j, String... m) {
        if (m.length != 0) {
            System.out.println(m.length);
            System.out.println(m[0]);
            System.out.println("hk");
        }
        System.out.println(i);
        System.out.println(j);
    }
    public static void main(String[] args) {
        Main6 s = new Main6();
        s.getFwp(1, 2);
        s.getFwp(1, 2, "sdfsadf");
//        s.getFwp(1, 2, "sdfsadf1", "sfasdf");
    }
}