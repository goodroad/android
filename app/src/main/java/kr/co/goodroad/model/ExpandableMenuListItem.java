package kr.co.goodroad.model;

import android.widget.ImageView;
import java.util.List;

/**
 * Created by hjlee on 2017-07-31.
 */

public class ExpandableMenuListItem {
    private String title;
    private ImageView collapse;
    private List<ExpandableMenuListItem> subMenus;
    private float rotate = 0;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ImageView getCollapse() {
        return collapse;
    }

    public void setCollapse(ImageView collapse) {
        this.collapse = collapse;
    }

    public List<ExpandableMenuListItem> getSubMenus() {
        return subMenus;
    }

    public void setSubMenus(List<ExpandableMenuListItem> subMenus) {
        this.subMenus = subMenus;
    }

    public float getRotate() {
        return rotate;
    }

    public void setRotate(float rotate) {
        this.rotate = rotate;
    }

    @Override
    public String toString() {
        return "ExpandableMenuListItem{" +
                "title='" + title + '\'' +
                ", collapse=" + collapse +
                ", subMenus=" + subMenus +
                ", rotate=" + rotate +
                '}';
    }
}
