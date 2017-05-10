package com.primewebtech.darts.gallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.primewebtech.darts.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private static final String TAG = GalleryActivity.class.getSimpleName();
    private File pictureDirectory;
    private List<File> sortedFiles;
    private RecyclerView mRecyclerView;
    private SimpleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,4));
        mAdapter = new SimpleAdapter(this);
        List<SectionedGridRecyclerViewAdapter.Section> sections =
                new ArrayList<SectionedGridRecyclerViewAdapter.Section>();
        //Sections
        sections.add(new SectionedGridRecyclerViewAdapter.Section(0,"Today"));
        sections.add(new SectionedGridRecyclerViewAdapter.Section(5,"Last Week"));
        sections.add(new SectionedGridRecyclerViewAdapter.Section(12,"Last Month"));
        sections.add(new SectionedGridRecyclerViewAdapter.Section(14,"April"));
        sections.add(new SectionedGridRecyclerViewAdapter.Section(20,"March"));

        //Add your adapter to the sectionAdapter
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        SectionedGridRecyclerViewAdapter mSectionedAdapter = new
                SectionedGridRecyclerViewAdapter(this,R.layout.section,R.id.section_text,mRecyclerView,mAdapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));
        mRecyclerView.setAdapter(mSectionedAdapter);


    }



}
