package cz.incad.vdkcr.server;

import org.aplikator.server.descriptor.Application;

import cz.incad.vdkcr.server.data.*;

public class Structure extends Application {

    public static final Zdroj zdroj = new Zdroj();
        
    public static final Sklizen sklizen = new Sklizen();



    public static final Zaznam zaznam = new Zaznam();
    public static final Status status = new Status();


    public static final Pohled pohled = new Pohled();
    public static final Knihovna knihovna = new Knihovna();

    static {
        zdroj.sklizen = zdroj.reverseCollectionProperty("sklizen", sklizen, sklizen.zdroj);
        //zaznam.setPersistersTriggers(new ZaznamTrigger());
    }
}
