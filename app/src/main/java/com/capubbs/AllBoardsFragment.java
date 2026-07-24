package com.capubbs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class AllBoardsFragment extends Fragment {
    public static ArrayList<String> showList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView listView=new ListView(inflater.getContext());
        listView.setAdapter(new ArrayAdapter<>(inflater.getContext(),
                android.R.layout.simple_expandable_list_item_1, Board.getNames()));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent=new Intent(getActivity(), ViewActivity.class);
            intent.putExtra("type","board");
            intent.putExtra("bid",Board.getIdByName(Board.getNames().get(position)));
            getActivity().startActivity(intent);
        });
        return listView;
    }

}