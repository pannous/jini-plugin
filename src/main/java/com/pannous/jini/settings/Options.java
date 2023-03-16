package com.pannous.jini.settings;

public class Options {
    public static final Options none = new Options(0);
    public static final Options replace = new Options(1);
    public static final Options comment = new Options(2);
    public static final Options popup = new Options(2 << 3);
    public static final Options select_all_if_none = new Options(2 << 4);
    public static final Options select_file_if_none = new Options(2 << 5);
    public static final Options select_line_if_none = new Options(2 << 6);
    public static final Options insert_before = new Options(2 << 7);
    public static final Options insert_after = new Options(2 << 8);
    public static final Options noExplanations = new Options(2 << 9);
    public static final Options newFile = new Options(2 << 10);
    public static final Options fix = new Options(2 << 11);;
    private final int value;

    public Options(int i) {
        value = i;
    }
    public boolean has(Options o) {
        return (value & o.value) != 0;
    }

    public Options or(Options replace) {
        return new Options(value | replace.value);
    }

    public Options remove(Options options) {
        return new Options(value & ~options.value);
    }
}
