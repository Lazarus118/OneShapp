package co.aulatech.oneshapp;

import android.net.Uri;

public class Item {
    String _id;
    String price;
    String desc;
    String img;
    String in_stock;
    String tel_num;
    Uri link;

    Item(String _id, String price, String desc, String img, String in_stock, Uri link, String tel_num) {
        this._id = _id;
        this.price = price;
        this.desc = desc;
        this.img = img;
        this.in_stock = in_stock;
        this.link = link;
        this.tel_num = tel_num;
    }


    public String getIn_stock() {
        return in_stock;
    }

    public void setIn_stock(String in_stock) {
        this.in_stock = in_stock;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Uri getLink() {
        return link;
    }

    public void setLink(Uri link) {
        this.link = link;
    }

    public String getTel_num() {
        return tel_num;
    }

    public void setTel_num(String tel_num) {
        this.tel_num = tel_num;
    }
}
