package graph;

import util.NumberChange;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/2/28 21:39
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class RelationShipLaw {
    private String lawName;
    private String tiaoName;
    private String kuanName;
    private String xiangName;
    private String releaseTime;

    public RelationShipLaw(String lawName, String tiaoName, String kuanName, String xiangName) {
        this.lawName = lawName;
        this.tiaoName = tiaoName;
        this.kuanName = kuanName;
        this.xiangName = xiangName;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
    }

    public String getTiaoName() {
        return tiaoName;
    }

    public void setTiaoName(String tiaoName) {
        this.tiaoName = tiaoName;
    }

    public String getKuanName() {
        return kuanName;
    }

    public void setKuanName(String kuanName) {
        this.kuanName = kuanName;
    }

    public String getXiangName() {
        return xiangName;
    }

    public void setXiangName(String xiangName) {
        this.xiangName = xiangName;
    }

    public int getKuanNum() {
        if (this.kuanName == null || this.kuanName.isEmpty() || this.kuanName.length() < 3) {
            return -1;
        }
        String strNum = this.kuanName.substring(1, this.kuanName.length() - 1);
        return NumberChange.chineseToNumber(strNum);
    }
}
