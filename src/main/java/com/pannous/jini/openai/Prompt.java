package com.pannous.jini.openai;


public class Prompt {
    public static final Prompt IMPLEMENT = new Prompt("Act like a professional programmer. From concise requests you infer the intended functionality. First write out the idea in plain text, as a comment in the appropirate programming language. Then write out the code in a function, multiple functions or the appropriate data format (such as xml, json or any other). Now, implement the following specification using code in the mentioned language. ");
    public static final Prompt CHAT = new Prompt("You are a professional assistant. You are chatting with the programmer about the following topic: ");
    public static final Prompt FIX = new Prompt("analyze and fix the following error");
    public static final Prompt DOCUMENTATION = new Prompt("write documentation for the following code");
    public static final Prompt EXPLAIN = new Prompt("explain and summarize the following");
    public static final Prompt COMPLETE = new Prompt("complete the following code");
    public static final Prompt CONVERT = new Prompt("convert the following code by transpiling it to ");
    public static final Prompt REVIEW = new Prompt("analyze, code review and make suggestions to improve the following code");
    public static final Prompt TESTS = new Prompt("emit tests for the following code");
    public static final Prompt OPTIMIZE = new Prompt("analyze the code for optimization potential, then optimize the code accordingly");
    public static final Prompt IMPROVE = new Prompt("analyze, code review the following code, then improve and optimize the code accordingly");
    public static final Prompt EDIT = new Prompt("edit the following code using the following instructions: ");
    public static final Prompt EXECUTE = new Prompt("Given english instructions, find the right terminal command in a zsh shell. The command should be in the following format: ```bash <command>```");
    private final String text;
    public String language;

    public Prompt(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String name() {
        if (this == FIX) return "FIX";
        if (this == DOCUMENTATION) return "DOCUMENTATION";
        if (this == EXPLAIN) return "EXPLAIN";
        if (this == COMPLETE) return "COMPLETE";
        if (this == CONVERT) return "CONVERT";
        if (this == REVIEW) return "REVIEW";
        if (this == TESTS) return "TESTS";
        if (this == OPTIMIZE) return "OPTIMIZE";
        if (this == IMPROVE) return "IMPROVE";
        if (this == EDIT) return "EDIT";
        return "OpenAI thinking …";
    }
}

//
//    enum Prompt {
//        FIX("analyze and fix the following error"),
//        DOCUMENTATION("write documentation for the following code"),
//        EXPLAIN("explain and summarize the following"),
//        COMPLETE("complete the following code"),
//        CONVERT("convert the following code by transpiling it to "), // ...
//        REVIEW("analyze, code review and improve the following code"),
//        TESTS("emit tests for the following code"),
//        OPTIMIZE("analyze, code review the following code, then improve and optimize the code accordingly"),
//        //    IMPROVE("improve the following code using the following instructions");
//        IMPROVE("improve the following code using the instructions after the IMPROVE keyword"),
//        EDIT("edit the following code using the following instructions");// todo: split
//
//        final String text;
//
//        Prompt(String text) {
//            this.text = text;
//        }
//
//        String getText() {
//            return text;
//        }
//    }
