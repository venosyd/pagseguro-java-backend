package bequiend.pagseguro.lib;

import bequiend.commons.util.ConfigReader;

/**
 * @author sergio lisan <sels@venosyd.com>
 */
public class PagSeguroConfig extends ConfigReader {

    public static final PagSeguroConfig INSTANCE = new PagSeguroConfig();

    private PagSeguroConfig() {
        super("pagseguro");
    }
}