package com.venosyd.opensource.aio.pagseguro.lib;

import com.venosyd.opensource.aio.commons.util.ConfigReader;

/**
 * @author sergio lisan <sels@venosyd.com>
 */
public class PagSeguroConfig extends ConfigReader {

    public static final PagSeguroConfig INSTANCE = new PagSeguroConfig();

    private PagSeguroConfig() {
        super("pagseguro");
    }
}