package cz.incad.vdkcr.server;

import org.aplikator.server.descriptor.Application;

import cz.incad.vdkcr.server.data.*;

public class Structure extends Application {

    public static final Zdroj zdroj = new Zdroj();
        
    public static final Sklizen sklizen = new Sklizen();


    public static final Autor autor = new Autor();
    public static final Edice edice = new Edice();
    public static final Exemplar exemplar = new Exemplar();
    public static final Rozsah rozsah = new Rozsah();
    public static final Identifikator identifikator = new Identifikator();
    public static final Jazyk jazyk = new Jazyk();
    public static final Vydani vydani = new Vydani();
    public static final Periodicita periodicita = new Periodicita();
    public static final Nazev nazev = new Nazev();
    public static final DigitalniVerze digitalniVerze = new DigitalniVerze();

    public static final Zaznam zaznam = new Zaznam();
    public static final Status status = new Status();


    public static final Pohled pohled = new Pohled();
    public static final Knihovna knihovna = new Knihovna();
    public static final UniqueDoc uniquedoc = new UniqueDoc();

    static {
        zdroj.sklizen = zdroj.reverseCollectionProperty("sklizen", sklizen, sklizen.zdroj);
        //zaznam.setPersistersTriggers(new ZaznamTrigger());
    }
}
