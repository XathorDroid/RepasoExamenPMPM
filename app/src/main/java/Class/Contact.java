package Class;

import java.io.Serializable;

public class Contact implements Serializable {

    private String name;
    private String tlf;
    private String mail;
    private int icon;

    public Contact(){}

    public Contact(String name, String tlf, String mail, int icon) {
        this.name = name;
        this.tlf = tlf;
        this.mail = mail;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTlf() {
        return tlf;
    }

    public void setTlf(String tlf) {
        this.tlf = tlf;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        String msg = getName()+" - "+getTlf()+" - "+getMail();
        return msg;
    }
}