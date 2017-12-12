import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/4 13:20
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Main8 {
    public static void main(String[] args){
        System.out.print(get());
        StringBuilder tes = new StringBuilder("cccc\r\n");
        for (int i = tes.length() -1; i > 0; i--){
            System.out.println(i +" " + tes.charAt(i));
            if(tes.charAt(i) == '\n'){
                System.out.println("mm");
            }
        }
        StringBuilder tes2 = new StringBuilder("ccccd");
        System.out.print(tes2.deleteCharAt(4));
    }
    public static String get(){
        String test = "";
        List<String> testlist = new ArrayList<String>();
        try {
            String tt = testlist.get(0);
        }catch (Exception e){
            return test;
        }finally {
            test = "bbbb";
            System.out.println("aa");
        }
        return test;
    }
}
