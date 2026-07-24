package com.capubbs;

import com.capubbs.lib.MyCalendar;

import java.util.ArrayList;

public class PostInfo {
    String author;
    int pid;
    int fid;
    long timestamp;
    int lzl;
    String content;
    String sig;
    String sigContent;
    boolean sigLoaded;
    ArrayList<String> imgs;

    public PostInfo(String _author, int _pid, int _fid, String _time, int _lzl, String _content, String _sig) {
        author=_author;
        pid=_pid;
        fid=_fid;
        timestamp= MyCalendar.format(_time);
        lzl=_lzl;
        content=_content;
        sig=_sig;
        sigContent = "";
        sigLoaded = false;
        imgs=new ArrayList<>();
    }
}
